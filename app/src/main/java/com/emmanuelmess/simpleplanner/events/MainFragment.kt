package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.ViewDataBinding
import com.emmanuelmess.simpleplanner.R
import com.emmanuelmess.simpleplanner.common.*
import com.emmanuelmess.simpleplanner.databinding.CardEventsCommentcontractedBinding
import com.emmanuelmess.simpleplanner.databinding.CardEventsCommentextendedBinding
import com.emmanuelmess.simpleplanner.databinding.CardEventsNocommentBinding
import kotlinx.android.synthetic.main.content_card_events_commentcontracted.view.*
import kotlinx.android.synthetic.main.fragment_main.*
import java.lang.ref.WeakReference
import java.util.*
import kotlin.concurrent.thread

class MainFragment : AppDatabaseAwareFragment() {
    companion object {
        val TAG = "main_fragment"

        @JvmStatic
        fun newInstance() = MainFragment()
    }

    data class TemporaryCardData(val event: Event, var type: Int, var isExtended: Boolean? = null) {
        companion object {
            val NO_COMMENT_TYPE = 0
            val COMMENTED_TYPE = 1
        }
    }
    val temporaryCardDatas = mutableListOf<TemporaryCardData>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onResume() {
        super.onResume()
        load()
    }

    fun load() {
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
                        eventEntity.toEvent(requireContext())
                    }
                }
            }
        }) { events ->
            events.forEach(::createEventViewFromEvent)
        }
    }

    public fun createEventViewFromEvent(event: Event) {
        val type: Int
        val isExtended: Boolean?
        if (event.comment.isEmpty()) {
            type = TemporaryCardData.NO_COMMENT_TYPE
            isExtended = null
        } else {
            type = TemporaryCardData.COMMENTED_TYPE
            isExtended = false
        }

        val temporaryCardData = TemporaryCardData(event, type, isExtended)

        temporaryCardDatas.add(temporaryCardData)
        add(event, temporaryCardData, temporaryCardDatas.size-1)
    }

    private fun add(event: Event, temporaryCardData: TemporaryCardData, index: Int) {
        val binding: ViewDataBinding

        if(temporaryCardData.type == TemporaryCardData.NO_COMMENT_TYPE) {
            binding = CardEventsNocommentBinding.inflate(layoutInflater, eventsLayout, true)
            binding.event = event
        } else {
            binding = CardEventsCommentcontractedBinding.inflate(layoutInflater, eventsLayout, true)
            binding.event = event
        }

        loadCard(binding.root as CardView, event, index, temporaryCardData)
    }

    private fun loadCard(card: CardView, event: Event, index: Int, temporaryCardData: TemporaryCardData) = with(card) {
        setCardBackgroundColor(MaterialColors.GREEN_500)
        constraintLayout.setBackgroundColor(MaterialColors.GREEN_500)

        if(temporaryCardData.type == TemporaryCardData.COMMENTED_TYPE) {
            constraintLayout.setOnClickListener { _ ->
                onExtendClick(index, temporaryCardData)
            }
        }

        doneButton.setOnClickListener {
            temporaryCardDatas.remove(temporaryCardData)
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

    private fun onExtendClick(index: Int, temporaryCardData: TemporaryCardData) {
        temporaryCardData.isExtended = !(temporaryCardData.isExtended!!)
        temporaryCardData.isExtended!!.let { isExtended ->
            eventsLayout.removeViewAt(index)

            if (isExtended) {
                val binding = CardEventsCommentextendedBinding.inflate(layoutInflater, eventsLayout, false)
                eventsLayout.addView(binding.root, index)
                binding.event = temporaryCardData.event

                loadCard(binding.root as CardView, temporaryCardData.event, index, temporaryCardData)
            } else {
                val binding = CardEventsCommentcontractedBinding.inflate(layoutInflater, eventsLayout, false)
                eventsLayout.addView(binding.root, index)
                binding.event = temporaryCardData.event

                loadCard(binding.root as CardView, temporaryCardData.event, index, temporaryCardData)
            }
        }
    }

}
