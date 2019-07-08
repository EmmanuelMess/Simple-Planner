package com.emmanuelmess.simpleplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.preference.PreferenceManager
import com.emmanuelmess.simpleplanner.common.AppDatabaseAwareActivity
import com.emmanuelmess.simpleplanner.events.AllEventsActivity
import com.emmanuelmess.simpleplanner.events.CreateDialogFragment
import com.emmanuelmess.simpleplanner.events.MainFragment
import com.emmanuelmess.simpleplanner.nightstate.NighttimeFragment
import com.emmanuelmess.simpleplanner.settings.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppDatabaseAwareActivity() {

    /**
     * Iff the main fragment is available it will be referenced here
     */
    var mainFragment: MainFragment? = null
    var nighttimeCallbackTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        showCorrectFragment()
    }

    override fun onResume() {
        super.onResume()
        setCallback()
    }

    override fun onPause() {
        super.onPause()
        nighttimeCallbackTimer?.cancel()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_allevents -> {
                startActivity(Intent(applicationContext, AllEventsActivity::class.java))
                true
            }
            R.id.action_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setCallback() {
        val callback = { runOnUiThread {
            showCorrectFragment()
        } }

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val callbackTime: Time

        if(showNighttimeFragment()) {
            callbackTime = preferences.getString("startDayTime", Time(6, 0).toString())!!.toTime()
        } else {
            callbackTime = preferences.getString("endDayTime", Time(22, 0).toString())!!.toTime()
        }

        val callbackCalendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, callbackTime.hourOfDay)
            set(Calendar.MINUTE, callbackTime.minute)
        }

        val now = Calendar.getInstance()

        if(callbackCalendar.before(now)) {
            callbackCalendar.set(Calendar.DAY_OF_YEAR, now.get(Calendar.DAY_OF_YEAR) +1)
        }

        val timeToCallback = callbackCalendar.timeInMillis - System.currentTimeMillis()

        nighttimeCallbackTimer = Timer()
        nighttimeCallbackTimer!!.schedule(object : TimerTask() {
            override fun run() = callback.invoke()
        }, timeToCallback)
    }

    private fun showCorrectFragment() {
        if (showNighttimeFragment()) {
            fab.hide()
            supportFragmentManager
                .beginTransaction()
                .add(R.id.mainFrameLayout, NighttimeFragment.newInstance(), NighttimeFragment.TAG)
                .commit()
        } else {
            mainFragment = MainFragment.newInstance()

            supportFragmentManager
                .beginTransaction()
                .add(R.id.mainFrameLayout, mainFragment!!, MainFragment.TAG)
                .commit()

            fab.setOnClickListener { _ ->
                CreateDialogFragment.newInstance().apply {
                    show(supportFragmentManager.beginTransaction(), CreateDialogFragment.TAG)
                    onPositiveButton = (mainFragment!!).adapter::addItem
                }
            }
        }

        setCallback()
    }

    private fun showNighttimeFragment(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        return preferences.getBoolean("useDayNight", false) && isNighttime()
    }

    private fun isNighttime(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        val startDayTime = preferences.getString("startDayTime", DEFAULT_DAY_START)!!.toTime()
        val startDaytimeCalendar = startDayTime.toCalendar()

        val endDayTime = preferences.getString("endDayTime", DEFAULT_DAY_END)!!.toTime()
        val endDaytimeCalendar = endDayTime.toCalendar()

        val now = Calendar.getInstance()

        return now.before(startDaytimeCalendar) || now.after(endDaytimeCalendar)
    }
}
