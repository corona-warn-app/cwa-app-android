package de.rki.coronawarnapp.test.api.ui

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.nearby.exposurenotification.ExposureConfiguration
import com.google.android.gms.nearby.exposurenotification.ExposureNotificationClient
import com.google.android.gms.nearby.exposurenotification.ExposureSummary
import com.google.android.gms.nearby.exposurenotification.TemporaryExposureKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.protobuf.ByteString
import com.google.zxing.BarcodeFormat
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.google.zxing.qrcode.QRCodeWriter
import de.rki.coronawarnapp.R
import de.rki.coronawarnapp.RiskLevelAndKeyRetrievalBenchmark
import de.rki.coronawarnapp.databinding.FragmentTestForAPIBinding
import de.rki.coronawarnapp.diagnosiskeys.server.LocationCode
import de.rki.coronawarnapp.exception.ExceptionCategory
import de.rki.coronawarnapp.exception.ExceptionCategory.INTERNAL
import de.rki.coronawarnapp.exception.TransactionException
import de.rki.coronawarnapp.exception.reporting.report
import de.rki.coronawarnapp.nearby.InternalExposureNotificationClient
import de.rki.coronawarnapp.nearby.InternalExposureNotificationPermissionHelper
import de.rki.coronawarnapp.receiver.ExposureStateUpdateReceiver
import de.rki.coronawarnapp.risk.TimeVariables
import de.rki.coronawarnapp.server.protocols.AppleLegacyKeyExchange
import de.rki.coronawarnapp.service.applicationconfiguration.ApplicationConfigurationService
import de.rki.coronawarnapp.sharing.ExposureSharingService
import de.rki.coronawarnapp.storage.AppDatabase
import de.rki.coronawarnapp.storage.ExposureSummaryRepository
import de.rki.coronawarnapp.storage.LocalData
import de.rki.coronawarnapp.storage.tracing.TracingIntervalRepository
import de.rki.coronawarnapp.transaction.RiskLevelTransaction
import de.rki.coronawarnapp.ui.viewmodel.TracingViewModel
import de.rki.coronawarnapp.util.CWADebug
import de.rki.coronawarnapp.util.KeyFileHelper
import de.rki.coronawarnapp.util.di.AppInjector
import de.rki.coronawarnapp.util.di.AutoInject
import de.rki.coronawarnapp.util.ui.observe2
import de.rki.coronawarnapp.util.ui.viewBindingLazy
import de.rki.coronawarnapp.util.viewmodel.CWAViewModelFactoryProvider
import de.rki.coronawarnapp.util.viewmodel.cwaViewModels
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import timber.log.Timber
import java.io.File
import java.lang.reflect.Type
import java.util.UUID
import javax.inject.Inject

@SuppressWarnings("TooManyFunctions", "MagicNumber", "LongMethod")
class TestForAPIFragment : Fragment(R.layout.fragment_test_for_a_p_i),
    InternalExposureNotificationPermissionHelper.Callback, AutoInject {

    @Inject lateinit var viewModelFactory: CWAViewModelFactoryProvider.Factory
    private val vm: TestForApiFragmentViewModel by cwaViewModels { viewModelFactory }

    companion object {
        const val CONFIG_SCORE = 8

        fun keysToJson(keys: List<TemporaryExposureKey>): String {
            return Gson().toJson(keys).toString()
        }

        fun jsonToKeys(json: String): Array<TemporaryExposureKey> {
            val listType: Type = object : TypeToken<Array<TemporaryExposureKey?>?>() {}.type
            return Gson().fromJson(json, listType)
        }
    }

    private val enfClient by lazy {
        AppInjector.component.enfClient
    }

    private var myExposureKeysJSON: String? = null
    private var myExposureKeys: List<TemporaryExposureKey>? = mutableListOf()
    private var otherExposureKey: AppleLegacyKeyExchange.Key? = null
    private var otherExposureKeyList = mutableListOf<AppleLegacyKeyExchange.Key>()

    private lateinit var internalExposureNotificationPermissionHelper: InternalExposureNotificationPermissionHelper

    private var token: String? = null

    // ViewModel for MainActivity
    private val tracingViewModel: TracingViewModel by activityViewModels()

    private lateinit var qrPager: ViewPager2
    private lateinit var qrPagerAdapter: RecyclerView.Adapter<QRPagerAdapter.QRViewHolder>

    // Data and View binding
    private val binding: FragmentTestForAPIBinding by viewBindingLazy()

    private var lastSetCountries: List<String>? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tracingViewModel = tracingViewModel

        val v: Long = PackageInfoCompat.getLongVersionCode(
            activity?.packageManager!!.getPackageInfo(
                GoogleApiAvailability.GOOGLE_PLAY_SERVICES_PACKAGE,
                0
            )
        )
        binding.labelGooglePlayServicesVersion.text =
            "Google Play Services version: " + v.toString()

        token = UUID.randomUUID().toString()

        internalExposureNotificationPermissionHelper =
            InternalExposureNotificationPermissionHelper(this, this)

        getExposureKeys()

        qrPager = binding.qrCodeViewpager
        qrPagerAdapter = QRPagerAdapter()
        qrPager.adapter = qrPagerAdapter

        // Load countries from App config and update Country UI element states
        lifecycleScope.launch {
            lastSetCountries =
                ApplicationConfigurationService.asyncRetrieveApplicationConfiguration()
                    .supportedCountriesList

            binding.inputCountryCodesEditText.setText(
                lastSetCountries?.joinToString(
                    ","
                )
            )

            updateCountryStatusLabel()
        }

        binding.buttonApiTestStart.setOnClickListener {
            start()
        }

        binding.buttonApiGetExposureKeys.setOnClickListener {
            getExposureKeys()
        }

        val last3HoursSwitch = binding.testApiSwitchLastThreeHoursFromServer
        last3HoursSwitch.isChecked = LocalData.last3HoursMode()
        last3HoursSwitch.setOnClickListener {
            vm.setLast3HoursMode(last3HoursSwitch.isChecked)
        }

        vm.last3HourToggleEvent.observe2(this) {
            showToast("Last 3 Hours Mode is activated: $it")
        }

        val backgroundNotificationSwitch = binding.testApiSwitchBackgroundNotifications
        backgroundNotificationSwitch.isChecked = LocalData.backgroundNotification()
        backgroundNotificationSwitch.setOnClickListener {
            val isBackgroundNotificationsActive = backgroundNotificationSwitch.isChecked
            showToast("Background Notifications are activated: $isBackgroundNotificationsActive")
            LocalData.backgroundNotification(isBackgroundNotificationsActive)
        }

        binding.buttonApiGetCheckExposure.setOnClickListener {
            checkExposure()
        }

        binding.buttonApiScanQrCode.setOnClickListener {
            IntentIntegrator.forSupportFragment(this)
                .setOrientationLocked(false)
                .setBeepEnabled(false)
                .initiateScan()
        }

        binding.buttonApiShareMyKeys.setOnClickListener {
            shareMyKeys()
        }

        binding.buttonApiEnterOtherKeys.setOnClickListener {
            enterOtherKeys()
        }

        binding.buttonApiSubmitKeys.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                try {
                    internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()

                    // SubmitDiagnosisKeysTransaction.start("123")
                    withContext(Dispatchers.Main) {
                        showToast("Key submission successful")
                    }
                } catch (e: TransactionException) {
                    e.report(INTERNAL)
                }
            }
        }

        binding.buttonCalculateRiskLevel.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                try {
                    RiskLevelTransaction.start()
                } catch (e: TransactionException) {
                    e.report(INTERNAL)
                }
            }
        }

        binding.buttonInsertExposureSummary.setOnClickListener {
            // Now broadcasts them to the worker.
            val intent = Intent(
                context,
                ExposureStateUpdateReceiver::class.java
            )
            intent.action = ExposureNotificationClient.ACTION_EXPOSURE_STATE_UPDATED
            context?.sendBroadcast(intent)
        }

        binding.buttonRetrieveExposureSummary.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                showToast(
                    ExposureSummaryRepository.getExposureSummaryRepository()
                        .getExposureSummaryEntities().toString()
                )
            }
        }

        binding.buttonClearDb.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    AppDatabase.getInstance(requireContext()).clearAllTables()
                }
            }
        }

        binding.buttonTracingIntervals.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                showToast(
                    TracingIntervalRepository.getDateRepository(requireContext()).getIntervals()
                        .toString()
                )
            }
        }

        binding.buttonTracingDurationInRetentionPeriod.setOnClickListener {
            tracingViewModel.viewModelScope.launch {
                showToast(TimeVariables.getActiveTracingDaysInRetentionPeriod().toString())
            }
        }

        binding.buttonFilterCountryCodes.setOnClickListener {
            filterCountryCodes()
        }

        binding.buttonRetrieveDiagnosisKeysAndCalcRiskLevel.setOnClickListener {
            startKeyRetrievalAndRiskCalcBenchmark()
        }

        binding.inputMeasureRiskKeyRepeatCount.setOnEditorActionListener { v, actionCode, event ->
            if (actionCode == EditorInfo.IME_ACTION_DONE) {
                startKeyRetrievalAndRiskCalcBenchmark()
            }
            false
        }

        binding.testLogfileToggle.isChecked = CWADebug.fileLogger?.isLogging ?: false
        binding.testLogfileToggle.setOnClickListener { buttonView ->
            CWADebug.fileLogger?.let {
                if (binding.testLogfileToggle.isChecked) {
                    it.start()
                } else {
                    it.stop()
                }
            }
        }

        binding.testLogfileShare.setOnClickListener {
            CWADebug.fileLogger?.let {
                lifecycleScope.launch {
                    val targetPath = withContext(Dispatchers.IO) {
                        async {
                            if (!it.logFile.exists()) return@async null

                            val externalPath = File(
                                requireContext().getExternalFilesDir(null),
                                "LogFile-${System.currentTimeMillis()}.log"
                            )

                            it.logFile.copyTo(externalPath)

                            return@async externalPath
                        }
                    }.await()
                    if (targetPath != null) {
                        Toast.makeText(
                            requireActivity(),
                            "Logfile copied to $targetPath",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireActivity(),
                            "No log file available",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updateExposureSummaryDisplay(null)
    }

    private fun startKeyRetrievalAndRiskCalcBenchmark() {
        hideKeyboard()
        lifecycleScope.launch {
            val repeatCount =
                binding.inputMeasureRiskKeyRepeatCount.text.toString().toInt()
            context?.let {
                RiskLevelAndKeyRetrievalBenchmark(
                    it,
                    lastSetCountries ?: listOf("DE")
                ).start(repeatCount) { status ->
                    binding.labelTestApiMeasureCalcKeyStatus.text = status
                }
            }
        }
    }

    private fun filterCountryCodes() {
        hideKeyboard()
        // Get user input country codes
        val rawCountryCodes = binding.inputCountryCodesEditText.text.toString()

        // Country codes can be separated by space or ,
        var countryCodes = rawCountryCodes.split(',', ' ').filter { it.isNotEmpty() }

        lastSetCountries = countryCodes

        // Trigger asyncFetchFiles which will use all Countries passed as parameter
        lifecycleScope.launch {
            val locationCodes = countryCodes.map { LocationCode(it) }
            AppInjector.component.keyFileDownloader.asyncFetchKeyFiles(locationCodes)
            updateCountryStatusLabel()
        }
    }

    private fun hideKeyboard() {
        activity?.currentFocus.let {
            val inputManager =
                context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(it?.windowToken, 0)
        }
    }

    /**
     * Updates the Label for country filter
     */
    private fun updateCountryStatusLabel() {
        binding.labelCountryCodeFilterStatus.text = "Country filter applied for: \n " +
                "${lastSetCountries?.joinToString(",")}"
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
        this.internalExposureNotificationPermissionHelper.onResolutionComplete(
            requestCode,
            resultCode
        )

        val result: IntentResult? =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Toast.makeText(requireContext(), "Cancelled", Toast.LENGTH_LONG).show()
            } else {
                ExposureSharingService.getOthersKeys(result.contents, onScannedKey)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun start() {
        this.internalExposureNotificationPermissionHelper.requestPermissionToStartTracing()
    }

    private fun getExposureKeys() {
        this.internalExposureNotificationPermissionHelper.requestPermissionToShareKeys()
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
            token = UUID.randomUUID().toString()
            LocalData.googleApiToken(token)

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

            val dir =
                File(
                    File(requireContext().getExternalFilesDir(null), "key-export"),
                    token ?: ""
                )
            dir.mkdirs()

            var googleFileList: List<File>
            lifecycleScope.launch {
                googleFileList = KeyFileHelper.asyncCreateExportFiles(appleFiles, dir)

                Timber.i("Provide ${googleFileList.count()} files with ${appleKeyList.size} keys with token $token")
                try {
                    // only testing implementation: this is used to wait for the broadcastreceiver of the OS / EN API
                    enfClient.provideDiagnosisKeys(
                        googleFileList,
                        ApplicationConfigurationService.asyncRetrieveExposureConfiguration(),
                        token!!
                    )
                    showToast("Provided ${appleKeyList.size} keys to Google API with token $token")
                } catch (e: Exception) {
                    e.report(ExceptionCategory.EXPOSURENOTIFICATION)
                }
            }
        }
    }

    private fun checkExposure() {
        Timber.d("Check Exposure with token $token")

        lifecycleScope.launch {
            try {
                val exposureSummary =
                    InternalExposureNotificationClient.asyncGetExposureSummary(token!!)
                updateExposureSummaryDisplay(exposureSummary)
                showToast("Updated Exposure Summary with token $token")
                Timber.d("Received exposure with token $token from QR Code")
                Timber.i(exposureSummary.toString())
            } catch (e: Exception) {
                e.report(ExceptionCategory.EXPOSURENOTIFICATION)
            }
        }
    }

    private fun updateExposureSummaryDisplay(exposureSummary: ExposureSummary?) {

        binding.labelExposureSummaryMatchedKeyCount.text = getString(
            R.string.test_api_body_matchedKeyCount,
            (exposureSummary?.matchedKeyCount ?: "-").toString()
        )

        binding.labelExposureSummaryDaysSinceLastExposure.text = getString(
            R.string.test_api_body_daysSinceLastExposure,
            (exposureSummary?.daysSinceLastExposure ?: "-").toString()
        )

        binding.labelExposureSummaryMaximumRiskScore.text = getString(
            R.string.test_api_body_maximumRiskScore,
            (exposureSummary?.maximumRiskScore ?: "-").toString()
        )

        binding.labelExposureSummarySummationRiskScore.text = getString(
            R.string.test_api_body_summation_risk,
            (exposureSummary?.summationRiskScore ?: "-").toString()
        )

        binding.labelExposureSummaryAttenuation.text = getString(
            R.string.test_api_body_attenuation,
            (exposureSummary?.attenuationDurationsInMinutes?.joinToString() ?: "-").toString()
        )
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
        val toast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        toast.show()
    }

    override fun onFailure(exception: Exception?) {
        showToast(exception?.localizedMessage ?: "Error during EN start")
    }

    override fun onStartPermissionGranted() {
        showToast("Started EN Tracing")
    }

    override fun onKeySharePermissionGranted(keys: List<TemporaryExposureKey>) {
        myExposureKeysJSON = keysToJson(keys)
        myExposureKeys = keys
        qrPagerAdapter.notifyDataSetChanged()

        updateKeysDisplay()
    }

    private fun getCustomConfig(): ExposureConfiguration = ExposureConfiguration
        .ExposureConfigurationBuilder()
        .setAttenuationScores(
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE
        )
        .setDaysSinceLastExposureScores(
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE
        )
        .setDurationScores(
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE
        )
        .setTransmissionRiskScores(
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE,
            CONFIG_SCORE
        )
        .build()

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
