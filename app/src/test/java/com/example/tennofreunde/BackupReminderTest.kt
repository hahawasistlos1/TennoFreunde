package com.example.tennofreunde

import com.example.tennofreunde.system.isBackupDue
import com.example.tennofreunde.system.isWithinUpcomingWindow
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.TimeUnit

class BackupReminderTest {
    @Test
    fun reminderBecomesDueSevenDaysAfterLastSuccessfulBackup() {
        val now = TimeUnit.DAYS.toMillis(20)
        assertFalse(isBackupDue(true, now, TimeUnit.DAYS.toMillis(14), 0L))
        assertTrue(isBackupDue(true, now, TimeUnit.DAYS.toMillis(13), 0L))
        assertFalse(isBackupDue(false, now, TimeUnit.DAYS.toMillis(1), 0L))
    }

    @Test
    fun firstReminderUsesFeatureActivationTime() {
        val started = TimeUnit.DAYS.toMillis(3)
        assertTrue(isBackupDue(true, TimeUnit.DAYS.toMillis(10), 0L, started))
    }

    @Test
    fun eventAlertOnlyOpensInsideUpcomingWindow() {
        val now = TimeUnit.HOURS.toMillis(10)
        assertTrue(isWithinUpcomingWindow(now, now + TimeUnit.MINUTES.toMillis(30), TimeUnit.MINUTES.toMillis(60)))
        assertFalse(isWithinUpcomingWindow(now, now + TimeUnit.MINUTES.toMillis(61), TimeUnit.MINUTES.toMillis(60)))
        assertFalse(isWithinUpcomingWindow(now, now - 1, TimeUnit.MINUTES.toMillis(60)))
    }
}
