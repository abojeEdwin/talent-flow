package com.talentFlow.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.Enums.CourseStatus;
import com.talentFlow.data.dto.AssignInstructorsRequest;
import com.talentFlow.data.dto.CourseResponse;

import java.util.List;
import java.util.UUID;

public interface AdminCourseService {
    List<CourseResponse> listCourses(CourseStatus status);

    CourseResponse publishCourse(UUID courseId, User actor);

    CourseResponse unpublishCourse(UUID courseId, User actor);

    CourseResponse archiveCourse(UUID courseId, User actor);

    CourseResponse assignInstructors(UUID courseId, AssignInstructorsRequest request, User actor);

    int bulkEnrollCohort(UUID courseId, UUID cohortId, User actor);

    int bulkEnrollTeam(UUID courseId, UUID teamId, User actor);

    void revokeEnrollment(UUID courseId, UUID userId, User actor);
}
