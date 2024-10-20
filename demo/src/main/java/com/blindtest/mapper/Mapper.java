package com.blindtest.mapper;

import com.blindtest.dto.UserDTO;
import com.blindtest.dto.SessionDTO;
import com.blindtest.dto.MusicDTO;
import com.blindtest.model.User;
import com.blindtest.model.Session;
import com.blindtest.model.Music;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;


import java.util.stream.Collectors;

public class Mapper {

    public static UserDTO toUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUserName(user.getName());
        dto.setIsAdmin(user.isAdmin());
        dto.setScore(user.getScore());
        dto.setReady(user.isReady());
        return dto;
    }

    public static User toUser(UserDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getUserName());
        user.setPassword(dto.getPassword());
        user.setAdmin(dto.getIsAdmin());
        user.setScore(dto.getScore());
        user.setReady(dto.isReady());
        return user;
    }

    public static MusicDTO toMusicDTO(Music music) {
        MusicDTO dto = new MusicDTO();
        dto.setId(music.getId());
        dto.setTitle(music.getTitle());
        dto.setArtist(music.getArtist());
        dto.setFilePath(music.getFilePath());
        dto.setImage(music.getImage());
        return dto;
    }

    public static Music toMusic(MusicDTO dto) {
        Music music = new Music();
        music.setId(dto.getId());
        music.setTitle(dto.getTitle());
        music.setArtist(dto.getArtist());
        music.setFilePath(dto.getFilePath());
        music.setImage(dto.getImage());
        return music;
    }

    public static SessionDTO toSessionDTO(Session session) {
        SessionDTO sessionDTO = new SessionDTO();
        sessionDTO.setId(session.getId());
        sessionDTO.setName(session.getName());
        sessionDTO.setAdminId(session.getAdmin().getId());
        sessionDTO.setUsers(session.getUsers().stream().map(Mapper::toUserDTO).collect(Collectors.toList()));
        sessionDTO.setSessionCode(session.getSessionCode()); // Assigner le sessionCode

        // Vérification si la liste de musiques n'est pas nulle avant de la mapper
        if (session.getMusics() != null) {
            sessionDTO.setMusicList(session.getMusics().stream().map(Mapper::toMusicDTO).collect(Collectors.toList()));
        } else {
            sessionDTO.setMusicList(null);
        }

        if (session.getScores() != null) {
            sessionDTO.setScores(session.getScores().entrySet().stream()
                .collect(Collectors.toMap(entry -> entry.getKey().getId(), Map.Entry::getValue)));
        } else {
            sessionDTO.setScores(null);
        }

        sessionDTO.setCurrentMusicIndex(session.getCurrentMusicIndex());
        sessionDTO.setStatus(session.getStatus());
        sessionDTO.setStartTime(session.getStartTime());
        sessionDTO.setEndTime(session.getEndTime());
        sessionDTO.setQuestionStartTime(session.getQuestionStartTime());

        // Vérification si la musique actuelle n'est pas nulle avant de la mapper
        if (session.getCurrentMusic() != null) {
            sessionDTO.setCurrentMusic(Mapper.toMusicDTO(session.getCurrentMusic()));
        } else {
            sessionDTO.setCurrentMusic(null);
        }

        return sessionDTO;
    }

     public static Session toSession(SessionDTO dto) {
        Session session = new Session();
        session.setId(dto.getId());
        session.setName(dto.getName());
        session.setAdmin(new User(dto.getAdminId()));
        
        // Convert List<User> to Set<User>
        List<User> userSet = new ArrayList<>(dto.getUsers().stream().map(Mapper::toUser).collect(Collectors.toList()));
        session.setUsers(userSet);

        // Convert List<Music> to appropriate collection type
        List<Music> musicList = (dto.getMusicList() != null) 
            ? dto.getMusicList().stream().map(Mapper::toMusic).collect(Collectors.toList())
            : null;
        session.setMusics(musicList);

        session.setCurrentMusicIndex(dto.getCurrentMusicIndex());
        session.setStatus(dto.getStatus());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setQuestionStartTime(dto.getQuestionStartTime());

        // Convert Map<Long, Integer> scores to appropriate Map<User, Integer>
        Map<User, Integer> scores = (dto.getScores() != null) 
            ? dto.getScores().entrySet().stream()
                .collect(Collectors.toMap(entry -> {
                    User user = new User();
                    user.setId(entry.getKey());
                    return user;
                }, Map.Entry::getValue))
            : null;
        session.setScores(scores);

        return session;
    }
}
