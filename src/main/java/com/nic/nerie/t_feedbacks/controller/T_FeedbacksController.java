package com.nic.nerie.t_feedbacks.controller;
import com.nic.nerie.audittrail.service.AudittrailService;
import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.m_phases.service.M_PhasesService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.t_feedbacks.service.T_FeedbackService;
import com.nic.nerie.t_feedbacksday.model.T_Feedbacksday;
import com.nic.nerie.t_feedbacksday.service.T_FeedbacksdayService;
import com.nic.nerie.t_participantfeedbacks.model.T_ParticipantFeedbacks;
import com.nic.nerie.t_participantfeedbacks.service.T_ParticipantFeedbacksService;
import com.nic.nerie.t_programtimetable.service.T_ProgramTimeTableService;
import com.nic.nerie.utils.UtilCommon;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/nerie/participant/feedback")
public class T_FeedbacksController  {
    private final T_ParticipantFeedbacksService participantFeedbacksService;
    private final MT_UserloginService userloginService;
    private final T_FeedbackService feedbackService;
    private final M_PhasesService phasesService;
    private final AudittrailService audittrailService;
    private final T_ProgramTimeTableService tProgramTimeTableService;
    private final T_FeedbacksdayService tFeedbacksdayService;
    private final T_ParticipantFeedbacksService tParticipantFeedbacksService;

    @Autowired
    public T_FeedbacksController(MT_UserloginService userloginService, T_ProgramTimeTableService programTimeTableService, M_PhasesService phasesService, T_ParticipantFeedbacksService participantFeedbacksService, T_FeedbackService feedbackService, AudittrailService audittrailService, T_ProgramTimeTableService tProgramTimeTableService, T_FeedbacksdayService tFeedbacksdayService, T_ParticipantFeedbacksService tParticipantFeedbacksService) {
        this.userloginService = userloginService;
        this.participantFeedbacksService = participantFeedbacksService;
        this.feedbackService = feedbackService;
        this.phasesService = phasesService;
        this.audittrailService = audittrailService;
        this.tProgramTimeTableService = tProgramTimeTableService;
        this.tFeedbacksdayService = tFeedbacksdayService;
        this.tParticipantFeedbacksService = tParticipantFeedbacksService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to P (Participant)
     */
    @PostMapping("/daily-feedback")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getDailySubjectsForFeedback(
            @RequestParam("programcode") String programCode,
            @RequestParam("phaseid") String phaseId,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        MT_Userlogin login = userloginService.findByUserId(userDetails.getUsername());
        if (login == null || login.getUsercode() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (!"P".equalsIgnoreCase(login.getUserrole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try {
            List<Object[]> subjects = feedbackService.getDayPrograms(new Date(), phaseId, login.getUsercode());
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to P (Participant)
     */
    @PostMapping("/save-daily-feedback")
    @ResponseBody
    public ResponseEntity<String> saveDailyFeedback(
            @RequestParam("programtimetablecode") String programTimetableCode,
            @RequestParam("feedback") String feedbackText,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("-1");
        }

        MT_Userlogin login = userloginService.findByUserId(userDetails.getUsername());
        if (login == null || login.getUsercode() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("-1");
        }

        auditMap.put("userid", userDetails.getUsername());

        if (!"P".equalsIgnoreCase(login.getUserrole())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access denied: Only Participants can submit feedback.");
        }

        if (!StringUtils.hasText(feedbackText)) {
            return ResponseEntity.badRequest().body("Feedback cannot be empty.");
        }

        try {
            boolean success = feedbackService.saveDailyFeedback(programTimetableCode, login.getUsercode(), feedbackText);

            if (success) {
                auditMap.put("actiontaken", "Save Daily Feedback Success");
                audittrailService.saveAuditTrail(auditMap);
                return ResponseEntity.ok("1");
            } else {
                auditMap.put("actiontaken", "Save Daily Feedback Failed (Service Layer)");
                audittrailService.saveAuditTrail(auditMap);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("-1");
            }
        } catch (Exception e) {
            auditMap.put("actiontaken", "Save Daily Feedback Failed (Exception)");
            audittrailService.saveAuditTrail(auditMap);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to U (Coordinator-Faculty)
     */
    @GetMapping("/daily-feedback/list")
    public String getDailySubjectsForFeedbackList(@RequestParam("phaseid") String phaseid, Model model) {
        MT_Userlogin user;

        try {
            user = userloginService.getUserloginFromAuthentication();
            if (user == null || !"U".equalsIgnoreCase(user.getUserrole())) {
                return "redirect:/error/403";
            }

            T_ParticipantFeedbacks tpfeedback = new T_ParticipantFeedbacks();

            model.addAttribute("subjectslist", tProgramTimeTableService.getSubjectDaysByPhaseId(phaseid));
            model.addAttribute("program", phasesService.getPhaseByPhaseId(phaseid));

            return "pages/list-daily-feedback";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/error/500";
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to U (Coordinator-Faculty)
     */
    @GetMapping("/daily-feedback/get")
    @ResponseBody
    public ResponseEntity<List<T_Feedbacksday>> getDayFeedbacks(@RequestParam("programtimetablecode") String programtimetablecode) {
        try {
            MT_Userlogin user = userloginService.getUserloginFromAuthentication();

            if (user == null || !"U".equalsIgnoreCase(user.getUserrole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            List<T_Feedbacksday> feedbacks = tFeedbacksdayService.getDayFeedbacksByProgramTimeTableCode(programtimetablecode);
            return ResponseEntity.ok(feedbacks);
        } catch (DataAccessResourceFailureException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to P (Participant) & U (Coordinator-Faculty)
     */
    @GetMapping("/view-overall-feedback")
    public String renderViewOverallFeedbackPage(@RequestParam("aid") String phaseid,
                                                @RequestParam(value = "usercode", required = false) String usercode,
                                                Model model) {
        try {
            MT_Userlogin user = userloginService.getUserloginFromAuthentication();

            if (user == null) {
                return "redirect:/error/403";
            }

            String userRole = user.getRole().getRoleCode().toUpperCase();

            if (!"P".equalsIgnoreCase(userRole) && !"U".equalsIgnoreCase(userRole)) {
                return "redirect:/error/403";
            }

            switch (userRole) {
                case "U":
                    model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
                    break;
                case "P":
                    model.addAttribute("layoutPath", "layouts/participant-layout");
                    break;
            }

            // Only allow feedback view for users with role U or P
            if (user.getUserrole().equals("U") || user.getUserrole().equals("P")) {
                T_ParticipantFeedbacks tpfeedback = participantFeedbacksService
                        .getFeedbackByPhaseIdAndUserCode(phaseid, (usercode != null && !usercode.isEmpty()) ? usercode : user.getUsercode());

                if (tpfeedback == null) {
                    tpfeedback = new T_ParticipantFeedbacks();
                }

                model.addAttribute("tpfeedback", tpfeedback);
                model.addAttribute("phaseid", phaseid);
                model.addAttribute("mprogramlist", phasesService.getPhaseDetailsForFeedbackByPhaseId(phaseid));
                model.addAttribute("view", true);
                model.addAttribute("login", user);

                return "pages/t_participants/overall-feedback";
            } else {
                return "redirect:/error/403";
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/error/500";
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to P (Participant)
     */
    @GetMapping("/write-overall-feedback")
    public String renderWriteOverallFeedbackPage(@RequestParam("aid") String phaseid, Model model) {
        MT_Userlogin user;

        try {
            user = userloginService.getUserloginFromAuthentication();
            if (user == null || !"P".equalsIgnoreCase(user.getUserrole())) {
                return "redirect:/error/403";
            }

            T_ParticipantFeedbacks tpfeedback = new T_ParticipantFeedbacks();

            model.addAttribute("tpfeedback", tpfeedback);
            model.addAttribute("phaseid", phaseid);
            model.addAttribute("mprogramlist", phasesService.getPhaseDetailsForFeedbackByPhaseId(phaseid));
            model.addAttribute("view", false);
            model.addAttribute("login", user);

            return "pages/t_participants/overall-feedback";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/error/500";
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive only to P (Participant)
     */
    @PostMapping("/save-overall-feedback")
    public ResponseEntity<String> saveOverallFeedback(
            @RequestParam("phid") String phaseid,
            @ModelAttribute("tpfeedback") T_ParticipantFeedbacks tpfeedback) {
        //TODO: Proper Validation
        try {
            MT_Userlogin user = userloginService.getUserloginFromAuthentication();
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized: User not logged in.");
            }

            // Check if user role is "P"
            if (!"P".equalsIgnoreCase(user.getUserrole())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Forbidden: You are not authorized to submit feedback.");
            }

            tpfeedback.setMtuserlogin(user);
            tpfeedback.setEntrydate(new Date());

            M_Phases phase = phasesService.getPhaseByPhaseId(phaseid);
            if (phase == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Invalid phase ID.");
            }
            tpfeedback.setPhaseid(phase);

            return participantFeedbacksService.saveOverallFeedback(tpfeedback);

        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing feedback: " + ex.getMessage());
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to U (Coordinator-Faculty)
     */
    @GetMapping("/overall-feedback/list")
    public String renderOverallFeedbackListPage(@RequestParam("aid") String phaseid, Model model) {
        try {
            MT_Userlogin user = userloginService.getUserloginFromAuthentication();

            if (user == null) {
                return "redirect:/error/403";
            }

            if (!"U".equalsIgnoreCase(user.getUserrole())) {
                return "redirect:/error/403";
            }

            List<T_ParticipantFeedbacks> feedbackList = tParticipantFeedbacksService.getFeedbacksByPhaseId(phaseid);

            model.addAttribute("feedbackslist", feedbackList);
            model.addAttribute("program", phasesService.getPhaseByPhaseId(phaseid));

            return "pages/list-overall-feedback";

        } catch (Exception ex) {
            ex.printStackTrace();
            return "redirect:/error/500";
        }
    }
}
