package com.travelai.ui.itinerary

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.travelai.data.model.TripPlanDay
import com.travelai.data.repository.ChatRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ItineraryViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ItineraryUiState(isLoading = true))
    val uiState: StateFlow<ItineraryUiState> = _uiState.asStateFlow()

    private val requestedSessionId: Long? =
        savedStateHandle.get<Long>(SESSION_ID_ARG)
            ?: savedStateHandle.get<String>(SESSION_ID_ARG)?.toLongOrNull()

    init {
        loadItinerary()
    }

    fun loadItinerary() {
        val sessionId = requestedSessionId
        if (sessionId == null || sessionId <= 0L) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = "Không tìm thấy chuyến đi."
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    sessionId = sessionId,
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                chatRepository.loadSession(sessionId)
                    ?: throw IllegalArgumentException("Không tìm thấy chuyến đi.")
            }.onSuccess { session ->
                val snapshot = session.tripPlanSnapshot
                val fallbackText = snapshot?.rawResponse
                    ?.takeIf { it.isNotBlank() }
                    ?: session.messages
                        .lastOrNull { it.role == ROLE_ASSISTANT }
                        ?.content
                        .orEmpty()

                _uiState.update {
                    it.copy(
                        sessionId = session.id,
                        title = session.title,
                        days = snapshot?.days.orEmpty(),
                        rawText = fallbackText,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = throwable.message ?: "Không thể tải lịch trình."
                    )
                }
            }
        }
    }

    private companion object {
        const val SESSION_ID_ARG = "sessionId"
        const val ROLE_ASSISTANT = "assistant"
    }
}

data class ItineraryUiState(
    val sessionId: Long? = null,
    val title: String = "",
    val days: List<TripPlanDay> = emptyList(),
    val rawText: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
