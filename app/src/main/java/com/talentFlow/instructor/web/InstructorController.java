package com.talentFlow.instructor.web;

import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.common.exception.ApiException;
import com.talentFlow.course.web.dto.AssignmentFeedbackResponse;
import com.talentFlow.course.web.dto.AssignmentResponse;
import com.talentFlow.course.web.dto.CourseModuleResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.CreateAssignmentRequest;
import com.talentFlow.course.web.dto.CreateCourseModuleRequest;
import com.talentFlow.course.web.dto.CreateCourseRequest;
import com.talentFlow.course.web.dto.CreateLessonRequest;
import com.talentFlow.course.web.dto.LearnerProgressResponse;
import com.talentFlow.course.web.dto.LessonResponse;
import com.talentFlow.course.web.dto.ProvideFeedbackRequest;
import com.talentFlow.course.domain.enums.LessonType;
import com.talentFlow.common.response.ApiMessageResponse;
import com.talentFlow.instructor.application.InstructorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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

    @GetMapping("/my-courses")
    public List<CourseResponse> listMyCourses(Authentication authentication) {
        return instructorService.listMyCourses(getActor(authentication));
    }

    // Module CRUD
    @PostMapping("/courses/{courseId}/modules")
    public CourseModuleResponse createCourseModule(
            @PathVariable UUID courseId,
            @Valid @RequestBody CreateCourseModuleRequest request,
            Authentication authentication
    ) {
        return instructorService.createCourseModule(courseId, request, getActor(authentication));
    }

    @GetMapping("/courses/{courseId}/modules")
    public List<CourseModuleResponse> listCourseModules(
            @PathVariable UUID courseId,
            Authentication authentication
    ) {
        return instructorService.listCourseModules(courseId, getActor(authentication));
    }

    @PutMapping("/modules/{moduleId}")
    public CourseModuleResponse updateCourseModule(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateCourseModuleRequest request,
            Authentication authentication
    ) {
        return instructorService.updateCourseModule(moduleId, request, getActor(authentication));
    }

    @DeleteMapping("/modules/{moduleId}")
    public ApiMessageResponse deleteCourseModule(
            @PathVariable UUID moduleId,
            Authentication authentication
    ) {
        instructorService.deleteCourseModule(moduleId, getActor(authentication));
        return new ApiMessageResponse("Module deleted successfully");
    }

    // Lesson CRUD
    @PostMapping(value = "/modules/{moduleId}/lessons", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LessonResponse createLesson(
            @PathVariable UUID moduleId,
            @Valid @RequestBody CreateLessonRequest request,
            Authentication authentication
    ) {
        return instructorService.createLesson(moduleId, request, getActor(authentication));
    }

    @PostMapping(value = "/modules/{moduleId}/lessons", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LessonResponse createLessonWithFile(
            @PathVariable UUID moduleId,
            @RequestParam("title") String title,
            @RequestParam("lessonType") LessonType lessonType,
            @RequestParam("position") Integer position,
            @RequestPart("file") MultipartFile file,
            Authentication authentication
    ) {
        return instructorService.createLessonWithFile(moduleId, title, lessonType, position, file, getActor(authentication));
    }

    @GetMapping("/lessons/{lessonId}")
    public LessonResponse getLesson(
            @PathVariable UUID lessonId,
            Authentication authentication
    ) {
        return instructorService.getLesson(lessonId, getActor(authentication));
    }

    @PutMapping(value = "/lessons/{lessonId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public LessonResponse updateLesson(
            @PathVariable UUID lessonId,
            @Valid @RequestBody CreateLessonRequest request,
            Authentication authentication
    ) {
        return instructorService.updateLesson(lessonId, request, getActor(authentication));
    }

    @PutMapping(value = "/lessons/{lessonId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LessonResponse updateLessonWithFile(
            @PathVariable UUID lessonId,
            @RequestParam("title") String title,
            @RequestParam("lessonType") LessonType lessonType,
            @RequestParam("position") Integer position,
            @RequestPart(value = "file", required = false) MultipartFile file,
            Authentication authentication
    ) {
        return instructorService.updateLessonWithFile(lessonId, title, lessonType, position, file, getActor(authentication));
    }

    @DeleteMapping("/lessons/{lessonId}")
    public ApiMessageResponse deleteLesson(
            @PathVariable UUID lessonId,
            Authentication authentication
    ) {
        instructorService.deleteLesson(lessonId, getActor(authentication));
        return new ApiMessageResponse("Lesson deleted successfully");
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
