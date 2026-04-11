package com.talentFlow.instructor.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.web.dto.AssignmentFeedbackResponse;
import com.talentFlow.course.web.dto.AssignmentResponse;
import com.talentFlow.course.web.dto.CourseMaterialResponse;
import com.talentFlow.course.web.dto.CourseModuleResponse;
import com.talentFlow.course.web.dto.CourseResponse;
import com.talentFlow.course.web.dto.CreateAssignmentRequest;
import com.talentFlow.course.web.dto.CreateCourseModuleRequest;
import com.talentFlow.course.web.dto.CreateCourseRequest;
import com.talentFlow.course.web.dto.LearnerProgressResponse;
import com.talentFlow.course.web.dto.ProvideFeedbackRequest;
import com.talentFlow.course.web.dto.UploadMaterialRequest;
import com.talentFlow.course.domain.enums.MaterialType;
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

    CourseMaterialResponse uploadMaterial(UUID courseId, UploadMaterialRequest request, User actor);

    CourseMaterialResponse uploadMaterialFile(UUID courseId,
                                              String title,
                                              MaterialType materialType,
                                              MultipartFile file,
                                              User actor);

    CourseModuleResponse createCourseModule(UUID courseId, CreateCourseModuleRequest request, User actor);

    AssignmentResponse createAssignment(UUID courseId, CreateAssignmentRequest request, User actor);

    List<LearnerProgressResponse> monitorLearnerProgress(UUID courseId, User actor);

    AssignmentFeedbackResponse provideFeedback(UUID submissionId, ProvideFeedbackRequest request, User actor);
}
