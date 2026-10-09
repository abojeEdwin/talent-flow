package com.talentFlow.common.service;

import java.util.UUID;

public interface MediaUrlService {

    String toAccessibleMediaUrl(String rawUrl);

    String getCourseCoverImageUrl(UUID courseId);
}
