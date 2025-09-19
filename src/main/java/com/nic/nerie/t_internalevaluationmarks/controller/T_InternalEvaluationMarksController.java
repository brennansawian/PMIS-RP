package com.nic.nerie.t_internalevaluationmarks.controller;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_subjects.model.M_Subjects;
import com.nic.nerie.m_subjects.service.M_SubjectService;
import com.nic.nerie.mt_test.model.MT_Test;
import com.nic.nerie.mt_test.service.MT_TestService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_faculties.model.T_Faculties;
import com.nic.nerie.t_faculties.service.T_FacultiesService;
import com.nic.nerie.t_internalevaluationmarks.model.T_InternalEvaluationMarks;
import com.nic.nerie.t_internalevaluationmarks.service.T_InternalEvaluationMarksService;
import com.nic.nerie.t_students.model.T_Students;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/internal-evaluation-marks")
public class T_InternalEvaluationMarksController {
    private final MT_UserloginService mtUserloginService;
    private final M_SubjectService mSubjectService;
    private final MT_TestService mtTestService;
    private final T_FacultiesService tFacultiesService;
    private final T_InternalEvaluationMarksService tInternalEvaluationMarksService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_InternalEvaluationMarksController(
        MT_UserloginService mtUserloginService, 
        M_SubjectService mSubjectService, 
        MT_TestService mtTestService, 
        T_FacultiesService tFacultiesService, 
        T_InternalEvaluationMarksService tInternalEvaluationMarksService,
        M_ProcessesService mProcessesService) {
        this.mtUserloginService = mtUserloginService;
        this.mSubjectService = mSubjectService;
        this.mtTestService = mtTestService;
        this.tFacultiesService = tFacultiesService;
        this.tInternalEvaluationMarksService = tInternalEvaluationMarksService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Internal Evaluation
     */
    @GetMapping("/upload-internal-evaluation-marks")
    public String renderInternalEvaluationMarksPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Internal Evaluation, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 39) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Internal Evaluation, " + request.getMethod(), user.getUserid()), "page");
        }

        // if ("A".equals(user.getUserrole())) {
        //     model.addAttribute("layoutPath", "layouts/local-admin-layout");
        // } else {
        //     model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
        // }

        List<Object[]> subjectList = mSubjectService.getSubjectsList(user.getUsercode());
        List<Object[]> testList = mtTestService.getTestList(user.getUsercode());

        model.addAttribute("subs", subjectList);
        model.addAttribute("tests", testList);

        return "pages/upload-internal-evaluation-marks";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Internal Evaluation
     */
    // TODO @Toiar: Internal evaluation cannot be saved individually
    @PostMapping("/saveInternalEvaluation")
    @ResponseBody
    public String saveInternalEvaluation(
            @RequestParam(value = "studentids", required = false) String[] studentids,
            @RequestParam(value = "studentmarks", required = false) String[] studentmarks,
            @RequestParam(value = "internalevaluationids", required = false) String[] internalevaluationids,
            @RequestParam("subjectcode") String subjectcode,
            @RequestParam("testid") String testid,
            HttpServletRequest request) {
        String res = "-1";
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
            mProcessesService.isProcessGranted(user.getUsercode(), 39) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        try {
            Date entryDate = new Date();

            T_Faculties faculty = tFacultiesService.getFaculty(user.getUsercode());

            M_Subjects subject = new M_Subjects();
            subject.setSubjectcode(subjectcode);

            MT_Test test = new MT_Test();
            test.setTestid(testid);

            if (studentids != null && studentmarks != null) {
                for (int i = 0; i < studentids.length; i++) {
                    T_InternalEvaluationMarks ie = new T_InternalEvaluationMarks();

                    ie.setEntrydate(entryDate);
                    ie.setMarks(new BigDecimal(studentmarks[i]));
                    ie.setSubjectcode(subject);
                    ie.setFacultyid(faculty);

                    T_Students student = new T_Students();
                    student.setStudentid(studentids[i]);
                    ie.setStudentid(student);

                    ie.setTestid(test);

                    if (internalevaluationids != null && i < internalevaluationids.length &&
                            internalevaluationids[i] != null && !internalevaluationids[i].isEmpty()) {
                        ie.setInternalevaluationid(internalevaluationids[i]);
                    }

                    res = tInternalEvaluationMarksService.saveStudentInternalEvaluationMarks(ie);
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return "-1";
        }

        return res;
    }
}
