package com.nic.nerie.t_notifications.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.t_notifications.model.T_Notifications;

public interface T_NotificationsRepository extends JpaRepository<T_Notifications, String> {
    @Query("from T_Notifications where receivertype=:receivertype ORDER BY entrydate DESC")
    List<T_Notifications> findByReceivertype(@Param("receivertype") String receivertype);

    @Query(value = "SELECT MAX(CAST(notificationid AS INTEGER)) FROM t_notifications", nativeQuery = true)
    Integer findMaxNotificationId();
}
