package com.example.bark_detector;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.mcp.SyncMcpToolCallbackProvider;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.List;


@SpringBootApplication
public class BarkDetectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(BarkDetectorApplication.class, args);
    }

    @Bean
    McpSyncClient mcpSyncClient() {
        var mcp = McpClient
                .sync(HttpClientSseClientTransport.builder("http://localhost:8888").build())
                .build();
        mcp.initialize();
        return mcp;
    }
}

@ResponseBody
@Controller
class BarkDetectorController {

    private final ChatClient ai;
    private final Environment environment;
    private final MetricsEndpoint metricsEndpoint;
    private final Counter barksReceived, alertsTriggered;

    BarkDetectorController(
            ToolCallbackProvider githubMcpProvider,
            McpSyncClient configServerMcpClient,
            ChatClient.Builder ai,
            Environment environment,
            MetricsEndpoint metricsEndpoint,
            MeterRegistry meterRegistry) {

        this.ai = ai
                .defaultToolCallbacks(new SyncMcpToolCallbackProvider(configServerMcpClient))
                .defaultToolCallbacks(githubMcpProvider.getToolCallbacks())
                .defaultTools(this)
                .build();

        this.environment = environment;
        this.metricsEndpoint = metricsEndpoint;

        this.barksReceived = meterRegistry.counter("barks.received");
        this.barksReceived.increment(2000);

        this.alertsTriggered = meterRegistry.counter("barks.alerted");
        this.alertsTriggered.increment(1);
    }

    @Tool(description = "returns all the metric names for the bark-detector application")
    Collection<String> metrics() {
        return this.metricsEndpoint.listNames().getNames();
    }

    @Tool(description = "returns the value of a metric for the bark-detector application")
    List<MetricsEndpoint.Sample> metricValue(@ToolParam String metricName) {
        return this.metricsEndpoint
                .metric(metricName, List.of())
                .getMeasurements();
    }

    @PostMapping("/barks")
    void receiveBark(@RequestParam float intensity) {
        var threshold = environment.getProperty("barks.alert-threshold", Float.class, 0F);
        System.out.println("the value for the the threshold is: " + threshold + ".");
        this.barksReceived.increment();
        if (intensity > threshold) {
            this.alertsTriggered.increment();
        }
    }

    @PostMapping("/dogops")
    String dogops(@RequestParam String question) {
        return this.ai
                .prompt(question)
                .call()
                .content();
    }

}
