package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.dto.AssignmentFeedbackResponse;
import com.talentFlow.data.dto.AssignmentResponse;
import com.talentFlow.data.dto.CourseModuleResponse;
import com.talentFlow.data.dto.CourseResponse;
import com.talentFlow.data.dto.CreateAssignmentRequest;
import com.talentFlow.data.dto.CreateCourseModuleRequest;
import com.talentFlow.data.dto.CreateCourseRequest;
import com.talentFlow.data.dto.CreateLessonRequest;
import com.talentFlow.data.dto.InstructorProgressResponse;
import com.talentFlow.data.dto.LearnerProgressResponse;
import com.talentFlow.data.dto.LessonResponse;
import com.talentFlow.data.dto.ProvideFeedbackRequest;
import com.talentFlow.data.Enums.CourseStatus;
import com.talentFlow.data.Enums.LessonType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

public interface InstructorService {
    CourseResponse createCourse(CreateCourseRequest request, User actor);

    CourseResponse createCourseWithMedia(String title,
                                         String description,
                                         MultipartFile coverImage,
                                         MultipartFile introVideo,
                                         User actor);

    Page<CourseResponse> listMyCourses(User actor, CourseStatus status, Pageable pageable);

    CourseModuleResponse createCourseModule(UUID courseId, CreateCourseModuleRequest request, User actor);

    Page<CourseModuleResponse> listCourseModules(UUID courseId, User actor, Pageable pageable);

    CourseModuleResponse updateCourseModule(UUID moduleId, CreateCourseModuleRequest request, User actor);

    void deleteCourseModule(UUID moduleId, User actor);

    LessonResponse createLesson(UUID moduleId, CreateLessonRequest request, User actor);

    LessonResponse createLessonWithFile(UUID moduleId,
                                        String title,
                                        LessonType lessonType,
                                        Integer position,
                                        MultipartFile file,
                                        User actor);

    LessonResponse getLesson(UUID lessonId, User actor);

    LessonResponse updateLesson(UUID lessonId, CreateLessonRequest request, User actor);

    LessonResponse updateLessonWithFile(UUID lessonId,
                                        String title,
                                        LessonType lessonType,
                                        Integer position,
                                        MultipartFile file,
                                        User actor);

    void deleteLesson(UUID lessonId, User actor);

    AssignmentResponse createAssignment(UUID courseId, CreateAssignmentRequest request, User actor);

    Page<AssignmentResponse> listAssignments(User actor, Pageable pageable);

    AssignmentResponse getAssignment(UUID assignmentId, User actor);

    void deleteAssignment(UUID assignmentId, User actor);

    Page<InstructorProgressResponse> listProgress(User actor, Pageable pageable);

    List<LearnerProgressResponse> monitorLearnerProgress(UUID courseId, User actor);

    AssignmentFeedbackResponse provideFeedback(UUID submissionId, ProvideFeedbackRequest request, User actor);
}
