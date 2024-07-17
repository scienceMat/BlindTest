package com.blindtest.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.*;

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

    @Column
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @Column
    private LocalDateTime questionStartTime;

    @ManyToMany
    @JoinTable(
      name = "session_users",
      joinColumns = @JoinColumn(name = "session_id"),
      inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "session")
    private Set<Answer> answers = new HashSet<>();

    @ManyToMany
    @JoinTable(
      name = "session_music",
      joinColumns = @JoinColumn(name = "session_id"),
      inverseJoinColumns = @JoinColumn(name = "music_id"))
    private List<Music> musicList = new ArrayList<>();
    
    @ElementCollection
    @MapKeyJoinColumn(name = "user_id")
    @Column(name = "score")
    private Map<User, Integer> scores = new HashMap<>();

    public Music getCurrentMusic() {
        if (currentMusicIndex < musicList.size()) {
            return musicList.get(currentMusicIndex);
        }
        return null;
    }

    public List<Music> getMusics() {
        return this.musicList;
    }

    public Map<User, Integer> getScores() {
        return this.scores;
    }
}
