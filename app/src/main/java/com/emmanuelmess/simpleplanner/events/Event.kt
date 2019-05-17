package com.emmanuelmess.simpleplanner.events

import android.content.Context
import android.text.format.DateUtils
import androidx.room.*
import com.emmanuelmess.simpleplanner.common.NoDatabaseException
import com.emmanuelmess.simpleplanner.events.EventTable.COMMENT_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.EVENT_TABLE
import com.emmanuelmess.simpleplanner.events.EventTable.ID_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMEEND_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TIMESTART_COLUMN
import com.emmanuelmess.simpleplanner.events.EventTable.TITLE_COLUMN
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future

data class Event(
    val title: String,
    val timeSpan: String,
    val comment: String,
    val delete: EventDao.() -> Unit
)

@Entity(tableName = EVENT_TABLE)
data class EventEntity(
    @PrimaryKey val uid: Int? = null,
    @ColumnInfo(name = TITLE_COLUMN) val title: String,
    @ColumnInfo(name = TIMESTART_COLUMN) val timeStart: Long,
    @ColumnInfo(name = TIMEEND_COLUMN) val timeEnd: Long,
    @ColumnInfo(name = COMMENT_COLUMN) val comment: String
) {
    @Suppress("MoveLambdaOutsideParentheses")
    fun toEvent(context: Context): Event {
        uid!!

        return Event(
            title,
            DateUtils.formatDateRange(context, timeStart, timeEnd, DateUtils.FORMAT_SHOW_TIME),
            comment,
            { this.delete(uid) }
        )
    }

    @Suppress("MoveLambdaOutsideParentheses")
    fun toEvent(context: Context, futureId: Future<Int>): Event = Event(
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
        }
    )
}

@Dao
interface EventDao {
    @Query("SELECT * FROM $EVENT_TABLE")
    fun getAll(): List<EventEntity>

    @Query("SELECT * FROM $EVENT_TABLE WHERE $ID_COLUMN IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<EventEntity>

    @Insert
    fun insert(event: EventEntity): Long

    @Query("DELETE FROM $EVENT_TABLE WHERE $ID_COLUMN=:id")
    fun delete(id: Int)
}

object EventTable {
    const val EVENT_TABLE = "event"

    const val ID_COLUMN = "uid"
    const val TITLE_COLUMN = "title"
    const val TIMESTART_COLUMN = "timestart"
    const val TIMEEND_COLUMN = "timeend"
    const val COMMENT_COLUMN = "comment"
}