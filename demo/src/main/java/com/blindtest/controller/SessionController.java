// src/main/java/com/blindtest/controller/SessionController.java
package com.blindtest.controller;

import com.blindtest.dto.MusicDTO;
import com.blindtest.dto.PlaylistDTO;
import com.blindtest.dto.SessionDTO;
import com.blindtest.dto.UserDTO;
import com.blindtest.model.AnswerRequest;
import com.blindtest.model.Session;
import com.blindtest.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/sessions")
public class SessionController {

    @Autowired
    private SessionService sessionService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public List<SessionDTO> getAllSessions() {
        return sessionService.getAllSessions();
    }

    @GetMapping("/{sessionId}/scores")
    public List<UserDTO> getSessionScores(@PathVariable Long sessionId) {
        return sessionService.getSessionScores(sessionId);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<SessionDTO> getSessionByUserId(@PathVariable Long userId) {
        Optional<SessionDTO> sessionOpt = sessionService.getSessionByUserId(userId);
        return sessionOpt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public SessionDTO createSession(@RequestBody SessionDTO sessionDTO) {
        return sessionService.createSession(sessionDTO.getName(), sessionDTO.getAdminId());
    }

    @PostMapping("/{sessionCode}/join")
    public SessionDTO joinSessionByCode(@PathVariable String sessionCode, @RequestBody UserDTO userDTO) {
        return sessionService.joinSessionByCode(sessionCode, userDTO.getId());
    }

    @PostMapping("/{sessionCode}/join-as-guest")
    public SessionDTO joinSessionAsGuest(@PathVariable String sessionCode, @RequestBody String userName) {
        UserDTO guestUser = sessionService.createGuestUser(userName);
        return sessionService.joinSessionByCode(sessionCode, guestUser.getId());
    }

    @PostMapping("/{sessionId}/leave")
    public SessionDTO leaveSession(@PathVariable Long sessionId, @RequestBody UserDTO userDTO) {
        return sessionService.leaveSession(sessionId, userDTO.getId());
    }

    @PostMapping("/{sessionId}/nextTrack")
    public ResponseEntity<Void> nextTrack(@PathVariable Long sessionId) {
        sessionService.nextTrack(sessionId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{sessionId}/previousTrack")
    public ResponseEntity<Void> previousTrack(@PathVariable Long sessionId) {
        sessionService.previousTrack(sessionId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{sessionId}/playlist")
    public PlaylistDTO getPlaylist(@PathVariable Long sessionId) {
        return sessionService.getPlaylist(sessionId);
    }

    @PostMapping("/{sessionId}/playlist")
    public PlaylistDTO addMusicToSession(@PathVariable Long sessionId, @RequestBody MusicDTO musicDTO) {
        return sessionService.addMusicToSession(sessionId, musicDTO);
    }

    @PostMapping("/{sessionId}/answer")
    public SessionDTO submitAnswer(@PathVariable Long sessionId, @RequestBody AnswerRequest answerRequest) {
        if (answerRequest.getUserName() == null || answerRequest.getUserName().isEmpty()) {
            throw new IllegalArgumentException("Invalid userName: userName is required");
        }
        return sessionService.submitAnswer(sessionId,  answerRequest.getUserName(), answerRequest.getTitle(), answerRequest.getArtist());
    }

    @GetMapping("/{sessionId}/current-music-index")
    public ResponseEntity<Integer> getCurrentMusicIndex(@PathVariable Long sessionId) {
        Session session = sessionService.getSessionById(sessionId);
    if (session == null) {
        return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(session.getCurrentMusicIndex());
}

    @PostMapping("/{sessionId}/start")
    public SessionDTO startSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.startSession(sessionId);
        return sessionDTO;
    }

    @PostMapping("/{sessionId}/stop")
    public SessionDTO stopSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.stopSession(sessionId);
        return sessionDTO;
    }

    @PostMapping("/{sessionId}/ready")
    public void indicateReady(@PathVariable Long sessionId, @RequestBody UserDTO userDTO) {
        sessionService.indicateReady(sessionId, userDTO.getId());
    }

    @GetMapping("/{sessionId}/checkAllReady")
    public boolean checkAllReady(@PathVariable Long sessionId) {
        return sessionService.checkAllReady(sessionId);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionDTO> getSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.getSession(sessionId);
        return ResponseEntity.ok(sessionDTO);
    }

    @GetMapping("/code/{sessionCode}")
    public ResponseEntity<SessionDTO> getSessionByCode(@PathVariable String sessionCode) {
        SessionDTO sessionDTO = sessionService.getSessionByCode(sessionCode);
        return ResponseEntity.ok(sessionDTO);
    }

    @PostMapping("/{sessionId}/current-music")
    public ResponseEntity<Void> updateCurrentMusicIndex(@PathVariable Long sessionId, @RequestBody Map<String, Integer> payload) {
        Integer index = payload.get("index");
        sessionService.updateCurrentMusicIndex(sessionId, index);
        return ResponseEntity.ok().build();
    }
}
