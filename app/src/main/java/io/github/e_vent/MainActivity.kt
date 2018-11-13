package io.github.e_vent

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.e_vent.ui.EventActivity
import kotlinx.android.synthetic.main.activity_main.*

/**
 * chooser activity for the demo.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        withDatabase.setOnClickListener {
            show()
        }
    }

    private fun show() {
        val intent = EventActivity.intentFor(this)
        startActivity(intent)
    }
}
