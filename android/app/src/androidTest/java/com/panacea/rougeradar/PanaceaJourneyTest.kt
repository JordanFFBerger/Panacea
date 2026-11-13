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

    private fun openEvent(query: String, title: String) {
        val list = app.onNodeWithTag("event-list")
        list.performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").performTextInput(query)
        app.onNodeWithTag("event-search").performImeAction()
        awaitEvent(title)
        app.onNodeWithText(title, substring = true).performClick()
    }

    private fun awaitEvent(title: String) {
        app.waitUntil(15_000) {
            try {
                app.onNodeWithTag("event-list").performScrollToNode(hasText(title, substring = true))
                true
            } catch (_: AssertionError) {
                false
            }
        }
    }

    @Test fun searchOpensFreeEvent() {
        openEvent("white light", "White Light Night")
        app.onNodeWithText("Free").performScrollTo().assertIsDisplayed()
    }

    @Test fun returningFromDetailsPreservesSearch() {
        openEvent("Mariachi", "Mariachi Herencia")
        app.onNodeWithText("← Back to events").performClick()
        app.onNodeWithTag("event-list").performScrollToNode(hasTestTag("event-search"))
        app.onNodeWithTag("event-search").assertTextContains("Mariachi")
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

    @Test fun savedEventAppearsInProfileAndSurvivesActivityRecreation() {
        openEvent("Mariachi", "Mariachi Herencia")
        app.onNodeWithText("♡ Save for later").performScrollTo().performClick()
        app.onNodeWithText("Profile").performClick()
        app.onNodeWithText("Saved Events").assertIsDisplayed()
        awaitEvent("Mariachi Herencia")
        app.activityRule.scenario.recreate()
        awaitEvent("Mariachi Herencia")
        app.onNodeWithText("Mariachi Herencia", substring = true).assertIsDisplayed()
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

    @Test fun fetchedCatalogEventAppearsInDiscoverAndOpensDetails() {
        openEvent("Mariachi Herencia", "Mariachi Herencia")
        app.onNodeWithText("From $20.00").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Organizer tickets ↗").performScrollTo().assertIsDisplayed()
        app.onNodeWithText("Count me in · demo signup").assertDoesNotExist()
    }

    @Test fun androidClientReadsFixturesOverHttpFromTheRunningBackend() = runBlocking {
        val events = EventApiClient.fetchEvents()
        assertTrue(events.any { it.id == "mariachi-heritage-2026" && it.priceMinUsd == 20.0 })
        assertTrue(events.any { it.id == "white-light-night-2026" && it.priceMinUsd == 0.0 })
        assertTrue(events.any { it.id == "weekend-kick-off-2026-11-05" && it.priceMinUsd == null })
    }
}
