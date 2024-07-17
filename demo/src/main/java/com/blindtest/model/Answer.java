package com.blindtest.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import java.util.List;
import lombok.Data;

@Entity
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "session_id", nullable = false)
    private Session session;

    @ManyToOne
    @JoinColumn(name = "music_id", nullable = false)
    private Music music;

    private String title;
    private String artist;
    private boolean isTitleCorrect;
    private boolean isArtistCorrect;
    private LocalDateTime answerTime;

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }
    public Session getSession() {
        return session;
    }
    public void setSession(Session session) {
        this.session = session;
    }
    public Music getMusic() {
        return music;
    }
    public void setMusic(Music music) {
        this.music = music;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getArtist() {
        return artist;
    }
    public void setArtist(String artist) {
        this.artist = artist;
    }
    public boolean isTitleCorrect() {
        return isTitleCorrect;
    }
    public void setTitleCorrect(boolean isTitleCorrect) {
        this.isTitleCorrect = isTitleCorrect;
    }
    public boolean isArtistCorrect() {
        return isArtistCorrect;
    }
    public void setArtistCorrect(boolean isArtistCorrect) {
        this.isArtistCorrect = isArtistCorrect;
    }
    public LocalDateTime getAnswerTime() {
        return answerTime;
    }
    public void setAnswerTime(LocalDateTime answerTime) {
        this.answerTime = answerTime;
    }

    // Getters and setters
}
