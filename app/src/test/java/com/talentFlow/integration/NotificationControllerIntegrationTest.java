package com.talentFlow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.auth.infrastructure.security.JwtService;
import com.talentFlow.notification.domain.Notification;
import com.talentFlow.notification.infrastructure.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Notification API Integration Tests")
public class NotificationControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;
    private String baseUrl = "/api/v1/notifications";

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setEmail("instructor@test.local");
        testUser.setPasswordHash("hashedPassword");
        testUser.setFirstName("Test");
        testUser.setLastName("Instructor");
        testUser.setRole(RoleName.INSTRUCTOR);
        testUser.setStatus(UserStatus.ACTIVE);
        testUser.setFailedLoginAttempts(0);
        testUser = userRepository.save(testUser);

        authToken = jwtService.generateToken(testUser.getEmail());
    }

    @Test
    @DisplayName("GET / should list notifications with pagination")
    void testListNotifications() throws Exception {
        // Create test notifications
        for (int i = 0; i < 5; i++) {
            Notification notif = new Notification();
            notif.setUser(testUser);
            notif.setType("TEST_TYPE");
            notif.setTitle("Test Notification " + i);
            notif.setMessage("Test message " + i);
            notif.setRead(i % 2 == 0);
            notif.setCreatedAt(LocalDateTime.now().minusHours(i));
            notificationRepository.save(notif);
        }

        mockMvc.perform(get(baseUrl + "/")
                .header("Authorization", "Bearer " + authToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", equalTo(5)));
    }

    @Test
    @DisplayName("PATCH /{notificationId}/read should mark single notification as read")
    void testMarkSingleNotificationAsRead() throws Exception {
        Notification unreadNotif = new Notification();
        unreadNotif.setUser(testUser);
        unreadNotif.setType("TEST_TYPE");
        unreadNotif.setTitle("Test Notification");
        unreadNotif.setMessage("Test message");
        unreadNotif.setRead(false);
        unreadNotif = notificationRepository.save(unreadNotif);

        mockMvc.perform(patch(baseUrl + "/" + unreadNotif.getId() + "/read")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", equalTo(true)))
                .andExpect(jsonPath("$.id", equalTo(unreadNotif.getId().toString())));

        // Verify in database
        Notification saved = notificationRepository.findById(unreadNotif.getId()).orElseThrow();
        assertThat(saved.isRead()).isTrue();
        assertThat(saved.getReadAt()).isNotNull();
    }

    @Test
    @DisplayName("PATCH /{notificationId}/read should return 404 for non-existent notification")
    void testMarkNonExistentNotificationAsRead() throws Exception {
        UUID fakeId = UUID.randomUUID();
        mockMvc.perform(patch(baseUrl + "/" + fakeId + "/read")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /read-all should mark all unread notifications as read")
    void testMarkAllAsReadWithPut() throws Exception {
        // Create mix of read and unread notifications
        for (int i = 0; i < 3; i++) {
            Notification notif = new Notification();
            notif.setUser(testUser);
            notif.setType("TEST_TYPE");
            notif.setTitle("Test Notification " + i);
            notif.setMessage("Test message " + i);
            notif.setRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(put(baseUrl + "/read-all")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("All notifications marked as read")))
                .andExpect(jsonPath("$.count", equalTo(3)));

        // Verify all are marked as read
        var unreadNotifications = notificationRepository.findByUserAndReadFalse(testUser);
        assertThat(unreadNotifications).isEmpty();
    }

    @Test
    @DisplayName("PATCH /read-all should mark all unread notifications as read")
    void testMarkAllAsReadWithPatch() throws Exception {
        // Create mix of read and unread notifications
        for (int i = 0; i < 3; i++) {
            Notification notif = new Notification();
            notif.setUser(testUser);
            notif.setType("TEST_TYPE");
            notif.setTitle("Test Notification " + i);
            notif.setMessage("Test message " + i);
            notif.setRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(patch(baseUrl + "/read-all")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("All notifications marked as read")))
                .andExpect(jsonPath("$.count", equalTo(3)));

        // Verify all are marked as read
        var unreadNotifications = notificationRepository.findByUserAndReadFalse(testUser);
        assertThat(unreadNotifications).isEmpty();
    }

    @Test
    @DisplayName("PUT /mark-all-read (legacy) should mark all as read with deprecation headers")
    void testMarkAllAsReadLegacyWithPut() throws Exception {
        // Create unread notifications
        for (int i = 0; i < 2; i++) {
            Notification notif = new Notification();
            notif.setUser(testUser);
            notif.setType("TEST_TYPE");
            notif.setTitle("Test Notification " + i);
            notif.setMessage("Test message " + i);
            notif.setRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(put(baseUrl + "/mark-all-read")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("Deprecation"))
                .andExpect(header().string("Deprecation", equalTo("true")))
                .andExpect(header().exists("Sunset"))
                .andExpect(header().exists("Warning"))
                .andExpect(jsonPath("$.message", equalTo("All notifications marked as read")))
                .andExpect(jsonPath("$.count", equalTo(2)));
    }

    @Test
    @DisplayName("PATCH /mark-all-read (legacy) should mark all as read with deprecation headers")
    void testMarkAllAsReadLegacyWithPatch() throws Exception {
        // Create unread notifications
        for (int i = 0; i < 2; i++) {
            Notification notif = new Notification();
            notif.setUser(testUser);
            notif.setType("TEST_TYPE");
            notif.setTitle("Test Notification " + i);
            notif.setMessage("Test message " + i);
            notif.setRead(false);
            notificationRepository.save(notif);
        }

        mockMvc.perform(patch(baseUrl + "/mark-all-read")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(header().exists("Deprecation"))
                .andExpect(header().string("Deprecation", equalTo("true")))
                .andExpect(header().exists("Sunset"))
                .andExpect(header().exists("Warning"))
                .andExpect(jsonPath("$.message", equalTo("All notifications marked as read")))
                .andExpect(jsonPath("$.count", equalTo(2)));
    }

    @Test
    @DisplayName("PUT /read-all should return 0 count when no unread notifications")
    void testMarkAllAsReadWhenNoUnread() throws Exception {
        mockMvc.perform(put(baseUrl + "/read-all")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", equalTo("All notifications marked as read")))
                .andExpect(jsonPath("$.count", equalTo(0)));
    }

    @Test
    @DisplayName("PATCH /{notificationId}/read should not modify already read notification")
    void testMarkAlreadyReadNotificationIdempotent() throws Exception {
        Notification readNotif = new Notification();
        readNotif.setUser(testUser);
        readNotif.setType("TEST_TYPE");
        readNotif.setTitle("Already Read");
        readNotif.setMessage("Test message");
        readNotif.setRead(true);
        readNotif.setReadAt(LocalDateTime.now());
        readNotif = notificationRepository.save(readNotif);

        LocalDateTime originalReadAt = readNotif.getReadAt();

        mockMvc.perform(patch(baseUrl + "/" + readNotif.getId() + "/read")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read", equalTo(true)));

        // Verify readAt wasn't updated
        Notification saved = notificationRepository.findById(readNotif.getId()).orElseThrow();
        assertThat(saved.getReadAt()).isEqualTo(originalReadAt);
    }

    @Test
    @DisplayName("Endpoints should reject requests without authentication")
    void testUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get(baseUrl + "/"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(put(baseUrl + "/read-all"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(patch(baseUrl + "/read-all"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Endpoints should respect role-based access control")
    void testRoleBasedAccessControl() throws Exception {
        User learner = new User();
        learner.setEmail("learner@test.local");
        learner.setPasswordHash("hashedPassword");
        learner.setFirstName("Test");
        learner.setLastName("Learner");
        learner.setRole(RoleName.INTERN);
        learner.setStatus(UserStatus.ACTIVE);
        learner = userRepository.save(learner);

        String learnerToken = jwtService.generateToken(learner.getEmail());

        // Should succeed - learners have access
        mockMvc.perform(get(baseUrl + "/")
                .header("Authorization", "Bearer " + learnerToken)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk());
    }
}

