package com.emmanuelmess.simpleplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.emmanuelmess.simpleplanner.common.AppDatabaseAwareActivity
import com.emmanuelmess.simpleplanner.events.AllEventsActivity
import com.emmanuelmess.simpleplanner.events.CreateDialogFragment
import com.emmanuelmess.simpleplanner.events.MainFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppDatabaseAwareActivity() {
    var nighttimeCallbackTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val mainFragment = MainFragment.newInstance()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.mainFrameLayout, mainFragment, MainFragment.TAG)
            .commit()

        fab.setOnClickListener { _ ->
            CreateDialogFragment.newInstance().apply {
                show(supportFragmentManager.beginTransaction(), CreateDialogFragment.TAG)
                onPositiveButton = {
                    mainFragment.load()
                }
            }
        }
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
            else -> super.onOptionsItemSelected(item)
        }
    }

}
