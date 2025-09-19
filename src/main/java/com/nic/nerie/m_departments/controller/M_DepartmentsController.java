package com.nic.nerie.m_departments.controller;

import com.nic.nerie.m_departments.model.M_Departments;
import com.nic.nerie.m_departments.service.M_DepartmentsService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/nerie/departments")
public class M_DepartmentsController {
    private final MT_UserloginService mtUserloginService;
    private final M_DepartmentsService mDepartmentsService;

    @Autowired
    public M_DepartmentsController(MT_UserloginService mtUserloginService, M_DepartmentsService mDepartmentsService) {
        this.mtUserloginService = mtUserloginService;
        this.mDepartmentsService = mDepartmentsService;
    }

    @PostMapping("/saveDepartments")
    @ResponseBody
    public ResponseEntity<String> saveDepartments(@ModelAttribute("macademicsubject") M_Departments mdept, Model model) {
        try {
            // Get authenticated user
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MT_Userlogin user = mtUserloginService.getUserloginFromAuthentication(auth);

            // Check if user is authenticated and is faculty
            if (user == null || !"1".equals(user.getIsfaculty())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("-1");
            }

            // Validate department name
            String deptName = mdept.getDepartmentname();
            if (deptName == null || deptName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body("3"); // Empty department name
            }

            if (deptName.trim().length() > 100) {
                return ResponseEntity.badRequest().body("4"); // Invalid length
            }

            // Check if department already exists
            if (mDepartmentsService.checkDepartmentExist(mdept)) {
                return ResponseEntity.ok("1"); // Already exists
            }

            // Save department
            boolean isSaved = mDepartmentsService.saveDepartmentDetails(mdept);
            if (isSaved) {
                return ResponseEntity.ok("2"); // Successfully saved
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1"); // Save failed
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1"); // General error
        }
    }

}
