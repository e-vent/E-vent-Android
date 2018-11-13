package io.github.e_vent.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import io.github.e_vent.R

class EventActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.event_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                    .replace(R.id.container, EventFragment())
                    .commitNow()
        }
    }
}
