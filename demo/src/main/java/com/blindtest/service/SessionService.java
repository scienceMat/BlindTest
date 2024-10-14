package com.blindtest.service;

import com.blindtest.dto.MusicDTO;
import com.blindtest.dto.PlaylistDTO;
import com.blindtest.dto.SessionDTO;
import com.blindtest.dto.UserDTO;
import com.blindtest.mapper.Mapper;
import com.blindtest.model.Answer;
import com.blindtest.model.Music;
import com.blindtest.model.Session;
import com.blindtest.model.User;
import com.blindtest.repository.AnswerRepository;
import com.blindtest.repository.MusicRepository;
import com.blindtest.repository.SessionRepository;
import com.blindtest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.Comparator;
import java.util.HashSet;

import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Optional;
@Service
public class SessionService {

    @Autowired
    private SessionRepository sessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MusicRepository musicRepository;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private AnswerRepository answerRepository;

    @Transactional
    public List<SessionDTO> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        System.out.println("Found " + sessions.size() + " sessions");
        return sessions.stream().map(Mapper::toSessionDTO).collect(Collectors.toList());
    }

    @Transactional
    public Session getSessionById(Long sessionId) {
    return sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
}

    @Transactional
    public SessionDTO createSession(String name, Long adminId) {
        User admin = userRepository.findById(adminId).orElseThrow(() -> new RuntimeException("Admin not found"));
        
        Session session = new Session();
        session.setName(name);
        session.setAdmin(admin);
        session.setStatus("waiting");
        session.setCurrentMusicIndex(0);
        session.setEndTime(LocalDateTime.now().plusHours(1));
        
        sessionRepository.save(session);
        sessionRepository.flush(); // Assurez-vous que les changements sont persistés
    
        System.out.println("Session created with ID: " + session.getId());
        return Mapper.toSessionDTO(session);
    }

    @Transactional // Assurez-vous que cette méthode est transactionnelle
    public SessionDTO joinSession(Long sessionId, Long userId) {
        System.out.println("Finding session with ID: " + sessionId);
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        System.out.println("Session found: " + session.getName());

        // Check if the session has already ended
        if (session.getEndTime() != null && session.getEndTime().isBefore(LocalDateTime.now())) {
            System.out.println("The session has ended: " + sessionId);
            this.deleteExpiredSessions();
            throw new RuntimeException("The session has ended: " + sessionId);
    
        }

        System.out.println("Finding user with ID: " + userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getName());

        // Check if the user is already part of the session
        if (session.getUsers().contains(user)) {
            throw new RuntimeException("User is already in this session");
        }

        System.out.println("Adding user to session");
        session.getUsers().add(user);
        user.getSessions().add(session);  // Ensure bidirectional consistency

        System.out.println("Saving session");
        sessionRepository.save(session);

        return Mapper.toSessionDTO(session);
    }

    @Transactional
    public SessionDTO leaveSession(Long sessionId, Long userId) {
        System.out.println("Finding session with ID: " + sessionId);
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        System.out.println("Session found: " + session.getName());

        System.out.println("Finding user with ID: " + userId);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getName());

        // Check if the user is part of the session
        if (!session.getUsers().contains(user)) {
            throw new RuntimeException("User is not in this session");
        }

        System.out.println("Removing user from session");
        session.getUsers().remove(user);
        user.getSessions().remove(session);  // Ensure bidirectional consistency

        System.out.println("Saving session");
        sessionRepository.save(session);

        return Mapper.toSessionDTO(session);
    }

    @Transactional
    public Optional<SessionDTO> getSessionByUserId(Long userId) {
        // Fetch the user from the repository
        Optional<User> userOpt = userRepository.findById(userId);
    
        if (userOpt.isPresent()) {
            User user = userOpt.get();
    
            // Find all sessions where the user is a participant
            List<Session> sessions = sessionRepository.findFirstSessionByUser(user);
    
            if (!sessions.isEmpty()) {
                Session session = sessions.get(0); // Gérer selon le besoin (choisir la première, ou autre critère)
                return Optional.of(Mapper.toSessionDTO(session));
            }
        }
    
        return Optional.empty();
    }

    @Transactional
    public SessionDTO getSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        return Mapper.toSessionDTO(session);
    }

    @Transactional
    public List<UserDTO> getSessionScores(Long sessionId) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));

    return session.getUsers().stream()
        .map(user -> {
            UserDTO userDTO = new UserDTO();
            userDTO.setId(user.getId());
            userDTO.setUserName(user.getName());
            userDTO.setScore(session.getScores().get(user)); // Récupérer le score de l'utilisateur dans la session
            return userDTO;
        })
        .collect(Collectors.toList());
}


@Transactional
public synchronized SessionDTO submitAnswer(Long sessionId, Long userId, String title, String artist) {
    Session session = sessionRepository.findById(sessionId)
        .orElseThrow(() -> new RuntimeException("Session not found"));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));

    Music currentMusic = session.getCurrentMusic();
    if (currentMusic == null) {
        throw new RuntimeException("No music is currently playing in this session");
    }

    if (answerRepository.existsByUserAndSessionAndMusic(user, session, currentMusic)) {
        throw new RuntimeException("User has already answered this round");
    }

    // Vérifie si la réponse est correcte
    boolean isTitleCorrect = isSimilar(title, currentMusic.getTitle());
    boolean isArtistCorrect = isSimilar(artist, currentMusic.getArtist());

    // Calcul du score pour la réponse
    int score = 0;
    if (isTitleCorrect) score += 5;
    if (isArtistCorrect) score += 5;

    // Mise à jour du score du joueur dans la session
    session.getScores().put(user, session.getScores().getOrDefault(user, 0) + score);

    // Sauvegarder la réponse du joueur
    Answer answer = new Answer();
    answer.setUser(user);
    answer.setSession(session);
    answer.setMusic(currentMusic);
    answer.setTitle(title);
    answer.setArtist(artist);
    answer.setTitleCorrect(isTitleCorrect);
    answer.setArtistCorrect(isArtistCorrect);
    answer.setAnswerTime(LocalDateTime.now());

    answerRepository.save(answer);
    sessionRepository.save(session);

    // Vérifier si tous les joueurs ont répondu pour terminer le round
    if (checkIfAllPlayersAnswered(session)) {
        // Notifier la fin du round
        notifyEndOfRound(session);
        
        // Pause de la musique
        pauseMusic(session);

        // Attendre une courte durée avant de passer à la musique suivante
        try {
            Thread.sleep(5000); // Pause de 5 secondes pour laisser le temps à l'admin de voir les scores
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Passer à la musique suivante
        nextMusic(session);
    }

    // Retourner la session avec les scores mis à jour
    return Mapper.toSessionDTO(session);
}

// Mettre en pause la musique pour tous les utilisateurs
public void pauseMusic(Session session) {
    messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "PAUSE_MUSIC");
}

// Notifier la fin du round à tous les utilisateurs
public void notifyEndOfRound(Session session) {
    messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "END_OF_ROUND");
}

public void nextMusic(Session session) {
    int nextMusicIndex = session.getCurrentMusicIndex() + 1;

    if (nextMusicIndex < session.getMusicList().size()) {
        session.setCurrentMusicIndex(nextMusicIndex);
        sessionRepository.save(session);

        // Notifier les utilisateurs de la nouvelle musique
        messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "NEXT_MUSIC");
    } else {
        // Si c'était la dernière musique, terminer la session
        messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "SESSION_FINISHED");
    }
}

public boolean checkIfAllPlayersAnswered(Session session) {
    // Récupérer les utilisateurs de la session (sauf l'admin)
    List<User> nonAdminUsers = session.getUsers().stream()
        .filter(user -> !user.equals(session.getAdmin()))
        .toList();

    // Récupérer la musique en cours
    Music currentMusic = session.getCurrentMusic();
    if (currentMusic == null) {
        throw new RuntimeException("No current music found for the session.");
    }

    // Vérifier si chaque utilisateur a soumis une réponse pour la musique actuelle
    for (User user : nonAdminUsers) {
        boolean hasAnswered = session.getAnswers().stream()
            .anyMatch(answer -> answer.getUser().equals(user) && answer.getMusic().equals(currentMusic));
        if (!hasAnswered) {
            return false;  // Si un utilisateur n'a pas répondu, retourner false
        }
    }

    // Si tous les utilisateurs ont répondu
    return true;
}


@Transactional
private void checkAndProceedToNextRound(Session session) {
    // Exclure l'admin du total des utilisateurs
    int totalUsers = (int) session.getUsers().stream()
            .filter(user -> !user.isAdmin()) // Filtrer les utilisateurs qui ne sont pas admin
            .count();
    
    int answeredUsers = answerRepository.countBySessionAndMusic(session, session.getCurrentMusic());

    // Si tous les joueurs (sauf l'admin) ont répondu
    if (answeredUsers == totalUsers) {
        // Envoyer l'événement de fin de round
        messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "END_OF_ROUND");

        int nextIndex = session.getCurrentMusicIndex() + 1;
        
        if (nextIndex < session.getMusics().size()) {
            // Attendre 5 secondes avant de passer à la musique suivante
            new Thread(() -> {
                try {
                    Thread.sleep(15000); // Pause de 15 secondes
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                session.setCurrentMusicIndex(nextIndex);
                session.setQuestionStartTime(LocalDateTime.now());
                sessionRepository.save(session);

                messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "NEXT_MUSIC");
            }).start();
        } else {
            // Si tous les morceaux ont été joués, marquer la session comme terminée
            session.setStatus("finished");
            session.setEndTime(LocalDateTime.now());
            sessionRepository.save(session);

            messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "SESSION_FINISHED");
        }
    }
}

    @Transactional
    public void updateCurrentMusicIndex(Long sessionId, int index) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        session.setCurrentMusicIndex(index);
        sessionRepository.save(session);
    }

    private boolean isSimilar(String input, String target) {
        return levenshteinDistance(input.toLowerCase(), target.toLowerCase()) < 3;
    }

    @Transactional
    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];
    
        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    @Transactional
    public void nextTrack(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        if (session.getCurrentMusicIndex() < session.getMusics().size() - 1) {
            session.setCurrentMusicIndex(session.getCurrentMusicIndex() + 1);
            session.setQuestionStartTime(LocalDateTime.now());
            sessionRepository.save(session);

            // Send a WebSocket notification with the updated session
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, "NEXT_TRACK");
        }
    }

    @Transactional
    public void previousTrack(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getCurrentMusicIndex() > 0) {
            session.setCurrentMusicIndex(session.getCurrentMusicIndex() - 1);
            session.setQuestionStartTime(LocalDateTime.now());
            sessionRepository.save(session);

            // Send a WebSocket notification with the updated session
            messagingTemplate.convertAndSend("/topic/session/" + sessionId, "PREVIOUS_TRACK");
        }
    }

    @Transactional
    public void endRound(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "END_ROUND");

        List<User> users = userRepository.findUsersBySessionId(sessionId);
        users.sort(Comparator.comparing(user -> session.getScores().getOrDefault(user, 0), Comparator.reverseOrder()));

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nextTrack(sessionId);
    }

    @Transactional
    public SessionDTO startSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
    
        session.setStatus("in-progress");
        session.setStartTime(LocalDateTime.now());
        session.setCurrentMusicIndex(0);
        session.setQuestionStartTime(LocalDateTime.now().plusSeconds(10)); // Start the question after 10 seconds
        sessionRepository.save(session);
    
        // Send start message to session-specific topic
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "START_SESSION");
    
        // Send countdown timer event
        sendCountdownEvent(sessionId, 10);
    
        return Mapper.toSessionDTO(session);
    }

    @Transactional
    private void sendCountdownEvent(Long sessionId, int countdownTime) {
        for (int i = countdownTime; i >= 0; i--) {
            try {
                Thread.sleep(1000); // Wait for 1 second before sending the next countdown event
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Notify the start of the round after countdown
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "NEXT_ROUND");
    }

    @Transactional
    public SessionDTO stopSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new RuntimeException("Session not found"));
        
        session.setStatus("pause");
        session.setStartTime(LocalDateTime.now());
        sessionRepository.save(session);

        // Send stop message to session-specific topic
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "STOP_SESSION");

        return Mapper.toSessionDTO(session);
    }

    @Transactional
    public SessionDTO nextQuestion(Long sessionId) { 
    Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
    if (session.getCurrentMusicIndex() < session.getMusics().size() - 1) {
        session.setCurrentMusicIndex(session.getCurrentMusicIndex() + 1);
        session.setQuestionStartTime(LocalDateTime.now().plusSeconds(10)); // Start the question after 10 seconds
        sessionRepository.save(session);

        // Send countdown timer event
        sendCountdownEvent(sessionId, 10);
    } else {
        session.setStatus("finished");
        session.setEndTime(LocalDateTime.now());
        sessionRepository.save(session);

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "SESSION_FINISHED");
    }
    return Mapper.toSessionDTO(session);
}

    @Transactional  
    public void indicateReady(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setReady(true);
        userRepository.save(user);
    }
    @Transactional
    public boolean checkAllReady(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        return session.getUsers().stream().allMatch(User::isReady);
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void removeExpiredUsers() {
        List<User> users = userRepository.findAll();
        LocalDateTime now = LocalDateTime.now();

        for (User user : users) {
            if (user.getCreatedTime().isBefore(now.minusHours(1))) {
                // Remove user from sessions first if needed
                sessionRepository.findAll().forEach(session -> {
                    if (session.getUsers().remove(user)) {
                        sessionRepository.save(session);
                    }
                });

                userRepository.delete(user);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void deleteExpiredSessions() {
        List<Session> sessions = sessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
    
        for (Session session : sessions) {
            if (session.getEndTime() != null && session.getEndTime().isBefore(now.minusHours(1))) {
                // Supprimer toutes les musiques associées
                session.removeAllMusic();
    
                // Supprimer toutes les relations d'utilisateurs
                session.removeAllUsers();
    
                // Supprimer les réponses associées
                answerRepository.deleteAllBySessionId(session.getId());
    
                // Supprimer la session elle-même
                sessionRepository.delete(session);
            }
        }
    }

    @Transactional
    public PlaylistDTO getPlaylist(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        List<MusicDTO> musics = session.getMusics().stream()
                .map(Mapper::toMusicDTO)
                .collect(Collectors.toList());
        PlaylistDTO playlistDTO = new PlaylistDTO();
        playlistDTO.setSessionId(sessionId);
        playlistDTO.setMusics(musics);
        return playlistDTO;
    }

    @Transactional
    public PlaylistDTO addMusicToSession(Long sessionId, MusicDTO musicDTO) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        Music music = new Music();
        music.setTitle(musicDTO.getTitle());
        music.setArtist(musicDTO.getArtist());
        music.setFilePath(musicDTO.getFilePath());
        music.setImage(musicDTO.getImage());
        musicRepository.save(music);
        session.getMusics().add(music);
        sessionRepository.save(session);
        return getPlaylist(sessionId);
    }
}
