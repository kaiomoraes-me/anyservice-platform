package com.anyservice.user.service.trie;

import com.anyservice.user.dto.UserSearchDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

public class UserTrieTest {

    private UserTrie trie;
    private UserSearchDto kaio;
    private UserSearchDto kaila;

    @BeforeEach
    void setUp() {
        trie = new UserTrie();
        kaio = new UserSearchDto(1L, "Kaio", "kaio123", "url1");
        kaila = new UserSearchDto(2L, "Kaila", "kaila_oficial", "url2");
        trie.insert(kaio);
        trie.insert(kaila);
    }

    @Test
    void testPrefixSearchByName() {
        List<UserSearchDto> results = trie.search("Kai", 10);
        assertEquals(2, results.size(), "Deve retornar Kaio e Kaila");
        assertTrue(results.contains(kaio));
        assertTrue(results.contains(kaila));
    }

    @Test
    void testSearchByUniqueUsername() {
        List<UserSearchDto> results = trie.search("@kaio123", 10);
        assertEquals(1, results.size(), "Busca pela `@` deve ser exclusiva ao username");
        assertTrue(results.contains(kaio));
        assertFalse(results.contains(kaila));
    }
    
    @Test
    void testSearchNoMatch() {
        List<UserSearchDto> results = trie.search("Zebra", 10);
        assertTrue(results.isEmpty());
    }
}
