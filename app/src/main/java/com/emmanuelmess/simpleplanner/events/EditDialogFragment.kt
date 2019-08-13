package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.emmanuelmess.simpleplanner.R
import com.emmanuelmess.simpleplanner.common.AppDatabaseAwareActivity
import com.emmanuelmess.simpleplanner.common.NoDatabaseException
import com.emmanuelmess.simpleplanner.common.setToFirstInstant
import kotlinx.android.synthetic.main.fragment_createdialog.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.FutureTask
import kotlin.concurrent.thread

class EditDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "dialog_fragment"

        @JvmStatic
        fun newInstance() = EditDialogFragment()
    }

    var onPositiveButton: ((Event) -> Unit)? = null
    var eventToEdit: Event? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle)
    }

    override fun onCreateView(inflater: LayoutInflater, parent: ViewGroup?, state: Bundle?): View? {
        super.onCreateView(inflater, parent, state)

        return requireActivity().layoutInflater.inflate(R.layout.fragment_createdialog, parent, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window?.setLayout(width, height)
        }

        with(toolbar) {
            setNavigationOnClickListener {
                dialog?.cancel()
            }
            inflateMenu(R.menu.menu_dialog)
            setOnMenuItemClickListener(::onMenuItemClick)
        }

        titleEditText.setOnEditorActionListener { v, actionId, event ->
            if(titleEditText.text != null && titleEditText.text!!.isEmpty()) {
                titleTextInputLayout.error = null
            }

            false
        }

        val callback = {
            val startCalendar = Calendar.getInstance().setToFirstInstant().apply {
                set(Calendar.MINUTE, timeStartChip.minute)
                set(Calendar.HOUR_OF_DAY, timeStartChip.hourOfDay)
            }

            val endCalendar = Calendar.getInstance().setToFirstInstant().apply {
                set(Calendar.MINUTE, timeEndChip.minute)
                set(Calendar.HOUR_OF_DAY, timeEndChip.hourOfDay)
            }

            if(startCalendar.before(endCalendar)
                && !(timeStartChip.minute == timeEndChip.minute && timeStartChip.hourOfDay == timeEndChip.hourOfDay)) {
                errorTextView.visibility = GONE
            }
        }
        timeStartChip.callback = callback
        timeEndChip.callback = callback

        if(eventToEdit != null) {
            eventToEdit?.let { eventToEdit ->
                titleEditText.setText(eventToEdit.title)
                timeStartChip.setValue(eventToEdit.timeStartHourOfDay, eventToEdit.timeStartMinute)
                timeEndChip.setValue(eventToEdit.timeEndHourOfDay, eventToEdit.timeEndMinute)
                commentEditText.setText(eventToEdit.comment)
            }
        } else {
            timeStartChip.setValue(10, 0)
            timeEndChip.setValue(15, 0)
        }
    }

    private fun onMenuItemClick(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_save) {
            val checkFailed: () -> Boolean = {
                val startCalendar = Calendar.getInstance().setToFirstInstant().apply {
                    set(Calendar.MINUTE, timeStartChip.minute)
                    set(Calendar.HOUR_OF_DAY, timeStartChip.hourOfDay)
                }

                val endCalendar = Calendar.getInstance().setToFirstInstant().apply {
                    set(Calendar.MINUTE, timeEndChip.minute)
                    set(Calendar.HOUR_OF_DAY, timeEndChip.hourOfDay)
                }

                if (!startCalendar.before(endCalendar)
                    || (timeStartChip.minute == timeEndChip.minute && timeStartChip.hourOfDay == timeEndChip.hourOfDay)
                ) {
                    errorTextView.visibility = VISIBLE
                    true
                } else if (titleEditText.text == null || titleEditText.text!!.isEmpty()) {
                    titleTextInputLayout.error = "Title can't be empty"
                    true
                } else {
                    false
                }
            }

            if(checkFailed()) {
                return true
            }

            val entity = EventEntity(
                eventToEdit?.getUid?.invoke(),
                titleEditText.text.toString(),
                timeStartChip.hourOfDay,
                timeStartChip.minute,
                timeEndChip.hourOfDay,
                timeEndChip.minute,
                commentEditText.text.toString()
            )

            val futureId = saveData(entity)
            onPositiveButton?.invoke(entity.toEvent(requireContext(), futureId))

            dismiss()

            return true
        }

        return false
    }

    private fun saveData(entity: EventEntity): Future<Int> {
        val uDatabase = WeakReference((activity as AppDatabaseAwareActivity).database)

        val future = FutureTask {
            val sDatabase = uDatabase.get() ?: throw NoDatabaseException()

            sDatabase.eventDao().insert(entity).toInt()
        }

        thread {
            future.run()
        }

        return future
    }
}
