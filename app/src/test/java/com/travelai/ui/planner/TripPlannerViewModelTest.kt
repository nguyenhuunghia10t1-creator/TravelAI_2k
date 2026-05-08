package com.travelai.ui.planner

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TripPlannerViewModelTest {
    @Test
    fun createTrip_requiresDestination() {
        val viewModel = TripPlannerViewModel()

        viewModel.onDestinationChange("")
        viewModel.createTrip()

        val state = viewModel.uiState.value
        assertEquals("Nhập điểm đến trước khi tạo lịch trình.", state.errorMessage)
        assertNull(state.submittedPrompt)
    }

    @Test
    fun createTrip_submitsPromptWithOptionalFields() {
        val viewModel = TripPlannerViewModel()

        viewModel.onDestinationChange("Đà Nẵng")
        viewModel.onDaysChange("3")
        viewModel.onPeopleChange("2")
        viewModel.onBudgetChange("5 triệu / người")
        viewModel.onTravelStyleChange("Ăn uống và nghỉ dưỡng")
        viewModel.onTransportChange("Taxi")
        viewModel.createTrip()

        val prompt = viewModel.uiState.value.submittedPrompt.orEmpty()
        assertTrue(prompt.contains("- Điểm đến: Đà Nẵng"))
        assertTrue(prompt.contains("- Số ngày: 3"))
        assertTrue(prompt.contains("- Số người: 2"))
        assertTrue(prompt.contains("- Ngân sách: 5 triệu / người"))
        assertTrue(prompt.contains("- Phong cách: Ăn uống và nghỉ dưỡng"))
        assertTrue(prompt.contains("- Phương tiện: Taxi"))
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
