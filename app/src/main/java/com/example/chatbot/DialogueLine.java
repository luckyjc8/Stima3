package com.example.chatbot;

public class DialogueLine {
    private String author;
    private String message;

    public DialogueLine(String author, String message) {
        this.author = author;
        this.message = message;
    }

    public String getAuthor() {
        return author;
    }

    public String getMessage() {
        return message;
    }
}
