package com.blindtest.controller;

import com.blindtest.model.Music;
import com.blindtest.service.MusicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/music")
public class MusicController {

    @Autowired
    private MusicService musicService;

    @PostMapping("/upload")
    public Music uploadMusic(@RequestParam("file") MultipartFile file,
                             @RequestParam("title") String title,
                             @RequestParam("artist") String artist,
                             @RequestParam("image") String image
                             ) throws IOException {
        return musicService.saveMusic(file, title, artist,image);
    }

    @GetMapping("/{id}")
    public Music getMusic(@PathVariable Long id) {
        return musicService.getMusic(id);
    }
}
