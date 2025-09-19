package com.nic.nerie.t_notifications.service;

import com.nic.nerie.t_notifications.model.T_Notifications;
import com.nic.nerie.t_notifications.repository.T_NotificationsRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.HashSet;
import java.util.Set;

@Service
@Validated
public class T_NotificationsService {
    private final T_NotificationsRepository tNotificationsRepository;

    @Autowired
    public T_NotificationsService(T_NotificationsRepository tNotificationsRepository) {
        this.tNotificationsRepository = tNotificationsRepository;
    }

    @Transactional(readOnly = true)
    public Set<T_Notifications> findByReceivertype(@NotNull @NotBlank String receivertype) {
        try {
            return new HashSet<>(tNotificationsRepository.findByReceivertype(receivertype.trim()));
        } catch (Exception e) {
            throw new DataAccessResourceFailureException("Failed to retrieve notifications by receiver type", e);
        }
    }

    @Transactional
    public String addNotifications(T_Notifications noti) {
        try {
            if (noti.getNotificationid() == null || noti.getNotificationid().isEmpty()) {
                Integer maxId = tNotificationsRepository.findMaxNotificationId();
                if (maxId == null) {
                    maxId = 0;
                }
                noti.setNotificationid(String.valueOf(maxId + 1));
            }

            tNotificationsRepository.save(noti);
            return "1";
        } catch (Exception e) {
            System.out.println("E: addNotifications: " + e);
            return "-1";
        }
    }
}
