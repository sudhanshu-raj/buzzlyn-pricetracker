package com.org.scraper_bkd.service;

import com.org.scraper_bkd.model.OllamaResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class OllamaService {

    private final WebClient webClient = WebClient.create("http://localhost:11434");

    public Mono<String> generateResponse(String model, String prompt) {
        return webClient.post()
                .uri("/api/generate")
                .header("Content-Type", "application/json")
                .bodyValue(Map.of("model", model, "prompt", prompt))
                .retrieve()
                .bodyToFlux(OllamaResponse.class) // Process streaming JSON
                .map(OllamaResponse::getResponse) // Extract the "response" field
                .collectList() // Collect all responses into a list
                .map(responses -> String.join("", responses)); // Concatenate all parts
    }

    public Mono<Integer> checkProductAvailability(String query) {
        String model = "llama3:latest";
        String prompt = "From the given product stock status, answer whether the product is in stock or not. " +
                "Answer in 0 and 1: 0 means it's out of stock, and 1 means it's in stock. Product stock: " + query;

        return generateResponse(model, prompt)
                .flatMap(response -> {
                    // Extract the final numeric answer (0 or 1)
                    int extractedAnswer = extractBinaryAnswer(response);
                    return Mono.just(extractedAnswer);
                });
    }
    private Integer extractBinaryAnswer(String response) {

        if (response.contains("1")) {
            return 1;  // Product is in stock
        } else if (response.contains("0")) {
            return 0;  // Product is out of stock
        }
        return -1;  // In case response is unclear
    }


    public static void main(String[] args) {

        OllamaService service = new OllamaService();
        String query="not stock";
        int result = service.checkProductAvailability("In Stock").block();
        System.out.println("result::"+result);
    }
}
