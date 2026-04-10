package com.eventflow;

import com.eventflow.model.User;
import com.eventflow.repository.UserRepository;
import com.eventflow.service.UserService;
import com.eventflow.dto.EventFlowDtos.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EventFlowApplicationTests {

    @Autowired MockMvc mockMvc;
    @Autowired UserService userService;
    @Autowired UserRepository userRepository;

    @Test
    void contextLoads() {
        assertThat(userRepository).isNotNull();
    }

    @Test
    void homePageIsPublic() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isOk());
    }

    @Test
    void eventsPageIsPublic() throws Exception {
        mockMvc.perform(get("/events"))
               .andExpect(status().isOk());
    }

    @Test
    void loginPageIsPublic() throws Exception {
        mockMvc.perform(get("/auth/login"))
               .andExpect(status().isOk());
    }

    @Test
    void adminPageRequiresAuth() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    void managePageRequiresAuth() throws Exception {
        mockMvc.perform(get("/manage/dashboard"))
               .andExpect(status().is3xxRedirection());
    }

    @Test
    void userRegistrationWorks() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("Test");
        dto.setLastName("User");
        dto.setEmail("test.unique@example.com");
        dto.setPassword("Password123");
        dto.setConfirmPassword("Password123");

        User user = userService.registerUser(dto);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test.unique@example.com");
        assertThat(user.getRole()).isEqualTo(User.Role.USER);
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void duplicateEmailThrows() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("A");
        dto.setLastName("B");
        dto.setEmail("admin@eventflow.com"); // already seeded
        dto.setPassword("Password123");
        dto.setConfirmPassword("Password123");

        assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already registered");
    }

    @Test
    void passwordMismatchThrows() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFirstName("A");
        dto.setLastName("B");
        dto.setEmail("new@example.com");
        dto.setPassword("Password123");
        dto.setConfirmPassword("DifferentPassword");

        assertThatThrownBy(() -> userService.registerUser(dto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("do not match");
    }
}
