package com.talentFlow.learner.web;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.web.dto.LessonCompletionResponse;
import com.talentFlow.learner.application.LearnerCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lessons")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INTERN','INSTRUCTOR','ADMIN')")
public class LearnerLessonController {

    private final LearnerCourseService learnerCourseService;
    private final UserRepository userRepository;

    @PostMapping("/{lessonId}/complete")
    public LessonCompletionResponse completeLesson(@PathVariable UUID lessonId, Authentication authentication) {
        return learnerCourseService.completeLesson(lessonId, getActor(authentication));
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
