package com.nic.nerie.mt_programdetails.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.sql.DataSource;

import com.nic.nerie.m_coursecategories.model.M_CourseCategories;
import com.nic.nerie.m_coursecategories.service.M_CourseCategoriesService;
import com.nic.nerie.m_offices.model.M_Offices;
import com.nic.nerie.m_phasemoredetails.service.M_PhaseMoreDetailsService;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.nic.nerie.audittrail.service.AudittrailService;
import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.m_programs.service.M_ProgramsService;
import com.nic.nerie.mt_program_members.model.MT_ProgramMembers;
import com.nic.nerie.mt_program_members.service.MT_ProgramMembersService;
import com.nic.nerie.mt_programdetails.model.MT_ProgramDetails;
import com.nic.nerie.mt_programdetails.service.MT_ProgramDetailsService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.utils.ExceptionUtil;
import com.nic.nerie.utils.UtilCommon;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/nerie/program-details")
public class MT_ProgramDetailsController {

    private final DataSource dataSource;
    private final MT_ProgramDetailsService mtProgramDetailsService;
    private final M_ProgramsService mProgramsService;
    private final MT_ProgramMembersService mtProgramMembersService;
    private final MT_UserloginService mtUserloginService;
    private final M_ProcessesService mProcessesService;
    private final M_CourseCategoriesService mCourseCategoriesService;
    private final M_PhaseMoreDetailsService mPhaseMoreDetailsService;
    private final AudittrailService audittrailService;
    private static final Logger genericLogger = LoggerFactory.getLogger(MT_ProgramDetailsController.class);
    private static final Logger persistenceLogger = LoggerFactory.getLogger("DATA_PERSISTENCE_LOGGER");

    @Autowired
    public MT_ProgramDetailsController(
            DataSource dataSource,
            MT_ProgramDetailsService mtProgramDetailsService,
            M_ProgramsService mProgramsService,
            MT_ProgramMembersService mtProgramMembersService,
            MT_UserloginService mtUserloginService,
            M_ProcessesService mProcessesService, 
            M_CourseCategoriesService mCourseCategoriesService, 
            M_PhaseMoreDetailsService mPhaseMoreDetailsService,
            AudittrailService audittrailService) {
        this.dataSource = dataSource;
        this.mtProgramDetailsService = mtProgramDetailsService;
        this.mProgramsService = mProgramsService;
        this.mtProgramMembersService = mtProgramMembersService;
        this.mtUserloginService = mtUserloginService;
        this.mProcessesService = mProcessesService;
        this.mCourseCategoriesService = mCourseCategoriesService;
        this.mPhaseMoreDetailsService = mPhaseMoreDetailsService;
        this.audittrailService = audittrailService;
    }

    /*
     * Public endpoint
     */
    @PostMapping("/list")
    public ResponseEntity<?> getProgramDaysByPhaseid(@RequestParam("phaseid") String phaseid) {
        // validating phaseid
        if (phaseid == null || phaseid.isEmpty())
            return ResponseEntity.badRequest().body("phaseid cannot be null or empty");

        if (phaseid.trim().length() > 3)
            return ResponseEntity.badRequest().body("Invalid phaseid");

        return ResponseEntity.ok(mtProgramDetailsService.getProgramDaysByPhaseid(phaseid));
    }

    /*
     * Public endpoint
     */
    @PostMapping("/list/timetable")
    public ResponseEntity<?> getprogramtimetable(@RequestParam("phaseid") String phaseid,
            @RequestParam("programday") String programday) {
        // validating phaseid
        if (phaseid == null || phaseid.isEmpty())
            return ResponseEntity.badRequest().body("phaseid cannot be null or empty");
        if (phaseid.trim().length() > 3)
            return ResponseEntity.badRequest().body("Invalid phaseid");

        // validating programday
        if (programday == null || programday.isEmpty())
            return ResponseEntity.badRequest().body("programday cannot be null or empty");
        try {
            Integer.parseInt(programday.trim());
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body("Invalid programday");
        }

        return ResponseEntity
                .ok(mtProgramDetailsService.getProgramTimetableDetailsByPhaseidAndProgramday(phaseid, programday));
    }

    @GetMapping("/getAllProgramDetailsBasedOnProgramCode")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public ResponseEntity<JSONArray> getAllProgramDetailsBasedOnProgramCode(
            @RequestParam("programcode") String programcode) {
        JSONArray res = new JSONArray();
        try {

            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy");

            M_Programs mpg = mProgramsService.getProgram(programcode);
            if (mpg == null) {
                // return a 404
            }
            if (mpg != null) {
                res.add(mpg);
            }

            List<MT_ProgramDetails> pdet = mtProgramDetailsService.getProgramDetailsByProgramCode(programcode);
            JSONArray programDetailsArray = new JSONArray(); // Renamed to avoid confusion with outer res

            for (MT_ProgramDetails p : pdet) {
                JSONObject obj = new JSONObject();
                obj.put("phase", p.getPhaseid() != null ? p.getPhaseid().getPhaseid() : null);
                obj.put("programDetailid", p.getProgramdetailid());

                String formattedStartDate = "";
                if (p.getStartdate() != null) {
                    formattedStartDate = outputFormat.format(p.getStartdate());
                }
                obj.put("startDate", formattedStartDate);

                String formattedEndDate = "";
                if (p.getEnddate() != null) {
                    formattedEndDate = outputFormat.format(p.getEnddate());
                }
                obj.put("endDate", formattedEndDate);

                obj.put("finalized", p.getFinalized());
                obj.put("closed", p.getClosed());

                List<MT_ProgramMembers> mems = mtProgramMembersService.getProgramMembers(
                        p.getProgramcode().getProgramcode(),
                        p.getPhaseid().getPhaseid());
                JSONArray coos = new JSONArray();
                for (MT_ProgramMembers m : mems) {
                    coos.add(m.getMtuserlogin().getUsername());
                }
                obj.put("coordinator", coos);

                List<Object[]> venues = mProgramsService.getProgramVenuesAndRP(
                        p.getProgramcode().getProgramcode(),
                        p.getPhaseid().getPhaseid());
                JSONArray vens = new JSONArray();
                for (Object[] venue : venues) {
                    JSONObject venueObj = new JSONObject();
                    // venue[2] was officename, venue[4] was venuename, venue[5] was rpname
                    // Based on your snippet:
                    venueObj.put("venueNames", venue[4] != null ? venue[4].toString() : "");
                    venueObj.put("RPNames", venue[5] != null ? venue[5].toString() : "");
                    vens.add(venueObj);
                }
                obj.put("VenuesAndRP", vens);
                programDetailsArray.add(obj);
            }
            res.add(programDetailsArray);

            return ResponseEntity.ok(res);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JSONArray());
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & Z (Principal-director)
     * Endpoint tied with 'Close Program' process (processcode = 17)
     */
    @PostMapping("/phase/close")
    public ResponseEntity<String> closePhase(
            @RequestParam("phaseid") String phaseId,
            @RequestParam("closingreport") String closingReport,
            HttpServletRequest request
        ) {
        MT_Userlogin user = null;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        
        if (!(
            List.of("A", "Z").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 17)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating required parameters
        if (phaseId == null || phaseId.isEmpty())
            return ResponseEntity.badRequest().body("phaseid cannot be null or empty");
        if (phaseId.trim().length() > 3)
            return ResponseEntity.badRequest().body("Invalid phaseid");
        if (!mtProgramDetailsService.existsByPhaseid(phaseId))
            return ResponseEntity.badRequest().body("Phase with phaseid " + phaseId + " does not exist");

        // validating closingReport
        if (closingReport == null || closingReport.isEmpty())
            return ResponseEntity.badRequest().body("closingReport cannot be null or empty");
        if (closingReport.trim().length() > 300)
            return ResponseEntity.badRequest().body("closingreport must not exceed 300 characters");

        
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            if (mtProgramDetailsService.closePhase(phaseId, closingReport)) {
                persistenceLogger.info("M_Phases with phaseid {} closed successfully by userid {}", phaseId, user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(), "M_Phases with phaseid " + phaseId + " closed successfully");

                return ResponseEntity.ok("1");
            }
            
            throw new PersistenceException();
        } catch (Exception ex) {
            persistenceLogger.info("M_Phases with phaseid {} close failed by userid {}", phaseId, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "M_Phases with phaseid " + phaseId + " close failed");


            return ResponseEntity.ok("-1");
        }
    }

    // Unused method
    @PostMapping("/course/reopen")
    public ResponseEntity<String> reopenCourse(@RequestParam("coursecode") String programdetailid) {
        if (mtProgramDetailsService.reopenPhase(programdetailid))
            return ResponseEntity.ok("1");
        return ResponseEntity.ok("-1");
    }

    @PostMapping("/getMoreOngoingProgramList")
    @ResponseBody
    public List<Object[]> getMoreOngoingProgramList(Model model) {
        List<Object[]> districtlist = null;
        districtlist = mtProgramDetailsService.getMoreOngoingProgramList(0);
        return districtlist;
    }

    @PostMapping("/getMoreUpcomingProgramList")
    @ResponseBody
    public List<Object[]> getMoreUpcomingProgramList(Model model) {
        List<Object[]> districtlist = null;
        districtlist = mtProgramDetailsService.getMoreUpcomingProgramList(0);
        return districtlist;
    }

    @PostMapping("/getMoreCompletedProgramList")
    @ResponseBody
    public ResponseEntity<List<Object[]>> getmoreCompletedProgramList(Model model) {
        return ResponseEntity.ok(mtProgramDetailsService.getMoreCompletedProgramList(0));
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role Z (Principal-Director)
     * Endpoint tied with 'Manage Program' process (processcode = 7)
     */
    @PostMapping("/principal-director/accept")
    public ResponseEntity<String> acceptProgram(
            @RequestParam("file1") MultipartFile file,
            @RequestParam(value = "aprogramdetailid", required = false) String programdetailid,
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
            userRole.equalsIgnoreCase("Z") &&
            mProcessesService.isProcessGranted(user.getUsercode(), 7)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // Validating fields
        if (file == null || file.isEmpty())
            return ResponseEntity.ok("1");

        if (programdetailid == null || programdetailid.isBlank())
            return ResponseEntity.badRequest().body("aprogramdetailid field missing");

        MT_ProgramDetails programDetails = new MT_ProgramDetails();
        programDetails.setProgramdetailid(programdetailid.trim());
        programDetails.setApprovaldate(new Date());
        programDetails.setFinalized("Y");
        programDetails.setMtuserloginapproval(user);

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            programDetails.setApprovalletter(file.getBytes());
            mtProgramDetailsService.approveProgram(programDetails);
            persistenceLogger.info("MT_ProgramDetails with programdeatailid {} approved successfully by userid {}", programdetailid, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " approved successfully");

            return ResponseEntity.ok("2");
        } catch (IOException ex) {
            genericLogger.error(ex.toString());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " approve failed");

            return ResponseEntity.badRequest().body("Invalid file");
        } catch (Exception ex) {
            persistenceLogger.error("MT_ProgramDetails approve failed.\nMessage {}\nException {}\nuserid {}", ex.getMessage(), ex, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " approve failed");

            return ResponseEntity.ok("3");
        }
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role Z (Principal-Director)
     * Endpoint tied with 'Manage Program' process (processcode = 7)
     */
    @PostMapping("/principal-director/reject")
    public ResponseEntity<String> rejectProgram(
            @RequestParam("file2") MultipartFile file,
            @RequestParam("rprogramdetailid") String programdetailid,
            @RequestParam("rejectremark") String rejectremark,
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
            userRole.equalsIgnoreCase("Z") &&
            mProcessesService.isProcessGranted(user.getUsercode(), 7)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // Validating fields
        if (file == null || file.isEmpty())
            return ResponseEntity.ok("1");

        if (programdetailid == null || programdetailid.isBlank() || rejectremark == null || rejectremark.isBlank())
            return ResponseEntity.badRequest().body("Required fields are missing");

        MT_ProgramDetails programDetails = new MT_ProgramDetails();
        programDetails.setProgramdetailid(programdetailid.trim());
        programDetails.setRejectiondate(new Date());
        programDetails.setFinalized("R");
        programDetails.setMtuserloginapproval(user);
        programDetails.setRejectionremarks(rejectremark.trim());

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            programDetails.setRejectionletter(file.getBytes());
            mtProgramDetailsService.rejectProgram(programDetails);
            persistenceLogger.info("MT_ProgramDetails with programdeatailid {} rejected successfully by userid {}", programdetailid, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " rejected successfully");

            return ResponseEntity.ok("2");
        } catch (IOException ex) {
            genericLogger.error(ex.toString());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " reject failed");
            
            return ResponseEntity.badRequest().body("Invalid file");
        } catch (Exception ex) {
            persistenceLogger.error("MT_ProgramDetails reject failed.\nMessage {}\nException {}\nuserid {}", ex.getMessage(), ex, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programdetailid " + programdetailid + " reject failed");
            
            return ResponseEntity.ok("");
        }
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role Z (Principal-Director)
     * Endpoint tied with 'Manage Program' process (processcode = 7)
     */
    @GetMapping("/principal-director/delete")
    public ResponseEntity<String> deleteProgram(@RequestParam("programcode") String programcode, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            userRole.equalsIgnoreCase("Z") &&
            mProcessesService.isProcessGranted(user.getUsercode(), 7)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // Validating fields
        if (programcode == null || programcode.isBlank() || !mProgramsService.existsByProgramcode(programcode.trim()))
            return ResponseEntity.badRequest().body("Invalid programcode or Program does not exist");

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            mtProgramDetailsService.deleteProgramAndRelatedEntities(programcode.trim());
            persistenceLogger.info("MT_ProgramDetails with programcode {} deleted successfully by userid {}", programcode, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programcode " + programcode + " deleted successfully");

            return ResponseEntity.ok("1");
        } catch (Exception ex) {
            persistenceLogger.error("MT_ProgramDetails delete failed.\nMessage {}\nException {}\nuserid {}", ex.getMessage(), ex, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_programdetails with programcode " + programcode + " delete failed");

            return ResponseEntity.ok("");
        }
    }

    @GetMapping("/view-approval")
    public void viewApprovalLetter(@RequestParam("pdid") String programdetailid, HttpServletResponse response)
            throws IOException {
        MT_ProgramDetails programDetails = mtProgramDetailsService.getProgramDetailsByProgramdetailid(programdetailid);

        byte[] approvalLetter = programDetails.getApprovalletter();

        if (approvalLetter != null) {
            response.reset();
            response.setContentType("application/pdf"); // Adjust based on the file type
            response.setContentLength(approvalLetter.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(approvalLetter);
                out.flush(); // Ensure all data is sent
            } catch (IOException e) {
                response.sendRedirect("/nerie/error/500");
            }
        } else {
            response.sendRedirect("/nerie/error/404");
        }
    }

    // TODO @Abanggi: Handle IOException in ControllerAdvice
    // TODO @Abanggi: Change from NOT FOUND to NO CONTENT
    @GetMapping("/view-rejection")
    public void viewRejectionLetter(@RequestParam("pdid") String programdetailid, HttpServletResponse response) throws IOException {
        MT_ProgramDetails programDetails = mtProgramDetailsService.getProgramDetailsByProgramdetailid(programdetailid);

        byte[] rejectionLetter = programDetails.getRejectionletter();

        if (rejectionLetter != null) {
            response.reset();
            response.setContentType("application/pdf"); // Adjust based on the file type
            response.setContentLength(rejectionLetter.length);

            try (OutputStream out = response.getOutputStream()) {
                out.write(rejectionLetter);
                out.flush(); // Ensure all data is sent
            } catch (IOException e) {
                response.sendRedirect("/nerie/error/500");
            }
        } else {
            response.sendRedirect("/nerie/error/404");
        }
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role A and U (Local Admin and Coordinator/Faculty)
     * Endpoint tied to Add/Edit Program process
     */
    //TODO: Proper validation and Audit Trail
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<String> updateProgramDetails(
            @RequestParam(value = "programcode") String programcode,
            @RequestParam(value = "programname") String programname,
            @RequestParam(value = "programid", required = false) String programid,
            @RequestParam(value = "programdescription") String programdescription,
            @RequestParam(value = "evenues") String[] venues,
            @RequestParam(value = "ephasedescription") String phasedescription,
            @RequestParam(value = "estartdate") String startdate,
            @RequestParam(value = "ephaseid") String phaseid,
            @RequestParam(value = "eenddate") String enddate,
            @RequestParam(value = "elastdate") String lastdate,
            @RequestParam(value = "ecourseclosedate") String courseclosedate,
            @RequestParam(value = "ecoordinators") String[] coordinators,
            HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (AuthenticationCredentialsNotFoundException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(List.of("A", "U").contains(userRole) &&
                mProcessesService.isProcessGranted(user.getUsercode(), 2))) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
                            user.getUserid()),
                    "json");
        }

        // TODO @Toiar: Add more validations
        if (programname == null || programname.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body("3"); // Program name is required
        }

        M_Programs mprogram = new M_Programs();
        mprogram.setProgramcode(programcode);
        mprogram.setProgramname(programname);
        mprogram.setProgramid(programid);
        mprogram.setProgramdescription(programdescription);

        M_Offices mo = new M_Offices();
        mo.setOfficecode(user.getMoffices().getOfficecode());
        mprogram.setMoffices(mo);
        mprogram.setClosed("N");
        MT_Userlogin log = new MT_Userlogin();
        log.setUsercode(user.getUsercode());
        mprogram.setUsercode(log);

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        try {
            boolean res = mtProgramDetailsService.updateProgramDetails(
                    mprogram, venues, coordinators, phasedescription,
                    startdate, enddate, lastdate, courseclosedate, phaseid);

            // updateProgramDetails either returns true or throw exception never false
            if (res) {
                persistenceLogger.info("MT_ProgramDetails updated successfully by userid {}", user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(),
                        "MT_ProgramDetails updated successfully");
            }

            return ResponseEntity.ok(res ? "1" : "2");
        } catch (Exception e) {
            e.printStackTrace();
            persistenceLogger.error("MT_ProgramDetails update failed.\nMessage {}\nException {}\nuserid ", e.getMessage(), e, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "MT_ProgramDetails update failed");
            
            throw new RuntimeException(e);
        }
    }

    /*
     * Secured endpoint
     * Endpoint exclusive to role A and U (Local Admin and Coordinator/Faculty)
     * Endpoint tied to Add/Edit Program process
     */
    //TODO: Proper validation and Audit Trail
    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<String> saveProgramDetails(@RequestParam(value = "programcattwo") String programcattwo,
                                     @RequestParam(value = "programDetailProgcode") String pcode,
                                     @RequestParam(value = "programDetailPhaseID") String phaseid,
                                     @RequestParam(value = "venuesDET") String[] venues,
                                     @RequestParam(value = "startdateDET") String startdate,
                                     @RequestParam(value = "enddateDET") String enddate,
                                     @RequestParam(value = "lastdateDET") String lastdate,
                                     @RequestParam(value = "courseclosedateDET") String courseclosedate,
                                     @RequestParam(value = "programcategory") String programcategory,
                                     @RequestParam(value = "focusareas") String[] focusareas,
                                     @RequestParam(value = "targetgroup") String[] targetgroup,
                                     @RequestParam(value = "stages") String[] stages,
                                     @RequestParam(value = "budget") String budget,
                                     @RequestParam(value = "objectives") String objectives,
                                     @RequestParam(value = "methodology") String methodology,
                                     @RequestParam(value = "tools") String tools,
                                     @RequestParam(value = "kpindicators") String kpindicators,
                                     @RequestParam(value = "outcomes") String outcomes,
                                     @RequestParam(value = "localcoordinator") String localcoordinator,
                                     HttpServletRequest request) {
        String response = "";
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (AuthenticationCredentialsNotFoundException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 2))) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }
        
        try {
            HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
            
            M_CourseCategories cc = mCourseCategoriesService.getCourseCategoryById(programcategory);
            boolean res = mtProgramDetailsService.saveProgramDetails(pcode,phaseid,venues, startdate, enddate, lastdate, courseclosedate,cc,programcattwo);

            if (res) {
                persistenceLogger.info("MT_ProgramDetails saved successfully by userid {}", user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(),
                        "MT_ProgramDetails saved successfully");

                boolean res2 = mPhaseMoreDetailsService.savePhaseMoreDetails(focusareas,targetgroup,stages,budget,objectives,methodology,tools,kpindicators,outcomes,pcode,phaseid);
                if(res2) {
                    persistenceLogger.info("M_PhasesMoreDetails saved successfully by userid {}", user.getUserid());
                    audittrailService.logAuditTrail(auditMap, user.getUserid(), "M_PhasesMoreDetails saved successfully");
                    response = "2";
                }
                else {
                    persistenceLogger.error("M_PhasesMoreDetails save unsuccessful  by userid {}", user.getUserid());
                    audittrailService.logAuditTrail(auditMap, user.getUserid(), "M_PhasesMoreDetails save failed");
                    response = "4";
                }
            } else {
                persistenceLogger.error("MT_ProgramDetails save unsuccessful by userid {}", user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(), "MT_ProgramDetails save failed");
                response = "4";
            }

            if(res && !(localcoordinator.equals(""))){
                if(mtProgramMembersService.setCoordinatorAsLocalCoordinator(localcoordinator).equals("1")) {
                    persistenceLogger.info("Successfully set coordinator as local coordinator for program_memberid {} by userid {}", localcoordinator, user.getUserid());
                    audittrailService.logAuditTrail(auditMap, user.getUserid(), "Successfully set coordinator as local coordinator for program_memberid " + localcoordinator);
                    response = "2";
                }
                else {
                    persistenceLogger.info("Failed setting coordinator as local coordinator for program_memberid {} by userid {}", localcoordinator, user.getUserid());
                    audittrailService.logAuditTrail(auditMap, user.getUserid(), "Failed setting coordinator as local coordinator for program_memberid " + localcoordinator);
                    response = "4";
                }
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            persistenceLogger.error("Error happened while attempting to save MT_ProgramDetails.\nMessage {}\nException {}\nUserid {}", e.getMessage(), e, user.getUserid());
            throw new RuntimeException(e);
        }
    }
}