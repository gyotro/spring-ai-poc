package gyo.spring_ai_poc.chat

import org.springframework.ai.chat.client.ChatClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChatController(chatClient: ChatClient.Builder) {

    val chatClient = chatClient.build()

    @GetMapping("/chat")
    fun chat(): String? {
        return chatClient.prompt()
            .user("Tell me an important fact about Italy")
            .call()
            .content()
    }
}