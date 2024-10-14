package com.blindtest.service;

import com.blindtest.dto.UserDTO;
import com.blindtest.mapper.Mapper;
import com.blindtest.model.User;
import com.blindtest.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return Mapper.toUserDTO(user);
    }

    @Transactional
    public List<UserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(Mapper::toUserDTO).collect(Collectors.toList());
    }

    @Transactional
    public UserDTO createUser(String userName, String password, boolean isAdmin) {
        User user = new User();
        user.setName(userName);
        user.setPassword(passwordEncoder.encode(password));
        user.setAdmin(isAdmin);
        userRepository.save(user);
        return Mapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO getUserByUsername(String userName) {
        User user = userRepository.findByName(userName).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return Mapper.toUserDTO(user);
    }

    @Transactional
    public UserDTO authenticateUser(String userName, String password) {
        User user = userRepository.findByName(userName).orElseThrow(() -> new RuntimeException("User not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }
        return Mapper.toUserDTO(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByName(username).orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
        return new org.springframework.security.core.userdetails.User(user.getName(), user.getPassword(), new ArrayList<>());
    }
}
