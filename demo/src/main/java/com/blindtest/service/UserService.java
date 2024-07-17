package com.blindtest.service;

import com.blindtest.dto.UserDTO;
import com.blindtest.mapper.Mapper;
import com.blindtest.model.User;
import com.blindtest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return Mapper.toUserDTO(user);
    }

    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(Mapper::toUserDTO).collect(Collectors.toList());
    }

    public UserDTO createUser(String userName, String password, boolean isAdmin) {
        User user = new User();
        user.setName(userName);
        user.setPassword(password);
        user.setAdmin(isAdmin);
        userRepository.save(user);
        return Mapper.toUserDTO(user);
    }

    public UserDTO authenticateUser(String userName, String password) {
        User user = userRepository.findByName(userName).orElseThrow(() -> new RuntimeException("User not found"));
        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        return Mapper.toUserDTO(user);
    }
}
