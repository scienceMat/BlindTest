package com.blindtest.dto;

import java.util.List;

public class PlaylistDTO {
    private Long sessionId;
    private List<MusicDTO> musics;

    // Getters and setters
    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public List<MusicDTO> getMusics() {
        return musics;
    }

    public void setMusics(List<MusicDTO> musics) {
        this.musics = musics;
    }
}
