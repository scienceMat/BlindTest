package com.blindtest.model;

import lombok.Data;

@Data
public class AnswerRequest {
    private String userName;
    private String title;
    private String artist;
    
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

}
