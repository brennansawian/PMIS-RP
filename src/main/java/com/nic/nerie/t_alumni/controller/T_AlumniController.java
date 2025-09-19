package com.nic.nerie.t_alumni.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_course_academics.model.M_Course_Academics;
import com.nic.nerie.m_course_academics.service.M_Course_AcademicsService;
import com.nic.nerie.m_departments.model.M_Departments;
import com.nic.nerie.m_departments.service.M_DepartmentsService;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_alumni.model.T_Alumni;
import com.nic.nerie.t_alumni.service.T_AlumniService;
import com.nic.nerie.t_students.service.T_StudentsService;
import com.nic.nerie.utils.EmailValidator;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/alumni")
public class T_AlumniController {
    private final MT_UserloginService mtUserloginService;
    private final T_AlumniService tAlumniService;
    private final M_DepartmentsService mDepartmentsService;
    private final T_StudentsService tStudentsService;
    private final M_Course_AcademicsService mCourseAcademicsService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_AlumniController(
        MT_UserloginService mtUserloginService, 
        T_AlumniService tAlumniService, 
        M_DepartmentsService mDepartmentsService, 
        T_StudentsService tStudentsService,
        M_Course_AcademicsService mCourseAcademicsService,
        M_ProcessesService mProcessesService
    ) {
        this.mtUserloginService = mtUserloginService;
        this.tAlumniService = tAlumniService;
        this.mDepartmentsService = mDepartmentsService;
        this.tStudentsService = tStudentsService;
        this.mCourseAcademicsService = mCourseAcademicsService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role A (Local-Admin)
     */
    @PostMapping("/check-exist")
    public ResponseEntity<String> checkAlumniExistence(@RequestParam("rollno") String rollno, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (!user.getRole().getRoleCode().equalsIgnoreCase("A"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        
        if (rollno == null || rollno.isBlank() || rollno.length() > 11)
            return ResponseEntity.badRequest().body("Invalid rollno");

        try {
            if (tAlumniService.existsByRollno(rollno))
                return ResponseEntity.ok("1");
            return ResponseEntity.ok("");
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }            
    }

    @GetMapping("/detail/{alumniid}")
    public ResponseEntity<List<Object[]>> getAlumniDetail(@PathVariable("alumniid") String alumniid, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (!user.getRole().getRoleCode().equalsIgnoreCase("A"))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        if (alumniid == null || alumniid.isBlank())
            return ResponseEntity.badRequest().build();

        try {
            return ResponseEntity.ok(tAlumniService.getAlumniDetails(alumniid));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin)
     * Manage Alumni
     */
    @GetMapping("/manage")
    public String renderManageAlumniPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Manage Alumni, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        // Only Local Admin (A) can manage Alumni
        if (!(
            userRole.equalsIgnoreCase("A") &&
            mProcessesService.isProcessGranted(user.getUsercode(), 32)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Manage Alumni, " + request.getMethod(), user.getUserid()), "page");
        }

        model.addAttribute("departments", mDepartmentsService.getDepartmentList());
        model.addAttribute("alumnilist", tAlumniService.getAlumniList());
        model.addAttribute("rolllist", tStudentsService.getRollnoList());
        
        return "pages/manage-alumni";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin)
     * Manage Alumni
     */
    @PostMapping("/save")
    public ResponseEntity<String> saveAlumniDetails(
        @RequestParam(value = "alumniid", required = false) String alumniid,
        @RequestParam("departmentcode") String departmentcode,
        @RequestParam("coursecode") String coursecode,
        @RequestParam("batch") String batch,
        @RequestParam("rollno") String rollno,
        @RequestParam("fname") String fname,
        @RequestParam(value = "mname", required = false) String mname,
        @RequestParam("lname") String lname,
        @RequestParam("gender") String gender,
        @RequestParam(value = "email", required = false) String email,
        @RequestParam(value = "mobileno", required = false) String mobileno,
        @RequestParam(value = "currentoccupation", required = false) String currentoccupation,
        HttpServletRequest request
    ) {
        MT_Userlogin user;

        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        // Only Local Admin (A) can manage Alumni
        if (!(
            userRole.equalsIgnoreCase("A") &&
            mProcessesService.isProcessGranted(user.getUsercode(), 32)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        /* 
         * Validation and initialization [START]
        */
        // Validating necessary parameters
        if (
            departmentcode == null || departmentcode.isBlank() ||
            coursecode == null || coursecode.isBlank() ||
            batch == null || batch.isBlank() ||
            rollno == null || rollno.isBlank() ||
            fname == null || fname.isBlank() || 
            lname == null || lname.isBlank() || 
            gender == null || gender.isBlank()
        )
            return ResponseEntity.badRequest().body("Required parameters cannot be null or blank");

        T_Alumni newAlumni = new T_Alumni();

        // Optional parameter. Is generated new for CREATE operations.
        newAlumni.setAlumniid(alumniid != null ? alumniid.trim() : "");
        
        // Validating departmentcode
        M_Departments newAlumniDepartment = null;
        try {
            newAlumniDepartment = mDepartmentsService.getDepartmentByDepartmentcode(departmentcode);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Error retrieving department information");
        } finally {
            if (newAlumniDepartment != null)
                newAlumni.setDepartmentcode(newAlumniDepartment);
            else
                return ResponseEntity.badRequest().body("Department with departmentcode = " + departmentcode + " does not exist");
        }

        // Validating coursecode
        M_Course_Academics newAlumniCourse = null;
        try {
            newAlumniCourse = mCourseAcademicsService.getCourseAcademicsByCoursecode(coursecode);
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Error retrieving course information");
        } finally {
            if (newAlumniCourse != null)
                newAlumni.setCoursecode(newAlumniCourse);
            else
                return ResponseEntity.badRequest().body("Course with coursecode = " + departmentcode + " does not exist");
        }
        
        // Validating primitive parameters
        // Validating batch
        // batch must be in the form: 'yyyy-yyyy' 
        // TODO @Abanggi: Need more validation
        batch = batch.trim();
        if (batch.length() != 9)
            return ResponseEntity.badRequest().body("Invalid batch value");
        newAlumni.setBatch(batch);

        // Validating rollno
        rollno = rollno.trim();
        if (rollno.length() > 11)
            return ResponseEntity.badRequest().body("Invalid Student rollno");
        newAlumni.setRollno(rollno);
        
        // Validating fname
        fname = fname.trim();
        if (fname.length() > 30)
            return ResponseEntity.badRequest().body("First name must be lesser than 30 characters long");
        newAlumni.setFname(fname);

        // Validating mname
        mname = mname.trim();
        if (mname != null && mname.length() > 30)
            return ResponseEntity.badRequest().body("Middle name must be lesser than 30 characters long");
        else 
            newAlumni.setMname(mname);  // Optional parameter
        
        // Validating lname
        lname = lname.trim();
        if (lname.length() > 30)
            return ResponseEntity.badRequest().body("Last name must be lesser than 30 characters long");
        newAlumni.setLname(lname);
        
        // Validating gender
        gender = gender.toUpperCase().trim();
        if (!gender.equals("M") && !gender.equals("F") && !gender.equals("O"))        
            return ResponseEntity.badRequest().body("Invalid gender value");
        newAlumni.setGender(gender);

        // Validating email if provided
        if (email != null && !email.isBlank()) {
            email = email.trim();
            if (!EmailValidator.isEmailValid(email))
                return ResponseEntity.badRequest().body("Invalid Email address");
            newAlumni.setEmail(email);
        }

        // Validating number if provided
        if (mobileno != null && !mobileno.isBlank()) {
            mobileno = mobileno.trim();
            if (mobileno.length() != 10)
                return ResponseEntity.badRequest().body("Invalid Mobile number");
            newAlumni.setMobileno(mobileno);
        }

        // Validating currentoccupation
        if (currentoccupation != null && !currentoccupation.isBlank())
            newAlumni.setCurrentoccupation(currentoccupation.trim());
        
        /* [END] */ 
        
        T_Alumni savedAlumni = null;

        System.out.println("[Test] Alumniid = " + newAlumni.getAlumniid());

        try {
            if (newAlumni.getAlumniid() != null || !newAlumni.getAlumniid().isBlank()) {    // For new Alumni
                savedAlumni = tAlumniService.configureAndSaveTAlumni(newAlumni);
            } else  // For updating Alumni
                savedAlumni = tAlumniService.saveOrUpdateTAlumni(savedAlumni);
        } catch (Exception ex) {
            ex.printStackTrace();
        }            

        if (savedAlumni == null)
            return ResponseEntity.internalServerError().body("Error saving Alumni details. Something went wrong.");
        else    
            return ResponseEntity.ok(savedAlumni.getAlumniid());
    }
}
