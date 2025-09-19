package com.nic.nerie.t_studymaterials.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_subjects.service.M_SubjectService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_faculties.model.T_Faculties;
import com.nic.nerie.t_faculties.service.T_FacultiesService;
import com.nic.nerie.t_studymaterials.model.T_StudyMaterials;
import com.nic.nerie.t_studymaterials.service.T_StudyMaterialsService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/nerie/study-materials")
public class T_StudyMaterialsController {
    private final T_StudyMaterialsService tStudyMaterialsService;
    private final MT_UserloginService mtUserloginService;
    private final M_SubjectService mSubjectService;
    private final T_FacultiesService tFacultiesService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_StudyMaterialsController(
        T_StudyMaterialsService tStudyMaterialsService, 
        MT_UserloginService mtUserloginService, 
        M_SubjectService mSubjectService, 
        T_FacultiesService tFacultiesService,
        M_ProcessesService mProcessesService) {
        this.tStudyMaterialsService = tStudyMaterialsService;
        this.mtUserloginService = mtUserloginService;
        this.mSubjectService = mSubjectService;
        this.tFacultiesService = tFacultiesService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Upload Study Materials
     */
    @GetMapping("/upload-study-materials")
    public String renderUploadStudyMaterialPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Upload Study Materials, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 37) &&
            user.getIsfaculty().equalsIgnoreCase("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Upload Study Materials, " + request.getMethod(), user.getUserid()), "page");
        }

        T_Faculties faculty = tFacultiesService.getFaculty(user.getUsercode());
        if (faculty == null) {
            return "pages/error/500?msg=User+faculty+does+not+exist";
        }

        List<Object[]> subjectList = mSubjectService.getSubjectsList(user.getUsercode());
        List<T_StudyMaterials> studyMaterials = tStudyMaterialsService.getAllStudyMaterials(faculty.getFacultyid());

        model.addAttribute("subs", subjectList);
        model.addAttribute("allmaterials", studyMaterials);
        model.addAttribute("studymaterials", new T_StudyMaterials());

        return "pages/upload-study-materials";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Upload Study Materials
     */
    @PostMapping("/upload-study-materials")
    @ResponseBody
    public String uploadStudyMaterialPost(@ModelAttribute("studymaterials") T_StudyMaterials materials,
                                          @RequestParam(name = "file1", required = false) MultipartFile file,
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
            mProcessesService.isProcessGranted(user.getUsercode(), 37) &&
            user.getIsfaculty().equalsIgnoreCase("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        try {
            T_Faculties faculty = tFacultiesService.getFaculty(user.getUsercode());

            if (faculty == null) {
                return "-1";
            }

            materials.setFacultyid(faculty);
            materials.setUploaddate(new java.util.Date());

            if (file != null && !file.isEmpty()) {
                materials.setReldoc(file.getBytes());
            } else {
                if (materials.getStudymaterialid() != null && !materials.getStudymaterialid().isEmpty()) {
                    T_StudyMaterials existing = tStudyMaterialsService.getStudyMaterialDocument(materials.getStudymaterialid());
                    if (existing != null && existing.getReldoc() != null) {
                        materials.setReldoc(existing.getReldoc());
                    }
                }
            }

            String result = tStudyMaterialsService.uploadStudyMaterial(materials);

            if (result != null && !result.isEmpty() && !result.equals("-1")) {
                return "1"; // success
            } else {
                return "-1"; //failure
            }

        } catch (IOException e) {
            e.printStackTrace();
            return "-1";
        }
    }

    @GetMapping("/getStudyMaterialsListSubject")
    @ResponseBody
    public List<Object[]> getStudyMaterialsBySubject(
            @RequestParam("subjectcode") String subjectcode,
            HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        T_Faculties faculty = tFacultiesService.getFaculty(user.getUsercode());

        return tStudyMaterialsService.getStudyMaterialsListSubjectFaculty(subjectcode, faculty.getFacultyid());
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * Download Study Materials
     */
    @GetMapping("/viewStudyMaterialDocument")
    public void viewStudyMaterialDocument(HttpServletResponse response, @RequestParam("sid") String sid) throws IOException {
        T_StudyMaterials sm = tStudyMaterialsService.getStudyMaterialDocument(sid);

        if (sm != null && sm.getReldoc() != null) {
            byte[] fileContent = sm.getReldoc();
            response.reset(); // Good practice to reset response
            response.setContentType("application/pdf");
            response.setContentLength(fileContent.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(fileContent);
                out.flush();
            } catch (IOException e) {
                System.err.println("Error writing file to output stream. " + e.getMessage());
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Study material document not found.");
        }
    }

    @GetMapping("/getstudymaterials")
    public ResponseEntity<List<T_StudyMaterials>> getStudyMaterials(@RequestParam("subjectcode") String subjectcode) {
        return ResponseEntity.ok(tStudyMaterialsService.getStudyMaterialsListSubject(subjectcode));
    }
}
