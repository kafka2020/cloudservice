package ru.netology.cloudservice.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.netology.cloudservice.dto.LoginRequest;
import ru.netology.cloudservice.entity.UserEntity;
import ru.netology.cloudservice.repository.UserRepository;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CloudServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cloud")
            .withUsername("cloud")
            .withPassword("cloud");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired UserRepository userRepository;
    @Autowired PasswordEncoder passwordEncoder;

    static String token;

    @Test
    @Order(1)
    void login_returnsAuthToken() throws Exception {
        // подменяем seed-пользователя свежим bcrypt-хэшем "password",
        // чтобы тест не зависел от хэша из миграции
        userRepository.findByLogin("user1").ifPresent(u -> {
            u.setPassword(passwordEncoder.encode("password"));
            userRepository.save(u);
        });
        if (userRepository.findByLogin("user1").isEmpty()) {
            userRepository.save(UserEntity.builder()
                    .login("user1")
                    .password(passwordEncoder.encode("password"))
                    .build());
        }

        var body = objectMapper.writeValueAsString(new LoginRequest("user1", "password"));

        var result = mockMvc.perform(post("/login")
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.['auth-token']", notNullValue()))
                .andReturn();

        token = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("auth-token").asText();
    }

    @Test
    @Order(2)
    void uploadAndList() throws Exception {
        MockMultipartFile mp = new MockMultipartFile(
                "file", "hello.txt", "text/plain", "hello world".getBytes());

        mockMvc.perform(multipart("/file")
                        .file(mp)
                        .param("filename", "hello.txt")
                        .header("auth-token", token))
                .andExpect(status().isOk());

        mockMvc.perform(get("/list").header("auth-token", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename", is("hello.txt")))
                .andExpect(jsonPath("$[0].size", is(11)));
    }

    @Test
    @Order(3)
    void unauthorizedWithoutToken() throws Exception {
        mockMvc.perform(get("/list"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(4)
    void deleteFile() throws Exception {
        mockMvc.perform(delete("/file")
                        .param("filename", "hello.txt")
                        .header("auth-token", token))
                .andExpect(status().isOk());
    }
}
