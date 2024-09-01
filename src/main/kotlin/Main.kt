package griffio

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.java.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

// https://docs.anthropic.com/en/api/messages
@Serializable
data class Message(
    val role: String,
    val content: String
)

@Serializable
data class MessageRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int,
    val temperature: Double? = null,
    val system: String? = null
)

@Serializable
data class MessageResponse(
    val id: String? = null,
    val type: String? = null,
    val role: String? = null,
    val content: List<MessageContent>? = null,
    val model: String? = null,
    val stop_reason: String? = null,
    val stop_sequence: String? = null,
    val usage: Usage? = null
)

@Serializable
data class MessageContent(
    val type: String? = null,
    val text: String? = null
)

@Serializable
data class Usage(
    val input_tokens: Int? = null,
    val output_tokens: Int? = null
)

@Serializable
data class ErrorResponse(
    val type: String? = null,
    val message: String? = null,
    val error: ErrorDetails? = null
)

@Serializable
data class ErrorDetails(
    val type: String? = null,
    val message: String? = null
)

sealed class AnthropicResult {
    data class Success(val response: MessageResponse) : AnthropicResult()
    data class Error(val error: ErrorResponse) : AnthropicResult()
}

class AnthropicClient(private val apiKey: String) {
    private val client = HttpClient(Java) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    private val baseUrl = "https://api.anthropic.com/v1"

    suspend fun createMessage(request: MessageRequest): AnthropicResult {
        return try {
            val response: HttpResponse = client.post("$baseUrl/messages") {
                contentType(ContentType.Application.Json)
                header("x-api-key", apiKey)
                header("anthropic-version", "2023-06-01")
                setBody(request)
            }

            if (response.status.isSuccess()) {
                AnthropicResult.Success(response.body())
            } else {
                val errorBody = response.bodyAsText()
                val errorResponse = Json.decodeFromString<ErrorResponse>(errorBody)
                AnthropicResult.Error(errorResponse)
            }
        } catch (e: Exception) {
            AnthropicResult.Error(ErrorResponse(message = e.message ?: "Unknown error occurred"))
        }
    }

    fun close() {
        client.close()
    }
}

// Example usage
suspend fun main() {
    val apiKey = "FIXME"
    val anthropicClient = AnthropicClient(apiKey)

    val request = MessageRequest(
        model = "claude-3-sonnet-20240229",
        messages = listOf(
            Message(role = "user", content = "Explain the concept of photosynthesis in a concise paragraph" +
                    " returned in lines of 80 characters.")
        ),
        max_tokens = 500,
        system = "You are a helpful and excited scientist respond with enthusiasm"
    )

    try {
        when (val result = anthropicClient.createMessage(request)) {
            is AnthropicResult.Success -> println("Response: ${result.response.content?.firstOrNull()?.text}")
            is AnthropicResult.Error -> {
                val errorMessage = result.error.message
                    ?: result.error.error?.message
                    ?: "Unknown error"
                println("Error: $errorMessage")
            }
        }
    } finally {
        anthropicClient.close()
    }
}
