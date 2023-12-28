package org.example;


import org.example.service.TestService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AppTest {
    @Autowired
    private TestService testService;

    @Test
    public void testTestConnection() {
        testService.testConnection();
    }
}
