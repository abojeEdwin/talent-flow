package com.talentFlow.admin.web;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.common.response.ApiMessageResponse;
import com.talentFlow.course.application.AdminCourseService;
import com.talentFlow.course.domain.enums.CourseStatus;
import com.talentFlow.course.web.dto.AssignInstructorsRequest;
import com.talentFlow.course.web.dto.CourseResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;
    private final UserRepository userRepository;

    @GetMapping
    public List<CourseResponse> listCourses(@RequestParam(required = false) CourseStatus status) {
        return adminCourseService.listCourses(status);
    }

    @PatchMapping("/{courseId}/publish")
    public CourseResponse publishCourse(@PathVariable UUID courseId, Authentication authentication) {
        return adminCourseService.publishCourse(courseId, getActor(authentication));
    }

    @PatchMapping("/{courseId}/unpublish")
    public CourseResponse unpublishCourse(@PathVariable UUID courseId, Authentication authentication) {
        return adminCourseService.unpublishCourse(courseId, getActor(authentication));
    }

    @PatchMapping("/{courseId}/archive")
    public CourseResponse archiveCourse(@PathVariable UUID courseId, Authentication authentication) {
        return adminCourseService.archiveCourse(courseId, getActor(authentication));
    }

    @PutMapping("/{courseId}/instructors")
    public CourseResponse assignInstructors(
            @PathVariable UUID courseId,
            @Valid @RequestBody AssignInstructorsRequest request,
            Authentication authentication
    ) {
        return adminCourseService.assignInstructors(courseId, request, getActor(authentication));
    }

    @PostMapping("/{courseId}/enrollments/cohorts/{cohortId}")
    public ApiMessageResponse bulkEnrollCohort(
            @PathVariable UUID courseId,
            @PathVariable UUID cohortId,
            Authentication authentication
    ) {
        int count = adminCourseService.bulkEnrollCohort(courseId, cohortId, getActor(authentication));
        return new ApiMessageResponse("Enrolled " + count + " learners from cohort");
    }

    @PostMapping("/{courseId}/enrollments/teams/{teamId}")
    public ApiMessageResponse bulkEnrollTeam(
            @PathVariable UUID courseId,
            @PathVariable UUID teamId,
            Authentication authentication
    ) {
        int count = adminCourseService.bulkEnrollTeam(courseId, teamId, getActor(authentication));
        return new ApiMessageResponse("Enrolled " + count + " learners from team");
    }

    @DeleteMapping("/{courseId}/enrollments/{userId}")
    public ApiMessageResponse revokeEnrollment(
            @PathVariable UUID courseId,
            @PathVariable UUID userId,
            Authentication authentication
    ) {
        adminCourseService.revokeEnrollment(courseId, userId, getActor(authentication));
        return new ApiMessageResponse("Enrollment revoked successfully");
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
