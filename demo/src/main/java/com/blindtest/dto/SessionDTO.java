package com.blindtest.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SessionDTO {
    private Long id;
    private String name;
    private Long adminId;
    private List<UserDTO> users;
    private List<MusicDTO> musicList;
    private int currentMusicIndex;
    private String status;
    private String sessionCode;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime questionStartTime;
    private MusicDTO currentMusic;
    private Map<Long, Integer> scores;
    private Number round; // Mapping des scores par ID utilisateur

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public List<UserDTO> getUsers() {
        return users;
    }

    public void setUsers(List<UserDTO> users) {
        this.users = users;
    }

    public List<MusicDTO> getMusicList() {
        return musicList;
    }

    public void setMusicList(List<MusicDTO> musicList) {
        this.musicList = musicList;
    }

    public int getCurrentMusicIndex() {
        return currentMusicIndex;
    }

    public void setCurrentMusicIndex(int currentMusicIndex) {
        this.currentMusicIndex = currentMusicIndex;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public LocalDateTime getQuestionStartTime() {
        return questionStartTime;
    }

    public void setQuestionStartTime(LocalDateTime questionStartTime) {
        this.questionStartTime = questionStartTime;
    }

    public MusicDTO getCurrentMusic() {
        return currentMusic;
    }

    public void setCurrentMusic(MusicDTO currentMusic) {
        this.currentMusic = currentMusic;
    }

    public Map<Long, Integer> getScores() {
        return scores;
    }

    public void setScores(Map<Long, Integer> scores) {
        this.scores = scores;
    }

    public Number getRound() {
        return round;
    }

    public void setRound(Number round) {
        this.round = round;
    }
}
