package com.emmanuelmess.simpleplanner.events

import android.content.Context
import android.text.format.DateUtils
import androidx.room.*
import com.emmanuelmess.simpleplanner.common.NoDatabaseException
import com.emmanuelmess.simpleplanner.common.setToFirstInstant
import com.emmanuelmess.simpleplanner.events.EventTable.COMMENT_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.EVENT_TABLE
import com.emmanuelmess.simpleplanner.events.EventTable.ID_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMEEND_HOUROFDAY_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMEEND_MINUTE_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMESTART_HOUROFDAY_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMESTART_MINUTE_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TITLE_COLUMN
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

data class Event(
    val title: String,
    val timeSpan: String,
    val comment: String,
    val delete: EventDao.() -> Unit,
    val timeStartHourOfDay: Int,
    val timeStartMinute: Int,
    val timeEndHourOfDay: Int,
    val timeEndMinute: Int,
    val getUid: () -> Int?
)

@Entity(tableName = EVENT_TABLE)
data class EventEntity(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = TITLE_COLUMN) val title: String,
    @ColumnInfo(name = TIMESTART_HOUROFDAY_COLUMN) val timeStartHourOfDay: Int,
    @ColumnInfo(name = TIMESTART_MINUTE_COLUMN) val timeStartMinute: Int,
    @ColumnInfo(name = TIMEEND_HOUROFDAY_COLUMN) val timeEndHourOfDay: Int,
    @ColumnInfo(name = TIMEEND_MINUTE_COLUMN) val timeEndMinute: Int,
    @ColumnInfo(name = COMMENT_COLUMN) val comment: String
) {
    @Suppress("MoveLambdaOutsideParentheses")
    fun toEvent(context: Context): Event {
        uid!!

        val timeStart = Calendar.getInstance().setToFirstInstant().apply {
            set(Calendar.HOUR_OF_DAY, timeStartHourOfDay)
            set(Calendar.MINUTE, timeStartMinute)
        }.timeInMillis

        val timeEnd = Calendar.getInstance().setToFirstInstant().apply {
            set(Calendar.HOUR_OF_DAY, timeEndHourOfDay)
            set(Calendar.MINUTE, timeEndMinute)
        }.timeInMillis

        return Event(
            title,
            DateUtils.formatDateRange(context, timeStart, timeEnd, DateUtils.FORMAT_SHOW_TIME),
            comment,
            { this.delete(uid) },
            timeStartHourOfDay,
            timeStartMinute,
            timeEndHourOfDay,
            timeEndMinute,
            { uid }
        )
    }

    @Suppress("MoveLambdaOutsideParentheses")
    fun toEvent(context: Context, futureId: Future<Int>): Event {
        val timeStart = Calendar.getInstance().setToFirstInstant().apply {
            set(Calendar.HOUR_OF_DAY, timeStartHourOfDay)
            set(Calendar.MINUTE, timeStartMinute)
        }.timeInMillis

        val timeEnd = Calendar.getInstance().setToFirstInstant().apply {
            set(Calendar.HOUR_OF_DAY, timeEndHourOfDay)
            set(Calendar.MINUTE, timeEndMinute)
        }.timeInMillis

        return Event(
            title,
            DateUtils.formatDateRange(context, timeStart, timeEnd, DateUtils.FORMAT_SHOW_TIME),
            comment,
            {
                try {
                    this.delete(futureId.get())
                } catch (e: ExecutionException) {
                    if(e.cause is NoDatabaseException) {
                        //Database was probably null
                        // and nothing was written to the database
                    } else {
                        throw RuntimeException(e)
                    }
                }
            },
            timeStartHourOfDay,
            timeStartMinute,
            timeEndHourOfDay,
            timeEndMinute,
            {
                try {
                    futureId.get()
                } catch (e: ExecutionException) {
                    if (e.cause is NoDatabaseException) {
                        //Database was probably null
                        // and nothing was written to the database
                        null
                    } else {
                        throw RuntimeException(e)
                    }
                }
            }
        )
    }
}

@Dao
interface EventDao {
    @Query("SELECT * FROM $EVENT_TABLE")
    fun getAll(): List<EventEntity>

    @Query("""
        SELECT * FROM $EVENT_TABLE 
        WHERE ((:nowHourOfDay) BETWEEN $TIMESTART_HOUROFDAY_COLUMN AND $TIMEEND_HOUROFDAY_COLUMN)
        AND ((:nowHourOfDay) != $TIMESTART_HOUROFDAY_COLUMN OR ($TIMESTART_MINUTE_COLUMN <= (:minute)))
        AND ((:nowHourOfDay) != $TIMEEND_HOUROFDAY_COLUMN OR ((:minute) >= $TIMEEND_MINUTE_COLUMN))
        """)
    fun getAllDoableNow(nowHourOfDay: Short, minute: Short): List<EventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(event: EventEntity): Long

    @Query("DELETE FROM $EVENT_TABLE WHERE $ID_COLUMN=:id")
    fun delete(id: Int)
}

object EventTable {
    const val EVENT_TABLE = "event"

    const val ID_COLUMN = "uid"
    const val TITLE_COLUMN = "title"
    const val TIMESTART_HOUROFDAY_COLUMN = "timestart_hourofday"
    const val TIMESTART_MINUTE_COLUMN = "timestart_minute"
    const val TIMEEND_HOUROFDAY_COLUMN = "timeend_hourofday"
    const val TIMEEND_MINUTE_COLUMN = "timeend_minute"
    const val COMMENT_COLUMN = "comment"
}