package com.nic.nerie.mt_resourcepersons.controller;

import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.nic.nerie.audittrail.service.AudittrailService;
import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_designations.model.M_Designations;
import com.nic.nerie.m_designations.service.M_DesignationsService;
import com.nic.nerie.m_financialyear.service.M_FinancialYearService;
import com.nic.nerie.m_honorarium.dto.HonorariumFormDTO;
import com.nic.nerie.m_honorarium.model.M_Honorarium;
import com.nic.nerie.m_honorarium.service.M_HonorariumService;
import com.nic.nerie.m_offices.model.M_Offices;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.m_qualificationcategories.model.M_QualificationCategories;
import com.nic.nerie.m_qualificationcategories.service.M_QualificationCategoriesService;
import com.nic.nerie.m_qualifications.model.M_Qualifications;
import com.nic.nerie.m_qualifications.service.M_QualificationsService;
import com.nic.nerie.m_qualificationsubjects.model.M_QualificationSubjects;
import com.nic.nerie.m_qualificationsubjects.service.M_QualificationSubjectsService;
import com.nic.nerie.m_taform.dto.TaFormDTO;
import com.nic.nerie.m_taform.model.M_Taform;
import com.nic.nerie.m_taform.service.M_TaformService;
import com.nic.nerie.mt_resourcepersons.model.MT_ResourcePersons;
import com.nic.nerie.mt_resourcepersons.service.MT_ResourcePersonsService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.mt_userloginrole.model.MT_UserloginRole;
import com.nic.nerie.mt_userloginrole.service.MT_UserloginRoleService;
import com.nic.nerie.t_conveyancecharge.model.T_ConveyanceCharge;
import com.nic.nerie.t_conveyancecharge.service.T_ConveyanceChargeService;
import com.nic.nerie.utils.EmailValidator;
import com.nic.nerie.utils.ExceptionUtil;
import com.nic.nerie.utils.RandomPasswordGenerator;
import com.nic.nerie.utils.UtilCommon;

import jakarta.persistence.PersistenceException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie/resource-persons")
public class MT_ResourcePersonsController {
    private final MT_ResourcePersonsService mtResourcePersonsService;
    private final MT_UserloginService mtUserloginService;
    private final M_DesignationsService mDesignationsService;
    private final M_QualificationsService qualificationsService;
    private final M_QualificationCategoriesService mQualificationCategoriesService;
    private final M_QualificationSubjectsService mQualificationSubjectsService;
    private final M_FinancialYearService mFinancialYearService;
    private final M_ProcessesService mProcessesService;
    private final AudittrailService audittrailService;
    private final MT_UserloginRoleService mtUserloginRoleService;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private final M_HonorariumService mHonorariumService;
    private final M_TaformService mTaformService;
    private final T_ConveyanceChargeService tconveyanceChargeService;
    private static final Logger persistenceLogger = LoggerFactory.getLogger("DATA_PERSISTENCE_LOGGER");
    
    @Autowired
    public MT_ResourcePersonsController(MT_ResourcePersonsService mtResourcePersonsService,
    MT_UserloginService mtUserloginService,
    M_QualificationsService qualificationsService,
    M_QualificationCategoriesService mQualificationCategoriesService,
    M_HonorariumService mHonorariumService,
    M_QualificationSubjectsService mQualificationSubjectsService,
    M_DesignationsService mDesignationsService,
    M_FinancialYearService mFinancialYearService,
    M_ProcessesService mProcessesService,
    AudittrailService audittrailService,
    MT_UserloginRoleService mtUserloginRoleService,
    BCryptPasswordEncoder bCryptPasswordEncoder,
    M_TaformService mTaformService,
    T_ConveyanceChargeService tconveyanceChargeService) {
        this.mtResourcePersonsService = mtResourcePersonsService;
        this.mtUserloginService = mtUserloginService;
        this.qualificationsService = qualificationsService;
        this.mQualificationCategoriesService = mQualificationCategoriesService;
        this.mQualificationSubjectsService = mQualificationSubjectsService;
        this.mDesignationsService = mDesignationsService;
        this.mFinancialYearService = mFinancialYearService;
        this.mHonorariumService = mHonorariumService;
        this.mProcessesService = mProcessesService;
        this.audittrailService = audittrailService;
        this.mtUserloginRoleService = mtUserloginRoleService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.mTaformService = mTaformService;
        this.tconveyanceChargeService = tconveyanceChargeService;
    }
    
    /*
    * Public endpoint
    */
    @PostMapping("/is-email-available")
    public ResponseEntity<String> checkEmailAvailability(@RequestParam("rpemailid") String email) {
        // validating rpemailid
        if (email == null || email.trim().isEmpty())
        return ResponseEntity.badRequest().body("rpemailid parameter is missing");
        
        if (!EmailValidator.isEmailValid(email))
        return ResponseEntity.badRequest().body("Invalid email format");
        
        return mtResourcePersonsService.checkEmailAvailability(email) ? ResponseEntity.ok("0") : ResponseEntity.ok("1");
    }
    
    /*
    * Secured endpoint
    * endpoint exclusive to role A (Local-admin) & U (Co-ordinator faculty)
    * 'Add Resource Person' process (processcode = 8)
    */
    @GetMapping("/create")
    public String renderResourcePersonsCreatePage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Add Resource Person, " + request.getMethod()),
            "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        
        if (!(List.of("A", "U").contains(userRole) &&
        mProcessesService.isProcessGranted(user.getUsercode(), 8))) {
            throw new MyAuthorizationDeniedException(
            ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(),
            "Add Resource Person, " + request.getMethod(), user.getUserid()),
            "page");
        }
        
        switch (user.getRole().getRoleCode().toUpperCase()) {
            case "A":
            model.addAttribute("layoutPath", "layouts/local-admin-layout");
            break;
            case "U":
            model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
            break;
        }
        
        model.addAttribute("mtresourceList", mtResourcePersonsService.getAllResourcePersons());
        model.addAttribute("designationlist", mDesignationsService.getDesignationList("N"));
        model.addAttribute("qualificationcategorylist",
        mQualificationCategoriesService.getAllOrderedByQualificationcategoryname());
        
        return "pages/create-resource-person";
    }
    
    /*
    * Secured endpoint
    * endpoint exclusive to role A (Local-admin) & U (Co-ordinator faculty)
    * Endpoint tied with 'Add Resource Person' process (processcode = 8)
    */
    @PostMapping("/save")
    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> saveResourcePerson(
    @RequestParam(value = "rpslno", required = false) String rpslno,
    @RequestParam("rpname") String rpname,
    @RequestParam("rpemailid") String rpemailid,
    @RequestParam(value = "qualificationsubjectcode", required = false) String qualificationsubjectcode,
    @RequestParam(value = "rpspecialization", required = false) String rpspecialization,
    @RequestParam("designationcodess") String designationcodess,
    @RequestParam("rpinstitutename") String rpinstitutename,
    @RequestParam("rpofficeaddress") String rpofficeaddress,
    @RequestParam("rpresidentialaddress") String rpresidentialaddress,
    @RequestParam(value = "rpofficephone", required = false) String rpofficephone,
    @RequestParam(value = "rpresidencephone", required = false) String rpresidencephone,
    @RequestParam("rpmobileno") String rpmobileno,
    @RequestParam(value = "rpfax", required = false) String rpfax,
    @RequestParam("rpqualificationcategory") String rpqualificationcategory,
    @RequestParam("rpqualification") String rpqualification,
    @RequestParam(value = "qcinput", required = false) String qcinput,
    @RequestParam(value = "dinput", required = false) String dinput,
    HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        
        if (!(List.of("A", "U").contains(userRole) &&
        mProcessesService.isProcessGranted(user.getUsercode(), 8))) {
            throw new MyAuthorizationDeniedException(
            ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
            user.getUserid()),
            "json");
        }
        
        // validating necessary fields
        if (rpemailid == null || rpemailid.trim().length() == 0)
        return ResponseEntity.ok("3");
        
        if (rpemailid.trim().length() > 50)
        return ResponseEntity.ok("4");
        
        if (!mtResourcePersonsService.checkEmailAvailability(rpemailid))
        return ResponseEntity.ok("1");
        
        if (rpinstitutename == null || rpinstitutename.trim().length() == 0)
        return ResponseEntity.ok("7");
        
        if (rpinstitutename.trim().length() > 50)
        return ResponseEntity.ok("8");
        
        if (rpmobileno == null || rpmobileno.trim().length() == 0)
        return ResponseEntity.ok("9");
        
        if (rpmobileno.trim().length() != 10)
        return ResponseEntity.ok("10");
        
        if (rpname == null || rpname.trim().length() == 0)
        return ResponseEntity.ok("11");
        
        if (rpname.trim().length() > 50)
        return ResponseEntity.ok("12");
        
        if (rpofficeaddress == null || rpofficeaddress.trim().length() == 0)
        return ResponseEntity.ok("17");
        
        if (rpofficeaddress.trim().length() > 100)
        return ResponseEntity.ok("18");
        
        if (rpresidentialaddress == null || rpresidentialaddress.trim().length() == 0)
        return ResponseEntity.ok("19");
        
        if (rpresidentialaddress.trim().length() > 100)
        return ResponseEntity.ok("20");
        
        if (rpqualificationcategory == null || rpqualificationcategory.isBlank() || rpqualification == null ||
        rpqualification.isBlank() || designationcodess == null || designationcodess.isBlank())
        return ResponseEntity.badRequest().build();
        // end of validation
        
        // intializing resource person instance
        MT_ResourcePersons newResourcePerson = new MT_ResourcePersons();
        newResourcePerson.setRpslno(rpslno != null ? rpslno.trim() : "");
        newResourcePerson.setRpname(rpname.trim());
        newResourcePerson.setRpemailid(rpemailid.trim());
        newResourcePerson.setMtuserlogin(user);
        newResourcePerson.setRpspecialization(rpspecialization != null ? rpspecialization.trim() : "");
        newResourcePerson.setRpinstitutename(rpinstitutename.trim());
        newResourcePerson.setRpofficeaddress(rpofficeaddress.trim());
        newResourcePerson.setRpresidentialaddress(rpresidentialaddress.trim());
        newResourcePerson.setRpofficephone(rpofficephone != null ? rpofficephone.trim() : "");
        newResourcePerson.setRpresidencephone(rpresidencephone != null ? rpresidencephone.trim() : "");
        newResourcePerson.setRpmobileno(rpmobileno.trim());
        newResourcePerson.setRpfax(rpfax != null ? rpfax.trim() : "");
        newResourcePerson.setRpresidencephone(rpresidencephone != null ? rpresidencephone.trim() : "");
        
        // setting moffices
        M_Offices newResourcePersonOffice = new M_Offices();
        newResourcePersonOffice.setOfficecode(user.getMoffices().getOfficecode());
        newResourcePerson.setMoffices(newResourcePersonOffice);
        
        // setting mqualificationsubjects
        if (qualificationsubjectcode != null && !qualificationsubjectcode.isBlank()) {
            Optional<M_QualificationSubjects> qualificationSubjects = mQualificationSubjectsService
            .findById(qualificationsubjectcode);
            if (qualificationSubjects.isEmpty())
            return ResponseEntity.badRequest().build();
            newResourcePerson.setMqualificationsubjects(qualificationSubjects.get());
        }
        
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        // setting qualificationcode
        if (rpqualification.toLowerCase().equals("others")) {
            if (qcinput != null && !qcinput.isBlank()) {
                if (qualificationsService.checkQualificationExistByQualificationnameAndQualificationcategorycode(
                qcinput, rpqualificationcategory)) {
                    M_Qualifications newResourcePersonQualification = qualificationsService
                    .findByQualificationnameAndQualificationcategorycode(qcinput, rpqualificationcategory);
                    
                    if (newResourcePersonQualification == null)
                    return ResponseEntity.badRequest().build();
                    
                    newResourcePerson.setQualificationcode(newResourcePersonQualification);
                } else {
                    M_Qualifications newQualification = new M_Qualifications();
                    // assuming qualificationcategory already exists
                    M_QualificationCategories existingQualificationCategory = mQualificationCategoriesService
                    .findByQualificationcategorycode(rpqualificationcategory);
                    
                    if (existingQualificationCategory == null)
                    return ResponseEntity.badRequest().build();
                    
                    newQualification.setMqualificationcategories(existingQualificationCategory);
                    newQualification.setQualificationname(qcinput);
                    newQualification.setQualificationcode("");
                    
                    M_Qualifications savedQualification = null;
                    try {
                        if ((savedQualification = qualificationsService
                        .saveQualificationDetails(newQualification)) != null) {
                            persistenceLogger.info(
                            "M_Qualifications with qualificationcode {} saved successfully by userid {}",
                            savedQualification.getQualificationcode(), user.getUserid());
                            audittrailService.logAuditTrail(auditMap, user.getUserid(),
                            "m_qualifications with qualificationcode "
                            + savedQualification.getQualificationcode() + " saved successfully");
                            newResourcePerson.setQualificationcode(savedQualification);
                        } else
                        throw new PersistenceException();
                    } catch (Exception ex) {
                        persistenceLogger.error("M_Qualifications save failed.\nMessage {}\nException {}\nuserid {}",
                        ex.getMessage(), ex, user.getUserid());
                        audittrailService.logAuditTrail(auditMap, user.getUserid(),
                        "m_qualifications with qualificationcode save failed");
                        
                        return ResponseEntity.ok("-1");
                    }
                }
            } else
            return ResponseEntity.ok("21");
        } else {
            M_QualificationCategories newResourcePersonQualificationCategories = mQualificationCategoriesService
            .findByQualificationcategorycode(rpqualificationcategory);
            Optional<M_Qualifications> newResourcePersonQualifications = qualificationsService
            .findById(rpqualification);
            if (newResourcePersonQualifications.isEmpty() || newResourcePersonQualificationCategories == null)
            return ResponseEntity.badRequest().build();
            newResourcePersonQualifications.get().setMqualificationcategories(newResourcePersonQualificationCategories);
            newResourcePerson.setQualificationcode(newResourcePersonQualifications.get());
        }
        
        if (designationcodess.toLowerCase().equals("others")) {
            if (dinput != null && !dinput.isBlank()) {
                if (mDesignationsService.checkDesignationExistByDesignationname(dinput)) {
                    M_Designations newResourcePersonDesignation = mDesignationsService.findByDesignationname(dinput);
                    if (newResourcePersonDesignation == null)
                    return ResponseEntity.badRequest().build();
                    newResourcePerson.setDesignationcode(newResourcePersonDesignation);
                } else {
                    M_Designations newDesignations = new M_Designations();
                    newDesignations.setDesignationname(dinput);
                    newDesignations.setIsparticipantdesignation("N");
                    M_Designations savedDesignations = null;
                    try {
                        if ((savedDesignations = mDesignationsService.saveDesignation(newDesignations)) != null) {
                            persistenceLogger.info(
                            "M_Designations with designationcode {} saved successfully by userid {}",
                            savedDesignations.getDesignationcode(), user.getUserid());
                            audittrailService.logAuditTrail(auditMap, user.getUserid(),
                            "m_designations with designationcode " + savedDesignations.getDesignationcode()
                            + " saved successfully");
                            newResourcePerson.setDesignationcode(savedDesignations);
                        } else
                        throw new PersistenceException();
                    } catch (Exception ex) {
                        persistenceLogger.error("M_Designations save failed.\nMessage {}\nException {}\nuserid {}",
                        ex.getMessage(), ex, user.getUserid());
                        audittrailService.logAuditTrail(auditMap, user.getUserid(), "m_designations save failed");
                        
                        return ResponseEntity.badRequest().build();
                    }
                }
            } else
            return ResponseEntity.ok("23");
        } else {
            M_Designations newResourcePersonDesignations = mDesignationsService.getDesignation(designationcodess);
            if (newResourcePersonDesignations == null)
            return ResponseEntity.badRequest().build();
            newResourcePerson.setDesignationcode(newResourcePersonDesignations);
        }
        
        try {
            if ((newResourcePerson = mtResourcePersonsService.saveResourcePersons(newResourcePerson)) != null) {
                persistenceLogger.info("MT_ResourcePersons with rpslno {} saved successfully by userid {}",
                newResourcePerson.getRpslno(), user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(),
                "mt_resourcepersons with rpslno " + newResourcePerson.getRpslno() + " saved successfully");
                
                // HERE WE CREATE A NEW USERLOGIN FOR RP
                // preparing MT_Userlogin for persistence
                MT_Userlogin newUser = new MT_Userlogin();
                // newUser.setUsercode(usercode != null ? usercode.trim() : ""); // request body
                // doesn't require usercode as it is generated in service
                newUser.setUserid(rpemailid.trim());
                
                String newUserPassword = RandomPasswordGenerator.generateRandomPassword();
                System.out.println("newUserPassword:::::::::" + newUserPassword);
                newUser.setUserpassword(getBcryptPassword(newUserPassword));
                newUser.setUsername(rpname.trim());
                newUser.setUserdescription("Resource Person");
                newUser.setUsermobile(rpmobileno.trim());
                newUser.setEmailid(rpemailid.trim());
                newUser.setUseBcrypt(true);
                
                // setting isModified
                if (newUser.getUsercode() == null || newUser.getUsercode().isBlank())
                newUser.setIsmodified("N"); // hadn't been modified
                else
                newUser.setIsmodified("Y"); // modified or updated
                
                // setting enabled
                newUser.setEnabled((short) 1);
                
                MT_UserloginRole newUserRole;
                
                if ((newUserRole = mtUserloginRoleService.findByRoleCode("R")) != null) {
                    newUser.setUserrole("R");
                    newUser.setRole(newUserRole);
                } else
                return ResponseEntity.ok("-1");
                // Set default isFaculty for users created by Super Admin
                newUser.setIsfaculty("0");
                
                // Validation and persisting user entity
                mtUserloginService.save(newUser);
                String responsesucess="Successfully Saved! Your User ID is "+rpemailid.trim()+" and Your Password is "+newUserPassword;
                return ResponseEntity.ok(responsesucess);
            } else
            throw new PersistenceException();
        } catch (RuntimeException ex) {
            persistenceLogger.error("MT_ResourcePersons save failed.\nMessage {}\nException {}\nuserid {}",
            ex.getMessage(), ex, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(), "mt_resourcepersons save failed");
        }
        
        return ResponseEntity.ok("-1");
    }
    
    public String getBcryptPassword(String newPassword) {
        return bCryptPasswordEncoder.encode(newPassword.trim());
    }
    
    /*
    * Secured endpoint
    * endpoint exclusive to role A (Local-admin) & U (Co-ordinator faculty)
    * 'Map Resource Person' process (processcode = 9)
    */
    @GetMapping("/map")
    public String renderResourcePersonMapPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        
        if (!(List.of("A", "U").contains(userRole) &&
        mProcessesService.isProcessGranted(user.getUsercode(), 9))) {
            throw new MyAuthorizationDeniedException(
            ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod(), user.getUserid()),
            "page");
        }
        
        switch (userRole) {
            case "A":
            model.addAttribute("layoutPath", "layouts/local-admin-layout");
            break;
            case "U":
            model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
            break;
        }
        
        model.addAttribute("fyList", mFinancialYearService.getFyByUsercode(user.getUsercode()));
        
        MT_ResourcePersons resourcePersons = new MT_ResourcePersons();
        model.addAttribute("mtresourceperson", resourcePersons);
        
        return "pages/map-resource-person";
    }
    
    /*
    * Secured endpoint
    * endpoint exclusive to role A (Local-admin) & U (Co-ordinator faculty)
    * Endpoint tied with 'Map Resource Person' process (processcode = 9)
    */
    @PostMapping("/map/save")
    public ResponseEntity<String> saveResourcePersonCourseMap(@RequestParam("programcode") String programcode,
    @RequestParam("phaseid") String phaseid, @RequestParam("resourceperson") List<String> resourcePersons,
    HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (RuntimeException ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        
        if (!(List.of("A", "U").contains(userRole) &&
        mProcessesService.isProcessGranted(user.getUsercode(), 9))) {
            throw new MyAuthorizationDeniedException(
            ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
            user.getUserid()),
            "json");
        }
        
        // validating required parameters
        if (programcode == null || programcode.isBlank() || phaseid == null || phaseid.isBlank()
        || resourcePersons == null)
        return ResponseEntity.badRequest().build();
        
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);
        try {
            // saveResourcePersonsCourseMap() doesn't utilize the M_Programs() instance
            if (mtResourcePersonsService.saveResourcePersonsCourseMap(new M_Programs(), phaseid, resourcePersons)) {
                persistenceLogger.info(
                "MT_ResourcePersons mapped successfully with M_Phases with phaseid {} by userid {}",
                programcode, user.getUserid());
                audittrailService.logAuditTrail(auditMap, user.getUserid(),
                "mt_resourcepersons mapped successfully with m_phases with phaseid " + programcode);
                
                return ResponseEntity.ok("2");
            } else
            throw new PersistenceException();
        } catch (RuntimeException ex) {
            persistenceLogger.error(
            "MT_ResourcePersons mapping failed with M_Phases of phaseid {}.\nMessage {}\nException {}\nuserid {}",
            programcode, ex.getMessage(), ex, user.getUserid());
            audittrailService.logAuditTrail(auditMap, user.getUserid(),
            "mt_resourcepersons mapping failed with m_phases of phaseid " + programcode);
        }
        
        return ResponseEntity.ok("1");
    }
    
    /*
    * Public endpoint
    */
    @PostMapping("/list/phase")
    public ResponseEntity<?> getAllResourcePersonsWithPhase(@RequestParam("phaseid") String phaseid) {
        // validating phaseid
        if (phaseid == null || phaseid.isBlank())
        return ResponseEntity.badRequest().body("phaseid parameter is missing");
        if (phaseid.trim().length() > 6)
        return ResponseEntity.badRequest().body("Invalid phaseid");
        
        return ResponseEntity.ok(mtResourcePersonsService.getAllResourcePersonsWithPhase(phaseid));
    }
    
    /*
    * Public endpoint
    */
    @PostMapping("/list/course-phase")
    public ResponseEntity<?> getResourcePersonsByPhaseid(@RequestParam("phaseid") String phaseid) {
        // validaint phaseid
        if (phaseid == null || phaseid.isBlank())
        return ResponseEntity.badRequest().body("phaseid parameter is missing");
        if (phaseid.trim().length() > 6)
        return ResponseEntity.badRequest().body("Invalid phaseid");
        
        return ResponseEntity.ok(mtResourcePersonsService.getResourcePersonsByPhaseid(phaseid));
    }
    
    @GetMapping("/honorariumform")
    public String showForm(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        // Call the repository method using the logged-in user's email
        String email = user.getUserid();
        List<Object[]> programData = mtResourcePersonsService.findRPPrograms(email);
        
        model.addAttribute("programData", programData);
        model.addAttribute("honorarium", new M_Honorarium());
         List<HonorariumFormDTO> hlist =  mHonorariumService.getHonorariumFormsByUser(user.getUsercode());
        model.addAttribute("HonorariumForms",hlist);   
        return "pages/resource-person/honorariumform"; // This should match your Thymeleaf template name
    }
    
    @PostMapping("/savehonorariumform")
    public String savehonorariumform(@ModelAttribute M_Honorarium honorariumFilled, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        honorariumFilled.setRpUserlogin(user);
        honorariumFilled.setTotalamount(honorariumFilled.getRateperday() * honorariumFilled.getNumberofdays());
        // Save
        mHonorariumService.saveForm(honorariumFilled);
        return "redirect:/nerie/home";
    }
    
    @GetMapping("/my-programs")
    public String getMyPrograms(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        // Call the repository method using the logged-in user's email
        String email = user.getUserid();
        List<Object[]> programData = mtResourcePersonsService.findRPPrograms(email);
        
        model.addAttribute("programData", programData);
        return "pages/resource-person/myprograms";
    }
    
    @GetMapping("/taform")
    public String showtaForm(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        // Call the repository method using the logged-in user's email
        String email = user.getUserid();
        List<Object[]> programData = mtResourcePersonsService.findRPPrograms(email);
        model.addAttribute("TaForms", mTaformService.getTaFormsByUserAndType(user.getUsercode(),true));
        model.addAttribute("programData", programData);
        model.addAttribute("taform", new M_Taform());
        return "pages/resource-person/taform";
    }
    
    @PostMapping("/taform")
    public String submitTaform(HttpServletRequest request,
    @ModelAttribute M_Taform taform,
    // --- single value fields ---
    @RequestParam("namerecord") String namerecord,
    @RequestParam("designation") String designation,
    @RequestParam("basicpay") double basicpay,
    @RequestParam("address") String address,
    @RequestParam("city") String city,
    @RequestParam("pincode") String pincode,
    @RequestParam("resaddress") String resaddress,
    @RequestParam("rcity") String rcity,
    @RequestParam("rpincode") String rpincode,
    @RequestParam("accountnumber") String accountnumber,
    @RequestParam("bankname") String bankname,
    @RequestParam("branch") String branch,
    @RequestParam("ifsc") String ifsc,
    @RequestParam("pancardnumber") String pancardnumber,
    
    // --- multi value fields ---
    @RequestParam("date") List<Date> dates,
    @RequestParam("departure") List<String> departures,
    @RequestParam("arrival") List<String> arrivals,
    @RequestParam("kms") List<Double> kms,
    @RequestParam("mode") List<String> modes,
    @RequestParam("amount") List<Double> amounts) {
        
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        taform.setNamerecord(namerecord);
        taform.setDesignation(designation);
        taform.setBasicpay(basicpay);
        taform.setAddress(address);
        taform.setCity(city);
        taform.setPincode(pincode);
        taform.setResaddress(resaddress);
        taform.setRcity(rcity);
        taform.setRpincode(rpincode);
        taform.setAccountnumber(accountnumber);
        taform.setBankname(bankname);
        taform.setBranch(branch);
        taform.setIfsc(ifsc);
        taform.setPancardnumber(pancardnumber);
        taform.setRpUserlogin(user);
        taform.setIslocal(true);
        mTaformService.saveForm(taform);
        
        // Example: iterate table rows
        for (int i = 0; i < dates.size(); i++) {
            T_ConveyanceCharge cc = new T_ConveyanceCharge();
            cc.setDate(dates.get(i));
            cc.setAmount(amounts.get(i));
            cc.setKms(kms.get(i));
            cc.setModeofconveyance(modes.get(i));
            cc.setPlaceofarrival(arrivals.get(i));
            cc.setPlaceofdeparture(departures.get(i));
            cc.setTaform(taform);
            
            System.out.printf("Row %d -> %s to %s (%s km, %s, ₹%s)%n",
            i + 1,
            departures.get(i),
            arrivals.get(i),
            kms.get(i),
            modes.get(i),
            amounts.get(i));
            
            tconveyanceChargeService.saveForm(cc);
        }
        // You can now save the single value fields + loop through the rows
        
        return "redirect:/nerie/home";
    }
    
    @GetMapping("/nltaform")
    public String getnltaform(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        // Call the repository method using the logged-in user's email
        String email = user.getUserid();
        List<Object[]> programData = mtResourcePersonsService.findRPPrograms(email);
        
        model.addAttribute("programData", programData);
        model.addAttribute("taform", new M_Taform());
        List<TaFormDTO> list =  mTaformService.getTaFormsByUserAndType(user.getUsercode(),false);
        model.addAttribute("TaForms",list);   
        return "pages/resource-person/nltaform";
    }
    
    @PostMapping("/nontaform")
    public String submitNonTaform(HttpServletRequest request,
    @ModelAttribute M_Taform taform,
    // --- single value fields ---
    @RequestParam("namerecord") String namerecord,
    @RequestParam("designation") String designation,
    @RequestParam("basicpay") double basicpay,
    @RequestParam("address") String address,
    @RequestParam("city") String city,
    @RequestParam("pincode") String pincode,
    @RequestParam("resaddress") String resaddress,
    @RequestParam("rcity") String rcity,
    @RequestParam("rpincode") String rpincode,
    @RequestParam("accountnumber") String accountnumber,
    @RequestParam("bankname") String bankname,
    @RequestParam("branch") String branch,
    @RequestParam("ifsc") String ifsc,
    @RequestParam("pancardnumber") String pancardnumber,
    
    // --- multi value fields (PART 1)---
    @RequestParam("datedeparture") List<Date> datesofdeparture,
    @RequestParam("placedeparture") List<String> placesofdeparture,
    @RequestParam("timedeparture") List<String> timesofdeparture,
    @RequestParam("datearrival") List<Date> datesofarrival,
    @RequestParam("placearrival") List<String> placesofarrival,
    @RequestParam("timearrival") List<String> timesofarrival,
    //@RequestParam("part1kms") List<Double> part1kms,
    @RequestParam("part1mode") List<String> part1modes,
    @RequestParam("part1amount") List<Double> part1amounts,
    @RequestParam("partdetails") List<String> details,
    
    // --- multi value fields (PART 2)---
    @RequestParam("date") List<Date> dates,
    @RequestParam("departure") List<String> departures,
    @RequestParam("arrival") List<String> arrivals,
    @RequestParam("kms") List<Double> kms,
    @RequestParam("mode") List<String> modes,
    @RequestParam("amount") List<Double> amounts,
    
    // --- multi value fields (PART 3)---
    @RequestParam("part3amount") Double part3amount,
    @RequestParam("part3noofdays") Double part3noofdays) {
        
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        
        taform.setNamerecord(namerecord);
        taform.setDesignation(designation);
        taform.setBasicpay(basicpay);
        taform.setAddress(address);
        taform.setCity(city);
        taform.setPincode(pincode);
        taform.setResaddress(resaddress);
        taform.setRcity(rcity);
        taform.setRpincode(rpincode);
        taform.setAccountnumber(accountnumber);
        taform.setBankname(bankname);
        taform.setBranch(branch);
        taform.setIfsc(ifsc);
        taform.setPancardnumber(pancardnumber);
        taform.setRpUserlogin(user);
        taform.setIslocal(false);
        mTaformService.saveForm(taform);
        
        // Example: Part 1 ------- iterate table rows
        for (int i = 0; i < datesofdeparture.size(); i++) {
            T_ConveyanceCharge cc = new T_ConveyanceCharge();
            cc.setDateofdeparture(datesofdeparture.get(i));
            cc.setPlaceofdeparture(placesofdeparture.get(i));
            cc.setTimeofdeparture(timesofdeparture.get(i));
            cc.setTimeofarrival(timesofarrival.get(i));
            cc.setDateofarrival(datesofarrival.get(i));
            cc.setDetailsoftravel(details.get(i));
            cc.setAmount(part1amounts.get(i));
            cc.setKms(kms.get(i));
            cc.setModeofconveyance(part1modes.get(i));
            cc.setPlaceofarrival(placesofarrival.get(i));
            cc.setTaform(taform);
            cc.setNonlocalpartno("1");
            // System.out.printf("Row %d -> %s to %s (%s km, %s, ₹%s)%n",
            //         i + 1,
            //         departures.get(i),
            //         arrivals.get(i),
            //         kms.get(i),
            //         modes.get(i),
            //         amounts.get(i));
            tconveyanceChargeService.saveForm(cc);
        }
        
        // Example: Part 2 ------- iterate table rows
        for (int i = 0; i < dates.size(); i++) {
            T_ConveyanceCharge cc = new T_ConveyanceCharge();
            cc.setDate(dates.get(i));
            cc.setAmount(amounts.get(i));
            cc.setKms(kms.get(i));
            cc.setModeofconveyance(modes.get(i));
            cc.setPlaceofarrival(arrivals.get(i));
            cc.setPlaceofdeparture(departures.get(i));
            cc.setTaform(taform);
            cc.setNonlocalpartno("2");
            
            System.out.printf("Row %d -> %s to %s (%s km, %s, ₹%s)%n",
            i + 1,
            departures.get(i),
            arrivals.get(i),
            kms.get(i),
            modes.get(i),
            amounts.get(i));
            
            tconveyanceChargeService.saveForm(cc);
        }
        T_ConveyanceCharge cc = new T_ConveyanceCharge();
        
        cc.setNonlocalpartno("3");
        cc.setAmount(part3amount);
        cc.setDetailsoftravel(part3noofdays+" Days");
        cc.setTaform(taform);
        tconveyanceChargeService.saveForm(cc);
        return "redirect:/nerie/home";
    }
    
    @GetMapping("/taformdetails/{id}")
    public String getformdetails(@PathVariable(value="id") Long id,
    Model model, HttpServletRequest request) {
        M_Taform taform =  mTaformService.getById(id);
        List<T_ConveyanceCharge> ccs = tconveyanceChargeService.findByTaform(taform);
        Double totalAmountpart3=0.0;
        for(T_ConveyanceCharge cc:ccs){
            if(cc.getNonlocalpartno()!=null && cc.getNonlocalpartno().equals("3")){
                Double cmt =cc.getAmount();

                String days = cc.getDetailsoftravel();
                Double daysValue = 0.0;
                
                if (days != null && !days.isBlank()) {
                    String num = days.replaceAll("[^0-9.]", "");
                    if (!num.isEmpty()) {
                        daysValue = Double.parseDouble(num);
                    }
                }
                totalAmountpart3 = cmt*daysValue;
            }
            
        }
        double totalAmountpart1= ccs.stream()
        .filter(cc -> "1".equals(cc.getNonlocalpartno()))
        .mapToDouble(cc -> cc.getAmount() != null ? cc.getAmount() : 0)
        .sum();

        double totalAmountpart2 = ccs.stream()
        .filter(cc -> cc.getNonlocalpartno() == null || "2".equals(cc.getNonlocalpartno()))
        .mapToDouble(cc -> cc.getAmount() != null ? cc.getAmount() : 0)
        .sum();

        model.addAttribute("taform",taform);
        model.addAttribute("ccs",ccs);
        model.addAttribute("totalAmountpart1",totalAmountpart1);
        model.addAttribute("totalAmountpart2",totalAmountpart2);
        model.addAttribute("totalAmountpart3",totalAmountpart3);
        return "pages/resource-person/taformdetails";
    }

    @GetMapping("/honorariumformdetails/{id}")
    public String gethonorariumformdetailsformdetails(@PathVariable(value="id") Long id,
    Model model, HttpServletRequest request) {
        M_Honorarium honorariumform =  mHonorariumService.getById(id);
        model.addAttribute("honorariumform",honorariumform);
        return "pages/resource-person/honorariumformdetails";
    }
    
    @GetMapping("/editprofile")
    public String geteditprofile(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
            ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
            "Map Resource Person, " + request.getMethod()),
            "page");
        }
        model.addAttribute("userlogin", user);
        return "pages/resource-person/editprofile";
    }
}