package com.travelai.ui.planner

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class TripPlannerViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(TripPlannerUiState())
    val uiState: StateFlow<TripPlannerUiState> = _uiState.asStateFlow()

    fun onDestinationChange(value: String) = updateField { copy(destination = value, errorMessage = null) }

    fun onDaysChange(value: String) = updateField { copy(days = value.filter(Char::isDigit), errorMessage = null) }

    fun onBudgetChange(value: String) = updateField { copy(budget = value, errorMessage = null) }

    fun onPeopleChange(value: String) = updateField { copy(people = value.filter(Char::isDigit), errorMessage = null) }

    fun onTravelStyleChange(value: String) = updateField { copy(travelStyle = value, errorMessage = null) }

    fun onTransportChange(value: String) = updateField { copy(transport = value, errorMessage = null) }

    fun onNoteChange(value: String) = updateField { copy(note = value, errorMessage = null) }

    fun createTrip() {
        val state = _uiState.value

        val validationError = state.validationError()
        if (validationError != null) {
            _uiState.update { it.copy(errorMessage = validationError) }
            return
        }

        _uiState.update {
            it.copy(
                errorMessage = null,
                submittedPrompt = state.toPlannerPrompt()
            )
        }
    }

    fun consumeSubmittedPrompt() {
        _uiState.update { it.copy(submittedPrompt = null) }
    }

    private fun updateField(reducer: TripPlannerUiState.() -> TripPlannerUiState) {
        _uiState.update { it.reducer().copy(submittedPrompt = null) }
    }
}

data class TripPlannerUiState(
    val destination: String = "",
    val days: String = "3",
    val budget: String = "",
    val people: String = "2",
    val travelStyle: String = "Tự túc cân bằng",
    val transport: String = "Đi bộ, taxi hoặc xe công nghệ",
    val note: String = "",
    val errorMessage: String? = null,
    val submittedPrompt: String? = null
)

private fun TripPlannerUiState.validationError(): String? {
    val daysValue = days.toIntOrNull()
    val peopleValue = people.toIntOrNull()

    return when {
        destination.isBlank() -> "Nhập điểm đến trước khi tạo lịch trình."
        daysValue == null || daysValue <= 0 -> "Số ngày phải lớn hơn 0."
        peopleValue == null || peopleValue <= 0 -> "Số người phải lớn hơn 0."
        else -> null
    }
}

private fun TripPlannerUiState.toPlannerPrompt(): String = buildString {
    appendLine("Hãy lập lịch trình du lịch chi tiết theo ngày và buổi cho chuyến đi sau:")
    appendLine("- Điểm đến: ${destination.trim()}")
    appendLine("- Số ngày: ${days.toIntOrNull() ?: 1}")
    appendLine("- Số người: ${people.toIntOrNull() ?: 1}")
    appendOptionalLine("Ngân sách", budget)
    appendOptionalLine("Phong cách", travelStyle)
    appendOptionalLine("Phương tiện", transport)
    appendOptionalLine("Ghi chú", note)
    appendLine()
    appendLine("Yêu cầu trả lời:")
    appendLine("- Chia rõ Ngày 1, Ngày 2... và Sáng / Chiều / Tối.")
    appendLine("- Gợi ý địa điểm, món ăn, thời lượng và thứ tự di chuyển hợp lý.")
    appendLine("- Nêu lưu ý chi phí phù hợp với ngân sách nếu có.")
    append("- Trả lời thực tế, dễ làm theo, bằng tiếng Việt.")
}

private fun StringBuilder.appendOptionalLine(
    label: String,
    value: String
) {
    val trimmedValue = value.trim()
    if (trimmedValue.isNotBlank()) {
        appendLine("- $label: $trimmedValue")
    }
}
