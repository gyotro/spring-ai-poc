# Spring AI POC

A Proof of Concept (POC) project demonstrating the integration of Spring AI capabilities in a Kotlin-based Spring Boot application, featuring MCP (Model Context Protocol) tool integration and Koog API for enhanced AI tooling capabilities.

## Features

- REST API endpoints for AI-powered chat interactions
- Streaming responses for real-time AI interactions
- MCP (Model Context Protocol) tool integration for weather information
- Built with Spring Boot and Kotlin
- Demonstrates usage of Spring AI's ChatClient and Function Callbacks
- Ready-to-run with minimal configuration

## Prerequisites

- Java 17 or higher
- Maven 3.6.3 or higher
- (Optional) Your favorite IDE (IntelliJ IDEA recommended)

## Getting Started

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd spring-ai-poc
   ```

2. **Build the application**
   ```bash
   ./mvnw clean package
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Test the API**
   Open your browser or use a tool like curl to access the chat endpoint:
   ```
   http://localhost:8080/chat
   ```

## API Endpoints

### GET /chat
Returns an AI-generated response to a predefined prompt about Italy.

**Response:**
```
AI-generated fact about Italy
```

### GET /stream
Streams AI-generated responses in real-time using Server-Sent Events (SSE).

**Query Parameters:**
- `message`: (Optional) The message to send to the AI. Default: "tell me 5 facts about Java"

**Response:**
Server-Sent Events stream with AI responses

### GET /ai/chat
Chat endpoint with MCP tool integration for weather information.

**Query Parameters:**
- `message`: (Optional) The message to send to the AI. Default: "What's the weather in Rome?"

**Response:**
Server-Sent Events stream with AI responses and tool call results

**Example:**
```
What's the weather in Paris?
```

**Response:**
```
The current weather in Paris is 22°C and sunny.
```

## Project Structure

```
src/
├── main/
│   ├── kotlin/gyo/spring_ai_poc/
│   │   ├── chat/
│   │   │   ├── ChatController.kt    # REST controller for chat endpoints
│   │   │   └── McpConfig.kt         # MCP tool configuration
│   │   └── SpringAiPocApplication.kt # Main application class
│   └── resources/
│       └── application.properties    # Application configuration
```

## Configuration

## Configuration

The application can be configured via `application.properties`:

```properties
# Server port (default: 8080)
server.port=8080

# Spring AI configuration
# Add your AI provider configuration here when needed

# MCP Server Configuration
mcp.server.url=http://localhost:8085/sse
mcp.tool.name=weather-client
mcp.tool.version=1.0.0
```

## MCP Tool Integration with Koog API

This project integrates with MCP (Model Context Protocol) tools using Koog's AI tooling framework for enhanced functionality:

- **Koog API Integration**: Utilizes Koog's AI tooling framework for MCP (Model Context Protocol) implementation
- **Weather Tool**: Get weather information for any location using MCP tools
- The AI will automatically use the appropriate tool when the user asks about weather information
- Tools are called asynchronously and their results are streamed back to the user
- **Tool Registry**: Leverages Koog's ToolRegistry for dynamic tool discovery and execution

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the [MIT License](LICENSE).

---

Built with ❤️ using Spring Boot and Kotlin
