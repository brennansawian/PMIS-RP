package com.nic.nerie.m_programs.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.nic.nerie.audittrail.service.AudittrailService;
import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_coursecategories.model.M_CourseCategories;
import com.nic.nerie.m_coursecategories.service.M_CourseCategoriesService;
import com.nic.nerie.m_financialyear.service.M_FinancialYearService;
import com.nic.nerie.m_offices.model.M_Offices;
import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.m_phases.service.M_PhasesService;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.m_programs.service.M_ProgramsService;
import com.nic.nerie.m_venues.service.M_VenuesService;
import com.nic.nerie.mt_program_members.model.MT_ProgramMembers;
import com.nic.nerie.mt_program_members.service.MT_ProgramMembersService;
import com.nic.nerie.mt_programdetails.model.MT_ProgramDetails;
import com.nic.nerie.mt_programdetails.service.MT_ProgramDetailsService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.t_applications.service.TApplicationsService;
import com.nic.nerie.t_feedbacks.model.T_Feedbacks;
import com.nic.nerie.utils.ExceptionUtil;
import com.nic.nerie.utils.UtilCommon;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/program")
public class M_ProgramController {

    private final MT_UserloginService mtUserloginService;
    private final TApplicationsService tApplicationsService;
    private final AudittrailService audittrailService;
    private final M_ProgramsService mProgramsService;
    private final M_CourseCategoriesService mCourseCategoriesService;
    private final M_VenuesService mVenuesService;
    private final M_FinancialYearService mFinancialYearService;
    private final MT_ProgramDetailsService mtProgramDetailsService;
    private final MT_ProgramMembersService mtProgramMembersService;
    private final M_PhasesService mPhasesService;
    private final M_ProcessesService mProcessesService;

    private static final Logger persistenceLogger = LoggerFactory.getLogger("DATA_PERSISTENCE_LOGGER");

    @Autowired
    public M_ProgramController(
            MT_UserloginService mtUserloginService,
            TApplicationsService tApplicationsService,
            AudittrailService audittrailService,
            M_ProgramsService mProgramsService,
            M_CourseCategoriesService mCourseCategoriesService,
            M_VenuesService mVenuesService,
            M_FinancialYearService mFinancialYearService,
            MT_ProgramDetailsService mtProgramDetailsService,
            MT_ProgramMembersService mtProgramMembersService,
            M_PhasesService mPhasesService,
            M_ProcessesService mProcessesService
    ) {
        this.mtUserloginService = mtUserloginService;
        this.tApplicationsService = tApplicationsService;
        this.audittrailService = audittrailService;
        this.mProgramsService = mProgramsService;
        this.mCourseCategoriesService = mCourseCategoriesService;
        this.mVenuesService = mVenuesService;
        this.mFinancialYearService = mFinancialYearService;
        this.mtProgramDetailsService = mtProgramDetailsService;
        this.mtProgramMembersService = mtProgramMembersService;
        this.mPhasesService = mPhasesService;
        this.mProcessesService = mProcessesService;
    }
    
    /*
     * Public endpoint
     */
    @PostMapping("exists-by-programid")
    public ResponseEntity<String> checkProgramExistsByProgramid(@RequestParam("programid") String programid) {
        // validating programid
        if (programid == null || programid.isBlank())
            return ResponseEntity.badRequest().body("Required parameters are missing");
        if (programid.trim().length() > 30)
            return ResponseEntity.badRequest().body("Invalid programid");

        if (mProgramsService.existsByProgramid(programid))
            return ResponseEntity.ok("1");  // program with programid already exists
        return ResponseEntity.ok("");   // programid available
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Co-ordinator faculty)
     * Add/Edit Program
     */
    @GetMapping("/manage")
    public String renderProgramDetailsPage(Model model, HttpServletRequest request) {
        MT_Userlogin user; 
        
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Add/Edit Program, " + request.getMethod()), "page");
        }
        
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "U").contains(userRole) && 
            mProcessesService.isProcessGranted(user.getUsercode(), 2)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Add/Edit Program, " + request.getMethod(), user.getUserid()), "page");
        }

        switch (userRole) {
            case "A":
                model.addAttribute("layoutPath", "layouts/local-admin-layout");
                model.addAttribute("userRole", "A");
                break;
            case "U":
                model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
                model.addAttribute("userRole", "U");
                model.addAttribute("userCode", user.getUsercode());
                break;
        }

        model.addAttribute("mcoursecategoryList", mCourseCategoriesService.getAllCourseCategories());
        model.addAttribute("mvenueList", mVenuesService.getAllOfficeVenues(user.getMoffices().getOfficecode()));
        model.addAttribute("mcoordinatorlist", mtUserloginService.getOfficeUserForCoordinator(user.getMoffices().getOfficecode()));
        model.addAttribute("mcourselist", mProgramsService.getOfficeCourseList(user.getMoffices().getOfficecode(), "N"));
        model.addAttribute("mprogramList", mProgramsService.getPhaseCourseList(user.getMoffices().getOfficecode(), "N"));
        model.addAttribute("fylist", mFinancialYearService.getfy());

        return "pages/program-details";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role Z (Principal-Director)
     * 'Manage Program' process (processcode = 7)
     */
    @GetMapping("/principal-director/manage")
    public String renderPrincipalDirectorProgramDetailsPage(Model model, HttpServletRequest request) {
        MT_Userlogin user; 
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Manage Program, " + request.getMethod()), "page");
        }

        if (!(user.getRole().getRoleCode().equalsIgnoreCase("Z") && mProcessesService.isProcessGranted(user.getUsercode(), 7))) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Manage Program, " + request.getMethod(), user.getUserid()), "page");
        }

        model.addAttribute("layoutPath", "layouts/principal-director-layout");
        model.addAttribute("mprogramListForApproval", mProgramsService.getPhaseCourseList(user.getMoffices().getOfficecode(), "N"));

        return "pages/principal-director/manage-program";
    }

    /*
     * Public endpoint
     */
    @PostMapping("/details")
    public ResponseEntity<?> getProgramDetailsByCode(@RequestParam("programcode") String programcode) {
        // validating programcode
        if (programcode == null || programcode.isBlank())
            return ResponseEntity.badRequest().body("Required parameters missing");
        if (programcode.trim().length() > 6)
            return ResponseEntity.badRequest().body("Invalid programcode");

        return ResponseEntity.ok(mProgramsService.getProgramDetailsBasedOnCode(programcode));
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Co-ordinator faculty)
     * Endpoint tied to Add/Edit Program process
     */
    @PostMapping("/inst/save")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> saveProgram(
        @RequestParam("programcattwo") String programcattwo,
        @RequestParam("coursecategorycode") String coursecategorycode,
        @RequestParam(value = "programcode", required = false) String programcode, // hidden
        @RequestParam("programname") String programname,
        @RequestParam("programid") String programid,
        @RequestParam("programdescription") String programdescription,
        @RequestParam("phases") String phases,
        @RequestParam(value = "phasedescription", required = false) String phasedescription,
        @RequestParam("startdate") String startdate,
        @RequestParam("enddate") String enddate,
        @RequestParam("lastdate") String lastdate,
        @RequestParam("venues") List<String> venues,
        @RequestParam("coordinators") List<String> coordinators,
        @RequestParam("courseclosedate") String courseclosedate,
        HttpServletRequest request
    ) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase(); 
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (!(List.of("A", "U").contains(userRole) && mProcessesService.isProcessGranted(user.getUsercode(), 2))) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating required fields
        if (programcattwo == null || programcattwo.isBlank()
            || coursecategorycode == null || coursecategorycode.isBlank()
            || programid == null || programid.isBlank()
            || programdescription == null || programdescription.isBlank()
            || phases == null || phases.isBlank()
            || startdate == null || startdate.isBlank()
            || enddate == null || enddate.isBlank()
            || lastdate == null || lastdate.isBlank()
            || venues == null || venues.isEmpty()
            || coordinators == null || coordinators.isEmpty()
            || courseclosedate == null || courseclosedate.isBlank()
        )
            return ResponseEntity.badRequest().body("Required parameters are missing");

        if (programname == null || programname.isBlank())
            return ResponseEntity.ok("3");
        else if (mProgramsService.existsByProgramname(programname.trim()))
            return ResponseEntity.ok("1");
        
        M_CourseCategories courseCategories = mCourseCategoriesService.getByCoursecategorycode(coursecategorycode.trim());
        if (courseCategories == null)
            return ResponseEntity.badRequest().body("Course category doesn not exist");
        
        if (programcode != null && !programcode.isBlank()) 
            if (!mProgramsService.existsByProgramcode(programcode))
                return ResponseEntity.badRequest().body("Program does not exist. Update failed!");
        
        try {
            SimpleDateFormat parser = new SimpleDateFormat("dd-MM-yyyy");
            parser.parse(startdate);
            parser.parse(enddate);
            parser.parse(lastdate);
            parser.parse(courseclosedate);
        } catch (ParseException ex) {
            return ResponseEntity.badRequest().body("Invalid date formate");
        }

        M_Programs newProgram = new M_Programs();
        newProgram.setProgramcode(programcode != null ? programcode.trim() : "");
        newProgram.setProgramcattwo(programcattwo.trim());
        newProgram.setMcoursecategories(courseCategories);
        newProgram.setProgramname(programname.trim());
        newProgram.setProgramid(programid.trim());
        newProgram.setProgramdescription(programdescription.trim());
        newProgram.setUsercode(user);

        M_Offices newProgramOffice = new M_Offices();
        newProgramOffice.setOfficecode(user.getMoffices().getOfficecode());
        newProgram.setMoffices(newProgramOffice);
        newProgram.setClosed("N");
        newProgram.setClosingreport("");

        try {
            mProgramsService.saveProgramPhaseVenuesCoordinatorsProgramDetails(newProgram, venues, coordinators, phases, phasedescription, startdate, enddate, lastdate, courseclosedate);
            persistenceLogger.info("M_Program and associated entities saved successfully by userid " + user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "Program saved successfully");
            
            return ResponseEntity.ok("2");  // successfully saved
        } catch (Exception ex) {
            persistenceLogger.error("M_Program and associated entities save failed.\nMessage {}\nException {}\nuserid {}", ex.getMessage(), ex, user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "Program save unsuccessful");
            
            return ResponseEntity.ok("-1");  // something went wrong... 
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Co-ordinator faculty)
     * Endpoint tied to Add/Edit Program process
     */
    @PostMapping("/batch/save")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> saveProgramDetails(
            @RequestParam("programid") String programid,
            @RequestParam(value = "programcode", required = false) String programcode,
            @RequestParam("programname") String programname,
            @RequestParam("programdescription") String programdescription,
            @RequestParam("phases") String phases,
            @RequestParam(value = "phasedescription", required = false) String phasedescription,
            @RequestParam("coordinators") List<String> coordinators,
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
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 2)
        )) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating required fields
        // invalid programname is handled uniquely in client-side
        if (
            programid == null || programid.isBlank() ||
            programdescription == null || programdescription.isBlank() ||
            phases == null || phases.isBlank() ||
            coordinators == null || coordinators.size() == 0    
        )
            return ResponseEntity.badRequest().body("Required parameters are missing");

        // validating programid
        if (programid.trim().length() > 30)
            return ResponseEntity.badRequest().body("programid must not exceed 30 characters");

        // validating programcode
        // only for updating...
        if (programcode != null && !programcode.isBlank())
            if (!mProgramsService.existsByProgramcode(programcode.trim()))
                return ResponseEntity.badRequest().body("Program does not exist. Update failed.");

        // validating programname
        if (programname == null || programname.isBlank())
            return ResponseEntity.ok("3");
        if (mProgramsService.existsByProgramname(programname))   
            return ResponseEntity.ok("1");  // programname is already taken

        // validating programdescription
        // saved as text in db
        // client-side capped programdescription at 500 characters
        if (programdescription.trim().length() > 500)
            return ResponseEntity.badRequest().body("programdescription must not exceed 500 characters"); 

        // validating phases
        if (!phases.trim().equals("Yes") && !phases.trim().equals("No"))
            return ResponseEntity.badRequest().body("Invalid phases value");

        // validating phasedescription
        // saved as text in db
        // client-side capped phasedescription at 300 characters
        if (phasedescription.trim().length() > 300)
            return ResponseEntity.badRequest().body("phasedescription must not exceed 300 characters");
        
        // preparing M_Programs for persisting
        M_Programs newProgram = new M_Programs();
        newProgram.setProgramid(programid.trim());
        newProgram.setProgramcode(programcode != null ? programcode.trim() : "");
        newProgram.setProgramname(programname.trim());
        newProgram.setProgramdescription(programdescription.trim());
        newProgram.setClosed("N");
        newProgram.setClosingreport("");
        newProgram.setUsercode(user);
        
        M_Offices newOffice = new M_Offices();
        newOffice.setOfficecode(user.getMoffices().getOfficecode());
        newProgram.setMoffices(newOffice);
        
        try {
            // saving M_Programs
            if ((newProgram = mProgramsService.saveProgramDetails(newProgram)) == null)
                throw new RuntimeException("Error saving M_Program. Checked in M_ProgramController.");
            persistenceLogger.info("M_Programs saved successfully by userid " + user.getUserid());

            // saving M_Phases
            M_Phases newPhases = new M_Phases();
            newPhases.setProgramcode(newProgram);
            newPhases.setPhaseno("1");
            if (phases.trim().equals("Yes"))
                newPhases.setPhasedescription(phasedescription.trim());
            
            if ((newPhases = mPhasesService.savePhaseDetails(newPhases)) == null)
                throw new RuntimeException("Error saving M_Phases. Checked in M_ProgramController.");
            persistenceLogger.info("M_Phases saved successfully by userid " + user.getUserid());

            // saving MT_ProgramMembers
            mtProgramMembersService.insertProgramMembersFromArraylist(coordinators, newProgram.getProgramcode(), newPhases.getPhaseid());
            persistenceLogger.info("MT_ProgramMembers saved successfully by userid " + user.getUserid());

            // saving MT_ProgramDetails
            MT_ProgramDetails newProgramDetails = new MT_ProgramDetails();
            newProgramDetails.setPhaseid(newPhases);
            newProgramDetails.setProgramcode(newProgram);
            newProgramDetails.setEntrydate(new Date());
            newProgramDetails.setClosed("N");
            newProgramDetails.setFinalized("N");
            newProgramDetails.setTtfinalized("N");
            newProgramDetails.setClosingreport("");

            if ((newProgramDetails = mtProgramDetailsService.saveProgramDetails(newProgramDetails)) == null)
                throw new RuntimeException("Error saving MT_ProgramDetails. Checked in M_ProgramController.");

            persistenceLogger.info("MT_ProgramDetails saved successfully by userid " + user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "Program and associated entities saved successfully");
            
            return ResponseEntity.ok("2");  // program successfully saved
        } catch (Exception ex) {
            persistenceLogger.error("M_Program and associated entities save failed.\nMessage {} \nException {} \nuserid {}", ex.getMessage(), ex, user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "Program and associated entities save failed");
            
            return ResponseEntity.ok("4");  // something went wrong...
        }
    }

    @PostMapping("/details/update")
    public ResponseEntity<String> updateProgramDetails(
            @RequestParam("programcode") String programcode,
            @RequestParam("ephaseid") String ephaseid,
            @RequestParam("programname") String programname,
            @RequestParam("programid") String programid,
            @RequestParam("programdescription") String programdescription,
            @RequestParam(value = "ephasedescription", required = false) String ephasedescription,
            @RequestParam("estartdate") String estartdate,
            @RequestParam("eenddate") String eenddate,
            @RequestParam("elastdate") String elastdate,
            @RequestParam("evenues") List<String> evenues,
            @RequestParam("ecoordinators") List<String> ecoordinators,
            @RequestParam("ecourseclosedate") String ecourseclosedate,
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

        if (!(
            List.of("A", "U").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 2)
        )) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        M_Programs updatedProgram = new M_Programs();
        if ((programcode == null || programcode.isBlank()) || (programname == null
                || programname.isBlank()) || (programid == null || programid.isBlank()))
            return ResponseEntity.ok("3");

        updatedProgram.setProgramcode(programcode.trim());
        updatedProgram.setProgramname(programid.trim());
        updatedProgram.setProgramid(programid.trim());
        updatedProgram.setProgramdescription(programdescription != null ? programdescription.trim() : "");
        M_Offices editedProgramOffice = new M_Offices();
        editedProgramOffice.setOfficecode(user.getMoffices().getOfficecode());
        updatedProgram.setMoffices(editedProgramOffice);
        updatedProgram.setClosed("N");
        updatedProgram.setUsercode(user);

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (mProgramsService.updateProgramPhaseProgramDetails(updatedProgram, evenues, ecoordinators, ephasedescription,
                estartdate, eenddate, elastdate, ecourseclosedate, ephaseid)) {
            persistenceLogger.info("MT_ProgramDetails and related entities updated successfully by userid {}", user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "Program details updated successfully by userid " + user.getUserid());

            return ResponseEntity.ok("1");
        }

        persistenceLogger.info("MT_ProgramDetails and related entities update failed by userid {}", user.getUserid());
        logAuditTrail(auditMap, user.getUserid(), "Program details update failed by userid " + user.getUserid());
        
        return ResponseEntity.ok("2");
    }

    @PostMapping("/ongoing/list")
    public ResponseEntity<List<Object[]>> getOngoingPrograms(@RequestParam(value = "usercode", required = false) String usercode) {
        List<Object[]> ongoingPrograms;

        if (usercode != null && !usercode.isBlank())
            ongoingPrograms = mProgramsService.getOngoingProgramsByUsercode(usercode.trim());
        else
            ongoingPrograms = mProgramsService.getOngoingPrograms();

        return ResponseEntity.ok(ongoingPrograms);
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/financial-year/list")
    public ResponseEntity<List<Object[]>> getProgramsByFinancialyear(@RequestParam("fystart") String fystart, @RequestParam("fyend") String fyend, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        } 

        if (user.getRole().getRoleCode().toUpperCase().equals("U"))
            return ResponseEntity.ok(mProgramsService.getProgramsByOfficecodeFinancialyearAndUsercode(user.getMoffices().getOfficecode(), fystart, fyend, user.getUsercode()));
        else
            return ResponseEntity.ok(mProgramsService.getProgramsByFinancialyear(user.getMoffices().getOfficecode(), fystart, fyend));
    }

    @PostMapping("/financial-year/accepted-list")
    public ResponseEntity<List<Object[]>> getAcceptedProgramsByFinancialyear(@RequestParam("fystart") String fystart, @RequestParam("fyend") String fyend) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (user.getRole().getRoleCode().toUpperCase().equals("U"))
            return ResponseEntity.ok(mProgramsService.getAcceptedProgramsBasedOnFyAndUsercode(user.getMoffices().getOfficecode(), fystart, fyend, user.getUsercode()));
        else
            return ResponseEntity.ok(mProgramsService.getAcceptedProgramsBasedOnFy(user.getMoffices().getOfficecode(), fystart, fyend));
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & U (Co-ordinator faculty)
     */
    @PostMapping("/phases/save")
    public ResponseEntity<String> savePhaseAndProgramDetails(@RequestParam("newvenues") String[] venues,
                                                   @RequestParam("newphasedescription") String phasedescription,
                                                   @RequestParam("newstartdate") String startdate,
                                                   @RequestParam("newenddate") String enddate,
                                                   @RequestParam("newlastdate") String lastdate,
                                                   @RequestParam("newcourseclosedate") String courseclosedate,
                                                   @RequestParam("newcoordinators") String[] coordinators,
                                                   @RequestParam("ongoingprograms") String pcode,
                                                   @RequestParam("programtypeco") String programtypeco,
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
            mProcessesService.isProcessGranted(user.getUsercode(), 2)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating required parameters
        if (
            venues == null || venues.length == 0 ||
            phasedescription == null || phasedescription.isBlank() ||
            startdate == null || startdate.isBlank() || 
            enddate == null || enddate.isBlank() ||
            lastdate == null || lastdate.isBlank() || 
            courseclosedate == null || courseclosedate.isBlank() ||
            coordinators == null || coordinators.length == 0 ||
            pcode == null || pcode.isBlank() ||
            programtypeco == null || programtypeco.isBlank()
        )
            return ResponseEntity.badRequest().body("Required parameters are missing");

        // validating phasedescription
        // phasedescription capped at 500 characters in client-side
        if (phasedescription.trim().length() > 500)
            return ResponseEntity.badRequest().body("phasedescription must not exceed 500 characters");
        
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (mProgramsService.savePhaseAndProgramDetails(venues, coordinators, phasedescription, startdate, enddate,
            lastdate, courseclosedate, pcode, programtypeco)) {
            persistenceLogger.info("M_Phases and MT_ProgramDetails saved successfully by userid " + user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "m_phases and mt_programdetails saved successfully");
            
            return ResponseEntity.ok("1");
        }

        persistenceLogger.info("M_Phases and MT_ProgramDetails save failed by userid {}", user.getUserid());
        logAuditTrail(auditMap, user.getUserid(), "m_phases and mt_programdetails save failed");

        return ResponseEntity.ok("2");
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & Z (Principal Director)
     * 'Close Program' process (processcode = 17)
     */
    @GetMapping("/close")
    public String renderCloseProgramPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Close Program, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "Z").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 17)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Close Program, " + request.getMethod(), user.getUserid()), "page");
        }

        switch (user.getRole().getRoleCode().toUpperCase()) {
            case "A":
                model.addAttribute("layoutPath", "layouts/local-admin-layout");
                break;
            case "Z":
                model.addAttribute("layoutPath", "layouts/principal-director-layout");
                break;
        }

        model.addAttribute("FYofunclosecourselist", mtProgramDetailsService.getFYofunclosecourse(user.getMoffices().getOfficecode()));

        return "pages/close-program";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & Z (Principal Director)
     * Endpoint tied with 'Close Program' process (processcode = 17)
     */
    @PostMapping("/close")
    public ResponseEntity<String> closeProgram(
        @RequestParam("pcode") String programCode, 
        @RequestParam("closingreport") String closingReport,
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
            List.of("A", "Z").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 17)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating required fields
        if (programCode == null || programCode.isBlank() || closingReport == null || closingReport.isBlank())
            return ResponseEntity.badRequest().body("Required parameters are missing");
        
        // validating programCode
        if (programCode.trim().length() > 6)
            return ResponseEntity.badRequest().body("Invalid programcode");
        if (!mProgramsService.existsByProgramcode(programCode))
            return ResponseEntity.badRequest().body("Program does not exist. Close failed.");

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            if (mProgramsService.closeProgram(programCode, closingReport)) {
                persistenceLogger.info("M_Program with programcode {} closed successfully by userid {}", programCode, user.getUserid());
                logAuditTrail(auditMap, user.getUserid(), "M_Program with programcode " + programCode + " closed successfully");
             
                return ResponseEntity.ok("1");
            } else 
                throw new PersistenceException();
        } catch (Exception ex) {
            persistenceLogger.error("M_Program with programcode {} close failed by userid {}", programCode, user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "M_Program with programcode " + programCode + " close failed");
    
            return ResponseEntity.ok("2");
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & Z (Principal Director)
     * 'Un-Close Program' process (processcode = 19)
     * Referred to as 'unclose' in old codebase
     */
    @GetMapping("/reopen")
    public String renderReopenProgramPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Un-Close Program, " + request.getMethod()), "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "Z").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 19)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), "Un-Close Program, " + request.getMethod(), user.getUserid()), "page");
        }

        switch (user.getRole().getRoleCode().toUpperCase()) {
            case "A":
                model.addAttribute("layoutPath", "layouts/local-admin-layout");
                break;
            case "Z":
                model.addAttribute("layoutPath", "layouts/principal-director-layout");
                break;
        }

        model.addAttribute("FYofclosecourselist", mFinancialYearService.getfy());

        return "pages/reopen-program";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role A (Local-admin) & Z (Principal Director)
     * Endpoint tied with 'Un-Close Program' process (processcode = 19)
     * When a program is closed, it has to be re-opened by re-opening one of it's phase
     * Thus, no reopen-program endpoint.
     */
    @PostMapping("/reopen-phase")
    public ResponseEntity<String> reopenPhase(@RequestParam("phid") String phaseid, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();

        if (!(
            List.of("A", "Z").contains(userRole) &&
            mProcessesService.isProcessGranted(user.getUsercode(), 19)
        )) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), user.getUserid()), "json");
        }

        // validating phaseid
        if (phaseid == null || phaseid.isBlank())
            return ResponseEntity.badRequest().body("Required parameters are missing");
        if (phaseid.trim().length() > 6)
            return ResponseEntity.badRequest().body("Invalid phaseid");

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            if (mProgramsService.unclosePhase(phaseid)) {
                persistenceLogger.info("M_Phases with phaseid {} reopened successfully by userid {}", phaseid, user.getUserid());
                logAuditTrail(auditMap, user.getUserid(), "M_Phases with phaseid " + phaseid + " reopened successfully");
    
                return ResponseEntity.ok("1");
            } else 
                throw new PersistenceException();
        } catch (Exception ex) {
            persistenceLogger.info("M_Phases with phaseid {} reopen failed.\nMessage {}\nException {}\nuserid {}", phaseid, ex.getMessage(), ex, user.getUserid());
            logAuditTrail(auditMap, user.getUserid(), "M_Phases with phaseid " + phaseid + " reopen failed");
    
            return ResponseEntity.ok("2");
        }
    }
    
    /*
     * Secured endpoint
     */
    @PostMapping("/open-course/list")
    public ResponseEntity<List<Object[]>> getfyuncloseprogramList(
        @RequestParam("fystart") String fystart, 
        @RequestParam("fyend") String fyend,
        HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        } 

        return ResponseEntity.ok(mProgramsService.getUnCloseCourseList(user.getMoffices().getOfficecode(), fystart, fyend));
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/close-course/list")
    public ResponseEntity<List<Object[]>> getfycloseprogramList(
        @RequestParam("fystart") String fystart, 
        @RequestParam("fyend") String fyend, 
        HttpServletRequest request
    ) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        } 

        return ResponseEntity.ok(mProgramsService.getCloseCourseList(user.getMoffices().getOfficecode(), fystart, fyend));
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role P (Participant)
     * Programs > Accepted/Rejected Programs
     */
    //Participant Program Controller
    @GetMapping("/accepted-rejected-programs")
    public String showAcceptRejectProgramsPage(Model model,
                                               @AuthenticationPrincipal UserDetails userDetails,
                                               RedirectAttributes redirectAttributes,
                                               HttpServletRequest request) {

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (userDetails == null) {
            logAuditTrail(auditMap, "UNKNOWN_UNAUTHENTICATED",
                    "Access programsacceptedrejected page - Failed (Unauthenticated)");
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), "Add/Edit Designation, " + request.getMethod()), "page");
        }

        String currentUsername = userDetails.getUsername();
        MT_Userlogin login = mtUserloginService.findByUserId(currentUsername);

        if (login == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User account error. Please log in again.");
            throw new DataAccessResourceFailureException("Error retrieving MT_Userlogin by username " + currentUsername);
        }

        String usercodeFromDb = login.getUsercode();

        /*if ("Y".equalsIgnoreCase(login.getIsmodified())) {
            // If profile needs to be updated, show the edit profile page with all necessary data
            model.addAttribute("userlist", login);

            T_Participants participantProfile = tParticipantsService.getSpecificParticipant(usercodeFromDb);

            if (participantProfile == null) {
                // Initialize new participant profile if it doesn't exist
                participantProfile = new T_Participants();
                participantProfile.setUsercode(usercodeFromDb);

                // Initialize all nested objects to avoid null pointers
                if (participantProfile.getMqualifications() == null) participantProfile.setMqualifications(new M_Qualifications());
                if (participantProfile.getMqualificationsubjects() == null) participantProfile.setMqualificationsubjects(new M_QualificationSubjects());
                if (participantProfile.getMdesignations() == null) participantProfile.setMdesignations(new M_Designations());
                if (participantProfile.getMstatesparticipant() == null) participantProfile.setMstatesparticipant(new M_States());
                if (participantProfile.getMparticipantofficetypes() == null) participantProfile.setMparticipantofficetypes(new M_ParticipantOfficeTypes());
                if (participantProfile.getMstates() == null) participantProfile.setMstates(new M_States());
                if (participantProfile.getMdistricts() == null) participantProfile.setMdistricts(new M_Districts());
                if (participantProfile.getMminorities() == null) participantProfile.setMminorities(new M_Minorities());
                if (participantProfile.getMcategories() == null) participantProfile.setMcategories(new M_Categories());
            }

            model.addAttribute("tparticipant", participantProfile);
            model.addAttribute("currentUserDetails", login);

            // Pre-populate dropdowns based on existing data
            if (participantProfile.getMstates() != null && participantProfile.getMstates().getStatecode() != null) {
                model.addAttribute("districtlist", mDistrictsService.getStateDistrict(participantProfile.getMstates().getStatecode()));
            } else {
                model.addAttribute("districtlist", Collections.emptyList());
            }

            if (participantProfile.getMqualifications() != null && participantProfile.getMqualifications().getQualificationcode() != null) {
                model.addAttribute("mqualificationsubjectlist",
                        mQualificationSubjectsService.getQualificationSubject(participantProfile.getMqualifications().getQualificationcode()));
            } else {
                model.addAttribute("mqualificationsubjectlist", Collections.emptyList());
            }

            // Add all dropdown lists
            model.addAttribute("mqualificationlist", mQualificationsService.getQualificationList());
            model.addAttribute("mdesignationlist", mDesignationsService.getDesignationList("Y"));
            model.addAttribute("statelist", mStatesService.getAllStates());
            model.addAttribute("participantofficetypelist", mParticipantOfficeTypesService.getAllParticipantOfficeType());
            model.addAttribute("mcategorylist", mCategoriesService.getAllCategories());
            model.addAttribute("mminoritylist", mMinoritiesService.getAllMinorities());
            model.addAttribute("ismodified", "Y");
            return "pages/t_participants/edit-participant-profile";
        } else {
            // If profile is complete, show the accepted/rejected programs page
            List<Object[]> courseList = mProgramsService.getInviteCourseList(usercodeFromDb);
            model.addAttribute("mcourselist", courseList);
            return "pages/t_participants/accept-reject-programs";
        }*/
        // If profile is complete, show the accepted/rejected programs page
        List<Object[]> courseList = mProgramsService.getInviteCourseList(usercodeFromDb);
        model.addAttribute("mcourselist", courseList);
        return "pages/t_participants/accept-reject-programs";
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role P (Participant)
     * This endpoint saves the participant's accepted program
     * Programs > Accepted/Rejected Programs
     */
    @PostMapping("/save-accept-program")
    @ResponseBody
    public ResponseEntity<String> saveAcceptProgramByParticipant(
            @RequestParam("phaseid") String phaseid,
            @RequestParam(value = "remarks", required = false, defaultValue = "") String remarks,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (userDetails == null) {
            logAuditTrail(auditMap, "UNKNOWN_UNAUTHENTICATED",
                    "Accept Program Failed (Unauthenticated)");
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        String username = userDetails.getUsername();

        MT_Userlogin login = mtUserloginService.findByUserId(username);
        if (login == null || login.getUsercode() == null) {
            logAuditTrail(auditMap, username,
                    "Accept Program Failed (User Not Found In DB)");
            throw new DataAccessResourceFailureException("Error retrieving MT_Userlogin by username " + username);
        }

        if (!login.getRole().getRoleCode().equalsIgnoreCase("P")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), login.getUserid()), "json");
        }

        String usercode = login.getUsercode();

        boolean success;
        try {
            success = tApplicationsService.acceptProgramInvitation(phaseid, usercode, remarks);

            if (success) {
                logAuditTrail(auditMap, usercode, "Accept Program Success");
                return ResponseEntity.ok("2");
            } else {
                logAuditTrail(auditMap, usercode, "Accept Program Failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Save Failed (Application not updated or already processed)");
            }
        } catch (Exception e) {
            logAuditTrail(auditMap, usercode, "Accept Program Failed (Exception)");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Save Failed (Server Error)");
        }
    }

    /*
     * Secured endpoint
     * This endpoint is exclusive to role P (Participant)
     * This endpoint saves the participant's rejected program
     * Programs > Accepted/Rejected Programs
     */
    @PostMapping("/save-reject-program")
    @ResponseBody
    public ResponseEntity<String> saveRejectProgramByParticipant(
            @RequestParam("phaseid") String phaseid,
            @RequestParam(value = "remarks", required = false, defaultValue = "") String remarks,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        if (userDetails == null) {
            logAuditTrail(auditMap, "UNKNOWN_UNAUTHENTICATED",
                    "Reject Program Failed (Unauthenticated)");
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        String username = userDetails.getUsername();
        auditMap.put("userid", username);

        MT_Userlogin login = mtUserloginService.findByUserId(username);
        if (login == null || login.getUsercode() == null) {
            logAuditTrail(auditMap, username, "Reject Program Failed (User Not Found In DB)");
            throw new DataAccessResourceFailureException("Error retrieving MT_Userlogin by username " + username);
        }
        
        if (!login.getRole().getRoleCode().equalsIgnoreCase("P")) {
            throw new MyAuthorizationDeniedException(
                ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(), login.getUserid()), "json");
        }
        
        String usercode = login.getUsercode();

        
        boolean success;
        try {
            success = tApplicationsService.rejectProgramInvitation(phaseid, usercode, remarks);

            if (success) {
                logAuditTrail(auditMap, usercode, "Reject Program Success");
                return ResponseEntity.ok("2");
            } else {
                logAuditTrail(auditMap, usercode, "Reject Program Failed");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Save Failed (Application not updated or already processed)");
            }
        } catch (Exception e) {
            logAuditTrail(auditMap, usercode, "Reject Program Failed (Exception)");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Save Failed (Server Error)");
        }
    }

    // My Programs Page Endpoint For Participants
    @GetMapping("/my-programs")
    public String showMyProgramsPage(Model model, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            throw new MyAuthenticationCredentialsNotFoundException(
                ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "page");
        }

        String currentUsername = userDetails.getUsername();

        MT_Userlogin login = mtUserloginService.findByUserId(currentUsername);

        if (login == null || login.getUsercode() == null) {
            throw new DataAccessResourceFailureException("Error retrieving MT_Userlogin by username " + currentUsername);
        }

        String usercode = login.getUsercode();

        /*if ("Y".equalsIgnoreCase(login.getIsmodified())) {
            model.addAttribute("userlist", login);
            T_Participants p = tParticipantsService.getSpecificParticipant(usercode);
            model.addAttribute("tparticipant", p != null ? p : new T_Participants());
            populateDropdownLists(model, p);
            return "pages/t_participants/edit-participant-profile";
        } else {
            try {
                List<Object[]> programList = mProgramsService.getParticipantProgramsList(usercode);
                model.addAttribute("mprogramlist", programList);
                T_Feedbacks feedbackFormObject = new T_Feedbacks();
                feedbackFormObject.setPhaseid(new M_Phases());
                model.addAttribute("tfeedback", feedbackFormObject);
                return "pages/t_participants/participant-programs";
            } catch (Exception e) { return "pages/t_participants/participant-programs"; }
        }*/
        List<Object[]> programList = mProgramsService.getParticipantProgramsList(usercode);
        model.addAttribute("mprogramlist", programList);
        T_Feedbacks feedbackFormObject = new T_Feedbacks();
        feedbackFormObject.setPhaseid(new M_Phases());
        model.addAttribute("tfeedback", feedbackFormObject);
        return "pages/t_participants/participant-programs";
    }

    @GetMapping("/getAllProgramDetails")
    @ResponseBody
    @SuppressWarnings("unchecked")
    public JSONArray getAllProgramDetailsBasedOnProgramCode(@RequestParam(required = true) String programcode) {
        JSONArray res = new JSONArray();
        try {
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy");

            // Get main program details
            M_Programs mpg = mProgramsService.getProgram(programcode);
            if (mpg != null) {
                res.add(mpg);
            }

            // Get program phase details
            List<MT_ProgramDetails> pdet = mtProgramDetailsService.getProgramDetailsByProgramCode(programcode);
            JSONArray programDetailsArray = new JSONArray();

            for (MT_ProgramDetails p : pdet) {
                JSONObject obj = new JSONObject();

                obj.put("phase", p.getPhaseid());
                obj.put("programDetailId", p.getProgramdetailid());

                // Format start date
                if (p.getStartdate() != null) {
                    obj.put("startDate", outputFormat.format(p.getStartdate()));
                } else {
                    obj.put("startDate", "");
                }

                // Format end date
                if (p.getEnddate() != null) {
                    obj.put("endDate", outputFormat.format(p.getEnddate()));
                } else {
                    obj.put("endDate", "");
                }

                obj.put("finalized", p.getFinalized());
                obj.put("closed", p.getClosed());

                // Get coordinators (example: assuming method exists)
                List<MT_ProgramMembers> mems = mtProgramMembersService.getProgramMembers(p.getProgramcode().getProgramcode(), p.getPhaseid().getPhaseid());
                JSONArray coordinatorArray = new JSONArray();
                for (MT_ProgramMembers m : mems) {
                    coordinatorArray.add(m.getMtuserlogin().getUsername());
                }
                obj.put("coordinator", coordinatorArray);

                // Get venues and RPs
                List<Object[]> venues = mProgramsService.getProgramVenuesAndRP(p.getProgramcode().getProgramcode(), p.getPhaseid().getPhaseid());
                JSONArray venueArray = new JSONArray();

                for (Object[] venue : venues) {
                    JSONObject venueObj = new JSONObject();
                    venueObj.put("venueNames", venue[4]); // venuename
                    venueObj.put("RPNames", venue[5]);   // rpname
                    venueArray.add(venueObj);
                }

                obj.put("VenuesAndRP", venueArray);
                programDetailsArray.add(obj);
            }

            res.add(programDetailsArray);

        } catch (Exception e) {
            // Log exception here if needed
            e.printStackTrace();
        }

        return res;
    }

    @PostMapping("/to-populate")
    public ResponseEntity<?> getProgramDetailsByCodeToPopulateForm(@RequestParam("programcode") String programcode) {
        if (programcode == null || programcode.isBlank())
            return ResponseEntity.badRequest().body("Required fields are missing or blank");

        return ResponseEntity.ok(mProgramsService.getProgramDetailsBasedOnCodeToPopulateForm(programcode));
    }
    
    /*
     * Secured endpoint
     */
    @PostMapping("/approved-program/list")
    public ResponseEntity<?> getApprovedProgramsByFinancialYear(@RequestParam("fystart") String fystart, @RequestParam("fyend") String fyend) {
        MT_Userlogin user;
        
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } 

        if (
            fystart == null || fystart.isBlank() ||
            fyend == null || fyend.isBlank()
        )
            return ResponseEntity.badRequest().body("Required fields are missing");

        if (user.getRole().getRoleCode().toUpperCase().equals("U"))
            return ResponseEntity.ok(mProgramsService.getApprovedProgramsByUser(user.getMoffices().getOfficecode(), fystart, fyend, "Y", user.getUsercode()));
        else
            return ResponseEntity.ok(mProgramsService.getApprovedPrograms(user.getMoffices().getOfficecode(), fystart, fyend, "Y"));
    }

    @PostMapping("/rejected-program/list")
    public ResponseEntity<?> getRejectedProgramsByFinancialYear(@RequestParam("fystart") String fystart, @RequestParam("fyend") String fyend) {
        MT_Userlogin user;
        
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } 

        if (
            fystart == null || fystart.isBlank() ||
            fyend == null || fyend.isBlank()
        )
            return ResponseEntity.badRequest().body("Required fields are missing");

        if (user.getRole().getRoleCode().toUpperCase().equals("U"))
            return ResponseEntity.ok(mProgramsService.getRejectedProgramByUser(user.getMoffices().getOfficecode(), fystart, fyend, "R", user.getUsercode()));
        else
            return ResponseEntity.ok(mProgramsService.getRejectedPrograms(user.getMoffices().getOfficecode(), fystart, fyend, "R"));
    }


    @Transactional(propagation = Propagation.REQUIRED)
    private void logAuditTrail(HashMap<String, String> auditMap, String userId, String actionTaken) {
        if (auditMap != null) {
            auditMap.put("userid", userId);
            auditMap.put("actiontaken", actionTaken);
            audittrailService.saveAuditTrail(auditMap);
        }
    }

}