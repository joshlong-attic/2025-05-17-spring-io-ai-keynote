package com.example.assistant;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.PromptChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.sql.DataSource;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@SpringBootApplication
public class AssistantApplication {

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    @Bean
    PromptChatMemoryAdvisor promptChatMemoryAdvisor(DataSource dataSource) {
        var jdbc = JdbcChatMemoryRepository
                .builder()
                .dataSource(dataSource)
                .build();
        var m = MessageWindowChatMemory
                .builder()
                .chatMemoryRepository(jdbc)
                .build();
        return PromptChatMemoryAdvisor
                .builder(m)
                .build();
    }

}

record Dog(@Id int id, String name, String owner, String description) {
}

interface DogRepository extends ListCrudRepository<Dog, Integer> {
}

@Controller
@ResponseBody
class AssistantController {

    private final ChatClient ai;

    AssistantController(
            DogRepository repository,
            DogAdoptionScheduler scheduler ,
            ChatClient.Builder builder, VectorStore vectorStore, PromptChatMemoryAdvisor chatMemoryAdvisor) {

        if (false)
            repository.findAll().forEach(dog -> {
                var dogument = new Document("id: %s, name: %s, description: %s".formatted(
                        dog.id(), dog.name(), dog.description()
                ));
                vectorStore.add(List.of(dogument));
            });

        var system = """
                You are an AI powered assistant to help people adopt a dog from the adoption\s
                agency named Pooch Palace with locations in Antwerp, Seoul, Tokyo, Singapore, Paris,\s
                Mumbai, New Delhi, Barcelona, San Francisco, and London. Information about the dogs available\s
                will be presented below. If there is no information, then return a polite response suggesting we\s
                don't have any dogs available.
                """;
        this.ai = builder
                .defaultSystem(system)
                .defaultTools(scheduler)
                .defaultAdvisors(chatMemoryAdvisor, new QuestionAnswerAdvisor(vectorStore))
                .build();
    }

    @PostMapping("/{user}/assistant")
    String assistant(@PathVariable String user, @RequestParam String question) {
        return this.ai
                .prompt(question)
                .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, user))
                .call()
                .content();
    }
}

@Component
class DogAdoptionScheduler {

    @Tool(description = "schedule an appointment to pickup or adopt a dog from a Pooch Palace location")
    String schedule(@ToolParam(description = "the id of the dog") int dogId,
                    @ToolParam(description = "the name of the dog") String dogName) {
        var i = Instant
                .now()
                .plus(3, ChronoUnit.DAYS)
                .toString();
        System.out.println("returning " + i + " for dog " + dogName + "/" + dogId);
        return i;
    }

}
