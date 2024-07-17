package com.blindtest.controller;

import com.blindtest.dto.UserDTO;
import com.blindtest.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public UserDTO createUser(@RequestBody UserDTO userDTO) {
        return userService.createUser(userDTO.getUserName(), userDTO.getPassword(), userDTO.getIsAdmin());
    }

    @PostMapping("/login")
    public UserDTO authenticateUser(@RequestBody UserDTO userDTO) {
        return userService.authenticateUser(userDTO.getUserName(), userDTO.getPassword());
    }

    @GetMapping("/{id}")
    public UserDTO getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
