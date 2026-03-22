package com.talentFlow.learner.application;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.web.dto.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface LearnerCourseService {
    List<CourseResponse> browsePublishedCourses();

    CourseResponse enrollInCourse(UUID courseId, User learner);

    List<CourseResponse> myEnrollments(User learner);
}
