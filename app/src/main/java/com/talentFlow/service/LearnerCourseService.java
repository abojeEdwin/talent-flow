package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.dto.CourseDetailResponse;
import com.talentFlow.data.dto.CourseResponse;
import com.talentFlow.data.dto.LessonCompletionResponse;

import java.util.List;
import java.util.UUID;

public interface LearnerCourseService {

    List<CourseResponse> browsePublishedCourses();

    CourseResponse enrollInCourse(UUID courseId, User learner);

    List<CourseResponse> myEnrollments(User learner);

    CourseDetailResponse getCourseDetail(UUID courseId, User learner);

    LessonCompletionResponse completeLesson(UUID lessonId, User learner);

    String getCourseCoverImageUrl(UUID courseId);

}
