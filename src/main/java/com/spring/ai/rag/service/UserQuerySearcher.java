package com.spring.ai.rag.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
public class UserQuerySearcher {

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    private OllamaChatModel ollamaChatModel;

    public String searchUserQuery(String userQuery){
        SearchRequest searchRequest = SearchRequest.builder().query(userQuery).topK(5).build();
        List<Document> listOfMatchingDoc = vectorStore.similaritySearch(searchRequest);
        if (!CollectionUtils.isEmpty(listOfMatchingDoc)) {
            List<String> context = listOfMatchingDoc.stream().map(Document::getText).toList();
            String prompt = "Answer the User query based on the context:" + "\n"
                    + "<userQuery>" + userQuery + "</userQuery>" + "\n"
                    + "<context>" + String.join("\n\n", context) +"</context>";
            return ollamaChatModel.call(prompt);
        }else {

            return "";
        }

    }
}
