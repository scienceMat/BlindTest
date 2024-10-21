package com.blindtest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Entity
@Data
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int currentMusicIndex;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    // Ajouter un champ pour le code de session
    @Column
    private String sessionCode;

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column(nullable = false)
    private int currentRound = 1;  

    @Column(nullable = false)
    private boolean roundActive = false;  // Round actif ou non



    @Column
    private LocalDateTime questionStartTime;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
    name = "session_users",
            joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id"))
    private List<User> users = new ArrayList<>();

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Answer> answers;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE})
    @JoinTable(
        name = "session_music",
        joinColumns = @JoinColumn(name = "session_id"),
        inverseJoinColumns = @JoinColumn(name = "music_id"))
    private List<Music> musicList = new ArrayList<>();

    @ElementCollection
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "score")
    private Map<User, Integer> scores = new HashMap<>();

    // Additional Methods
    public Music getCurrentMusic() {
        if (currentMusicIndex < musicList.size()) {
            return musicList.get(currentMusicIndex);
        }
        return null;
    }

    public List<Music> getMusics() {
        return this.musicList;
    }

    public void setMusics(List<Music> musicList) {
        if (musicList != null) {
            this.musicList = musicList;
        } else {
            this.musicList = new ArrayList<>();
        }
    }

    public void nextRound() {
        this.currentRound++;
        this.roundActive = true;  // Réactiver le round
    }

    public void endRound() {
        this.roundActive = false;  // Marquer le round comme terminé
    }

    public int getCurrentRound() {
        return currentRound;
    }

    public void setUsers(List<User> userList) {
        if (userList != null) {
            this.users = userList;
        } else {
            this.users = new ArrayList<>();
        }
    }

    public Map<User, Integer> getScores() {
        return this.scores;
    }

    public void removeAllUsers() {
        for (User user : new ArrayList<>(users)) {  // Utilisation de List pour éviter les problèmes avec HashSet
            removeUser(user);
        }
    }

    public void removeUser(User user) {
        users.remove(user);
        user.getSessions().remove(this);
    }

    public void removeAllMusic() {
        for (Music music : new ArrayList<>(musicList)) {
            music.getSessions().remove(this);
            this.musicList.remove(music);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Session session = (Session) o;
        return Objects.equals(id, session.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

}
