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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    public List<SessionDTO> getAllSessions() {
        List<Session> sessions = sessionRepository.findAll();
        System.out.println("Found " + sessions.size() + " sessions");
        return sessions.stream().map(Mapper::toSessionDTO).collect(Collectors.toList());
    }

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

    public SessionDTO joinSession(Long sessionId, Long userId) {
        System.out.println("Finding session with ID: " + sessionId);
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        System.out.println("Session found: " + session.getName());
    
        System.out.println("Finding user with ID: " + userId);
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("User found: " + user.getName());
    
        if (session.getUsers().contains(user)) {
            throw new RuntimeException("User is already in this session");
        }
    
        System.out.println("Adding user to session");
        session.getUsers().add(user);
    
        System.out.println("Saving session");
        sessionRepository.save(session);
    
        return Mapper.toSessionDTO(session);
    }

    public SessionDTO getSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        return Mapper.toSessionDTO(session);
    }

    public SessionDTO submitAnswer(Long sessionId, Long userId, String title, String artist) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));

        Music currentMusic = session.getCurrentMusic();
        if (currentMusic == null) {
            throw new RuntimeException("No music is currently playing in this session");
        }

        if (answerRepository.existsByUserAndSessionAndMusic(user, session, currentMusic)) {
            throw new RuntimeException("User has already answered this round");
        }

        boolean isTitleCorrect = isSimilar(title, currentMusic.getTitle());
        boolean isArtistCorrect = isSimilar(artist, currentMusic.getArtist());

        int score = 0;
        if (isTitleCorrect) score += 5;
        if (isArtistCorrect) score += 5;

        session.getScores().put(user, session.getScores().getOrDefault(user, 0) + score);

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

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "UPDATE_SCORE");
        checkAndProceedToNextRound(session);

        return Mapper.toSessionDTO(session);
    }

    private void checkAndProceedToNextRound(Session session) {
        int totalUsers = session.getUsers().size();
        int answeredUsers = answerRepository.countBySessionAndMusic(session, session.getCurrentMusic());

        if (answeredUsers == totalUsers) {
            messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "END_OF_ROUND");

            int nextIndex = session.getCurrentMusicIndex() + 1;
            if (nextIndex < session.getMusics().size()) {
                session.setCurrentMusicIndex(nextIndex);
                session.setQuestionStartTime(LocalDateTime.now());
                sessionRepository.save(session);

                messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "NEXT_MUSIC");
            } else {
                session.setStatus("finished");
                session.setEndTime(LocalDateTime.now());
                sessionRepository.save(session);

                messagingTemplate.convertAndSend("/topic/session/" + session.getId(), "SESSION_FINISHED");
            }
        }
    }

    public void updateCurrentMusicIndex(Long sessionId, int index) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        session.setCurrentMusicIndex(index);
        sessionRepository.save(session);
    }

    private boolean isSimilar(String input, String target) {
        return levenshteinDistance(input.toLowerCase(), target.toLowerCase()) < 3;
    }

    private int levenshteinDistance(String a, String b) {
        int[][] dp = new int[a.length() + 1][b.length() + 1];

        for (int i = 0; i <= a.length(); i++) {
            for (int j = 0; j <= b.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][i] = i;
                } else {
                    dp[i][j] = Math.min(dp[i - 1][j - 1] + (a.charAt(i - 1) == b.charAt(j - 1) ? 0 : 1),
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1));
                }
            }
        }
        return dp[a.length()][b.length()];
    }

    public void nextTrack(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        session.setCurrentMusicIndex(session.getCurrentMusicIndex() + 1);
        session.setQuestionStartTime(LocalDateTime.now());
        sessionRepository.save(session);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "NEXT_TRACK");
    }

    public void endRound(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, "END_ROUND");

        List<User> users = userRepository.findUsersBySessionId(sessionId);
        users.sort(Comparator.comparing(user -> session.getScores().getOrDefault(user, 0), Comparator.reverseOrder()));

        messagingTemplate.convertAndSend("/topic/session/" + sessionId, users);

        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        nextTrack(sessionId);
    }

    public SessionDTO startSession(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        session.setStatus("in-progress");
        session.setStartTime(LocalDateTime.now());
        session.setCurrentMusicIndex(0);
        session.setQuestionStartTime(LocalDateTime.now());
        sessionRepository.save(session);
        messagingTemplate.convertAndSend("/topic/start", "START_SESSION");

        return Mapper.toSessionDTO(session);
    }

    public SessionDTO nextQuestion(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        if (session.getCurrentMusicIndex() < session.getMusics().size() - 1) {
            session.setCurrentMusicIndex(session.getCurrentMusicIndex() + 1);
            session.setQuestionStartTime(LocalDateTime.now());
        } else {
            session.setStatus("finished");
            session.setEndTime(LocalDateTime.now());
        }
        sessionRepository.save(session);
        return Mapper.toSessionDTO(session);
    }

    public void indicateReady(Long sessionId, Long userId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setReady(true);
        userRepository.save(user);
    }

    public boolean checkAllReady(Long sessionId) {
        Session session = sessionRepository.findById(sessionId).orElseThrow(() -> new RuntimeException("Session not found"));
        return session.getUsers().stream().allMatch(User::isReady);
    }

    @Scheduled(fixedRate = 3600000)
    public void removeExpiredUsers() {
        List<User> users = userRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (User user : users) {
            if (user.getCreatedTime().isBefore(now.minusHours(1))) {
                userRepository.delete(user);
            }
        }
    }

    @Scheduled(fixedRate = 60000)
    public void deleteExpiredSessions() {
        List<Session> sessions = sessionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        sessions.forEach(session -> {
            if (session.getEndTime().isBefore(now)) {
                sessionRepository.delete(session);
            }
        });
    }

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
