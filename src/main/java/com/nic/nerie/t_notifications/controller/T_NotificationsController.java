package com.nic.nerie.t_notifications.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/nerie/notifications")
public class T_NotificationsController {
    /*
     * Secured endpoint
     * This endpoint is exclusive to role Z (Principal-Director)
     */
    @GetMapping("/manage")
    public String renderManageNotificationsPage() {
        return "";
    }
}
