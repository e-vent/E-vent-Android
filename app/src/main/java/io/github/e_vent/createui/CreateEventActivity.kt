package io.github.e_vent.createui

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import io.github.e_vent.R
import io.github.e_vent.ServiceLocator
import io.github.e_vent.api.EventRetrofitApi
import io.github.e_vent.util.isValidEventStr
import io.github.e_vent.vo.ServerEvent

import kotlinx.android.synthetic.main.activity_create_event.*

/**
 * A create screen that offers event creation via Name/Description.
 */
class CreateEventActivity : AppCompatActivity() {
    /**
     * Keep track of the create task to ensure we can cancel it if requested.
     */
    private var mPostTask: EventCreateTask? = null

    private lateinit var api: EventRetrofitApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_event)
        api = ServiceLocator.instance(this).getEventApi()
        // Set up the login form.
        create_event_button.setOnClickListener { attemptCreate() }
    }

    /**
     * Attempts to create the event specified by the form.
     * If there are form errors (invalid fields, etc.), the
     * errors are presented and no actual creation attempt is made.
     */
    private fun attemptCreate() {
        if (mPostTask != null) {
            return
        }

        // Reset errors.
        name.error = null
        desc.error = null

        // Store values at the time of the creation attempt.
        val nameStr = name.text.toString()
        val descStr = desc.text.toString()

        var cancel = false
        var focusView: View? = null

        // Check for a valid desc
        if (!isDescValid(descStr)) {
            desc.error = getString(R.string.error_invalid_desc)
            focusView = desc
            cancel = true
        }

        // Check for a valid name
        if (!isNameValid(nameStr)) {
            name.error = getString(R.string.error_invalid_name)
            focusView = name
            cancel = true
        }

        if (cancel) {
            // There was an error; don't attempt creation and focus the first
            // form field with an error.
            focusView?.requestFocus()
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true)
            mPostTask = EventCreateTask(nameStr, descStr)
            mPostTask!!.execute(null as Void?)
        }
    }

    private fun isNameValid(name: String): Boolean {
        val l = name.length
        return 0 < l && l < 20 && isValidEventStr(name)
    }

    private fun isDescValid(desc: String): Boolean {
        val l = desc.length
        return l <= 280 && isValidEventStr(desc)
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private fun showProgress(show: Boolean) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        val shortAnimTime = resources.getInteger(android.R.integer.config_shortAnimTime).toLong()
        login_form.visibility = if (show) View.GONE else View.VISIBLE
        login_form.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 0 else 1).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_form.visibility = if (show) View.GONE else View.VISIBLE
                    }
                })
        login_progress.visibility = if (show) View.VISIBLE else View.GONE
        login_progress.animate()
                .setDuration(shortAnimTime)
                .alpha((if (show) 1 else 0).toFloat())
                .setListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        login_progress.visibility = if (show) View.VISIBLE else View.GONE
                    }
                })
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    @SuppressLint("StaticFieldLeak")
    private inner class EventCreateTask(
            private val nameStr: String, private val descStr: String
    ) : AsyncTask<Void, Void, Int>() {
        override fun doInBackground(vararg params: Void): Int? {
            Log.i("CreateEvent", "About to contact server to create event")

            try {
                // Simulate network access.
                Thread.sleep(1000)
            } catch (e: InterruptedException) {
                return null
            }

            try {
                val result = api.create(ServerEvent(nameStr, descStr, "tabaret")).execute()
                if (result.isSuccessful) {
                    return result.body()!!
                } else {
                    Log.i("CreateEvent", "Got error from server while creating event")
                    return null
                }
            } catch (e: Exception) {
                Log.e("CreateEvent", "Error while using API to create event", e)
                return null
            }
        }

        override fun onPostExecute(createdEventID: Int?) {
            mPostTask = null
            showProgress(false)
            if (createdEventID != null) {
                // TODO show created event
                finish()
            } else {
                name.error = getString(R.string.error_server_error)
                name.requestFocus()
            }
        }

        override fun onCancelled() {
            mPostTask = null
            showProgress(false)
        }
    }
}
