package com.emmanuelmess.simpleplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.room.Room.databaseBuilder
import com.emmanuelmess.simpleplanner.common.AppDatabase
import com.emmanuelmess.simpleplanner.common.AppDatabase.Companion.DATABASE_NAME
import com.emmanuelmess.simpleplanner.common.AsyncTaskRunnable
import com.emmanuelmess.simpleplanner.common.MaterialColors.BLUE_500
import com.emmanuelmess.simpleplanner.databinding.CardEventsBinding
import com.emmanuelmess.simpleplanner.events.CreateDialogFragment
import com.emmanuelmess.simpleplanner.events.Event
import com.emmanuelmess.simpleplanner.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_card_events.view.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {
    lateinit var db : AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        db = databaseBuilder(
            applicationContext,
            AppDatabase::class.java, DATABASE_NAME
        ).build()

        fab.setOnClickListener { _ ->
            CreateDialogFragment.newInstance().apply {
                show(supportFragmentManager.beginTransaction(), CreateDialogFragment.TAG)
                onPositiveButton = ::add
            }
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {
                startActivity(Intent(applicationContext, SettingsActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun load() {
        eventsLayout.removeAllViews()

        val uDatabase = WeakReference(db)
        AsyncTaskRunnable<List<Event>>(true, {
            uDatabase.get().let { sDatabase ->
                if(sDatabase == null) {
                    cancel(false)
                    null
                } else {
                    sDatabase.eventDao().getAll().map { eventEntity ->
                        eventEntity.toEvent(applicationContext)
                    }
                }
            }
        }) { events ->
            events.forEach { event ->
                add(event)
            }
        }
    }

    private fun add(event: Event) {
        val binding = CardEventsBinding.inflate(layoutInflater, eventsLayout, true)
        binding.event = event
        loadCard(binding.root as CardView, event)
    }

    private fun loadCard(card: CardView, event: Event) = with(card) {
        setCardBackgroundColor(BLUE_500)
        constraintLayout.setBackgroundColor(BLUE_500)
        doneButton.setOnClickListener {
            eventsLayout.removeView(card)
            thread {
                event.delete(db.eventDao())
            }
        }
    }
}
