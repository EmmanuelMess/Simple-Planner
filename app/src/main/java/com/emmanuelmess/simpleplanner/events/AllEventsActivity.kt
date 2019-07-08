package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emmanuelmess.simpleplanner.R
import com.emmanuelmess.simpleplanner.common.AppDatabaseAwareActivity
import com.emmanuelmess.simpleplanner.common.AsyncTaskRunnable
import com.emmanuelmess.simpleplanner.common.crashOnMainThread
import kotlinx.android.synthetic.main.activity_allevents.*
import kotlinx.android.synthetic.main.content_all_events.*
import java.lang.ref.WeakReference
import kotlin.concurrent.thread

class AllEventsActivity : AppDatabaseAwareActivity() {

    lateinit var adapter: EventAdapter
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allevents)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        adapter = EventAdapter(this, { event ->
            val uDatabase = WeakReference(database)
            thread {
                crashOnMainThread {
                    uDatabase.get()?.let { sDatabase ->
                        event.delete(sDatabase.eventDao())
                    }
                }
            }
        }, listOf())

        allEventsLayout.adapter = adapter
        allEventsLayout.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    override fun onResume() {
        super.onResume()

        load()
    }

    private fun load() {
        progressBar.visibility = View.VISIBLE

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

            adapter.setItems(events)
        }
    }

}
