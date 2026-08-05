package com.anyservice.user.service;

import com.anyservice.user.dto.UserSearchDto;
import com.anyservice.user.model.User;
import com.anyservice.user.repository.UserRepository;
import com.anyservice.user.service.trie.UserTrie;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {
    private final UserRepository userRepository;
    private final UserTrie userTrie;

    public SearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.userTrie = new UserTrie();
    }

    @PostConstruct
    public void initTrie() {
        List<User> allUsers = userRepository.findAll();
        for (User u : allUsers) {
            if (u.isEnabled()) {
                userTrie.insert(new UserSearchDto(u.getId(), u.getName(), u.getUsernameIdentifier(), u.getProfilePictureUrl()));
            }
        }
    }

    public List<UserSearchDto> search(String query) {
        return userTrie.search(query, 10); 
    }
}
