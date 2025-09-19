package com.nic.nerie.t_faculties.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_departments.service.M_DepartmentsService;
import com.nic.nerie.m_designations.service.M_DesignationsService;
import com.nic.nerie.m_offices.model.M_Offices;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_subjects.service.M_SubjectService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_faculties.model.T_Faculties;
import com.nic.nerie.t_faculties.service.T_FacultiesService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/faculties")
public class T_FacultiesController {
    private final MT_UserloginService mtUserloginService;
    private final M_DepartmentsService mDepartmentsService;
    private final M_DesignationsService mDesignationsService;
    private final T_FacultiesService tFacultiesService;
    private final M_SubjectService mSubjectService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_FacultiesController(
        MT_UserloginService mtUserloginService, 
        M_DepartmentsService mDepartmentsService, 
        M_DesignationsService mDesignationsService, 
        T_FacultiesService tFacultiesService, 
        M_SubjectService mSubjectService, 
        M_ProcessesService mProcessesService
    ) {
        this.mtUserloginService = mtUserloginService;
        this.mDepartmentsService = mDepartmentsService;
        this.mDesignationsService = mDesignationsService;
        this.tFacultiesService = tFacultiesService;
        this.mSubjectService = mSubjectService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role A & U
     */
    @GetMapping("/register-faculties")
    public String renderFacultiesDetails(@ModelAttribute("tfaculty") T_Faculties fac, Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Manage Faculties, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 30)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Manage Faculties, " + request.getMethod(), user.getUserid()), "page");
        }
        
        if (user.getUserrole().equals("A")) {
            model.addAttribute("faculties", mtUserloginService.getFacultyCandidates());
            model.addAttribute("departments", mDepartmentsService.getDepartments());
            model.addAttribute("designation", mDesignationsService.getDesignations());
            model.addAttribute("allfaculties", tFacultiesService.getFacultySubjectsList());
            model.addAttribute("allsubjects", mSubjectService.getAllSubjectList());
            model.addAttribute("layoutPath", "layouts/local-admin-layout");
        } else {
            model.addAttribute("faculties", mtUserloginService.getFacultyCandidatesByUser(user.getUsercode())); //get only currently logged in user
            model.addAttribute("departments", mDepartmentsService.getDepartments());
            model.addAttribute("designation", mDesignationsService.getDesignations());
            model.addAttribute("allfaculties", tFacultiesService.getFacultySubjectsListByUser(user.getUsercode())); //get only currently logged in user
            model.addAttribute("allsubjects", mSubjectService.getAllSubjectList());
            model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
        }

        return "pages/register-faculties";
    }

    /*
     * Public endpoint
     */
    @GetMapping("/facultyDetails")
    @ResponseBody
    public List<Object[]> facultydetails(@RequestParam("usercode") String usercode) {
        List<Object[]> res = null;
        try {
            res = tFacultiesService.getFacultyDetails(usercode);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    /*
     * Public endpoint
     */
    @GetMapping("/facultySubjects")
    @ResponseBody
    public ResponseEntity<List<Object[]>> facultySubjects(@RequestParam String usercode) {
        try {
            List<Object[]> subjects = mSubjectService.getSubjectsListByFaculty(usercode);
            return ResponseEntity.ok(subjects);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/createEditFaculty")
    @ResponseBody
    public ResponseEntity<String> createFaculty(@ModelAttribute("tfaculty") T_Faculties fac,
                                @RequestParam("subject") String[] subjects,
                                @RequestParam("course") String[] courses,
                                HttpServletRequest request) {
        String res = "-1";
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        } 
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 30)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        try {
            M_Offices offices = new M_Offices();
            offices.setOfficecode(user.getMoffices().getOfficecode());

            // Create/update faculty
            String facultyId = tFacultiesService.createFaculty(fac);

            if ("-1".equals(facultyId)) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
            }

            String fid = tFacultiesService.createFaculty(fac);

            if (!fid.equals("-1")) {
                if ((subjects != null)) {
                    res = tFacultiesService.saveFacultySubjects(fac.getUsercode().getUsercode(), subjects);
                }
                if ((courses != null)) {
                    res = tFacultiesService.saveFacultyCourses(fac.getUsercode().getUsercode(), courses);
                }
            }
            if (facultyId == null || "".equals(facultyId)) {
                T_Faculties facucode = tFacultiesService.getFacultyByFacultyID(fid);
                List<Integer> processids = mProcessesService.getMenuProcesses(8);
                for (Integer x : processids) {
                    String ress = mProcessesService.createUserProcess(facucode.getUsercode().getUsercode(), x);
                    if (ress.equals("-1")) {
                        break;
                    }
                }
            }
            return ResponseEntity.ok(res);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
        }
    }
}
