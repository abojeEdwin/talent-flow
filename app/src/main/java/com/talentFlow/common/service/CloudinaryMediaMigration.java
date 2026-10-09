package com.talentFlow.common.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.entity.CourseMaterial;
import com.talentFlow.data.entity.Lesson;
import com.talentFlow.data.entity.AssignmentSubmission;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class CloudinaryMediaMigration implements CommandLineRunner {

    private final Cloudinary cloudinary;
    private final MongoTemplate mongoTemplate;

    @Value("${cloudinary.migration.enabled:false}")
    private boolean enabled;

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        migrateCourses();
        migrateMaterials();
        migrateLessons();
        migrateSubmissions();
    }

    private void migrateCourses() {
        for (Course course : mongoTemplate.findAll(Course.class)) {
            String cover = migrateUrl(course.getCoverImageUrl(), "course-covers");
            String intro = migrateUrl(course.getIntroVideoUrl(), "course-videos");
            boolean changed = false;
            if (cover != null && !cover.equals(course.getCoverImageUrl())) {
                course.setCoverImageUrl(cover);
                changed = true;
            }
            if (intro != null && !intro.equals(course.getIntroVideoUrl())) {
                course.setIntroVideoUrl(intro);
                changed = true;
            }
            if (changed) {
                mongoTemplate.save(course);
            }
        }
    }

    private void migrateMaterials() {
        for (CourseMaterial material : mongoTemplate.findAll(CourseMaterial.class)) {
            String migrated = migrateUrl(material.getContentUrl(), "course-materials");
            if (migrated != null && !migrated.equals(material.getContentUrl())) {
                material.setContentUrl(migrated);
                mongoTemplate.save(material);
            }
        }
    }

    private void migrateLessons() {
        for (Lesson lesson : mongoTemplate.findAll(Lesson.class)) {
            String migrated = migrateUrl(lesson.getContentUrl(), "lessons");
            if (migrated != null && !migrated.equals(lesson.getContentUrl())) {
                lesson.setContentUrl(migrated);
                mongoTemplate.save(lesson);
            }
        }
    }

    private void migrateSubmissions() {
        for (AssignmentSubmission submission : mongoTemplate.findAll(AssignmentSubmission.class)) {
            String migrated = migrateUrl(submission.getContentUrl(), "assignment-submissions");
            if (migrated != null && !migrated.equals(submission.getContentUrl())) {
                submission.setContentUrl(migrated);
                mongoTemplate.save(submission);
            }
        }
    }

    private String migrateUrl(String url, String folder) {
        if (url == null || url.isBlank() || url.contains("res.cloudinary.com")) {
            return url;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                    url,
                    ObjectUtils.asMap("folder", folder, "resource_type", "auto")
            );
            Object secureUrl = result.get("secure_url");
            if (secureUrl == null || secureUrl.toString().isBlank()) {
                throw new IllegalStateException("Cloudinary did not return a secure URL");
            }
            return secureUrl.toString();
        } catch (Exception exception) {
            log.error("Failed to migrate media URL {}", url, exception);
            return url;
        }
    }
}
