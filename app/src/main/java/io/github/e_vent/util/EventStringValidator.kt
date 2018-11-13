package io.github.e_vent.util

import io.github.e_vent.ServiceLocator

fun isValidEventStr(x: String): Boolean {
    for (c in x) {
        if (c < ' ' || c > '~') { // ASCII control and Unicode are banned
            return false
        }
    }
    return true
}
