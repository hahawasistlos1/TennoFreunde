package com.example.tennofreunde

import com.example.tennofreunde.system.TennoUpdateMessagingService
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UpdatePushVersionTest {
    @Test
    fun onlyNewerReleaseVersionsCreateUpdatePushes() {
        assertTrue(TennoUpdateMessagingService.isNewerVersion("11.8", "11.7"))
        assertTrue(TennoUpdateMessagingService.isNewerVersion("12.0.1", "11.99"))
        assertFalse(TennoUpdateMessagingService.isNewerVersion("11.7", "11.7"))
        assertFalse(TennoUpdateMessagingService.isNewerVersion("11.6", "11.7"))
        assertFalse(TennoUpdateMessagingService.isNewerVersion("v11.7-beta", "11.7"))
    }
}
