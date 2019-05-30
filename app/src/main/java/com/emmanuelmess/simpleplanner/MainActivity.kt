package com.emmanuelmess.simpleplanner

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.cardview.widget.CardView
import com.emmanuelmess.simpleplanner.common.*
import com.emmanuelmess.simpleplanner.databinding.CardEventsBinding
import com.emmanuelmess.simpleplanner.events.AllEventsActivity
import com.emmanuelmess.simpleplanner.events.CreateDialogFragment
import com.emmanuelmess.simpleplanner.events.Event
import com.emmanuelmess.simpleplanner.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_card_events.view.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

class MainActivity : AppDatabaseAwareActivity() {

    data class TemporaryCardData(var isExtended: Boolean = false)
    val temporaryCardDatas = mutableListOf<TemporaryCardData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.setOnClickListener { _ ->
            CreateDialogFragment.newInstance().apply {
                show(supportFragmentManager.beginTransaction(), CreateDialogFragment.TAG)
                onPositiveButton = {
                        event -> add(event, temporaryCardDatas.size)
                }
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

    private fun load() {
        eventsLayout.removeAllViews()

        val nowMillis = Calendar.getInstance().setToFirstDay().timeInMillis

        val uDatabase = WeakReference(database)
        AsyncTaskRunnable<List<Event>>(true, {
            uDatabase.get().let { sDatabase ->
                if(sDatabase == null) {
                    cancel(false)
                    null
                } else {
                    sDatabase.eventDao().getAllDoableNow(nowMillis).map { eventEntity ->
                        eventEntity.toEvent(applicationContext)
                    }
                }
            }
        }) { events ->
            events.forEach { event ->
                add(event, temporaryCardDatas.size)
                temporaryCardDatas.add(TemporaryCardData())
            }
        }
    }

    private fun add(event: Event, temporaryIndex: Int) {
        val binding = CardEventsBinding.inflate(layoutInflater, eventsLayout, true)
        binding.event = event
        loadCard(binding.root as CardView, event, temporaryIndex)
    }

    private fun loadCard(card: CardView, event: Event, temporaryIndex: Int) = with(card) {
        setCardBackgroundColor(MaterialColors.GREEN_500)
        constraintLayout.setBackgroundColor(MaterialColors.GREEN_500)

        constraintLayout.setOnClickListener({view -> onExtendClick(view, temporaryIndex)})

        doneButton.setOnClickListener {
            eventsLayout.removeView(card)
            val uDatabase = WeakReference(database)
            thread {
                crashOnMainThread {
                    uDatabase.get()?.let { sDatabase ->
                        event.delete(sDatabase.eventDao())
                    }
                }
            }
        }
    }

    private fun onExtendClick(constraintLayout: View, temporaryIndex: Int) = with(constraintLayout) {
        val temporaryCardData = temporaryCardDatas[temporaryIndex]
        temporaryCardData.isExtended = !temporaryCardData.isExtended

        if(temporaryCardData.isExtended) {
            commentTextView.visibility = VISIBLE
            dropImageView.setImageResource(R.drawable.ic_arrow_drop_up_black_24dp)
        } else {
            commentTextView.visibility = GONE
            dropImageView.setImageResource(R.drawable.ic_arrow_drop_down_black_24dp)
        }
    }
}
