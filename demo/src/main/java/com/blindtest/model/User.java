package com.blindtest.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    
    private int score;
    
    private LocalDateTime createdTime;
    
    private String password;
    
    private boolean isAdmin;

    private boolean ready;

    @ManyToMany(mappedBy = "users")
    private Set<Session> sessions = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<Answer> answers = new HashSet<>();

    // Constructeur avec id
    public User(Long id) {
        this.id = id;
        this.createdTime = LocalDateTime.now();
    }

    // Constructeur par défaut
    public User() {
        this.createdTime = LocalDateTime.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdTime = LocalDateTime.now();
    }

    public boolean isReady() {
        return ready;
    }

    public void setReady(boolean ready) {
        this.ready = ready;
    }
}
