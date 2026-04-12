package com.talentFlow.instructor.application;

import com.talentFlow.auth.domain.User;
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

    List<CourseResponse> listMyCourses(User actor);

    // Course Module CRUD
    CourseModuleResponse createCourseModule(UUID courseId, CreateCourseModuleRequest request, User actor);

    List<CourseModuleResponse> listCourseModules(UUID courseId, User actor);

    CourseModuleResponse updateCourseModule(UUID moduleId, CreateCourseModuleRequest request, User actor);

    void deleteCourseModule(UUID moduleId, User actor);

    // Lesson CRUD
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

    List<LearnerProgressResponse> monitorLearnerProgress(UUID courseId, User actor);

    AssignmentFeedbackResponse provideFeedback(UUID submissionId, ProvideFeedbackRequest request, User actor);
}
