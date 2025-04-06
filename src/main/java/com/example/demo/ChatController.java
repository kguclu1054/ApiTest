package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
public class ChatController {

    @Autowired
    private CohereService cohereService;

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/chat")
    @ResponseBody
    public ChatResponse chat(@RequestBody ChatRequest request) {
        ChatResponse response = new ChatResponse();
        try {
            String botResponse = cohereService.generateResponse(request.getMessage());
            response.setResponse(botResponse);
        } catch (IOException e) {
            response.setResponse("Üzgünüm, bir hata oluştu: " + e.getMessage());
        }
        return response;
    }
}
