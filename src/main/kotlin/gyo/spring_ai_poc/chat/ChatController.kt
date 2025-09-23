package gyo.spring_ai_poc.chat

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.chat.prompt.ChatOptions
import org.springframework.ai.tool.function.FunctionToolCallback
import org.springframework.beans.factory.ObjectProvider
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import reactor.core.publisher.Flux

@RestController
class ChatController(private val chatClient: ChatClient.Builder,
                     private val toolRegistryProvider: ObjectProvider<ToolRegistry>
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @GetMapping("/chat")
    fun chat(): String? {
        return chatClient.build().prompt()
            .user("Tell me an important fact about Italy")
            .call()
            .content()
    }

    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(
        @RequestParam(
            required = false,
            defaultValue = "tell me 5 facts about Java"
        ) message: String
    ): Flux<String> {
        return chatClient.build().prompt()
            .user(message)
            .system( """
        You are a helpful assistant. 
        Only call a tool if the user explicitly asks for information the tool provides 
        (e.g., weather, forecasts, city temperature). 
        Otherwise, answer normally without suggesting a tool call.
    """.trimIndent())
            .stream()
            .content()
            .buffer(10)  // adjust batch size
            .map { it.joinToString(" ") }
    }


    @GetMapping("/ai/chat", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun chat(@RequestParam(defaultValue = "What's the weather in Rome?") message: String): Flux<String> {
        logger.info("Chat: $message")

        // ✅ Wrap MCP tool as Spring AI FunctionToolCallback
        val weatherToolCallback = FunctionToolCallback.builder<Map<String, Any>, String>("getWeather") { input, _ ->
            val location = (input["location"] as? String)?.trim() ?: "unknown"
            val toolRegistry = toolRegistryProvider.ifAvailable
                ?: return@builder "⚠️ ToolRegistry not initialized"

            val tool = toolRegistry.getTool("getWeather")
                ?: return@builder "Weather tool unavailable"

            val result = runBlocking { tool.runWithArgs(mapOf("location" to location)) }
            "The current weather in $location is: $result"
        }
            .description(
                "A function that retrieves live weather data. " +
                        "Always call this if the user asks about weather, forecasts, " +
                        "temperature, rain, sun, or similar conditions."
            )
            .inputType(Map::class.java)
            .build()

        return chatClient.build().prompt()
            .user(message)
            .system(
                """
            You are a helpful assistant.
            
            🔹 If the user asks about the weather, forecasts, temperature, or city conditions,
               ALWAYS call the "getWeather" tool. 
               Do NOT answer weather questions from your own knowledge.
            
            🔹 For all other questions, answer normally without tools.
            """.trimIndent()
            )
            .options(
                ChatOptions.builder()
                    .temperature(0.35) // ✅ slightly higher than 0.2 to allow tool calls
                    .build()
            )
            .toolCallbacks(weatherToolCallback)
            .stream()
            .content()
            .filter { it.isNotBlank() }
            .buffer(10)
            .map { it.joinToString(" ") }
    }

    @GetMapping("/ai/test")
    fun testWeather(@RequestParam message: String): String {
        val weatherToolCallback = FunctionToolCallback.builder<Map<String, Any>, String>("getWeather") { input, _ ->
            val location = (input["location"] as? String)?.trim() ?: "unknown"
            val tool = toolRegistryProvider.ifAvailable?.getTool("getWeather")
                ?: return@builder "Weather tool unavailable"

            val result = runBlocking { tool.runWithArgs(mapOf("location" to location)) }
            "The current weather in $location is: $result"
        }
            .description("Get current weather for a given location")
            .inputType(Map::class.java)
            .build()

        return chatClient.build().prompt()
            .user(message)
            .system("You are a helpful assistant. Use the weather tool only for weather questions.")
            .toolCallbacks(weatherToolCallback)
            .call() // ✅ blocking call, no streaming
            .content() ?: "No answer"

    }


    // Extension function to run a Koog Tool with arguments safely
    suspend fun Tool<*, *>.runWithArgs(args: Map<String, Any?>): Any? {
        @Suppress("UNCHECKED_CAST")
        return (this as Tool<Map<String, Any?>, Any?>).run { args }
    }


}