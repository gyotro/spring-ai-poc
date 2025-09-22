package gyo.spring_ai_poc.chat

import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.mcp.McpToolRegistryProvider
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
// ✅ Koog MCP tool registry (Koog code, not Spring AI)
class MCPConfig {
    private val logger = LoggerFactory.getLogger(this::class.java)
    @Bean
    fun toolRegistry(): Lazy<ToolRegistry> = lazy {
        runBlocking {
            logger.info("MCP tool registry initialized")
            McpToolRegistryProvider.fromTransport(
                McpToolRegistryProvider.defaultSseTransport("http://localhost:8085/sse"),
                name = "weather-client",
                version = "1.0.0"
            )

        }
    }
}
