package gyo.spring_ai_poc.chat

import ai.koog.agents.core.tools.Tool
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import jakarta.servlet.http.HttpServletResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.ai.chat.client.ChatClient
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
        // Create a FunctionToolCallback that wraps your MCP tool
        logger.info("Chat: $message")
        val weatherToolCallback = FunctionToolCallback.builder<Map<String, Any>, String>("getWeather") { input, _ ->
            val location = (input["location"] as? String) ?: "unknown"
            val toolRegistry = toolRegistryProvider.ifAvailable
                ?: return@builder "⚠️ ToolRegistry not initialized"

            val tool = toolRegistry.getTool("getWeather")

            //val weatherTool = toolRegistry.getTool("getWeather") as? Tool<*, *>
            val result = if (tool != null) {
                runBlocking { tool.runWithArgs(mapOf("location" to location)) }
            } else {
                "Weather tool unavailable"
            }

            result.toString()
        }
            .description("Get the weather forecast for a given location")
            .inputType(Map::class.java)
            .build()

        return chatClient.build().prompt()
            .user(message)
            .system( """
        You are a helpful assistant. 
        Only call a tool if the user explicitly asks for information the tool provides 
        (e.g., weather, forecasts, city temperature). 
        Otherwise, answer normally without suggesting a tool call.
    """.trimIndent())
            .toolCallbacks(weatherToolCallback)  // make the tool available
            .stream()
            .content()
            .buffer(10)  // adjust batch size
            .map { it.joinToString(" ") }
    }
    suspend fun Tool<*, *>.runWithArgs(args: Map<String, Any?>): Any? {
        @Suppress("UNCHECKED_CAST")
        return (this as Tool<Map<String, Any?>, Any?>).run { args }
    }

}