package com.nic.nerie.t_studentassignment.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.multipart.MultipartFile;

import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_subjects.model.M_Subjects;
import com.nic.nerie.m_subjects.service.M_SubjectService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_assignmenttest.model.T_Assignmenttest;
import com.nic.nerie.t_assignmenttest.service.T_AssignmenttestService;
import com.nic.nerie.t_studentassignment.model.T_StudentAssignment;
import com.nic.nerie.t_studentassignment.service.T_StudentAssignmentService;
import com.nic.nerie.utils.ExceptionUtil;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/nerie/assignments")
public class T_StudentAssignmentController {
    private final T_StudentAssignmentService tStudentAssignmentService;
    private final MT_UserloginService mtUserloginService;
    private final T_AssignmenttestService tAssignmenttestService;
    private final M_SubjectService mSubjectService;
    private final M_ProcessesService mProcessesService;

    @Autowired
    public T_StudentAssignmentController(
        T_StudentAssignmentService tStudentAssignmentService, 
        MT_UserloginService mtUserloginService, 
        T_AssignmenttestService tAssignmenttestService, 
        M_SubjectService mSubjectService,
        M_ProcessesService mProcessesService
    ) {
        this.tStudentAssignmentService = tStudentAssignmentService;
        this.mtUserloginService = mtUserloginService;
        this.tAssignmenttestService = tAssignmenttestService;
        this.mSubjectService = mSubjectService;
        this.mProcessesService = mProcessesService;
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Assignments
     */
    @GetMapping("/upload-assignment")
    public String renderUploadAssignmentPage(@ModelAttribute("assignment") T_Assignmenttest assignmenttest, Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Assignments, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 40) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Assignments, " + request.getMethod(), user.getUserid()), "page");
        }

        List<Object[]> subs = mSubjectService.getSubjectsList(user.getUsercode());
        List<T_Assignmenttest> assignments = tAssignmenttestService.getAssignmentList(user.getUsercode());

        for (T_Assignmenttest assignment : assignments) {
            if ("LINK".equals(assignment.getSubmissiontype()) && assignment.getReldoc() != null) {
                assignment.setReldocAsString(new String(assignment.getReldoc(), StandardCharsets.UTF_8));
            }
        }
        
        model.addAttribute("alist", assignments);
        model.addAttribute("subs", subs);

        return "pages/upload-assignment";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Assignments
     */
    @PostMapping("/upload-assignment")
    @ResponseBody
    public ResponseEntity<String> uploadAssignment(
            @ModelAttribute("assignment") T_Assignmenttest assignment,
            @RequestParam(name = "file1", required = false) MultipartFile file1,
            @RequestParam(name = "submissiontype", required = false) String submissionType,
            @RequestParam(name = "submissionLink", required = false) String submissionLink,
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
            mProcessesService.isProcessGranted(user.getUsercode(), 40) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }
        
        try {
            // Set faculty who is uploading
            assignment.setUsercode(user);

            // Handle submission type: FILE or LINK
            if ("FILE".equalsIgnoreCase(submissionType) && file1 != null && !file1.isEmpty()) {
                assignment.setReldoc(file1.getBytes());
                assignment.setSubmissiontype("FILE");
            } else if ("LINK".equalsIgnoreCase(submissionType) && submissionLink != null && !submissionLink.trim().isEmpty()) {
                assignment.setReldoc(submissionLink.getBytes(StandardCharsets.UTF_8));
                assignment.setSubmissiontype("LINK");
            } else {
                return ResponseEntity.badRequest().body("-1");
            }

            // Save the assignment
            String result = tAssignmenttestService.uploadAssignment(assignment);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Coordinator-faculty)
     * isfaculty = 1
     * Assignments
     */
    @PostMapping("/editAssignment")
    @ResponseBody
    public ResponseEntity<String> editAssignment(
            @RequestParam("edassignmentid") String assignmentId,
            @RequestParam("edassignmentName") String title,
            @RequestParam("eddescription") String description,
            @RequestParam("edsubject") String subjectCode,
            @RequestParam("edassignmentdate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date uploadDate,
            @RequestParam("edlastdate") @DateTimeFormat(pattern = "dd-MM-yyyy") Date submissionDate,
            @RequestParam("edfullmark") int fullMark,
            @RequestParam("edpassmark") int passMark,
            @RequestParam(name = "submissiontype") String submissionType,
            @RequestParam(name = "submissionLink") String submissionLink,
            @RequestParam(name = "file1") MultipartFile file1,
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
            mProcessesService.isProcessGranted(user.getUsercode(), 40) &&
            user.getIsfaculty().equals("1")
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }
        
        try {
            // Fetch existing assignment
            T_Assignmenttest ed = tAssignmenttestService.getAssignmentDetails(assignmentId);
            if (ed == null) {
                return ResponseEntity.badRequest().body("-1");
            }

            // Update basic fields
            ed.setTitle(title);
            ed.setDescription(description);
            ed.setFullmark(fullMark);
            ed.setPassmark(passMark);
            ed.setSubmissiondate(submissionDate);
            ed.setUploaddate(uploadDate);

            // Set subject code
            M_Subjects subject = mSubjectService.getSubjectBySubjectCode(subjectCode);
            ed.setSubjectcode(subject);

            // Handle submission type: FILE or LINK
            if ("FILE".equalsIgnoreCase(submissionType) && file1 != null && !file1.isEmpty()) {
                ed.setReldoc(file1.getBytes());
                ed.setSubmissiontype("FILE");
            } else if ("LINK".equalsIgnoreCase(submissionType) && submissionLink != null && !submissionLink.trim().isEmpty()) {
                ed.setReldoc(submissionLink.getBytes(StandardCharsets.UTF_8));
                ed.setSubmissiontype("LINK");
            } else {
                return ResponseEntity.badRequest().body("-1");
            }

            // Set faculty who is updating
            ed.setUsercode(user);

            // Save updated assignment
            String result = tAssignmenttestService.uploadAssignment(ed);
            return ResponseEntity.ok(result);

        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
        }
    }

    @GetMapping("/view-submitted-assignment")
    public String renderViewSubmittedAssignmentPage(@ModelAttribute("studentasssignments") T_StudentAssignment tsa, String aid, Model model, HttpServletRequest request) {
        try {
            mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        T_Assignmenttest assignmentDetails = tAssignmenttestService.getAssignmentDetails(aid);
        List<T_StudentAssignment> submittedAssignments = tStudentAssignmentService.getSubmittedAssignments(aid);
        List<Object[]>  studentAssignmentsNames = tStudentAssignmentService.getSubmittedAssignmentsStudentsName(aid);

        model.addAttribute("assignmentdetails", assignmentDetails);
        model.addAttribute("studentasssignments", submittedAssignments);
        model.addAttribute("studentasssignmentsnames", studentAssignmentsNames);

        return "pages/view-submitted-assignment";
    }

    @GetMapping("/viewAssignmentDocument")
    public void viewOriginalAssignmentDocument(HttpServletResponse response, @RequestParam("fid") String fid) throws IOException {
        T_Assignmenttest ta = tAssignmenttestService.getAssignmentDetails(fid);

        if (ta != null && ta.getReldoc() != null) {
            byte[] fileContent = ta.getReldoc();
            // Set response headers
            response.reset();
            response.setContentType("application/pdf");
            response.setContentLength(fileContent.length);

            // Write the file content to the response output stream
            try (OutputStream out = response.getOutputStream()) {
                out.write(fileContent);
                out.flush(); // Ensure all data is sent
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error streaming file.");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found for assignment ID: " + fid);
        }
    }

    @GetMapping("/viewStudentUploadAssignmentDocument")
    public void viewStudentUploadedAssignmentDocument(HttpServletResponse response,
                                                      @RequestParam("fid") String assignmentTestId,
                                                      @RequestParam("sid") String studentUserCode)
                                                      throws IOException {

        T_StudentAssignment sa = tStudentAssignmentService.getStudentAssignmentDocument(assignmentTestId, studentUserCode);

        if (sa != null && sa.getReldoc() != null) {
            byte[] fileContent = sa.getReldoc();
            response.reset();
            response.setContentType("application/pdf");
            response.setContentLength(fileContent.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(fileContent);
                out.flush();
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error streaming file.");
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Submitted file not found for student: " + studentUserCode + " and assignment: " + assignmentTestId);
        }
    }

    @PostMapping("/saveStudentAssignmentMarks")
    @ResponseBody
    public ResponseEntity<String> saveStudentAssignmentMarks(
            @RequestParam("assignmentmarks") String[] assignmentmarks,
            @RequestParam("stdid") String[] stdid,
            HttpServletRequest request) {
        try {
            mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (assignmentmarks == null || stdid == null || assignmentmarks.length != stdid.length) {
            return ResponseEntity.badRequest().body("-1");
        }

        String finalResult = "1"; // Default to success
        for (int i = 0; i < stdid.length; i++) {
            String result = tStudentAssignmentService.saveStudentAssignmentMarks(stdid[i], assignmentmarks[i]);
            if ("-1".equals(result)) {
                finalResult = "-1"; // Mark failure if any update fails
            }
        }

        return ResponseEntity.ok(finalResult);
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * Assignments
     */
    @GetMapping("/viewassignments")
    public String renderAssignmentListPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "page");
        }
        List<Object[]> assignmentList = tStudentAssignmentService.getSubmitAssignmentList(user.getUsercode());
        
        if (!user.getRole().getRoleCode().equalsIgnoreCase("T")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "page");
        }
        
        for (Object[] record : assignmentList) {
            String submissionType = (String) record[11]; // Adjust index
            byte[] reldoc = (byte[]) record[3]; // Adjust index

            if ("LINK".equals(submissionType) && reldoc != null) {
                String reldocAsString = new String(reldoc, StandardCharsets.UTF_8);
                record[3] = reldocAsString; // Adjust index
            }
        }
        model.addAttribute("subs", assignmentList);
        model.addAttribute("usercode", user.getUsercode());

        return "pages/t_students/assignment-list";
    }

    @GetMapping("/viewassignmentsubmission")
    public void viewAssignmentDocument(HttpServletResponse response, @RequestParam("studentassignmentid") String studentassignmentid) throws IOException {
        T_StudentAssignment assignment = tStudentAssignmentService.getAssignmentSubmissionDetails(studentassignmentid);
        byte[] fileContent = assignment.getReldoc();

        if (fileContent != null) {
            response.reset();
            response.setContentType("application/pdf"); // Set the content type (adjust if needed)
            response.setContentLength(fileContent.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(fileContent);
                out.flush();
            }
        } else
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role T (Student)     
     * Assignments
     */
    @PostMapping("/edit-student-assignment")
    public ResponseEntity<String> editStudentAssignment(@RequestParam(required = true, value = "eassignmentfile") MultipartFile eassignmentfile, 
        @RequestParam("assignmentid") String assignmentid, @RequestParam("usercode") String usercode) {
        if (eassignmentfile == null || eassignmentfile.isEmpty() || assignmentid == null || assignmentid.isBlank() || usercode == null || usercode.isBlank()) 
            return ResponseEntity.badRequest().body("Required parameters are missing or invalid");
        
        try {
            T_StudentAssignment assignment = tStudentAssignmentService.getStudentAssignmentByAssignmentidAndUsercode(assignmentid, usercode);
            if (assignment == null)
                throw new EntityNotFoundException();
            assignment.setReldoc(eassignmentfile.getBytes());
            return tStudentAssignmentService.saveStudentAssignment(assignment) != null ? ResponseEntity.ok("1") : ResponseEntity.ok("-1");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().body("-1");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("-1");
        }
    }
}
