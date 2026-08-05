package com.anyservice.user.service.trie;

import com.anyservice.user.dto.UserSearchDto;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class TrieNode {
    Map<Character, TrieNode> children = new ConcurrentHashMap<>();
    Set<UserSearchDto> users = ConcurrentHashMap.newKeySet(); 
}
