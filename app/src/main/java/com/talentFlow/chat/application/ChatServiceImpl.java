package com.talentFlow.chat.application;

import com.talentFlow.admin.domain.Cohort;
import com.talentFlow.admin.domain.ProjectTeam;
import com.talentFlow.admin.domain.TeamMember;
import com.talentFlow.admin.infrastructure.repository.CohortRepository;
import com.talentFlow.admin.infrastructure.repository.ProjectTeamRepository;
import com.talentFlow.admin.infrastructure.repository.TeamMemberRepository;
import com.talentFlow.auth.domain.User;
import com.talentFlow.auth.domain.enums.RoleName;
import com.talentFlow.auth.domain.enums.UserStatus;
import com.talentFlow.auth.infrastructure.repository.UserRepository;
import com.talentFlow.chat.domain.Conversation;
import com.talentFlow.chat.domain.ConversationParticipant;
import com.talentFlow.chat.domain.Message;
import com.talentFlow.chat.domain.MessageReadReceipt;
import com.talentFlow.chat.domain.enums.ChatType;
import com.talentFlow.chat.domain.enums.MessageType;
import com.talentFlow.chat.domain.enums.ParticipantRole;
import com.talentFlow.chat.infrastructure.repository.ConversationParticipantRepository;
import com.talentFlow.chat.infrastructure.repository.ConversationRepository;
import com.talentFlow.chat.infrastructure.repository.MessageReadReceiptRepository;
import com.talentFlow.chat.infrastructure.repository.MessageRepository;
import com.talentFlow.chat.web.dto.*;
import com.talentFlow.common.exception.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.talentFlow.chat.domain.enums.ChatType.*;
import static com.talentFlow.chat.domain.enums.ParticipantRole.OWNER;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final int MAX_PARTICIPANTS = 20;

    private final UserRepository userRepository;
    private final ConversationRepository conversationRepository;
    private final ConversationParticipantRepository participantRepository;
    private final MessageRepository messageRepository;
    private final MessageReadReceiptRepository readReceiptRepository;
    private final CohortRepository cohortRepository;
    private final ProjectTeamRepository teamRepository;
    private final TeamMemberRepository teamMemberRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public Page<SearchUserResponse> searchUsers(String query, Pageable pageable) {
        return userRepository.searchActiveUsersByQuery(query, pageable)
                .map(user -> new SearchUserResponse(
                        user.getId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmail()
                ));
    }

    @Override
    @Transactional
    public ConversationResponse createDirectConversation(UUID otherUserId, User creator) {
        if (creator.getId().equals(otherUserId)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot create direct chat with yourself");
        }

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        if (otherUser.getStatus() != UserStatus.ACTIVE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User is not active");
        }

        Conversation conversation = conversationRepository.findDirectConversation(DIRECT, creator.getId(), otherUserId)
                .orElseGet(() -> createConversationEntity(DIRECT, null, creator, null, null, List.of(creator, otherUser)));

        return toConversationResponse(conversation, creator.getId());
    }

    @Override
    @Transactional
    public ConversationResponse createGroupConversation(CreateConversationRequest request, User creator) {
        ChatType type = request.getType();

        if (type == DIRECT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Use /direct/{userId} endpoint for direct messages");
        }

        if (type == COHORT_CHAT && creator.getRole() != RoleName.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can create cohort chats");
        }

        if (type == TEAM_CHAT && creator.getRole() != RoleName.ADMIN) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can create team chats");
        }

        Cohort cohort = null;
        ProjectTeam team = null;
        List<User> participants = new ArrayList<>();

        if (type == COHORT_CHAT) {
            cohort = cohortRepository.findById(request.getCohortId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Cohort not found"));

            if (conversationRepository.findByCohortId(cohort.getId()).isPresent()) {
                throw new ApiException(HttpStatus.CONFLICT, "Cohort chat already exists");
            }

            participants = getCohortUsers(cohort.getId());
        } else if (type == TEAM_CHAT) {
            team = teamRepository.findById(request.getTeamId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Team not found"));

            if (conversationRepository.findByTeamId(team.getId()).isPresent()) {
                throw new ApiException(HttpStatus.CONFLICT, "Team chat already exists");
            }

            participants = getTeamUsers(team.getId());
        } else if (type == FREE_GROUP) {
            participants.add(creator);
        }

        if (participants.size() > MAX_PARTICIPANTS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Number of participants exceeds limit of " + MAX_PARTICIPANTS);
        }

        Conversation conversation = createConversationEntity(type, request.getName(), creator, cohort, team, participants);
        return toConversationResponse(conversation, creator.getId());
    }

    @Override
    public Page<ConversationResponse> getUserConversations(User user, Pageable pageable) {
        return conversationRepository.findConversationsByUserId(user.getId(), pageable)
                .map(conversation -> toConversationResponse(conversation, user.getId()));
    }

    @Override
    public ConversationResponse getConversation(UUID conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));

        ensureParticipant(conversation.getId(), user.getId());

        return toConversationResponse(conversation, user.getId());
    }

    @Override
    public Page<MessageResponse> getMessages(UUID conversationId, User user, Pageable pageable) {
        ensureParticipant(conversationId, user.getId());

        return messageRepository.findByConversationIdOrderByCreatedAtDesc(conversationId, pageable)
                .map(message -> toMessageResponse(message, user.getId()));
    }

    @Override
    @Transactional
    public MessageResponse sendMessage(UUID conversationId, User sender, SendMessageRequest request) {
        ensureParticipant(conversationId, sender.getId());

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));

        Message replyTo = null;
        if (request.getReplyToMessageId() != null) {
            replyTo = messageRepository.findById(request.getReplyToMessageId())
                    .filter(m -> m.getConversation().getId().equals(conversationId))
                    .orElse(null);
        }

        Message message = new Message();
        message.setConversation(conversation);
        message.setSender(sender);
        message.setContent(request.getContent());
        message.setMessageType(MessageType.TEXT);
        message.setReplyToMessage(replyTo);

        message = messageRepository.save(message);

        conversation.setUpdatedAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        MessageResponse response = toMessageResponse(message, sender.getId());
        messagingTemplate.convertAndSend("/topic/chat/" + conversationId, response);

        return response;
    }

    @Override
    @Transactional
    public void markAsRead(UUID conversationId, User user) {
        ensureParticipant(conversationId, user.getId());

        List<Message> unreadMessages = messageRepository.findRecentByConversationId(
                conversationId,
                Pageable.ofSize(100)
        ).stream()
                .filter(m -> !readReceiptRepository.existsByMessageIdAndUserId(m.getId(), user.getId()))
                .toList();

        LocalDateTime now = LocalDateTime.now();
        List<UUID> messageIds = new ArrayList<>();

        for (Message message : unreadMessages) {
            MessageReadReceipt receipt = new MessageReadReceipt();
            receipt.setMessage(message);
            receipt.setUser(user);
            receipt.setReadAt(now);
            readReceiptRepository.save(receipt);
            messageIds.add(message.getId());
        }

        if (!messageIds.isEmpty()) {
            var readEvent = new ReadEventPayload(user.getId(), messageIds, now);
            messagingTemplate.convertAndSend("/topic/chat/" + conversationId + "/read", readEvent);
        }
    }

    @Override
    public ReadReceiptResponse getReadReceipts(UUID conversationId, UUID messageId) {
        ensureParticipant(conversationId, null);

        List<MessageReadReceipt> receipts = readReceiptRepository.findByMessageId(messageId);

        List<ReadReceiptResponse.ReceiptDetail> details = receipts.stream()
                .map(r -> new ReadReceiptResponse.ReceiptDetail(
                        messageId,
                        r.getUser().getId(),
                        r.getUser().getFirstName(),
                        r.getUser().getLastName(),
                        r.getReadAt()
                ))
                .toList();

        return new ReadReceiptResponse(details);
    }

    @Override
    @Transactional
    public void addParticipants(UUID conversationId, AddParticipantRequest request, User creator) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Conversation not found"));

        ensureParticipant(conversationId, creator.getId());

        if (conversation.getType() == DIRECT) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Cannot add participants to direct chat");
        }

        boolean isAdmin = participantRepository.existsByConversationIdAndUserIdAndRoleIn(
                conversationId, creator.getId(), List.of(OWNER, ParticipantRole.ADMIN));

        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only group admins can add participants");
        }

        List<ConversationParticipant> currentParticipants = participantRepository.findByConversationId(conversationId);
        int currentCount = currentParticipants.size();
        int toAdd = request.getUserIds().size();

        if (currentCount + toAdd > MAX_PARTICIPANTS) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cannot add " + toAdd + " participants. Current: " + currentCount + ", Max: " + MAX_PARTICIPANTS);
        }

        for (UUID userId : request.getUserIds()) {
            if (participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
                continue;
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found: " + userId));

            ConversationParticipant participant = new ConversationParticipant();
            participant.setConversation(conversation);
            participant.setUser(user);
            participant.setRole(ParticipantRole.MEMBER);
            participantRepository.save(participant);
        }

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId + "/participants",
                new ParticipantAddedPayload(conversationId, request.getUserIds()));
    }

    @Override
    @Transactional
    public void removeParticipant(UUID conversationId, UUID userId, User remover) {
        ensureParticipant(conversationId, remover.getId());

        ConversationParticipant participant = participantRepository.findByConversationIdAndUserId(conversationId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Participant not found"));

        boolean isAdmin = participantRepository.existsByConversationIdAndUserIdAndRoleIn(
                conversationId, remover.getId(), List.of(OWNER, ParticipantRole.ADMIN));

        boolean isSelf = remover.getId().equals(userId);

        if (!isAdmin && !isSelf) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only group admins can remove other participants");
        }

        participantRepository.delete(participant);

        messagingTemplate.convertAndSend("/topic/chat/" + conversationId + "/participants",
                new ParticipantRemovedPayload(conversationId, userId));
    }

    private Conversation createConversationEntity(ChatType type, String name, User creator, Cohort cohort,
                                          ProjectTeam team, List<User> participants) {
        Conversation conversation = new Conversation();
        conversation.setType(type);
        conversation.setName(name);
        conversation.setCreatedBy(creator);
        conversation.setCohort(cohort);
        conversation.setTeam(team);

        conversation = conversationRepository.save(conversation);

        for (User user : participants) {
            ConversationParticipant participant = new ConversationParticipant();
            participant.setConversation(conversation);
            participant.setUser(user);
            participant.setRole(OWNER);
            participantRepository.save(participant);
        }

        return conversation;
    }

    private List<User> getCohortUsers(UUID cohortId) {
        return teamMemberRepository.findByTeam_Cohort_Id(cohortId).stream()
                .map(TeamMember::getUser)
                .distinct()
                .collect(Collectors.toList());
    }

    private List<User> getTeamUsers(UUID teamId) {
        return teamMemberRepository.findByTeam_Id(teamId).stream()
                .map(TeamMember::getUser)
                .collect(Collectors.toList());
    }

    private void ensureParticipant(UUID conversationId, UUID userId) {
        if (userId != null && !participantRepository.existsByConversationIdAndUserId(conversationId, userId)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "You are not a participant of this conversation");
        }
    }

    private ConversationResponse toConversationResponse(Conversation conversation, UUID currentUserId) {
        List<ConversationParticipant> participants = participantRepository.findByConversationId(conversation.getId());

        List<ConversationResponse.ParticipantResponse> participantResponses = participants.stream()
                .map(p -> new ConversationResponse.ParticipantResponse(
                        p.getUser().getId(),
                        p.getUser().getFirstName(),
                        p.getUser().getLastName(),
                        p.getUser().getEmail(),
                        p.getRole(),
                        p.getJoinedAt()
                ))
                .toList();

        int unreadCount = 0;
        if (currentUserId != null) {
            unreadCount = (int) messageRepository.findRecentByConversationId(
                    conversation.getId(), Pageable.ofSize(100)).stream()
                    .filter(m -> !readReceiptRepository.existsByMessageIdAndUserId(m.getId(), currentUserId))
                    .count();
        }

        return new ConversationResponse(
                conversation.getId(),
                conversation.getType(),
                conversation.getName(),
                conversation.getCohort() != null ? conversation.getCohort().getId() : null,
                conversation.getCohort() != null ? conversation.getCohort().getName() : null,
                conversation.getTeam() != null ? conversation.getTeam().getId() : null,
                conversation.getTeam() != null ? conversation.getTeam().getName() : null,
                participantResponses,
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                unreadCount
        );
    }

    private MessageResponse toMessageResponse(Message message, UUID currentUserId) {
        boolean isRead = readReceiptRepository.existsByMessageIdAndUserId(message.getId(), currentUserId);

        return new MessageResponse(
                message.getId(),
                message.getContent(),
                new MessageResponse.SenderResponse(
                        message.getSender().getId(),
                        message.getSender().getFirstName(),
                        message.getSender().getLastName()
                ),
                message.getReplyToMessage() != null ? message.getReplyToMessage().getId() : null,
                message.getReplyToMessage() != null ? message.getReplyToMessage().getContent() : null,
                isRead,
                message.getCreatedAt()
        );
    }

    private record ReadEventPayload(UUID userId, List<UUID> messageIds, LocalDateTime readAt) {}
    private record ParticipantAddedPayload(UUID conversationId, List<UUID> userIds) {}
    private record ParticipantRemovedPayload(UUID conversationId, UUID userId) {}
}