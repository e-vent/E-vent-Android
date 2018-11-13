package io.github.e_vent.util

import android.content.Context
import android.content.SharedPreferences
import io.github.e_vent.R

fun getServerAddrPref(sp: SharedPreferences, ctxt: Context): String {
    return sp.getString(
            ctxt.getString(R.string.pref_id_server),
            ctxt.getString(R.string.pref_default_server)
    )!!
}