package io.github.e_vent.prefsui

import android.annotation.TargetApi
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.e_vent.R
import io.github.e_vent.util.getServerAddrPref


/*
 * This fragment shows general preferences only. It is used when the
 * activity is showing a two-pane settings UI.
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class SettingsFragment
    : PreferenceFragmentCompat(), Preference.OnPreferenceChangeListener
{
    private var oldOnPreferenceChangeListener: Preference.OnPreferenceChangeListener? = null
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.pref_general, rootKey)
    }

    override fun onResume() {
        super.onResume()
        Log.i("Settings", "onResume")

        val editText = findPreference("server_addr")
        editText.summary =
                getServerAddrPref(preferenceManager.sharedPreferences, this.requireActivity())
        oldOnPreferenceChangeListener = editText.onPreferenceChangeListener
        editText.onPreferenceChangeListener = this
    }

    override fun onPause() {
        super.onPause()
        Log.i("Settings", "onPause")
        val editText = findPreference("server_addr")
        editText.onPreferenceChangeListener = oldOnPreferenceChangeListener
        oldOnPreferenceChangeListener = null
    }

    override fun onPreferenceChange(pref: Preference, newValue: Any): Boolean {
        (pref as EditTextPreference).summary = newValue.toString()
        Log.i("Settings", "onPreferenceChange " + pref + " to " + newValue + " and " + oldOnPreferenceChangeListener)
        return oldOnPreferenceChangeListener?.onPreferenceChange(pref, newValue) ?: true
    }
}
