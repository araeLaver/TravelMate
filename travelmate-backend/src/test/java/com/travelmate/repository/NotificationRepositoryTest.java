package com.travelmate.repository;

import com.travelmate.entity.Notification;
import com.travelmate.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * NotificationRepository 테스트
 */
@DataJpaTest
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .email("test@example.com")
                .password("password")
                .nickname("테스터")
                .fullName("테스트 사용자")
                .build();
        testUser = entityManager.persist(testUser);
        entityManager.flush();
    }

    @Test
    void 사용자별_알림_조회_테스트() {
        // Given
        Notification notification1 = createNotification("알림 1", false);
        Notification notification2 = createNotification("알림 2", false);
        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.flush();

        // When
        Page<Notification> notifications = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(testUser.getId(), PageRequest.of(0, 10));

        // Then
        assertThat(notifications.getContent()).hasSize(2);
        assertThat(notifications.getContent().get(0).getTitle()).isEqualTo("알림 2");
        assertThat(notifications.getContent().get(1).getTitle()).isEqualTo("알림 1");
    }

    @Test
    void 읽지_않은_알림_조회_테스트() {
        // Given
        Notification unread1 = createNotification("읽지 않음 1", false);
        Notification unread2 = createNotification("읽지 않음 2", false);
        Notification read = createNotification("읽음", true);

        entityManager.persist(unread1);
        entityManager.persist(unread2);
        entityManager.persist(read);
        entityManager.flush();

        // When
        List<Notification> unreadNotifications = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUser.getId());

        // Then
        assertThat(unreadNotifications).hasSize(2);
        assertThat(unreadNotifications).extracting("isRead").containsOnly(false);
    }

    @Test
    void 읽지_않은_알림_개수_조회_테스트() {
        // Given
        entityManager.persist(createNotification("읽지 않음 1", false));
        entityManager.persist(createNotification("읽지 않음 2", false));
        entityManager.persist(createNotification("읽음", true));
        entityManager.flush();

        // When
        long count = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void 알림_읽음_처리_테스트() {
        // Given
        Notification notification1 = createNotification("알림 1", false);
        Notification notification2 = createNotification("알림 2", false);
        entityManager.persist(notification1);
        entityManager.persist(notification2);
        entityManager.flush();

        List<Long> ids = List.of(notification1.getId(), notification2.getId());

        // When
        int updated = notificationRepository.markAsRead(ids, testUser.getId(), LocalDateTime.now());
        entityManager.clear();

        // Then
        assertThat(updated).isEqualTo(2);

        Notification updatedNotification = notificationRepository.findById(notification1.getId()).get();
        assertThat(updatedNotification.isRead()).isTrue();
        assertThat(updatedNotification.getReadAt()).isNotNull();
    }

    @Test
    void 모든_알림_읽음_처리_테스트() {
        // Given
        entityManager.persist(createNotification("알림 1", false));
        entityManager.persist(createNotification("알림 2", false));
        entityManager.persist(createNotification("알림 3", false));
        entityManager.flush();

        // When
        int updated = notificationRepository.markAllAsRead(testUser.getId(), LocalDateTime.now());
        entityManager.clear();

        // Then
        assertThat(updated).isEqualTo(3);

        long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(testUser.getId());
        assertThat(unreadCount).isEqualTo(0);
    }

    @Test
    void 오래된_알림_삭제_테스트() {
        // Given
        Notification oldNotification = createNotification("오래된 알림", false);
        oldNotification.setCreatedAt(LocalDateTime.now().minusDays(100));

        Notification recentNotification = createNotification("최근 알림", false);

        entityManager.persist(oldNotification);
        entityManager.persist(recentNotification);
        entityManager.flush();

        // When
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(90);
        int deleted = notificationRepository.deleteOldNotifications(cutoffDate);
        entityManager.clear();

        // Then
        assertThat(deleted).isEqualTo(1);

        List<Notification> remaining = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(testUser.getId());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getTitle()).isEqualTo("최근 알림");
    }

    private Notification createNotification(String title, boolean isRead) {
        return Notification.builder()
                .user(testUser)
                .type(Notification.NotificationType.SYSTEM)
                .title(title)
                .message("테스트 메시지")
                .isRead(isRead)
                .readAt(isRead ? LocalDateTime.now() : null)
                .build();
    }
}
