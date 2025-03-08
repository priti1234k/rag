package com.spring.ai.rag.controller;

import com.spring.ai.rag.service.UserQuerySearcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    @Autowired
    private UserQuerySearcher userQuerySearcher;

    @GetMapping("/chat/with/llm")
    public ResponseEntity<String> requestUserQuery(@RequestParam("query") String userQuery){
        String response = userQuerySearcher.searchUserQuery(userQuery);
        return ResponseEntity.ok(response);
    }
}
