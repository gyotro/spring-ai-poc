package gyo.spring_ai_poc

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
class SpringAiPocApplication


fun main(args: Array<String>) {
    runApplication<SpringAiPocApplication>(*args)
}