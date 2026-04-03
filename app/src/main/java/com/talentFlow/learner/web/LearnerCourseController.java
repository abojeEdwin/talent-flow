package com.talentFlow.learner.web;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.web.dto.CourseDetailResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.learner.application.LearnerCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/api/v1/learner/courses", "/api/v1/courses"})
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERN','INSTRUCTOR','ADMIN')")
public class LearnerCourseController {

    private final LearnerCourseService learnerCourseService;
    private final UserRepository userRepository;

    @GetMapping
    public List<CourseResponse> browsePublishedCourses() {
        return learnerCourseService.browsePublishedCourses();
    }

    @PostMapping("/{courseId}/enroll")
    public CourseResponse enroll(@PathVariable UUID courseId, Authentication authentication) {
        return learnerCourseService.enrollInCourse(courseId, getActor(authentication));
    }

    @GetMapping("/{courseId}")
    public CourseDetailResponse courseDetail(@PathVariable UUID courseId, Authentication authentication) {
        return learnerCourseService.getCourseDetail(courseId, getActor(authentication));
    }

    @GetMapping("/my")
    public List<CourseResponse> myEnrollments(Authentication authentication) {
        return learnerCourseService.myEnrollments(getActor(authentication));
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
