package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.emmanuelmess.simpleplanner.R
import com.emmanuelmess.simpleplanner.common.*
import kotlinx.android.synthetic.main.fragment_main.view.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

class MainFragment : AppDatabaseAwareFragment() {
    companion object {
        val TAG = "main_fragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    lateinit var adapter: EventAdapter
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adapter = EventAdapter(requireContext(), { event ->
            val uDatabase = WeakReference(database)
            thread {
                crashOnMainThread {
                    uDatabase.get()?.let { sDatabase ->
                        event.delete(sDatabase.eventDao())
                    }
                }
            }
        }, listOf())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false).apply {
            eventsLayout.adapter = adapter
            eventsLayout.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    fun load() {
        val uDatabase = WeakReference(database)
        AsyncTaskRunnable<List<Event>>(true, {
            uDatabase.get().let { sDatabase ->
                if(sDatabase == null) {
                    cancel(false)
                    null
                } else {
                    val now = Calendar.getInstance()

                    sDatabase.eventDao().getAllDoableNow(
                        now.calendarHourOfDay.toShort(),
                        now.calendarMinute.toShort()
                    ).map { eventEntity ->
                        eventEntity.toEvent(requireContext())
                    }
                }
            }
        }) { events ->
            adapter.setItems(events)
        }
    }
}
