package com.open.spring.mvc.mortevision;

class Comment {
    private String user;
    private String text;
    
    public Comment(String user, String text) {
        this.user = user;
        this.text = text;
    }

    public String getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return "Comment by " + user + ": " + text;
    }
}