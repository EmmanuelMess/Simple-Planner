package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.View
import androidx.cardview.widget.CardView
import com.emmanuelmess.simpleplanner.R
import com.emmanuelmess.simpleplanner.common.AppDatabaseAwareActivity
import com.emmanuelmess.simpleplanner.common.AsyncTaskRunnable
import com.emmanuelmess.simpleplanner.common.MaterialColors
import com.emmanuelmess.simpleplanner.common.crashOnMainThread
import com.emmanuelmess.simpleplanner.databinding.CardEventsBinding
import kotlinx.android.synthetic.main.activity_allevents.*
import kotlinx.android.synthetic.main.content_all_events.*
import kotlinx.android.synthetic.main.content_card_events.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class AllEventsActivity : AppDatabaseAwareActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allevents)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        load()
    }

    override fun onResume() {
        super.onResume()

        load()
    }


    private fun load() {
        progressBar.visibility = View.VISIBLE

        eventsLayout.removeAllViews()

        val uDatabase = WeakReference(database)
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
            progressBar.visibility = View.GONE

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
        setCardBackgroundColor(MaterialColors.GREEN_500)
        constraintLayout.setBackgroundColor(MaterialColors.GREEN_500)
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

}
