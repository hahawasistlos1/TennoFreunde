package com.example.tennofreunde

import com.example.tennofreunde.utils.warframeItemImageUrl
import org.junit.Assert.assertEquals
import org.junit.Test

class WarframeImageResolverTest {
    @Test
    fun buildsCdnImageUrlsFromItemNames() {
        assertEquals(
            "https://cdn.warframestat.us/img/BratonPrime.png",
            warframeItemImageUrl("Braton Prime")
        )
        assertEquals(
            "https://cdn.warframestat.us/img/RhinoPrime.png",
            warframeItemImageUrl("Rhino Prime")
        )
    }
}
