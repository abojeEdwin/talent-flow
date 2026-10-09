package com.talentFlow.common.service;

import com.talentFlow.auth.data.entity.User;
import com.talentFlow.data.entity.Course;
import com.talentFlow.data.Enums.EnrollmentStatus;
import com.talentFlow.service.ProgressUpdatePublisher;
import com.talentFlow.data.dto.CourseProgressUpdateMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class ProgressUpdatePublisherImpl implements ProgressUpdatePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void publish(User learner, Course course, BigDecimal progressPct, EnrollmentStatus status) {
        CourseProgressUpdateMessage message = new CourseProgressUpdateMessage(
                learner.getId(),
                course.getId(),
                progressPct,
                status.name()
        );
        messagingTemplate.convertAndSend("/topic/progress/" + learner.getId() + "/" + course.getId(), message);
    }
}
