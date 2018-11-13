package io.github.e_vent.util

import android.content.Context
import android.content.SharedPreferences
import io.github.e_vent.R

fun getServerAddrPref(sp: SharedPreferences, ctxt: Context): String {
    return sp.getString(
            ctxt.resources.getString(R.string.pref_id_server),
            ctxt.resources.getString(R.string.pref_default_server)
    )!!
}

fun getClientEventValidationPref(sp: SharedPreferences, ctxt: Context): Boolean {
    return sp.getBoolean(
            ctxt.resources.getString(R.string.pref_id_validate_events_on_client),
            ctxt.resources.getBoolean(R.bool.pref_default_validate_events_on_client)
    )
}