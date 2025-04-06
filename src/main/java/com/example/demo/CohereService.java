package com.example.demo;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CohereService {

    @Value("${cohere.api.key}")
    private String apiKey;

    private static final String COHERE_API_URL = "https://api.cohere.ai/v1/chat";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final List<JsonNode> chatHistory = new ArrayList<>();

    public String generateResponse(String message) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        HttpPost post = new HttpPost(COHERE_API_URL);
        
        // API key'i header olarak ekle
        post.addHeader("Authorization", "Bearer " + apiKey);
        post.addHeader("Content-Type", "application/json");
        
        // Request body'sini oluştur
        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("message", message);
        
        // Chat geçmişi varsa ekle
        if (!chatHistory.isEmpty()) {
            requestBody.set("chat_history", objectMapper.valueToTree(chatHistory));
        }
        
        // Model belirleme (isteğe bağlı)
        requestBody.put("model", "command");
        
        post.setEntity(new StringEntity(requestBody.toString()));
        
        try (CloseableHttpResponse response = httpClient.execute(post)) {
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String result = EntityUtils.toString(entity);
                JsonNode jsonResponse = objectMapper.readTree(result);
                
                // Cevabı al
                String botResponse = jsonResponse.path("text").asText();
                
                // Sohbet geçmişine ekle
                ObjectNode userMessage = objectMapper.createObjectNode();
                userMessage.put("role", "USER");
                userMessage.put("message", message);
                chatHistory.add(userMessage);
                
                ObjectNode botMessage = objectMapper.createObjectNode();
                botMessage.put("role", "CHATBOT");
                botMessage.put("message", botResponse);
                chatHistory.add(botMessage);
                
                return botResponse;
            }
            return "Bir hata oluştu.";
        }
    }
}