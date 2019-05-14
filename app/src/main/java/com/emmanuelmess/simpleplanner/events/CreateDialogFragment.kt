package com.emmanuelmess.simpleplanner.events

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.emmanuelmess.simpleplanner.MainActivity
import com.emmanuelmess.simpleplanner.R
import kotlinx.android.synthetic.main.fragment_createdialog.*
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.FutureTask

class CreateDialogFragment : DialogFragment() {
    companion object {
        const val TAG = "dialog_fragment"

        @JvmStatic
        fun newInstance() = CreateDialogFragment()
    }

    var onPositiveButton: ((Event) -> Unit)? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.FullScreenDialogStyle);
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
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.action_save) {
                    val startCalendar = Calendar.getInstance().apply {
                        set(Calendar.MINUTE, timeStartChip.minute)
                        set(Calendar.HOUR_OF_DAY, timeStartChip.hourOfDay)
                    }

                    val endCalendar = Calendar.getInstance().apply {
                        set(Calendar.MINUTE, timeStartChip.minute)
                        set(Calendar.HOUR_OF_DAY, timeStartChip.hourOfDay)
                    }

                    val entity = EventEntity(
                        null,
                        titleEditText.text.toString(),
                        startCalendar.timeInMillis,
                        endCalendar.timeInMillis,
                        commentEditText.text.toString()
                    )


                    val futureId = saveData(entity)
                    onPositiveButton?.invoke(entity.toEvent(context, futureId))

                    dismiss()
                }
                item.itemId == R.id.action_save
            }
        }
    }

    private fun saveData(entity: EventEntity): FutureTask<Int> {
        val uDatabase = WeakReference((activity as MainActivity).db)

        val future = FutureTask<Int> {
            val sDatabase = uDatabase.get() ?: throw NullPointerException()

            sDatabase.eventDao().insert(entity).toInt()
        }

        future.run()

        return future
    }
}
