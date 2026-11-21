package com.mkhaled.library;

import com.mkhaled.library.support.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class LibraryApplicationTests {

    @Test
    void contextLoads() {
    }
}
