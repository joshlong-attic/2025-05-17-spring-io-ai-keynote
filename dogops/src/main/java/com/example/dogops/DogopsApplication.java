package com.example.dogops;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.List;

@SpringBootApplication
public class DogopsApplication {

    public static void main(String[] args) {
        SpringApplication.run(DogopsApplication.class, args);
    }

    @Bean
    McpSyncClient mcpSyncClient() {
        var mcp = McpClient
                .sync(HttpClientSseClientTransport.builder("http://localhost:8888").build()).build();
        mcp.initialize();
        return mcp;
    }
}


@Controller
@ResponseBody
class DogopsController {

    private final Counter received, alerted;
    private final MetricsEndpoint endpoint;
    private final ChatClient ai;

    DogopsController(
            McpSyncClient configServerMcpClient,
            ToolCallbackProvider githubMcpProvider,
            MeterRegistry registry,
            ChatClient.Builder ai,
            MetricsEndpoint endpoint1) {

        this.ai = ai
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(configServerMcpClient))
                .defaultToolCallbacks(githubMcpProvider.getToolCallbacks())
                .defaultTools(this)
                .build();

        // just imagine that we had some sort of collection mechanism to receive these values.

        this.endpoint = endpoint1;

        this.alerted = registry.counter("barks.alerted");
        this.alerted.increment(1);

        this.received = registry.counter("barks.received");
        this.received.increment(2000);
    }


    @Tool(description = "returns all the metric names for the bark-detector application")
    Collection<String> metricNames() {
        return this.endpoint.listNames().getNames();
    }

    @Tool(description = "returns the value of a metric for the bark-detector application")
    List<MetricsEndpoint.Sample> metricValue(String metricName) {
        return this.endpoint
                .metric(metricName, List.of())
                .getMeasurements();
    }


    @GetMapping("/dogops")
    String inquire(@RequestParam String question) {
        return this.ai
                .prompt(question)
                .call()
                .content();
    }
}