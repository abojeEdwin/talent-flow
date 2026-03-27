package com.talentFlow.progress.infrastructure.websocket;

import com.talentFlow.auth.domain.User;
import com.talentFlow.course.domain.Course;
import com.talentFlow.course.domain.enums.EnrollmentStatus;
import com.talentFlow.progress.application.ProgressUpdatePublisher;
import com.talentFlow.progress.web.dto.CourseProgressUpdateMessage;
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
