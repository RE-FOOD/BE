package com.iitp.domains.notification.repository;

import com.iitp.domains.notification.domain.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;


public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
