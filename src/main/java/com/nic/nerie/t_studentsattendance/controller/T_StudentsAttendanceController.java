package com.nic.nerie.t_studentsattendance.controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
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
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_faculties.service.T_FacultiesService;
import com.nic.nerie.t_students.model.T_Students;
import com.nic.nerie.t_students.service.T_StudentsService;
import com.nic.nerie.t_studentsattendance.model.T_StudentsAttendance;
import com.nic.nerie.t_studentsattendance.service.T_StudentsAttendanceService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/attendance")
public class T_StudentsAttendanceController {
    private final T_StudentsAttendanceService tStudentsAttendanceService;
    private final M_SubjectService mSubjectService;
    private final MT_UserloginService mtUserloginService;
    private final T_StudentsService tStudentsService;
    private final T_FacultiesService tFacultiesService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_StudentsAttendanceController(T_StudentsAttendanceService tStudentsAttendanceService,
                                          M_SubjectService mSubjectService,
                                          MT_UserloginService mtUserloginService, 
                                          T_StudentsService tStudentsService, 
                                          T_FacultiesService tFacultiesService,
                                          M_ProcessesService mProcessesService) {
        this.tStudentsAttendanceService = tStudentsAttendanceService;
        this.mSubjectService = mSubjectService;
        this.mtUserloginService = mtUserloginService;
        this.tStudentsService = tStudentsService;
        this.tFacultiesService = tFacultiesService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Upload Attendance
     */
    @GetMapping("/upload-attendance")
    public String renderUploadAttendancePage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Upload Attendance, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 35) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Upload Attendance, " + request.getMethod(), user.getUserid()), "page");
        }

        List<Object[]> subscodelist = mSubjectService.getSubjectsList(user.getUsercode());
        
        model.addAttribute("subs", subscodelist);
        
        return "pages/upload-attendance";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Upload Attendance
     */
    @PostMapping("/upload-attendance")
    @ResponseBody
    public String uploadAttendance(
            @RequestParam("subjectcode") String subjectcode,
            @RequestParam("dateselect") String dateselect,
            @RequestParam("starttime") String starttime,
            @RequestParam("endtime") String endtime,
            @RequestParam("attendancejsonstring") String attendancejsonstring,
            HttpServletRequest request) {

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
            mProcessesService.isProcessGranted(user.getUsercode(), 35) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        String res = "0";
        try {
            JSONParser parser = new JSONParser();
            JSONArray qdata = (JSONArray) parser.parse(attendancejsonstring);

            M_Subjects subject = mSubjectService.getSubjectBySubjectCode(subjectcode);
            if (subject == null) {
                return "-1"; // Subject not found
            }

            Date entry = new Date();
            Date attendancedate = new SimpleDateFormat("dd-MM-yyyy").parse(dateselect);

            DateFormat formatter = new SimpleDateFormat("HH:mm");
            Date starttime2 = formatter.parse(starttime);
            Date endtime2 = formatter.parse(endtime);

            for (int i = 0; i < qdata.size(); i++) {
                JSONObject jo = (JSONObject) parser.parse(qdata.get(i).toString());
                T_StudentsAttendance att = new T_StudentsAttendance();
                T_Students st = new T_Students();
                att.setSubjectcode(subject);
                st.setStudentid(jo.get("studentid").toString());
                att.setStudentid(st);
                att.setAttendancestatus(jo.get("pora").toString());
                att.setEntrydate(entry);
                att.setAttendancedate(attendancedate);
                att.setStarttime(starttime2);
                att.setEndtime(endtime2);
                att.setUsercode(user);

                res = tStudentsAttendanceService.saveStudentAttendance(att);

                if ("-1".equals(res)) {
                    break; // Stop if there's an error
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return "-1";
        }
        return res;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * View Student Attendance
     */ 
    @GetMapping("/view-student-attendance")
    public String renderViewStudentAttendance(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "View Student Attendance, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 36) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "View Student Attendance, " + request.getMethod(), user.getUserid()), "page");
        }

        List<Object[]> subscodelist = mSubjectService.getSubjectsList(user.getUsercode());
        List<String> timelist = tStudentsAttendanceService.getTimeList();
        List<Object[]> deptAndFaculty = tFacultiesService.getDeptAndFacultyDetails(user.getUsercode()); // You'll need to create this method

        model.addAttribute("subs", subscodelist);
        model.addAttribute("time", timelist);
        model.addAttribute("deptandfacultyname", deptAndFaculty);
        
        return "pages/view-student-attendance";
    }

    @GetMapping("/getStudentAttendance")
    @ResponseBody
    public List<Object[]> getStudentAttendance(@RequestParam("subjectcode") String subjectcode,
                                               @RequestParam("month") String month,
                                               @RequestParam("time") String time) {
        MT_Userlogin user = mtUserloginService.getUserloginFromAuthentication(
                SecurityContextHolder.getContext().getAuthentication());

        if (user == null) {
            throw new RuntimeException("User not authenticated");
        }
        return tStudentsAttendanceService.getStudentAttendanceDetails(user.getUsercode(), subjectcode, month, time);
    }

    @GetMapping("/getStudentsListBasedOnSubjectCode")
    @ResponseBody
    public List<Object[]> getStudentsListBasedOnSubjectCode(@RequestParam("subjectcode") String subjectcode) {
        M_Subjects subject = mSubjectService.getSubjectBySubjectCode(subjectcode);
        if("1".equals(subject.getIsshortterm())){
            return tStudentsService.getGeneralPhaseSubjectStudents(subjectcode);
        } else if ("0".equals(subject.getIsshortterm())) {
            if ("1".equals(subject.getIsoptional())) {
                return tStudentsService.getOptionalSemesterSubjectStudents(subjectcode);
            }
        }
        return tStudentsService.getGeneralSemesterSubjectStudents(subjectcode);
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * View My Attendance
     */
    @GetMapping("/viewattendance")
    public String renderAttendancePage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "page");
        }
        List<Object[]> subjects = mSubjectService.getStudentSubjectsList(user.getUsercode());

        if (!user.getRole().getRoleCode().equalsIgnoreCase("T")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "page");
        }

        model.addAttribute("sublist", subjects);

        return "pages/t_students/attendance-record";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * View My Attendance
     */
    @GetMapping("/getattendance")
    public ResponseEntity<List<Object[]>> getAttendance(@RequestParam("subjectcode") String subjectcode,
                                                        @RequestParam("month") String month, 
                                                        HttpServletRequest request) {
        MT_Userlogin user;

        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (!user.getRole().getRoleCode().equalsIgnoreCase("T")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        List<Object[]> attendance = tStudentsAttendanceService.getStudentAttendanceList(user.getUsercode(),
                subjectcode, month);

        return ResponseEntity.ok(attendance);
    }
}
