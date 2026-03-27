package com.talentFlow.integration;

import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.RoleRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LearnerCourseFlowIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    private Course publishedCourse;

    @BeforeEach
    void setup() {
        Role mentorRole = roleRepository.findByName(RoleName.MENTOR).orElseThrow();
        Role internRole = roleRepository.findByName(RoleName.INTERN).orElseThrow();

        User mentor = new User();
        mentor.setEmail("mentor@test.com");
        mentor.setFirstName("Mentor");
        mentor.setLastName("One");
        mentor.setPasswordHash("x");
        mentor.setStatus(UserStatus.ACTIVE);
        mentor.setEmailVerified(true);
        mentor.setFailedLoginAttempts(0);
        mentor.setRoles(Set.of(mentorRole));
        User savedMentor = userRepository.save(mentor);

        User learner = new User();
        learner.setEmail("learner@test.com");
        learner.setFirstName("Learner");
        learner.setLastName("One");
        learner.setPasswordHash("x");
        learner.setStatus(UserStatus.ACTIVE);
        learner.setEmailVerified(true);
        learner.setFailedLoginAttempts(0);
        learner.setRoles(Set.of(internRole));
        userRepository.save(learner);

        Course course = new Course();
        course.setTitle("Java Fundamentals");
        course.setDescription("Core Java");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setCreatedByUser(savedMentor);
        course.setPublishedAt(LocalDateTime.now());
        publishedCourse = courseRepository.save(course);
    }

    @Test
    @WithMockUser(username = "learner@test.com", roles = {"INTERN"})
    void learnerCanBrowseAndEnrollPublishedCourse() throws Exception {
        mockMvc.perform(get("/api/v1/courses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Fundamentals"));

        mockMvc.perform(post("/api/v1/courses/" + publishedCourse.getId() + "/enroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publishedCourse.getId().toString()));

        var learner = userRepository.findByEmailIgnoreCase("learner@test.com").orElseThrow();
        assertThat(courseEnrollmentRepository.findByCourseAndUser(publishedCourse, learner)).isPresent();
    }
}
