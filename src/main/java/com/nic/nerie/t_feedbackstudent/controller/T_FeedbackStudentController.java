package com.nic.nerie.t_feedbackstudent.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_feedbackstudent.model.T_FeedbackStudent;
import com.nic.nerie.t_feedbackstudent.service.T_FeedbackStudentService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/feedbacks")
public class T_FeedbackStudentController {
    private final T_FeedbackStudentService tFeedbackStudentService;
    private final MT_UserloginService mtUserloginService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_FeedbackStudentController(
        T_FeedbackStudentService tFeedbackStudentService, 
        MT_UserloginService mtUserloginService,
        M_ProcessesService mProcessesService) {
        this.tFeedbackStudentService = tFeedbackStudentService;
        this.mtUserloginService = mtUserloginService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role A (Local-admin) & U (Coordinator-Faculty)
     * View Feedbacks 
     */
    @GetMapping("/view-student-feedback")
    public String renderStudentFeedback(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "View Feedbacks, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 41) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "View Feedbacks, " + request.getMethod(), user.getUserid()), "page");
        }

        List<Object[]> subjectsListFeed = tFeedbackStudentService.getSubjectsListFeed(user.getUsercode());

        model.addAttribute("subjectlist", subjectsListFeed);
        
        return "pages/view-student-feedback.html";
    }

    @GetMapping("/getFeebackListBasedOnSubjectCode")
    @ResponseBody
    public List<Object[]> getFeebackListBasedOnSubjectCode(@RequestParam("subjectcode") String subjectcode, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        return tFeedbackStudentService.getStudentsFeedbackList(subjectcode, user.getUsercode());
    }
    
    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * Feedback
     */
    // TODO @Toiar: return a string denoting failed save
    @PostMapping(value = "/postsubjectfeedback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> saveFeedbackStudent(@RequestBody T_FeedbackStudent tFeedbackStudent, HttpServletRequest request) {
        MT_Userlogin user = null;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (!user.getRole().getRoleCode().equalsIgnoreCase("T")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        try {
            T_FeedbackStudent savedTFeedbackStudent = tFeedbackStudentService.saveFeedbackStudent(tFeedbackStudent);
            if (savedTFeedbackStudent != null)
                return ResponseEntity.ok().build();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return ResponseEntity.internalServerError().build();
    }
}
