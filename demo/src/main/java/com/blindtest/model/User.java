package com.blindtest.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    
    @Column(nullable = false)
    private boolean isAdmin;

    @Column
    private boolean isGuest;

    private boolean ready;

    @ManyToMany(mappedBy = "users", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Session> sessions = new ArrayList<>();  // Utilisation de List

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Answer> answers;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    
}
