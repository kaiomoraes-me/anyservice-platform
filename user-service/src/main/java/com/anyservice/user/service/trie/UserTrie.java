package com.anyservice.user.service.trie;

import com.anyservice.user.dto.UserSearchDto;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class UserTrie {
    private final TrieNode root = new TrieNode();
    private static final int MAX_QUERY_LENGTH = 50; // Blindagem contra DoS

    public void insert(UserSearchDto user) {
        if (user.getName() != null) {
            insertWord(sanitize(user.getName()), user);
        }
        if (user.getUsername() != null) {
            insertWord("@" + sanitize(user.getUsername()), user);
        }
    }

    private String sanitize(String input) {
        // Corta strings massivas e remove caracteres não-alfanuméricos perigosos (Anti-XSS/Zalgo)
        String clean = input.replaceAll("[^a-zA-Z0-9_\\-\\s@.]", "").toLowerCase();
        return clean.length() > MAX_QUERY_LENGTH ? clean.substring(0, MAX_QUERY_LENGTH) : clean;
    }

    private void insertWord(String word, UserSearchDto user) {
        if (word.isBlank()) return;
        TrieNode current = root;
        for (char c : word.toCharArray()) {
            // Operação 100% atômica, thread-safe e blindada contra concorrência extrema
            current = current.children.computeIfAbsent(c, k -> new TrieNode());
            current.users.add(user); 
        }
    }

    public List<UserSearchDto> search(String prefix, int limit) {
        if (prefix == null || prefix.isBlank() || prefix.length() > MAX_QUERY_LENGTH) {
            return Collections.emptyList();
        }
        
        String cleanPrefix = sanitize(prefix);
        TrieNode current = root;
        
        for (char c : cleanPrefix.toCharArray()) {
            current = current.children.get(c);
            if (current == null) {
                return Collections.emptyList();
            }
        }
        return current.users.stream().limit(limit).collect(Collectors.toList());
    }
}
