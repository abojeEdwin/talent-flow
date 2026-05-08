package com.talentFlow.media.application;

import java.util.UUID;

public interface MediaUrlService {

    String toAccessibleMediaUrl(String rawUrl);

    String getCourseCoverImagePresignedUrl(UUID courseId);
}
