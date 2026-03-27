package com.talentFlow.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.talentFlow.admin.domain.Cohort;
import com.talentFlow.admin.domain.ProjectTeam;
import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.domain.TeamMemberId;
import com.talentFlow.admin.infrastructure.repository.CohortRepository;
import com.talentFlow.admin.infrastructure.repository.ProjectTeamRepository;
import com.talentFlow.admin.infrastructure.repository.TeamMemberRepository;
import com.talentFlow.auth.domain.Role;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.RoleRepository;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.course.infrastructure.repository.CourseEnrollmentRepository;
import com.talentFlow.course.infrastructure.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AdminCourseGovernanceIT extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private CohortRepository cohortRepository;
    @Autowired
    private ProjectTeamRepository projectTeamRepository;
    @Autowired
    private TeamMemberRepository teamMemberRepository;
    @Autowired
    private CourseEnrollmentRepository courseEnrollmentRepository;

    private Course draftCourse;
    private User instructor;
    private User learner;
    private Cohort cohort;
    private ProjectTeam team;

    @BeforeEach
    void setup() {
        Role adminRole = roleRepository.findByName(RoleName.ADMIN).orElseThrow();
        Role mentorRole = roleRepository.findByName(RoleName.MENTOR).orElseThrow();
        Role internRole = roleRepository.findByName(RoleName.INTERN).orElseThrow();

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setFirstName("Admin");
        admin.setLastName("One");
        admin.setPasswordHash("x");
        admin.setStatus(UserStatus.ACTIVE);
        admin.setEmailVerified(true);
        admin.setFailedLoginAttempts(0);
        admin.setRoles(Set.of(adminRole));
        User savedAdmin = userRepository.save(admin);

        instructor = new User();
        instructor.setEmail("instructor@test.com");
        instructor.setFirstName("Inst");
        instructor.setLastName("One");
        instructor.setPasswordHash("x");
        instructor.setStatus(UserStatus.ACTIVE);
        instructor.setEmailVerified(true);
        instructor.setFailedLoginAttempts(0);
        instructor.setRoles(Set.of(mentorRole));
        instructor = userRepository.save(instructor);

        learner = new User();
        learner.setEmail("learner2@test.com");
        learner.setFirstName("Learner");
        learner.setLastName("Two");
        learner.setPasswordHash("x");
        learner.setStatus(UserStatus.ACTIVE);
        learner.setEmailVerified(true);
        learner.setFailedLoginAttempts(0);
        learner.setRoles(Set.of(internRole));
        learner = userRepository.save(learner);

        Course course = new Course();
        course.setTitle("Backend Bootcamp");
        course.setDescription("Bootcamp");
        course.setStatus(CourseStatus.DRAFT);
        course.setCreatedByUser(savedAdmin);
        draftCourse = courseRepository.save(course);

        cohort = new Cohort();
        cohort.setName("2026 Intake");
        cohort.setDescription("intake");
        cohort.setIntakeYear(2026);
        cohort.setStartDate(LocalDate.now());
        cohort.setEndDate(LocalDate.now().plusMonths(3));
        cohort.setActive(true);
        cohort = cohortRepository.save(cohort);

        team = new ProjectTeam();
        team.setCohort(cohort);
        team.setName("Team Alpha");
        team.setDescription("alpha");
        team = projectTeamRepository.save(team);

        TeamMember tm = new TeamMember();
        tm.setId(new TeamMemberId(team.getId(), learner.getId()));
        tm.setTeam(team);
        tm.setUser(learner);
        tm.setTeamRole("Backend");
        teamMemberRepository.save(tm);
    }

    @Test
    @WithMockUser(username = "admin@test.com", authorities = {"COURSE_MANAGE"})
    void adminCanPublishAssignAndManageEnrollments() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/courses/" + draftCourse.getId() + "/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));

        String assignBody = objectMapper.writeValueAsString(Map.of(
                "primaryInstructorId", instructor.getId(),
                "coInstructorIds", new String[]{}));
        mockMvc.perform(put("/api/v1/admin/courses/" + draftCourse.getId() + "/instructors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(assignBody))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/admin/courses/" + draftCourse.getId() + "/enrollments/teams/" + team.getId()))
                .andExpect(status().isOk());

        var enrollment = courseEnrollmentRepository.findByCourseAndUser(draftCourse, learner).orElseThrow();
        assertThat(enrollment.getStatus()).isEqualTo(EnrollmentStatus.ENROLLED);

        mockMvc.perform(delete("/api/v1/admin/courses/" + draftCourse.getId() + "/enrollments/" + learner.getId()))
                .andExpect(status().isOk());
        assertThat(courseEnrollmentRepository.findByCourseAndUser(draftCourse, learner).orElseThrow().getStatus())
                .isEqualTo(EnrollmentStatus.REVOKED);
    }
}
