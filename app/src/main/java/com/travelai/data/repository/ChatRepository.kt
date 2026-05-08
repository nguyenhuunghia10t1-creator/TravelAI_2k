package com.travelai.data.repository

import com.travelai.data.api.DeepSeekApi
import com.travelai.data.api.DeepSeekChatRequest
import com.travelai.data.api.DeepSeekMessage
import com.travelai.data.db.ChatDao
import com.travelai.data.db.entities.ChatMessageEntity
import com.travelai.data.db.entities.ChatSessionEntity
import com.travelai.data.db.entities.TripProfileEntity
import com.travelai.data.model.TripProfile
import com.travelai.data.model.toSessionTitle
import javax.inject.Inject
import javax.inject.Named

class ChatRepository @Inject constructor(
    private val deepSeekApi: DeepSeekApi,
    private val chatDao: ChatDao,
    @Named("DeepSeekApiKey") private val apiKey: String
) {
    suspend fun loadLatestSession(): StoredChatSession? {
        val session = chatDao.getLatestSession() ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun loadSession(sessionId: Long): StoredChatSession? {
        val session = chatDao.getSession(sessionId) ?: return null
        return loadSessionFromEntity(session)
    }

    suspend fun getSessions(): List<StoredChatSessionSummary> =
        chatDao.getSessions().map { session ->
            StoredChatSessionSummary(
                id = session.id,
                title = session.title,
                createdAt = session.createdAt,
                updatedAt = session.updatedAt
            )
        }

    private suspend fun loadSessionFromEntity(session: ChatSessionEntity): StoredChatSession {
        val messages = chatDao.getMessagesForSession(session.id)
        val tripProfile = chatDao.getTripProfile(session.id)

        return StoredChatSession(
            id = session.id,
            title = session.title,
            createdAt = session.createdAt,
            updatedAt = session.updatedAt,
            tripProfile = tripProfile?.toTripProfile(),
            messages = messages.map {
                StoredChatMessage(
                    role = it.role,
                    content = it.content,
                    createdAt = it.createdAt
                )
            }
        )
    }

    suspend fun createTripSession(profile: TripProfile): Long {
        val now = System.currentTimeMillis()
        val sessionId = chatDao.insertSession(
            ChatSessionEntity(
                title = profile.toSessionTitle(),
                createdAt = now,
                updatedAt = now
            )
        )
        chatDao.insertTripProfile(profile.toEntity(sessionId = sessionId, createdAt = now))
        return sessionId
    }

    suspend fun createSession(firstMessage: String): Long {
        val now = System.currentTimeMillis()
        return chatDao.insertSession(
            ChatSessionEntity(
                title = createSessionTitle(firstMessage),
                createdAt = now,
                updatedAt = now
            )
        )
    }

    suspend fun saveMessage(sessionId: Long, role: String, content: String) {
        val now = System.currentTimeMillis()
        chatDao.insertMessage(
            ChatMessageEntity(
                sessionId = sessionId,
                role = role,
                content = content,
                createdAt = now
            )
        )
        chatDao.updateSessionUpdatedAt(sessionId, now)
    }

    suspend fun sendMessage(messages: List<DeepSeekMessage>): String {
        if (messages.none { it.role == ROLE_USER && it.content.isNotBlank() }) {
            throw IllegalArgumentException("Tin nhắn không được để trống.")
        }
        if (apiKey.isBlank()) {
            throw IllegalStateException("Thiếu DEEPSEEK_API_KEY trong local.properties.")
        }

        val response = deepSeekApi.sendMessage(
            DeepSeekChatRequest(
                messages = messages
            )
        )

        return response.choices
            .firstOrNull()
            ?.message
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("DeepSeek không trả về nội dung.")
    }

    private fun createSessionTitle(firstMessage: String): String {
        val title = firstMessage.trim().lineSequence().firstOrNull().orEmpty()
        if (title.isBlank()) return "New trip"
        return if (title.length <= SESSION_TITLE_MAX_LENGTH) {
            title
        } else {
            title.take(SESSION_TITLE_MAX_LENGTH).trimEnd()
        }
    }

    private companion object {
        const val ROLE_USER = "user"
        const val SESSION_TITLE_MAX_LENGTH = 60
    }
}

private fun TripProfileEntity.toTripProfile(): TripProfile = TripProfile(
    destination = destination,
    days = days,
    budget = budget,
    people = people,
    travelStyle = travelStyle,
    transport = transport,
    note = note
)

private fun TripProfile.toEntity(
    sessionId: Long,
    createdAt: Long
): TripProfileEntity = TripProfileEntity(
    sessionId = sessionId,
    destination = destination.trim(),
    days = days,
    budget = budget.trim(),
    people = people,
    travelStyle = travelStyle.trim(),
    transport = transport.trim(),
    note = note.trim(),
    createdAt = createdAt
)

data class StoredChatSession(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long,
    val tripProfile: TripProfile?,
    val messages: List<StoredChatMessage>
)

data class StoredChatSessionSummary(
    val id: Long,
    val title: String,
    val createdAt: Long,
    val updatedAt: Long
)

data class StoredChatMessage(
    val role: String,
    val content: String,
    val createdAt: Long
)
