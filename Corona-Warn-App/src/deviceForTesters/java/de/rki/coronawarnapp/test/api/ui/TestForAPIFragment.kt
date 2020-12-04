package de.rki.coronawarnapp.test.api.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.protobuf.ByteString
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.zxing.qrcode.QRCodeWriter
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.appconfig.AppConfigProvider
import de.rki.coronawarnapp.databinding.FragmentTestForAPIBinding
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.ENFClient
import de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.risk.storage.RiskLevelStorage
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.sharing.ExposureSharingService
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.submission.data.tekhistory.TEKHistoryUpdater
import de.rki.coronawarnapp.test.menu.ui.TestMenuItem
import de.rki.coronawarnapp.util.KeyFileHelper
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber
import java.io.File
import java.lang.reflect.Type
import java.util.UUID
import javax.inject.Inject

@SuppressWarnings("TooManyFunctions", "LongMethod")
class TestForAPIFragment : Fragment(R.layout.fragment_test_for_a_p_i), AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    @Inject lateinit var enfClient: ENFClient
    @Inject lateinit var tekHistoryUpdater: TEKHistoryUpdater

    // TODO: This is ugly, remove when refactoring the fragment
    @Inject lateinit var appConfigProvider: AppConfigProvider
    @Inject lateinit var riskLevelStorage: RiskLevelStorage
    private val vm: TestForApiFragmentViewModel by cwaViewModels { viewModelFactory }

    companion object {
        val MENU_ITEM = TestMenuItem(
            title = "Test for API",
            description = "A mix of API related test options.",
            targetId = R.id.test_for_api_fragment
        )

        fun keysToJson(keys: List<TemporaryExposureKey>): String {
            return Gson().toJson(keys).toString()
        }

        fun jsonToKeys(json: String): Array<TemporaryExposureKey> {
            val listType: Type = object : TypeToken<Array<TemporaryExposureKey?>?>() {}.type
            return Gson().fromJson(json, listType)
        }
    }

    private var myExposureKeysJSON: String? = null
    private var myExposureKeys: List<TemporaryExposureKey>? = mutableListOf()
    private var otherExposureKey: AppleLegacyKeyExchange.Key? = null
    private var otherExposureKeyList = mutableListOf<AppleLegacyKeyExchange.Key>()

    private lateinit var qrPager: ViewPager2
    private lateinit var qrPagerAdapter: RecyclerView.Adapter<QRPagerAdapter.QRViewHolder>

    // Data and View binding
    private val binding: FragmentTestForAPIBinding by viewBindingLazy()

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        qrPager = binding.qrCodeViewpager
        qrPagerAdapter = QRPagerAdapter()
        qrPager.adapter = qrPagerAdapter

        // GMS Info card
        vm.gmsState.observe2(this) { state ->
            binding.googlePlayServicesVersionInfo.text =
                "Google Play Services version: ${state.version}"
        }

        vm.infoEvent.observe2(this) { showToast(it) }
        vm.errorEvents.observe2(this) { showToast(it.toString()) }
        vm.permissionRequiredEvent.observe2(this) { permissionRequest ->
            permissionRequest.invoke(requireActivity())
        }

        // Test action card
        binding.apply {
            buttonApiTestStart.setOnClickListener { vm.requestTracingPermission() }
            buttonApiGetExposureKeys.setOnClickListener { getExposureKeys() }

            buttonApiScanQrCode.setOnClickListener {
                IntentIntegrator.forSupportFragment(this@TestForAPIFragment)
                    .setOrientationLocked(false)
                    .setBeepEnabled(false)
                    .initiateScan()
            }

            buttonApiShareMyKeys.setOnClickListener { shareMyKeys() }
            buttonApiEnterOtherKeys.setOnClickListener { enterOtherKeys() }

            buttonApiSubmitKeys.setOnClickListener {
                vm.launch {
                    try {
                        tekHistoryUpdater.callback = object : TEKHistoryUpdater.Callback {
                            override fun onTEKAvailable(teks: List<TemporaryExposureKey>) {
                                launch(context = Dispatchers.Main) {
                                    myExposureKeysJSON = keysToJson(teks)
                                    myExposureKeys = teks
                                    qrPagerAdapter.notifyDataSetChanged()
                                }
                            }

                            override fun onPermissionDeclined() {
                                launch(context = Dispatchers.Main) {
                                    showToast("Permission declined")
                                }
                            }

                            override fun onError(error: Throwable) {
                                launch(context = Dispatchers.Main) {
                                    showToast(error.toString())
                                }
                            }
                        }

                        updateKeysDisplay()

                        // SubmitDiagnosisKeysTransaction.start("123")
                        withContext(Dispatchers.Main) {
                            showToast("Key submission successful")
                        }
                    } catch (e: TransactionException) {
                        e.report(INTERNAL)
                    }
                }
            }

            buttonCalculateRiskLevel.setOnClickListener { vm.calculateRiskLevelClicked() }

            buttonInsertExposureSummary.setOnClickListener {
                // Now broadcasts them to the worker.
                val intent = Intent(
                    context,
                    ExposureStateUpdateReceiver::class.java
                )
                intent.action = ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
                context?.sendBroadcast(intent)
            }

            buttonRetrieveExposureSummary.setOnClickListener {
                vm.launch {
                    val summary = riskLevelStorage.riskLevelResults.first().maxByOrNull {
                        it.calculatedAt
                    }?.toString() ?: "No results yet."

                    withContext(Dispatchers.Main) {
                        showToast(summary)
                    }
                }
            }

            buttonClearDb.setOnClickListener {
                vm.launch {
                    AppDatabase.getInstance(requireContext()).clearAllTables()
                }
            }

            buttonTracingIntervals.setOnClickListener {
                vm.launch {
                    val intervals = TracingIntervalRepository.getDateRepository(requireContext())
                        .getIntervals()
                        .toString()
                    withContext(Dispatchers.Main) {
                        showToast(intervals)
                    }
                }
            }

            buttonTracingDurationInRetentionPeriod.setOnClickListener {
                vm.launch {
                    val daysInRetention =
                        TimeVariables.getActiveTracingDaysInRetentionPeriod().toString()
                    withContext(Dispatchers.Main) {
                        showToast(daysInRetention)
                    }
                }
            }
        }
    }

    private val prettyKey = { key: AppleLegacyKeyExchange.Key ->
        StringBuilder()
            .append("\nKey data: ${key.keyData}")
            .append("\nRolling start number: ${key.rollingStartNumber}")
            .append("\nRisk period: ${key.rollingPeriod}")
            .toString()
    }

    private val onScannedKey = { key: AppleLegacyKeyExchange.Key? ->
        Timber.i("keys scanned..")
        key?.let {
            binding.textScannedKey.text = prettyKey(key)
            binding.textScannedKey.visibility = View.VISIBLE
        }
        otherExposureKeyList.add(key!!)
        otherExposureKey = key
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        vm.handleActivityResult(requestCode, resultCode, data)

        val result: IntentResult? = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                showToast("Cancelled")
            } else {
                ExposureSharingService.getOthersKeys(result.contents, onScannedKey)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getExposureKeys() {
        tekHistoryUpdater.updateTEKHistoryOrRequestPermission { permissionRequest ->
            permissionRequest(requireActivity())
        }
    }

    private fun shareMyKeys() {
        if (myExposureKeysJSON != null) {
            val share = Intent.createChooser(Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, myExposureKeysJSON)
            }, null)
            startActivity(share)
        } else {
            showToast("No Exposure Keys to share - Press 'Get Exposure Keys' first")
        }
    }

    private fun enterOtherKeys() {
        if (null == otherExposureKey) {
            showToast("No other keys provided. Please fill the EditText with the JSON containing keys")
        } else {
            val appleKeyList = mutableListOf<AppleLegacyKeyExchange.Key>()

            for (key in otherExposureKeyList) {
                appleKeyList.add(
                    AppleLegacyKeyExchange.Key.newBuilder()
                        .setKeyData(key.keyData)
                        .setRollingPeriod(144)
                        .setRollingStartNumber(key.rollingStartNumber)
                        .setTransmissionRiskLevel(1)
                        .build()
                )
            }

            val appleFiles = listOf(
                AppleLegacyKeyExchange.File.newBuilder()
                    .addAllKeys(appleKeyList)
                    .build()
            )

            val dir = File(
                File(requireContext().getExternalFilesDir(null), "key-export"),
                UUID.randomUUID().toString()
            )
            dir.mkdirs()

            var googleFileList: List<File>
            lifecycleScope.launch {
                googleFileList = KeyFileHelper.asyncCreateExportFiles(appleFiles, dir)

                Timber.i("Provide ${googleFileList.count()} files with ${appleKeyList.size} keys")
                try {
                    // only testing implementation: this is used to wait for the broadcastreceiver of the OS / EN API
                    enfClient.provideDiagnosisKeys(
                        googleFileList,
                        appConfigProvider.getAppConfig().diagnosisKeysDataMapping
                    )
                    showToast("Provided ${appleKeyList.size} keys to Google API")
                } catch (e: Exception) {
                    e.report(ExceptionCategory.EXPOSURENOTIFICATION)
                }
            }
        }
    }

    private fun updateKeysDisplay() {

        val myKeys =
            if (myExposureKeysJSON != null) jsonToKeys(
                myExposureKeysJSON!!
            ) else null

        val myKeysLabelAndCount = getString(
            R.string.test_api_body_my_keys,
            myKeys?.size ?: 0
        )
        binding.labelMyKeys.text = myKeysLabelAndCount
        binding.textMyKeys.text = myExposureKeysJSON

        myKeys
            ?.maxByOrNull { it.rollingStartIntervalNumber }
            ?.rollingStartIntervalNumber?.toLong()
            ?.let {
                val ms = it * 60L * 10L * 1000L
                val dateString = DateTime(ms, DateTimeZone.UTC)

                binding.labelLatestKeyDate.text = "Latest key is from: $dateString"
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    private inner class QRPagerAdapter :
        RecyclerView.Adapter<QRPagerAdapter.QRViewHolder>() {

        inner class QRViewHolder(val qrCode: ImageView) : RecyclerView.ViewHolder(qrCode)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QRViewHolder {
            val imageView = LayoutInflater.from(parent.context)
                .inflate(R.layout.test_qr_code_view, parent, false) as ImageView
            return QRViewHolder(imageView)
        }

        override fun getItemCount(): Int = myExposureKeys?.size ?: 0

        override fun onBindViewHolder(holder: QRViewHolder, position: Int) {
            myExposureKeys?.get(position)?.let {
                holder.qrCode.setImageBitmap(bitmapForImage(it))
            }
        }

        private fun bitmapForImage(key: TemporaryExposureKey): Bitmap {
            val legacyKey = AppleLegacyKeyExchange.Key.newBuilder()
                .setKeyData(ByteString.copyFrom(key.keyData))
                .setRollingPeriod(key.rollingPeriod)
                .setRollingStartNumber(key.rollingStartIntervalNumber)
                .build().toByteArray()
            val bMatrix = QRCodeWriter().encode(
                Base64.encodeToString(legacyKey, Base64.DEFAULT),
                BarcodeFormat.QR_CODE,
                300,
                300
            )
            val bmp =
                Bitmap.createBitmap(bMatrix.width, bMatrix.height, Bitmap.Config.RGB_565)
            for (x in 0 until bMatrix.width) {
                for (y in 0 until bMatrix.height) {
                    bmp.setPixel(x, y, if (bMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                }
            }
            return bmp
        }
    }
}
