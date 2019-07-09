package com.emmanuelmess.simpleplanner

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import com.emmanuelmess.simpleplanner.events.AllEventsActivity
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf

@RunWith(RobolectricTestRunner::class)
class ExampleUnitTest {
    @Test
    fun testFab() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                shadowOf(activity).clickMenuItem(R.id.action_allevents)

                val expectedIntent = Intent(activity, AllEventsActivity::class.java)
                val actual = shadowOf(activity.application).nextStartedActivity
                assertEquals(expectedIntent.component, actual.component)
            }
        }
    }
}
