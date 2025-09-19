package com.nic.nerie.mt_test.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_subjects.service.M_SubjectService;
import com.nic.nerie.mt_test.model.MT_Test;
import com.nic.nerie.mt_test.service.MT_TestService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/nerie/tests")
public class MT_TestController {
    private final MT_UserloginService mtUserloginService;
    private final M_SubjectService mSubjectService;
    private final MT_TestService mtTestService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public MT_TestController(
        MT_UserloginService mtUserloginService, 
        M_SubjectService mSubjectService, 
        MT_TestService mtTestService,
        M_ProcessesService mProcessesService) {
        this.mtUserloginService = mtUserloginService;
        this.mSubjectService = mSubjectService;
        this.mtTestService = mtTestService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Create Tests
     */
    @GetMapping("/create-tests")
    public String renderCreateTestsPage(@ModelAttribute("testdetails") MT_Test testdetail,
                                        Model model,
                                        HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Create Tests, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 38) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Create Tests, " + request.getMethod(), user.getUserid()), "page");
        }

        // if ("A".equals(user.getUserrole())) {
        //     model.addAttribute("layoutPath", "layouts/local-admin-layout");
        // } else {
        //     model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
        // }

        List<Object[]> subjectList = mSubjectService.getSubjectsList(user.getUsercode());
        List<Object[]> testList = mtTestService.getTestList(user.getUsercode());

        model.addAttribute("subs", subjectList);
        model.addAttribute("testlist", testList);

        return "pages/create-tests";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Create Tests
     */
    @PostMapping("/saveTestDetails")
    @ResponseBody
    public String saveTestDetails(@ModelAttribute("testdetails") MT_Test testDetail,
                                  HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
            
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 38) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        testDetail.setUsercode(user);

        String result = mtTestService.createtests(testDetail, user.getUsercode());

        return result; // Should be "1" for success, "-1" for failure
    }
}
