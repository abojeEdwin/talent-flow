package com.talentFlow.common.service;

import com.talentFlow.common.exception.ApiException;
import com.talentFlow.data.entity.Course;
import com.talentFlow.repository.CourseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaUrlServiceImpl implements MediaUrlService {

    private final CourseRepository courseRepository;

    @Override
    public String toAccessibleMediaUrl(String rawUrl) {
        if (rawUrl == null || rawUrl.isBlank()) {
            return rawUrl;
        }

        return rawUrl;
    }

    @Override
    public String getCourseCoverImageUrl(UUID courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Course not found"));

        String coverImageUrl = course.getCoverImageUrl();
        if (coverImageUrl == null || coverImageUrl.isBlank()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Course has no cover image");
        }

        return coverImageUrl;
    }
}
