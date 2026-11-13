package com.panacea.rougeradar

import android.content.Context
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PanaceaJourneyTest {
    @get:Rule val app = createAndroidComposeRule<MainActivity>()

    @Before fun resetAppPreferences() {
        app.activityRule.scenario.onActivity {
            it.getSharedPreferences("radar", Context.MODE_PRIVATE).edit().clear().commit()
        }
        app.activityRule.scenario.recreate()
    }

    private fun openJazz() {
        app.onNodeWithTag("event-list").performScrollToNode(hasText("Jazz under the stars"))
        app.onNodeWithText("Jazz under the stars").performClick()
        app.onNodeWithText("Jazz under the stars").assertIsDisplayed()
    }

    @Test fun searchOpensEventAndShowsPriceAndActivities() {
        app.onNodeWithTag("event-list").performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").performTextInput("chapter")
        app.onNodeWithTag("event-search").performImeAction()
        app.onNodeWithTag("event-list").performScrollToNode(hasText("One more chapter"))
        app.onNodeWithText("One more chapter").performClick()
        app.onNodeWithText("Free").assertIsDisplayed()
        app.onNodeWithText("✓ Book discussion").performScrollTo().assertIsDisplayed()
    }

    @Test fun returningFromDetailsPreservesSearch() {
        app.onNodeWithTag("event-list").performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").performTextInput("chapter")
        app.onNodeWithTag("event-search").performImeAction()
        app.onNodeWithTag("event-list").performScrollToNode(hasText("One more chapter"))
        app.onNodeWithText("One more chapter").performClick()
        app.onNodeWithText("← Back to events").performClick()
        app.onNodeWithTag("event-list").performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").assertTextContains("chapter")
    }

    @Test fun catalogParserAcceptsMissingCheckedDate() {
        val json = org.json.JSONObject("""{
            "id":"test", "title":"Test event", "category":"Music", "venue":"Venue",
            "startLocal":"2026-11-06T19:30:00-06:00", "timezone":"America/Chicago",
            "timeType":"performance", "sourceUrls":[]
        }""")
        val event = EventApiClient.parseEvent(json)
        assertEquals("test", event.id)
        assertNull(event.checkedDate)
    }

    @Test fun savingSurvivesActivityRecreation() {
        openJazz()
        app.onNodeWithText("♡ Save for later").performScrollTo().performClick()
        app.onNodeWithText("Saved").performClick()
        app.activityRule.scenario.recreate()
        app.onNodeWithTag("event-list").performScrollToNode(hasText("Jazz under the stars"))
        app.onNodeWithText("Jazz under the stars").assertIsDisplayed()
    }

    @Test fun signupAppearsInProfileAndCanBeUndone() {
        openJazz()
        app.onNodeWithText("Count me in · demo signup").performScrollTo().performClick()
        app.onNodeWithText("Profile").performClick()
        app.onNodeWithTag("event-list").performScrollToNode(hasText("Jazz under the stars"))
        app.onNodeWithText("Jazz under the stars").performClick()
        app.onNodeWithText("✓ On the demo list · undo").performScrollTo().performClick()
        app.onNodeWithText("Profile").performClick()
        app.onNodeWithTag("event-list").performScrollToNode(hasText("A little quiet here."))
        app.onNodeWithText("A little quiet here.").assertIsDisplayed()
    }

    @Test fun explicitThemeSurvivesActivityRecreation() {
        app.onNodeWithText("◐ Auto").performClick()
        app.onNodeWithText("✦  BATON ROUGE, LA").assertIsDisplayed()
        app.onNodeWithText("◐ Day").performClick()
        app.onNodeWithText("✦  AFTER HOURS").assertIsDisplayed()
        app.activityRule.scenario.recreate()
        app.onNodeWithText("◐ Night").assertIsDisplayed()
        app.onNodeWithText("Bored? We have the cure!").assertIsDisplayed()
    }

    @Test fun meetPanaceaOpensAndDismissesTheAttributedSynopsis() {
        app.onNodeWithText("Meet Panacea").performClick()
        app.onNodeWithText("goddess of cures and panaceas", substring = true).assertIsDisplayed()
        app.onNodeWithText("Theoi Project ↗").assertIsDisplayed()
        app.onNodeWithText("She has come down from Mount Olympus to cure your insatiable boredom!").assertIsDisplayed()
        app.onNodeWithText("Done").performClick()
        app.onNodeWithText("Theoi Project ↗").assertDoesNotExist()
    }

    @Test fun listeningRoomPlaysAndPausesTheBundledTrack() {
        openJazz()
        app.onNodeWithText("On the Cool Side").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Kevin MacLeod").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Play preview").performScrollTo().performClick()
        app.onNodeWithTag("preview-progress").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Pause preview").performScrollTo().performClick()
        app.onNodeWithText("Play preview").assertIsDisplayed()
    }
    @Test fun fetchedCatalogEventAppearsInDiscoverAndOpensDetails() {
        app.onNodeWithTag("event-list").performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").performTextInput("Mariachi Herencia")
        app.onNodeWithTag("event-search").performImeAction()
        app.waitUntil(15_000) {
            app.onAllNodesWithText("Baton Rouge event catalog and demo listings").fetchSemanticsNodes().isNotEmpty()
        }
        app.onNodeWithTag("event-list").performScrollToNode(hasText("Mariachi Herencia", substring = true))
        app.onNodeWithText("Mariachi Herencia", substring = true).performClick()
        app.onNodeWithText("From $20.00").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Organizer tickets ↗").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Count me in · demo signup").assertDoesNotExist()
    }

    @Test fun androidClientReadsRealEventsFromTheRunningBackend() = runBlocking {
        val events = EventApiClient.fetchEvents()
        assertTrue(events.any { it.id == "mariachi-heritage-2026" && it.priceMinUsd == 20.0 })
        assertTrue(events.any { it.id == "white-light-night-2026" && it.priceMinUsd == 0.0 })
        assertTrue(events.any { it.id == "weekend-kick-off-2026-11-05" && it.priceMinUsd == null })
    }
}
