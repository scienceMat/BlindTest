package com.blindtest.model;

import lombok.Data;

@Data
public class AnswerRequest {
    private Long userId;
    private String title;
    private String artist;
}
