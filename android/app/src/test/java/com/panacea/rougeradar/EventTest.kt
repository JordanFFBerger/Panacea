package com.panacea.rougeradar

import org.junit.Assert.*
import org.junit.Test

class EventTest {
    private val example=SampleEvents.all.first()
    @Test fun trendingRequiresBothVolumeAndGrowth() {
        assertFalse(example.copy(recent=19,previous=1).trending)
        assertFalse(example.copy(recent=20,previous=14).trending)
        assertTrue(example.copy(recent=21,previous=14).trending)
        assertTrue(example.copy(recent=20,previous=0).trending)
        assertFalse(example.copy(recent=0,previous=0).trending)
    }
    @Test fun freeEventsAndPaidEventsHaveClearPrices() {
        assertEquals("Free",example.copy(price=0).priceLabel)
        assertEquals("$18",example.copy(price=18).priceLabel)
    }
    @Test fun onlyMusicEventsOfferTheBundledRecording() {
        val track = requireNotNull(example.track)
        assertEquals("On the Cool Side", track.title)
        assertEquals("Kevin MacLeod", track.artist)
        assertEquals(R.raw.on_the_cool_side, track.audioRes)
        assertTrue(SampleEvents.all.filter { it.category != "Music" }.none { it.track != null })
    }
    @Test fun everySampleHasActivitiesAndUniqueIdentity() {
        assertEquals(SampleEvents.all.size,SampleEvents.all.map { it.id }.toSet().size)
        assertTrue(SampleEvents.all.all { it.activities.isNotEmpty() && it.daysAway>=0 && it.price>=0 })
    }
}
