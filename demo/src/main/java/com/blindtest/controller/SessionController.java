// src/main/java/com/blindtest/controller/SessionController.java
package com.blindtest.controller;

import com.blindtest.dto.MusicDTO;
import com.blindtest.dto.PlaylistDTO;
import com.blindtest.dto.SessionDTO;
import com.blindtest.dto.UserDTO;
import com.blindtest.model.AnswerRequest;
import com.blindtest.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping
    public SessionDTO createSession(@RequestBody SessionDTO sessionDTO) {
        return sessionService.createSession(sessionDTO.getName(), sessionDTO.getAdminId());
    }

    @PostMapping("/{sessionId}/join")
    public SessionDTO joinSession(@PathVariable Long sessionId, @RequestBody UserDTO userDTO) {
        return sessionService.joinSession(sessionId, userDTO.getId());
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
        return sessionService.submitAnswer(sessionId, answerRequest.getUserId(), answerRequest.getTitle(), answerRequest.getArtist());
    }

    @PostMapping("/{sessionId}/start")
    public SessionDTO startSession(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.startSession(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, sessionDTO);
        return sessionDTO;
    }

    @PostMapping("/{sessionId}/next")
    public SessionDTO nextQuestion(@PathVariable Long sessionId) {
        SessionDTO sessionDTO = sessionService.nextQuestion(sessionId);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, sessionDTO);
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

    @PostMapping("/{sessionId}/current-music")
    public ResponseEntity<Void> updateCurrentMusicIndex(@PathVariable Long sessionId, @RequestBody Map<String, Integer> payload) {
        Integer index = payload.get("index");
        sessionService.updateCurrentMusicIndex(sessionId, index);
        messagingTemplate.convertAndSend("/topic/session/" + sessionId, sessionService.getSession(sessionId));
        return ResponseEntity.ok().build();
    }
}
