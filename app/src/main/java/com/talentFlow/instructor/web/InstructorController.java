package com.talentFlow.instructor.web;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.web.dto.AssignmentFeedbackResponse;
import com.talentFlow.course.web.dto.AssignmentResponse;
import com.talentFlow.course.web.dto.CourseMaterialResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.CreateAssignmentRequest;
import com.talentFlow.course.web.dto.CreateCourseRequest;
import com.talentFlow.course.web.dto.LearnerProgressResponse;
import com.talentFlow.course.web.dto.ProvideFeedbackRequest;
import com.talentFlow.course.web.dto.UploadMaterialRequest;
import com.talentFlow.course.domain.enums.MaterialType;
import com.talentFlow.instructor.application.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/instructor")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('INSTRUCTOR','ADMIN')")
public class InstructorController {

    private final InstructorService instructorService;
    private final UserRepository userRepository;

    @PostMapping(value = "/courses", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CourseResponse createCourse(@Valid @RequestBody CreateCourseRequest request, Authentication authentication) {
        return instructorService.createCourse(request, getActor(authentication));
    }

    @PostMapping(value = "/courses", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseResponse createCourseWithMedia(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestPart(value = "coverImage", required = false) MultipartFile coverImage,
            @RequestPart(value = "introVideo", required = false) MultipartFile introVideo,
            Authentication authentication

    ) {
        return instructorService.createCourseWithMedia(title, description, coverImage, introVideo, getActor(authentication));
    }

    @GetMapping("/courses")
    public List<CourseResponse> listMyCourses(Authentication authentication) {
        return instructorService.listMyCourses(getActor(authentication));
    }

    @PostMapping(value = "/courses/{courseId}/materials", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CourseMaterialResponse uploadMaterial(
            @PathVariable UUID courseId,
            @Valid @RequestBody UploadMaterialRequest request,
            Authentication authentication
    ) {
        return instructorService.uploadMaterial(courseId, request, getActor(authentication));
    }

    @PostMapping(value = "/courses/{courseId}/materials", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CourseMaterialResponse uploadMaterialFile(
            @PathVariable UUID courseId,
            @RequestParam("title") String title,
            @RequestParam("materialType") MaterialType materialType,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        return instructorService.uploadMaterialFile(courseId, title, materialType, file, getActor(authentication));
    }

    @PostMapping("/courses/{courseId}/assignments")
    public AssignmentResponse createAssignment(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateAssignmentRequest request,
            Authentication authentication
    ) {
        return instructorService.createAssignment(courseId, request, getActor(authentication));
    }

    @GetMapping("/courses/{courseId}/progress")
    public List<LearnerProgressResponse> monitorLearnerProgress(@PathVariable UUID courseId, Authentication authentication) {
        return instructorService.monitorLearnerProgress(courseId, getActor(authentication));
    }

    @PostMapping("/submissions/{submissionId}/feedback")
    public AssignmentFeedbackResponse provideFeedback(
            @PathVariable UUID submissionId,
            @Valid @RequestBody ProvideFeedbackRequest request,
            Authentication authentication
    ) {
        return instructorService.provideFeedback(submissionId, request, getActor(authentication));
    }

    private User getActor(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails userDetails)) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Not an authenticated user");
        }
        return userRepository.findByEmailIgnoreCase(userDetails.getUsername())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authenticated user not found"));
    }
}
