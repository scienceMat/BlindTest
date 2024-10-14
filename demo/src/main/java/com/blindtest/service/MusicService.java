package com.blindtest.service;

import com.blindtest.model.Music;
import com.blindtest.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public class MusicService {

    @Autowired
    private MusicRepository musicRepository;

    public Music saveMusic(MultipartFile file, String title, String artist,String image) throws IOException {
        String filePath = "uploads/" + file.getOriginalFilename();
        File dest = new File(filePath);
        file.transferTo(dest);

        Music music = new Music();
        music.setTitle(title);
        music.setArtist(artist);
        music.setFilePath(filePath);
        music.setImage(image);

        return musicRepository.save(music);
    }

    @Transactional
    public Music getMusic(Long id) {
        return musicRepository.findById(id).orElse(null);
    }
}
