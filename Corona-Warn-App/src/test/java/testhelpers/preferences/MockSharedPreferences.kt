package testhelpers.preferences

import android.content.SharedPreferences

class MockSharedPreferences : SharedPreferences {
    private val dataMap = mutableMapOf<String, Any>()
    val dataMapPeek: Map<String, Any>
        get() = dataMap.toMap()

    override fun getAll(): MutableMap<String, *> = dataMap

    override fun getString(key: String, defValue: String?): String? =
        dataMap[key] as? String ?: defValue

    override fun getStringSet(key: String, defValues: MutableSet<String>?): MutableSet<String> {
        throw NotImplementedError()
    }

    override fun getInt(key: String, defValue: Int): Int =
        dataMap[key] as? Int ?: defValue

    override fun getLong(key: String, defValue: Long): Long =
        dataMap[key] as? Long ?: defValue

    override fun getFloat(key: String, defValue: Float): Float {
        throw NotImplementedError()
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean =
        dataMap[key] as? Boolean ?: defValue

    override fun contains(key: String): Boolean = dataMap.contains(key)

    override fun edit(): SharedPreferences.Editor = createEditor(dataMap.toMap()) { newData ->
        dataMap.clear()
        dataMap.putAll(newData)
    }

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw NotImplementedError()
    }

    private fun createEditor(
        toEdit: Map<String, Any>,
        onSave: (Map<String, Any>) -> Unit
    ): SharedPreferences.Editor {
        return object : SharedPreferences.Editor {
            private val editorData = toEdit.toMutableMap()
            override fun putString(key: String, value: String?): SharedPreferences.Editor = apply {
                value?.let { editorData[key] = it } ?: editorData.remove(key)
            }

            override fun putStringSet(
                key: String?,
                values: MutableSet<String>?
            ): SharedPreferences.Editor {
                throw NotImplementedError()
            }

            override fun putInt(key: String, value: Int): SharedPreferences.Editor = apply {
                editorData[key] = value
            }

            override fun putLong(key: String, value: Long): SharedPreferences.Editor = apply {
                editorData[key] = value
            }

            override fun putFloat(key: String, value: Float): SharedPreferences.Editor = apply {
                editorData[key] = value
            }

            override fun putBoolean(key: String, value: Boolean): SharedPreferences.Editor = apply {
                editorData[key] = value
            }

            override fun remove(key: String): SharedPreferences.Editor = apply {
                editorData.remove(key)
            }

            override fun clear(): SharedPreferences.Editor = apply {
                editorData.clear()
            }

            override fun commit(): Boolean {
                onSave(editorData)
                return true
            }

            override fun apply() {
                onSave(editorData)
            }
        }
    }
}
