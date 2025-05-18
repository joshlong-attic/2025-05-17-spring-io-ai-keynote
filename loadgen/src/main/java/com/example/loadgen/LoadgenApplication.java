package com.example.loadgen;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.client.RestClient;

import java.util.Map;

@EnableScheduling
@SpringBootApplication
public class LoadgenApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoadgenApplication.class, args);
    }


    private final RestClient http =
            RestClient.builder().build();

    @Scheduled(fixedDelay = 1000)
    void bark() {
        this.http.post()
                .uri("http://localhost:8080/bark", Map.of("intensity", Math.random()))
                .retrieve()
                .toBodilessEntity();
    }

}
