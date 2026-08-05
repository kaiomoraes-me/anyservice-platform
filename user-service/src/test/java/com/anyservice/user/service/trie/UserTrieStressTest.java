package com.anyservice.user.service.trie;

import com.anyservice.user.dto.UserSearchDto;
import org.junit.jupiter.api.Test;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;

public class UserTrieStressTest {

    @Test
    void testConcurrentInsertsAndSearch() throws InterruptedException {
        UserTrie trie = new UserTrie();
        int threadCount = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(100);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final int id = i;
            executor.submit(() -> {
                trie.insert(new UserSearchDto((long) id, "Kaio " + id, "kaio_" + id, "url"));
                trie.search("Ka", 10);
                latch.countDown();
            });
        }

        assertTrue(latch.await(5, TimeUnit.SECONDS), "Deadlock detectado sob estresse");
        
        assertEquals(10, trie.search("Kaio", 10).size(), "Deve retornar o limite de 10 sem travar");
        assertEquals(0, trie.search("Injecting<script>alert(1)</script>100000chars", 10).size(), "Deve mitigar XSS e strings gigantes");
        executor.shutdown();
    }
}
