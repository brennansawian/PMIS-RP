package com.nic.nerie.m_course_academics.controller;

import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.json.simple.JSONArray;
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
import com.nic.nerie.m_course_academics.model.M_Course_Academics;
import com.nic.nerie.m_course_academics.service.M_Course_AcademicsService;
import com.nic.nerie.m_departments.model.M_Departments;
import com.nic.nerie.m_departments.service.M_DepartmentsService;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/course-academics")
public class M_Course_AcademicsController {
    private final M_Course_AcademicsService mCourseAcademicsService;
    private final MT_UserloginService mtUserloginService;
    private final M_DepartmentsService mDepartmentsService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public M_Course_AcademicsController(
        M_Course_AcademicsService mCourseAcademicsService, 
        MT_UserloginService mtUserloginService, 
        M_DepartmentsService mDepartmentsService,
        M_ProcessesService mProcessesService) {
        this.mCourseAcademicsService = mCourseAcademicsService;
        this.mtUserloginService = mtUserloginService;
        this.mDepartmentsService = mDepartmentsService;
        this.mProcessesService = mProcessesService;
    }

    @PostMapping("/coursesbasedondepartment")
    public ResponseEntity<List<M_Course_Academics>> getCoursesBasedOnDepartment(@RequestParam("departmentcode") String departmentcode, @RequestParam("isshortterm") String isshortterm) {
        if (departmentcode == null || departmentcode.isBlank() || isshortterm == null || isshortterm.isBlank()) {
            return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        
        return ResponseEntity.ok(mCourseAcademicsService.getcoursesbasedondepartment(departmentcode, isshortterm));
    }

    @PostMapping("/list-by-departmentcode")
    public ResponseEntity<List<Object[]>> getCoursesBasedOnDepartmentCode(@RequestParam("departmentcode") String departmentcode) {
        MT_Userlogin user = null;

        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
        } finally {
            if (user == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            else if (!user.getRole().getRoleCode().toUpperCase().equals("A"))   // request originates from manage-alumni which is role A exclusive
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        try {
            return ResponseEntity.ok(mCourseAcademicsService.getCourseAcademicsByDepartmentcode(departmentcode));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.emptyList());
        }
    }

    @PostMapping("/generateacademicyearbyduration")
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONArray> generateAcademicYearByDuration(@RequestParam("duration") String duration) {
        JSONArray res = new JSONArray();
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR) + 2;

        for (int i = 0; i < 10; i++) {
            String ay = ((year - i) - Integer.valueOf(duration)) + "-" + (year - i);
            res.add(ay);
        }

        return ResponseEntity.ok(res);
    }

    @PostMapping("/getListOfCourses")
    @ResponseBody
    public List<Object[]> getlistofcourses(
            @RequestParam(value = "departmentcode", required = false) String dcode,
            Model model) {
        List<Object[]> list = null;
        list = mCourseAcademicsService.getCourseList(dcode);
        return list;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Co-ordinator faculty)
     * Manage Courses
     */
    @PostMapping("/saveMapDepartmentCourse")
    @ResponseBody
    public String saveMapDepartmentCourse(
            @RequestParam(value = "coursecode", required = false) String coursecode,
            @RequestParam(value = "coursename", required = false) String coursename,
            @RequestParam(value = "departmentcode", required = false) String departmentcode,
            @RequestParam(value = "isshortterm", required = false) String isshortterm,
            @RequestParam(value = "courseid", required = false) String courseid,
            @RequestParam(value = "courseduration", required = false) String courseduration,
            Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage("/nerie/course-academics/saveMapDepartmentCourse"), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 29)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage("/nerie/course-academics/saveMapDepartmentCourse", user.getUserid()), "json");
        }

        try {
            M_Course_Academics mcourse = new M_Course_Academics();
            mcourse.setCourseid(courseid);
            mcourse.setCoursecode(coursecode);
            mcourse.setCoursename(coursename);
            mcourse.setIsshortterm("on".equalsIgnoreCase(isshortterm) ? "1" : "0");
            mcourse.setDuration(courseduration);

            M_Departments department = mDepartmentsService.getDepartmentByCode(departmentcode);
            if (department == null) {
                return "4";
            }
            mcourse.setDepartmentcode(department);

            // Check if course already exists
            if (mCourseAcademicsService.checkAcademicCourseExist(mcourse)) {
                return "3"; // Course already exists
            }

            // Save or update course
            String response = mCourseAcademicsService.saveOrUpdateCourse(mcourse);

            //TODO: Audit Trail

            return response;

        } catch (Exception ex) {
            return "pages/error/404";        }
    }

    @PostMapping("/updateDepartmentCourse")
    @ResponseBody
    public String updateDepartmentCourse(
            @RequestParam(value = "coursecode", required = true) String coursecode,
            @RequestParam(value = "coursename", required = true) String coursename,
            @RequestParam(value = "departmentcode", required = true) String departmentcode,
            @RequestParam(value = "isshortterm", required = true) String isshortterm,
            @RequestParam(value = "courseid", required = true) String courseid,
            @RequestParam(value = "courseduration", required = true) String courseduration,
            HttpServletRequest request) {
        
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage("/nerie/course-academics/updateDepartmentCourse"), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 29)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage("/nerie/course-academics/updateDepartmentCourse", user.getUserid()), "json");
        }

        try {
            // Get existing course
            M_Course_Academics existingCourse = mCourseAcademicsService.getCourseByCode(coursecode);
            if (existingCourse == null) {
                return "5"; // Course not found
            }

            // Verify department exists
            M_Departments department = mDepartmentsService.getDepartmentByCode(departmentcode);
            if (department == null) {
                return "4"; // Department not found
            }

            // Check if the new course name conflicts with other courses (excluding current course)
            if (mCourseAcademicsService.isCourseNameTakenByOtherCourse(coursename, departmentcode, coursecode)) {
                return "3"; // Course name already used by another course
            }

            // Update course details
            existingCourse.setCourseid(courseid);
            existingCourse.setCoursename(coursename);
            existingCourse.setDepartmentcode(department);
            existingCourse.setIsshortterm("1".equals(isshortterm) ? "1" : "0");
            existingCourse.setDuration(courseduration);

            // Perform update
            String response = mCourseAcademicsService.saveOrUpdateCourse(existingCourse);

            //TODO: Audit Trail

            return response;

        } catch (Exception ex) {
            return "6"; // General update error
        }
    }

    @PostMapping("/getCoursesBasedOnDepartment")
    @ResponseBody
    public List<Object[]> getCoursesBasedOnDepartment(@RequestParam(value = "departmentcode", required = false) String departmentcode,
                                                      @RequestParam(value = "isshortterm", required = false) String isshortterm,
                                                      Model model) {
        return mCourseAcademicsService.getCoursesBasedOnDepartment(departmentcode, isshortterm);
    }

    @PostMapping("/getCoursesBasedOnDepartment2")
    @ResponseBody
    public List<Object[]> getCoursesBasedOnDepartment2(@RequestParam(value = "departmentcode", required = false) String departmentcode) {
        List<Object[]> clist = null;
        clist = mCourseAcademicsService.getCoursesBasedOnDepartmentFaculty(departmentcode);
        return clist;
    }

    @GetMapping("/getCoursesBasedOnDepartmentFaculty")
    @ResponseBody
    public List<Object[]> getCoursesBasedOnDepartmentFaculty(@RequestParam(value = "departmentcode", required = false) String departmentcode) {
        List<Object[]> clist = null;
        clist = mCourseAcademicsService.getCoursesBasedOnDepartmentFaculty(departmentcode);
        return clist;
    }
}
