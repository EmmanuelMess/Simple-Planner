package com.emmanuelmess.simpleplanner.events

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.emmanuelmess.simpleplanner.common.MaterialColors
import com.emmanuelmess.simpleplanner.databinding.CardEventsCommentcontractedBinding
import com.emmanuelmess.simpleplanner.databinding.CardEventsCommentextendedBinding
import com.emmanuelmess.simpleplanner.databinding.CardEventsNocommentBinding
import com.emmanuelmess.simpleplanner.databinding.CardEventsSelectedBinding
import kotlinx.android.synthetic.main.content_card_events_commentcontracted.view.*
import kotlinx.android.synthetic.main.content_card_events_commentcontracted.view.constraintLayout
import kotlinx.android.synthetic.main.content_card_events_selected.view.*

class EventAdapter(
    val context: Context,
    val onDone: (Event) -> Unit,
    val onEdit: (Event) -> Unit,
    eventsRaw: List<Event>
): RecyclerView.Adapter<EventAdapter.ItemViewHolder>() {
    val NO_COMMENT_TYPE = 0
    val COMMENTED_TYPE_COLLAPSED = 1
    val COMMENTED_TYPE_EXTENDED = 2
    val SELECTED_TYPE = 3

    private var events: MutableList<ItemData> = eventsRaw.map(::toItemData).toMutableList()
    private var selectedEventIndex: Int? = null

    init {
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int): Int {
        return if(events[position].isSelected) {
            SELECTED_TYPE
        } else when(events[position].type) {
            NO_COMMENT_TYPE -> NO_COMMENT_TYPE
            else ->
                if(events[position].isExtended == false)
                    COMMENTED_TYPE_COLLAPSED
                else if(events[position].isExtended == true)
                    COMMENTED_TYPE_EXTENDED
                else throw IllegalStateException()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding: ViewDataBinding

        when(viewType) {
            NO_COMMENT_TYPE -> {
                binding = CardEventsNocommentBinding.inflate(layoutInflater, parent, false)
            }
            COMMENTED_TYPE_COLLAPSED -> {
                binding = CardEventsCommentcontractedBinding.inflate(layoutInflater, parent, false)
            }
            COMMENTED_TYPE_EXTENDED -> {
                binding = CardEventsCommentextendedBinding.inflate(layoutInflater, parent, false)
            }
            SELECTED_TYPE -> {
                binding = CardEventsSelectedBinding.inflate(layoutInflater, parent, false)
            }
            else -> throw IllegalStateException()
        }

        return ItemViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val event = getItem(position)

        when(getItemViewType(position)) {
            NO_COMMENT_TYPE -> {
                (holder.dataBinding as CardEventsNocommentBinding).event = event
            }
            COMMENTED_TYPE_COLLAPSED -> {
                (holder.dataBinding as CardEventsCommentcontractedBinding).event = event
            }
            COMMENTED_TYPE_EXTENDED -> {
                (holder.dataBinding as CardEventsCommentextendedBinding).event = event
            }
            SELECTED_TYPE -> {
                (holder.dataBinding as CardEventsSelectedBinding).event = event
            }
            else -> throw IllegalStateException()
        }

        if(getItemViewType(position) != SELECTED_TYPE) {
            loadCard(
                events[position],
                getItemViewType(position),
                holder.dataBinding.root as CardView
            )
        } else {
            loadSelectedCard(holder.dataBinding.root as CardView)
        }
    }

    private fun loadCard(
        itemData: ItemData,
        type: Int,
        card: CardView
    ) = with(card) {
        setCardBackgroundColor(MaterialColors.GREEN_500)
        constraintLayout.setBackgroundColor(MaterialColors.GREEN_500)

        if(type == COMMENTED_TYPE_COLLAPSED) {
            constraintLayout.setOnClickListener { _ ->
                onExtendClick(itemData)
            }
        } else if(type == COMMENTED_TYPE_EXTENDED) {
            constraintLayout.setOnClickListener { _ ->
                onCollapseClick(itemData)
            }
        }

        constraintLayout.setOnLongClickListener {
            onLongClick(itemData)
            true
        }

        doneButton.setOnClickListener {
            onDone(itemData.event)
            removeItemAt(events.indexOf(itemData))
        }
    }

    private fun loadSelectedCard(
        card: CardView
    ) = with(card) {
        isSelected = true

        settingsButton.setOnClickListener {
            val itemData = events[selectedEventIndex!!]

            itemData.isSelected = false
            notifyItemChanged(selectedEventIndex!!)
            selectedEventIndex = null
            onEdit(itemData.event)
        }
    }

    private fun onExtendClick(itemData: ItemData) {
        itemData.isExtended = true
        notifyItemChanged(events.indexOf(itemData))
    }

    private fun onCollapseClick(itemData: ItemData) {
        itemData.isExtended = false
        notifyItemChanged(events.indexOf(itemData))
    }

    private fun onLongClick(itemData: ItemData) {
        if(selectedEventIndex != null) {
            events[selectedEventIndex!!].isSelected = false
            notifyItemChanged(selectedEventIndex!!)
        }

        itemData.isSelected = true
        selectedEventIndex = events.indexOf(itemData)
        notifyItemChanged(events.indexOf(itemData))
    }

    fun replaceItem(event: Event, newEvent: Event) {
        val itemIndex = findPosition(event)
        removeItemAt(itemIndex)
        addItemAt(itemIndex, newEvent)
    }

    fun addItemAt(index: Int, event: Event) {
        this.events.add(index, toItemData(event))
        notifyItemInserted(index)
    }

    fun setItems(events: List<Event>) {
        this.events = events.map(::toItemData).toMutableList()
        notifyDataSetChanged()
    }

    fun getItem(position: Int): Event = events[position].event

    override fun getItemId(position: Int): Long = events[position].id.toLong()

    override fun getItemCount(): Int = events.size

    fun removeItemAt(index: Int) {
        events.removeAt(index)
        notifyItemRemoved(index)
    }

    private fun findPosition(event: Event): Int {
        return this.events.indexOf(this.events.find{ it.event == event })
    }

    private fun toItemData(event: Event): ItemData {
        val type: Int
        val isExtended: Boolean?

        if (event.comment.isEmpty()) {
            type = NO_COMMENT_TYPE
            isExtended = null
        } else {
            type = COMMENTED_TYPE_COLLAPSED
            isExtended = false
        }

        return ItemData(event, type, isExtended)
    }

    data class ItemData(
        val event: Event,
        var type: Int,
        var isExtended: Boolean? = null,
        var isSelected: Boolean = false
    ) {
        val id = getNewId()

        companion object {
            private var lastUsedId = -1

            fun getNewId() = ++lastUsedId
        }

        override fun hashCode() = id

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as ItemData

            if (event != other.event) return false
            if (type != other.type) return false
            if (isExtended != other.isExtended) return false
            if (id != other.id) return false

            return true
        }
    }

    data class ItemViewHolder(
        val dataBinding: ViewDataBinding
    ): RecyclerView.ViewHolder(dataBinding.root)
}