package com.talentFlow.integration;

import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.RoleRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.CourseEnrollment;
import com.talentFlow.course.domain.CourseModule;
import com.talentFlow.course.domain.Lesson;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.domain.enums.LessonType;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseModuleRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import com.talentFlow.course.infrastructure.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
class CourseProgressFlowIT extends BaseIntegrationTest {

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
    @Autowired
    private CourseModuleRepository courseModuleRepository;
    @Autowired
    private LessonRepository lessonRepository;

    private User learner;
    private Course course;
    private Lesson lesson1;
    private Lesson lesson2;
    private Lesson lesson3;
    private Lesson lesson4;

    @BeforeEach
    void setup() {
        Role mentorRole = roleRepository.findByName(RoleName.MENTOR).orElseThrow();
        Role internRole = roleRepository.findByName(RoleName.INTERN).orElseThrow();

        User mentor = new User();
        mentor.setEmail("progress-mentor@test.com");
        mentor.setFirstName("Mentor");
        mentor.setLastName("Progress");
        mentor.setPasswordHash("x");
        mentor.setStatus(UserStatus.ACTIVE);
        mentor.setEmailVerified(true);
        mentor.setFailedLoginAttempts(0);
        mentor.setRoles(Set.of(mentorRole));
        mentor = userRepository.save(mentor);

        learner = new User();
        learner.setEmail("progress-learner@test.com");
        learner.setFirstName("Learner");
        learner.setLastName("Progress");
        learner.setPasswordHash("x");
        learner.setStatus(UserStatus.ACTIVE);
        learner.setEmailVerified(true);
        learner.setFailedLoginAttempts(0);
        learner.setRoles(Set.of(internRole));
        learner = userRepository.save(learner);

        course = new Course();
        course.setTitle("Progress Course");
        course.setDescription("Course for progress test");
        course.setStatus(CourseStatus.PUBLISHED);
        course.setCreatedByUser(mentor);
        course.setPublishedAt(LocalDateTime.now());
        course = courseRepository.save(course);

        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.setCourse(course);
        enrollment.setUser(learner);
        enrollment.setStatus(EnrollmentStatus.ENROLLED);
        enrollment.setEnrolledAt(LocalDateTime.now());
        enrollment.setProgressPct(BigDecimal.ZERO);
        courseEnrollmentRepository.save(enrollment);

        CourseModule module1 = new CourseModule();
        module1.setCourse(course);
        module1.setTitle("Module 1");
        module1.setPosition(1);
        module1 = courseModuleRepository.save(module1);

        CourseModule module2 = new CourseModule();
        module2.setCourse(course);
        module2.setTitle("Module 2");
        module2.setPosition(2);
        module2 = courseModuleRepository.save(module2);

        lesson1 = buildLesson(module1, "Lesson 1", 1);
        lesson2 = buildLesson(module1, "Lesson 2", 2);
        lesson3 = buildLesson(module2, "Lesson 3", 1);
        lesson4 = buildLesson(module2, "Lesson 4", 2);
    }

    @Test
    @WithMockUser(username = "progress-learner@test.com", roles = {"INTERN"})
    void learnerCompletingLessonsUpdatesProgressAndCompletesEnrollmentAtHundredPercent() throws Exception {
        mockMvc.perform(get("/api/v1/courses/" + course.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPct").value(0))
                .andExpect(jsonPath("$.modules.length()").value(2));

        mockMvc.perform(post("/api/v1/lessons/" + lesson1.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPct").value(25))
                .andExpect(jsonPath("$.enrollmentStatus").value("ENROLLED"))
                .andExpect(jsonPath("$.certificateQueued").value(false));

        mockMvc.perform(post("/api/v1/lessons/" + lesson2.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPct").value(50));

        mockMvc.perform(post("/api/v1/lessons/" + lesson3.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPct").value(75));

        mockMvc.perform(post("/api/v1/lessons/" + lesson4.getId() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.progressPct").value(100))
                .andExpect(jsonPath("$.enrollmentStatus").value("COMPLETED"))
                .andExpect(jsonPath("$.certificateQueued").value(true));

        CourseEnrollment enrollment = courseEnrollmentRepository.findByCourseAndUser(course, learner).orElseThrow();
        assertThat(enrollment.getProgressPct()).isEqualByComparingTo("100.00");
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.COMPLETED);
        assertThat(enrollment.getCompletedAt()).isNotNull();
    }

    private Lesson buildLesson(CourseModule module, String title, int position) {
        Lesson lesson = new Lesson();
        lesson.setModule(module);
        lesson.setTitle(title);
        lesson.setLessonType(LessonType.TEXT);
        lesson.setContentText("Content for " + title);
        lesson.setPosition(position);
        return lessonRepository.save(lesson);
    }
}
