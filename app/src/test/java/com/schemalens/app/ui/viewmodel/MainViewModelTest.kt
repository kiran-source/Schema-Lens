package com.schemalens.app.ui.viewmodel

import com.schemalens.app.data.AppTab
import com.schemalens.app.data.AppZone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class MainViewModelTest {

    private lateinit var viewModel: MainViewModel

    @Before
    fun setUp() {
        viewModel = MainViewModel()
    }

    @Test
    fun testInitialStateHasDefaultTracedSites() {
        val state = viewModel.uiState.value
        assertEquals(AppTab.SCHEMA, state.currentTab)
        assertEquals(AppZone.RED_LIGHT, state.activeZone)
        assertTrue(state.callSites.isNotEmpty())
        assertTrue(state.schemaEntities.isNotEmpty())
    }

    @Test
    fun testSelectTabUpdatesTabAndZone() {
        // Switching to Assess should switch to Green Light
        viewModel.selectTab(AppTab.ASSESS)
        assertEquals(AppTab.ASSESS, viewModel.uiState.value.currentTab)
        assertEquals(AppZone.GREEN_LIGHT, viewModel.uiState.value.activeZone)

        // Switching to Schema should switch to Red Light
        viewModel.selectTab(AppTab.SCHEMA)
        assertEquals(AppTab.SCHEMA, viewModel.uiState.value.currentTab)
        assertEquals(AppZone.RED_LIGHT, viewModel.uiState.value.activeZone)

        // Switching to Export should switch to Green Light
        viewModel.selectTab(AppTab.EXPORT)
        assertEquals(AppTab.EXPORT, viewModel.uiState.value.currentTab)
        assertEquals(AppZone.GREEN_LIGHT, viewModel.uiState.value.activeZone)
    }

    @Test
    fun testAddAndRemoveCustomIdentifier() {
        viewModel.addCustomIdentifier("user_account_id")
        assertTrue(viewModel.uiState.value.extractedIdentifiers.contains("user_account_id"))

        // Attempting to add duplicate should not duplicate
        val countBefore = viewModel.uiState.value.extractedIdentifiers.size
        viewModel.addCustomIdentifier("user_account_id")
        val countAfter = viewModel.uiState.value.extractedIdentifiers.size
        assertEquals(countBefore, countAfter)

        // Remove identifier
        viewModel.removeIdentifierChip("user_account_id")
        assertFalse(viewModel.uiState.value.extractedIdentifiers.contains("user_account_id"))
    }

    @Test
    fun testApplyChangePreset() {
        viewModel.updateChangeNotes("")
        viewModel.applyChangePreset("DROP users.email")
        assertEquals("DROP users.email", viewModel.uiState.value.changeNotes)

        viewModel.applyChangePreset("RENAME orders.total")
        assertEquals("DROP users.email RENAME orders.total", viewModel.uiState.value.changeNotes)
    }

    @Test
    fun testPerformTraceWithEmptyPackageSetsError() {
        viewModel.updatePackageName("")
        viewModel.performTrace()
        assertTrue(viewModel.uiState.value.callSites.isEmpty())
        assertTrue(viewModel.uiState.value.traceError != null)
    }
}
