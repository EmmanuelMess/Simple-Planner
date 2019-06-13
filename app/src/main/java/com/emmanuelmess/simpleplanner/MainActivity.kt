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
import com.emmanuelmess.simpleplanner.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppDatabaseAwareActivity() {

    /**
     * Iff the main fragment is available it will be referenced here
     */
    var mainFragment: MainFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

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
                    onPositiveButton = (mainFragment!!)::createEventViewFromEvent
                }
            }
        }
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

    private fun showNighttimeFragment(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        return preferences.getBoolean("useDayNight", false) && isNighttime()
    }

    private fun isNighttime(): Boolean {
        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val startDaytime = Calendar.getInstance().apply {
            timeInMillis = preferences.getLong("startDayTime", 0)
        }
        val endDaytime = Calendar.getInstance().apply {
            timeInMillis = preferences.getLong("endDayTime", 0)
        }
        val now = Calendar.getInstance()

        return now.before(startDaytime) || now.after(endDaytime)
    }
}
