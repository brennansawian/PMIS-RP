package com.nic.nerie.mt_userlogin.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.nic.nerie.audittrail.service.AudittrailService;
import com.nic.nerie.exceptions.MyAuthenticationCredentialsNotFoundException;
import com.nic.nerie.exceptions.MyAuthorizationDeniedException;
import com.nic.nerie.m_departments.service.M_DepartmentsService;
import com.nic.nerie.m_designations.model.M_Designations;
import com.nic.nerie.m_designations.service.M_DesignationsService;
import com.nic.nerie.m_offices.model.M_Offices;
import com.nic.nerie.m_offices.service.M_OfficesService;
import com.nic.nerie.m_phases.service.M_PhasesService;
import com.nic.nerie.m_processes.service.M_ProcessesService;
import com.nic.nerie.m_programs.service.M_ProgramsService;
import com.nic.nerie.mt_la_usermapping.service.MT_LeaveApplication_UserMappingService;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService;
import com.nic.nerie.mt_userloginrole.model.MT_UserloginRole;
import com.nic.nerie.mt_userloginrole.service.MT_UserloginRoleService;
import com.nic.nerie.t_notifications.model.T_Notifications;
import com.nic.nerie.t_notifications.service.T_NotificationsService;
import com.nic.nerie.t_studentassignment.service.T_StudentAssignmentService;
import com.nic.nerie.t_students.model.T_Students;
import com.nic.nerie.t_students.service.T_StudentsService;
import com.nic.nerie.utils.EmailValidator;
import com.nic.nerie.utils.ExceptionUtil;
import com.nic.nerie.utils.ImageUtil;
import com.nic.nerie.utils.UtilCommon;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
@RequestMapping("/nerie")
public class MT_UserloginController {
    private final MT_UserloginService mtUserloginService;
    private final T_StudentsService tStudentsService;
    private final T_NotificationsService tNotificationsService;
    private final T_StudentAssignmentService tStudentAssignmentService;
    private final M_PhasesService mPhasesService;
    private final M_ProgramsService mProgramsService;
    private final M_DesignationsService mDesignationsService;
    private final M_OfficesService mOfficesService;
    private final M_ProcessesService mProcessesService;
    private final MT_UserloginRoleService mtUserloginRoleService;
    private final AudittrailService audittrailService;
    private final MT_LeaveApplication_UserMappingService mtLeaveApplicationUserMappingService;
    private final M_DepartmentsService mDepartmentsService;

    private static final Logger dataPersistenceLogger = LoggerFactory.getLogger("DATA_PERSISTENCE_LOGGER");

    @Autowired
    public MT_UserloginController(MT_UserloginService mtUserloginService,
            T_StudentsService tStudentsService,
            T_NotificationsService tNotificationsService,
            T_StudentAssignmentService tStudentAssignmentService,
            M_PhasesService mPhasesService,
            M_ProgramsService mProgramsService,
            M_DesignationsService mDesignationsService,
            M_OfficesService mOfficesService,
            M_ProcessesService mProcessesService,
            MT_UserloginRoleService mtUserloginRoleService,
            AudittrailService audittrailService,
            MT_LeaveApplication_UserMappingService mtLeaveApplicationUserMappingService,
            M_DepartmentsService mDepartmentsService) {
        this.mtUserloginService = mtUserloginService;
        this.tStudentsService = tStudentsService;
        this.tNotificationsService = tNotificationsService;
        this.tStudentAssignmentService = tStudentAssignmentService;
        this.mPhasesService = mPhasesService;
        this.mProgramsService = mProgramsService;
        this.mDesignationsService = mDesignationsService;
        this.mOfficesService = mOfficesService;
        this.mProcessesService = mProcessesService;
        this.mtUserloginRoleService = mtUserloginRoleService;
        this.audittrailService = audittrailService;
        this.mtLeaveApplicationUserMappingService = mtLeaveApplicationUserMappingService;
        this.mDepartmentsService = mDepartmentsService;
    }

    /*
     * Public endpoint
     */
    @PostMapping("/users/check-email")
    public ResponseEntity<String> checkEmailAvailable(@RequestParam("emailid") String emailid) {
        if (!EmailValidator.isEmailValid(emailid))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");

        if (mtUserloginService.checkUserExists(emailid, null))
            return ResponseEntity.ok("1");
        return ResponseEntity.ok("2");
    }

    /*
     * Public endpoint
     */
    @PostMapping("/users/check-user")
    public ResponseEntity<String> checkUserExists(@RequestParam(value = "userid", required = false) String userid,
            @RequestParam(value = "usercode", required = false) String usercode,
            @RequestParam(value = "emailid", required = false) String emailid,
            @RequestParam(value = "usermobile", required = false) String usermobile) {
        if ((userid == null || userid.isBlank()) && (usercode == null || usercode.isBlank())
                && (emailid == null || emailid.isBlank()) && (usermobile == null || usermobile.isBlank()))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Requires at least one parameter");

        if (emailid != null && emailid.isBlank())
            if (!EmailValidator.isEmailValid(emailid))
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid email format");

        if (usermobile != null && usermobile.isBlank() && usermobile.trim().length() < 10)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mobile number needs to be 10 digit long");

        MT_Userlogin user = new MT_Userlogin();
        user.setUserid(userid != null ? userid.trim() : "");
        user.setUsercode(usercode != null ? usercode.trim() : "");
        user.setEmailid(emailid != null ? emailid.trim() : "");
        user.setUsermobile(usermobile != null ? usermobile.trim() : "");

        if (mtUserloginService.checkUserExists(user))
            return ResponseEntity.ok("1");

        return ResponseEntity.ok("2");
    }

    /*
     * Secured endpoint
     * This endpiont is exclusive to S (Admin), Z (Principal directory) & A
     * (Local-admin)
     */
    @GetMapping(value = "/users/manage")
    public String renderUserCreationPage(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(),
                            "Create User, " + request.getMethod()),
                    "page");
        }

        String userRole = user.getRole().getRoleCode().toUpperCase();
        List<Object[]> userList = null;
        List<M_Designations> designationsList = mDesignationsService.getDesignationList("N");

        if (!(List.of("A", "S", "Z").contains(userRole) &&
                !userRole.equalsIgnoreCase("S") ? mProcessesService.isProcessGranted(user.getUsercode(), 1) : true // S
                                                                                                                   // (Admin)
                                                                                                                   // is
                                                                                                                   // granted
                                                                                                                   // logically
        )) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(),
                            "Create User, " + request.getMethod(), user.getUserid()),
                    "page");
        }

        model.addAttribute("designationlist", designationsList);
        model.addAttribute("userrole", userRole);
        model.addAttribute("officelist", mOfficesService.getOfficesList());

        if (userRole.equals("S")) {
            userList = mtUserloginService.getUserListByRole("Z");
            List<Object[]> processlist = mProcessesService.getPrincipalProcesses();
            model.addAttribute("processlistp", processlist);
            model.addAttribute("plen", processlist.size());
        } else if (userRole.equals("Z")) {
            userList = mtUserloginService.getUserListByRole("A");
            List<Object[]> processlist = mProcessesService.getAllProcesses();
            model.addAttribute("processlist", processlist);
            model.addAttribute("plen", processlist.size());
        } else {
            userList = mtUserloginService.getadminUserList("U", user.getMoffices().getOfficecode());
            List<Object[]> processlist = mProcessesService.getLocalAdminProcesses(user.getUsercode());
            model.addAttribute("processlist", processlist);
            model.addAttribute("plen", processlist.size());
        }

        switch (userRole) {
            case "S":
                model.addAttribute("layoutPath", "layouts/admin-layout");
                break;
            case "Z":
                model.addAttribute("layoutPath", "layouts/principal-director-layout");
                break;
            case "A":
                model.addAttribute("layoutPath", "layouts/local-admin-layout");
                break;
            case "R":
                model.addAttribute("layoutPath", "layouts/resource-person-layout");
                break;
        }

        model.addAttribute("userlist", userList);
        return "pages/users/create-user";
    }

    /*
     * Secured endpoint
     * This endpiont is exclusive to S (Admin), Z (Principal directory) & A
     * (Local-admin)
     */
    @Transactional(rollbackFor = Exception.class)
    @PostMapping(value = "/users/save")
    public ResponseEntity<String> saveUser(
            @RequestParam(value = "officecode", required = false) String officecode,
            @RequestParam(value = "usercode", required = false) String usercode,
            @RequestParam("userid") String userid,
            @RequestParam("userpassword") String userpassword,
            @RequestParam("username") String username,
            @RequestParam("userdescription") String userdescription,
            @RequestParam("designationcode") String designationcode,
            @RequestParam(value = "dinput", required = false) String dinput,
            @RequestParam("usermobile") String usermobile,
            @RequestParam("emailid") String emailid,
            @RequestParam(value = "processes", required = false) List<String> processes,
            @RequestParam(value = "roleType", required = false) String roleType,
            @RequestParam(value = "leaveRole", required = false) String leaveRole,
            HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String userRole = user.getRole().getRoleCode().toUpperCase();
        HashMap<String, String> audittrail = UtilCommon.getClientDetails(request);

        if (!(List.of("A", "S", "Z").contains(userRole) &&
                !userRole.equalsIgnoreCase("S") ? mProcessesService.isProcessGranted(user.getUsercode(), 1) : true // S
                                                                                                                   // (Admin)
                                                                                                                   // is
                                                                                                                   // granted
                                                                                                                   // logically
        )) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
                            user.getUserid()),
                    "json");
        }

        // validating required fields
        if (userid == null || userid.isBlank() ||
                userpassword == null || userpassword.isBlank() ||
                username == null || username.isBlank() ||
                userdescription == null || userdescription.isBlank() ||
                designationcode == null || designationcode.isBlank() ||
                usermobile == null || usermobile.isBlank() ||
                emailid == null || emailid.isBlank())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body missing required fields");

        // validating officecode (if updating...)
        if (officecode != null && !officecode.isBlank()) {
            if (officecode.trim().length() > 6)
                return ResponseEntity.badRequest().body("Invalid officecode");

            if (!mOfficesService.existsByOfficecode(officecode))
                return ResponseEntity.badRequest().body("Office does not exist. Update failed.");
        }

        // validating usercode (if updating...)
        if (usercode != null && !usercode.isBlank()) {
            if (usercode.trim().length() > 6)
                return ResponseEntity.badRequest().body("Invalid usercode");

            if (!mtUserloginService.existsByUsercode(usercode))
                return ResponseEntity.badRequest().body("User does not exist. Update failed.");
        }

        // validating userid
        if (userid.trim().length() > 50)
            return ResponseEntity.badRequest().body("Userid must be lesser than 50 characters");

        // validating userpassword
        if (userpassword.trim().length() > 512)
            return ResponseEntity.badRequest().body("Userpassword must be lesser than 512 characters");

        // validating username
        if (username.trim().length() > 100)
            return ResponseEntity.badRequest().body("Username must be lesser than 100 characters");

        // validating userdescription
        if (userdescription.trim().length() > 300)
            return ResponseEntity.badRequest().body("Userdescription must be lesser than 300 characters");

        // validating designationcode
        if (designationcode.trim().length() > 3 && !designationcode.trim().toLowerCase().equals("others")) // designationcode
                                                                                                           // can be a
                                                                                                           // code or
                                                                                                           // 'others'
            return ResponseEntity.badRequest().body("Invalid designationcode");

        M_Designations des = null;
        if (designationcode.trim().length() <= 3) // if designationcode is an actual code
        {
            des = mDesignationsService.getDesignation(designationcode);
            if (des == null)
                return ResponseEntity.badRequest().body("Designation doesn not exist");
        }

        // validating usermobile
        if (usermobile.trim().length() > 50)
            return ResponseEntity.badRequest().body("Invalid mobileno");

        // validating email
        if (emailid.trim().length() > 50)
            return ResponseEntity.badRequest().body("Emailid must be lesser than 50 characters");
        if (!EmailValidator.isEmailValid(emailid))
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Emailid format");

        // validating processes
        // processes should not be null or empty unless if the new user is of role Z
        // (i.e., created by Admin (S))
        if ((processes == null || processes.size() == 0) && !userRole.equals("S"))
            return ResponseEntity.badRequest().body("Processes cannot be empty for role A & U");

        // preparing MT_Userlogin for persistence
        MT_Userlogin newUser = new MT_Userlogin();
        newUser.setUsercode(usercode != null ? usercode.trim() : ""); // request body doesn't require usercode as it is
                                                                      // generated in service
        newUser.setUserid(userid.trim());

        // TODO @Abanggi: Temporary solution
        if (usercode == null || usercode.isBlank())
            newUser.setUserpassword(mtUserloginService.getBcryptPassword(userpassword));
        else
            newUser.setUserpassword(mtUserloginService.getUserpasswordByUsercode(usercode)); // don't trust hashed pwd
                                                                                             // from client side

        newUser.setUsername(username.trim());
        newUser.setUserdescription(userdescription.trim());
        newUser.setUsermobile(usermobile.trim());
        newUser.setEmailid(emailid.trim());
        newUser.setUseBcrypt(true);

        // setting isModified
        if (newUser.getUsercode() == null || newUser.getUsercode().isBlank())
            newUser.setIsmodified("N"); // hadn't been modified
        else
            newUser.setIsmodified("Y"); // modified or updated

        // setting enabled
        newUser.setEnabled((short) 1);

        MT_UserloginRole newUserRole;
        if (userRole.equals("S")) {
            if ((newUserRole = mtUserloginRoleService.findByRoleCode("Z")) != null) {
                newUser.setUserrole("Z");
                newUser.setRole(newUserRole);
            } else
                return ResponseEntity.ok("3");

            // Set default isFaculty for users created by Super Admin
            newUser.setIsfaculty("0");

            // preparing M_Offices for assigning to MT_Userlogin
            if (officecode == null || officecode.isBlank())
                return ResponseEntity.ok("4");

            M_Offices offices = new M_Offices();
            offices.setOfficecode(officecode);
            newUser.setMoffices(offices);
        } else {
            if (userRole.equals("Z")) {
                if ((newUserRole = mtUserloginRoleService.findByRoleCode("A")) != null) {
                    newUser.setUserrole("A");
                    newUser.setRole(newUserRole);
                } else
                    return ResponseEntity.ok("3");

                // Set default isFaculty for users created by Admin
                newUser.setIsfaculty("0");

            } else if (userRole.equals("A")) {
                if ((newUserRole = mtUserloginRoleService.findByRoleCode("U")) != null) {
                    newUser.setUserrole("U");
                    newUser.setRole(newUserRole);
                } else
                    return ResponseEntity.ok("3");

                // Set isFaculty based on form input for users created by a Coordinator (Role A)
                if (roleType == null || !List.of("0", "1", "2").contains(roleType)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid Role Type specified.");
                }
                newUser.setIsfaculty(roleType);
            }

            // preparing M_Offices for assigning to MT_Userlogin
            String officeCode = user.getMoffices().getOfficecode();
            M_Offices offices = new M_Offices();
            offices.setOfficecode(officeCode);
            newUser.setMoffices(offices);
        }

        // setting mDesginations
        if (designationcode.toLowerCase().equals("others")) {
            // if designationcode is 'others', create new M_Designations first
            if (dinput != null && !dinput.isBlank()) {
                des = new M_Designations();
                des.setDesignationname(dinput.trim());
                des.setIsparticipantdesignation("N");

                try {
                    M_Designations savedDesignation;
                    if ((savedDesignation = mDesignationsService.saveDesignationDetails(des)) != null) {
                        dataPersistenceLogger.info(
                                "M_Designations entity saved successfully in MT_UserloginController.saveUser() by userid {}",
                                user.getUserid());
                        newUser.setMdesignations(savedDesignation);
                    }
                } catch (RuntimeException ex) {
                    dataPersistenceLogger.error(
                            "Error saving M_Designations entity in MT_UserloginController.saveUser() by userid {}." +
                                    "\nMessage {} \nException",
                            user.getUserid(), ex.getMessage(), ex);
                    return ResponseEntity.ok("3"); // error saving M_Designations...
                }
            } else
                return ResponseEntity.badRequest().body("Invalid designation detail");
        } else
            newUser.setMdesignations(des);

        // Validation and persisting user entity
        if (newUser.getUsercode().isBlank())
            if (mtUserloginService.checkUserExists(newUser))
                return ResponseEntity.ok("1"); // user already exists

        audittrail.put("userid", user.getUserid());
        audittrail.put("actiontaken", "");
        try {
            MT_Userlogin savedUser = mtUserloginService.saveUserDetailsAndProcessMappings(newUser, processes);
            if (savedUser != null) {
                dataPersistenceLogger.info(
                        "MT_Userlogin entity saved successfully in MT_UserloginController.saveUser() by userid {}",
                        user.getUserid());

                // Save leave role if provided
                if (leaveRole != null && !leaveRole.isBlank()) {
                    if (!mtLeaveApplicationUserMappingService.saveUserSlaRole(savedUser, leaveRole)) {
                        dataPersistenceLogger.error(
                                "Error saving user leave role in MT_UserloginController.saveUser() by userid {}",
                                user.getUserid());
                        audittrail.replace("actiontaken", "Saving users slarole failed by userid " + user.getUserid());

                        throw new Exception("Error saving leave role"); // Failed to save leave role
                    } else
                        dataPersistenceLogger.info(
                                "User leave role saved successfully in MT_UserloginController.saveUser() by userid {}",
                                user.getUserid());
                }

                audittrail.replace("actiontaken", "User Created Successfully by userid " + user.getUserid()); // Everything
                                                                                                              // saved
                                                                                                              // successfully
                audittrailService.saveAuditTrail(audittrail);

                return ResponseEntity.ok("2");
            }
        } catch (Exception ex) {
            if (ex.getClass().equals(NumberFormatException.class)) {
                dataPersistenceLogger
                        .error("NumberFormatException happened in MT_UserloginController.saveUser() by userid {}. " +
                                "\nMessage {} \nException {}", user.getUserid(), ex.getMessage(), ex);
            } else {
                dataPersistenceLogger.error("Something went wrong in MT_UserloginController.saveUser() by userid {}. " +
                        "\nMessage {} \nException {}", user.getUserid(), ex.getMessage(), ex);
            }
        }

        // actiontaken key might have a value set in try-catch
        if (audittrail.get("actiontaken").isBlank())
            audittrail.replace("actiontaken", "Create User Fail");
        audittrailService.saveAuditTrail(audittrail);

        return ResponseEntity.ok("3");
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/users/change-user-status")
    @ResponseBody
    public ResponseEntity<String> changeUserStatus(@RequestParam("usercode") String usercode,
            HttpServletRequest request) {
        try {
            mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (usercode == null || usercode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Invalid usercode");
        }

        try {
            // Service call
            boolean success = mtUserloginService.toggleUserStatus(usercode.trim());

            if (success) {
                return ResponseEntity.ok("1"); // Success
            } else {
                return ResponseEntity.ok(""); // No changes made
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Secured endpoint
     */
    @PostMapping(value = "/users/saveprofilephoto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateUserphotograph(@RequestParam(value = "file1") MultipartFile imageFile,
            HttpServletRequest request) {
        MT_Userlogin user;

        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        try {
            byte[] userphotograph = imageFile.getBytes();
            if (userphotograph.length > 0) {
                Integer rowsAffected = mtUserloginService.updateUserphotographByUsercode(user.getUsercode(),
                        userphotograph);
                if (rowsAffected == 0)
                    return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
                else if (rowsAffected > 1)
                    throw new DuplicateKeyException("PK constraint violated.");
                return ResponseEntity.ok(rowsAffected.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        return ResponseEntity.badRequest().build();
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/users/reset-password")
    @ResponseBody
    public ResponseEntity<String> resetUserPassword(
            @RequestParam("usercode") String usercode,
            @RequestParam("userpassword") String userpassword,
            HttpServletRequest request) {
        MT_Userlogin user = mtUserloginService.findByUsercode(usercode);

        if (user == null)
            return ResponseEntity.badRequest().body("User with usercode " + usercode + " does not exist");

        HashMap<String, String> audittrail = UtilCommon.getClientDetails(request);
        audittrail.put("userid", user.getUserid());
        audittrail.put("actiontaken", "");

        MT_Userlogin updatedUser = mtUserloginService.updateUserPassword(user, userpassword);

        if (updatedUser == null) {
            dataPersistenceLogger.error("Error resetting password of user with userid {}", user.getUserid());
            audittrail.put("actiontaken", "Reset Password Successfuly");
            audittrailService.saveAuditTrail(audittrail);
            return ResponseEntity.ok("");
        }

        dataPersistenceLogger.info("Successfully resetted password of user with userid {}", user.getUserid());
        audittrail.put("actiontaken", "User with userid " + user.getUserid() + " resetted password");
        audittrailService.saveAuditTrail(audittrail);
        return ResponseEntity.ok("1");
    }

    /*
     * Secured endpoint
     */
    @GetMapping("/users/change-password")
    public String showChangePasswordPage(Model model, @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {
        if (userDetails == null) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        String currentUserId = userDetails.getUsername();
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            return "redirect:/nerie/login";
        }

        MT_Userlogin currentUserLogin = mtUserloginService.findByUserId(currentUserId);

        if (currentUserLogin == null) {
            return "redirect:/nerie/login";
        }
        if ("S".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/admin-layout");
        } else if ("U".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
        } else if ("P".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/participant-layout");
        } else if ("T".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/student-layout");
        } else if ("A".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/local-admin-layout");
        } else if ("R".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/resource-person-layout");
        }

        model.addAttribute("userlogin", currentUserLogin);
        return "pages/users/change-user-password";
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/users/check-old-password")
    @ResponseBody
    public ResponseEntity<String> checkOldPasswordMatch(
            @RequestParam(value = "olduserpassword", required = true) String oldPasswordInput, // Make required=true?
                                                                                               // @Toiar: ofcourse
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        // Authentication check
        if (userDetails == null) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        String currentUserId = userDetails.getUsername();

        // Basic Input validation
        if (!StringUtils.hasText(oldPasswordInput)) {
            return ResponseEntity.badRequest().body("InputError");
        }
        if (oldPasswordInput.length() > 512) {
            return ResponseEntity.badRequest().body("LengthError");
        }

        // Call the verification service
        try {
            boolean passwordMatches = mtUserloginService.verifyPassword(currentUserId, oldPasswordInput);
            return ResponseEntity.ok(passwordMatches ? "1" : "0"); // 1 match, 0 mismatch

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("InputError");
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("UserNotFound");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ServerError");
        }
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/users/change-password")
    @ResponseBody
    public ResponseEntity<String> changeUserPassword(
            @RequestParam("olduserpassword") String oldPassword,
            @RequestParam("newuserpassword") String newPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request) {

        // Validate inputs
        if (userDetails == null) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (isInvalidPassword(oldPassword) || isInvalidPassword(newPassword)) {
            return ResponseEntity.badRequest().body("0");
        }

        String userId = userDetails.getUsername();
        MT_Userlogin user = mtUserloginService.findByUserId(userId);

        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("0");
        }

        // Prepare audit trail
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        // Verify old password
        if (!mtUserloginService.verifyPassword(userId, oldPassword)) {
            logAuditTrail(auditMap, userId, "Change password Failed - Wrong Old Password");
            return ResponseEntity.ok("0");
        }

        // Check if new password is same as old
        if (mtUserloginService.verifyPassword(userId, newPassword)) {
            logAuditTrail(auditMap, userId, "Change password Failed - New Same as Old");
            return ResponseEntity.ok("3");
        }

        // Check if password contains user ID
        if (user.getUserid() != null && newPassword.toUpperCase().contains(user.getUserid().toUpperCase())) {
            logAuditTrail(auditMap, userId, "Change password Failed - Contains User ID");
            return ResponseEntity.ok("5");
        }

        // Update password
        MT_Userlogin updatedUser = mtUserloginService.updateUserPassword(user, newPassword);
        if (updatedUser == null) {
            throw new RuntimeException("Failed to update participant profile details.");
        }

        logAuditTrail(auditMap, userId, "Change password Success");
        return ResponseEntity.ok("2");
    }

    private boolean isInvalidPassword(String password) {
        return password == null || password.isBlank() || password.length() < 8 || password.length() > 512;
    }

    /*
     * Secured endpoint
     * User Profile Section
     */
    @GetMapping("/users/profile")
    public String getEditProfilePage(Model model, HttpServletRequest request) {
        MT_Userlogin currentUserLogin;
        try {
            currentUserLogin = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "page");
        }
        byte[] photoBytes = currentUserLogin.getUserphotograph();
        String base64Image = null;

        if (photoBytes != null && photoBytes.length > 0) {
            try {
                base64Image = ImageUtil.convertToBase64(photoBytes);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        model.addAttribute("userlogin", currentUserLogin);
        model.addAttribute("profilepic", base64Image);

        if ("S".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/admin-layout");
        } else if ("U".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/coordinator-faculty-layout");
        } else if ("P".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/participant-layout");
        } else if ("A".equals(currentUserLogin.getUserrole())) {
            model.addAttribute("layoutPath", "layouts/local-admin-layout");
        }

        return "pages/users/edit-user-profile";
    }

    /*
     * Secured endpoint
     * TODO @Toiar: Handle exception from getUserloginFromAuthentcation()
     */
    @PostMapping("/users/profile/update")
    @ResponseBody
    public ResponseEntity<String> updateUserProfile(
            @ModelAttribute("userlogin") MT_Userlogin mtUserlogin,
            @RequestParam(value = "file1", required = false) MultipartFile file1,
            HttpServletRequest request) {
        HashMap<String, String> auditMap = UtilCommon.getClientDetails(request);

        MT_Userlogin existingUser;
        try {
            existingUser = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        String auditUserId = existingUser.getUserid();

        try {
            // Check if email already exists for another user
            boolean emailExists = mtUserloginService.checkUserExists(mtUserlogin.getEmailid(),
                    existingUser.getUsercode());

            if (emailExists) {
                return ResponseEntity.ok("1"); // Email already exists
            }

            // File validation
            if (file1 != null && !file1.isEmpty()) {
                existingUser.setUserphotograph(file1.getBytes());
            }

            // Update allowed fields based on role
            String userrole = existingUser.getUserrole();

            /*
             * TODO: Faculty Profile Update
             * 
             * if ("1".equals(existingUser.getIsfaculty())) {
             * 
             * existingUser.setEnabled(mtUserlogin.getEnabled());
             * existingUser.setIsmodified("N");
             * existingUser.setMdesignations(mtUserlogin.getMdesignations());
             * existingUser.setMoffices(mtUserlogin.getMoffices());
             * existingUser.setUserdescription(mtUserlogin.getUserdescription());
             * existingUser.setUserpassword(mtUserlogin.getUserpassword());
             * existingUser.setEmailid(mtUserlogin.getEmailid());
             * existingUser.setUserrole(mtUserlogin.getUserrole());
             * existingUser.setIsfaculty("1");
             * } else
             */
            if (List.of("A", "U", "T", "S").contains(userrole)) {
                existingUser.setUsername(mtUserlogin.getUsername());
                existingUser.setUsermobile(mtUserlogin.getUsermobile());
                existingUser.setEmailid(mtUserlogin.getEmailid());
            }

            // Save profile
            MT_Userlogin updatedUser = mtUserloginService.saveUserloginProfileDetails(existingUser);

            if (updatedUser != null) {
                logAuditTrail(auditMap, auditUserId, "Update Profile Success");
                return ResponseEntity.ok("2"); // Successfully saved
            } else {
                logAuditTrail(auditMap, existingUser.getUserid(), "Update Profile Failed");
                return ResponseEntity.ok("0"); // Save failed
            }

        } catch (IOException e) {
            logAuditTrail(auditMap, auditUserId, "Update Profile Failed - File Upload Error");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("4"); // File error
        } catch (Exception e) {
            logAuditTrail(auditMap, auditUserId, "Update Profile Failed - General Exception: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("0"); // General error
        }
    }

    /*
     * Secured endpoint
     */
    @PostMapping("/users/profile/check-email")
    @ResponseBody
    public ResponseEntity<String> checkEmailExistence(@ModelAttribute("userlogin") MT_Userlogin mtUserlogin,
            @RequestParam(value = "emailid") String emailid) {
        MT_Userlogin existingUser;

        try {
            existingUser = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage("/nerie/users/profile/check-email [POST]"), "json");
        }

        String usercode = existingUser.getUsercode();

        // Basic validation
        if (emailid == null || emailid.trim().isEmpty() || emailid.trim().length() > 50) {
            return ResponseEntity.badRequest().body("Invalid email format.");
        }
        if (usercode == null) {
            return ResponseEntity.badRequest().body("User identifier missing.");
        }

        boolean exists = mtUserloginService.checkUserExists(emailid.trim(), usercode);

        return ResponseEntity.ok(exists ? "1" : "");
    }

    @Transactional(propagation = Propagation.REQUIRED)
    private void logAuditTrail(HashMap<String, String> auditMap, String userId, String actionTaken) {
        if (auditMap != null) {
            auditMap.put("userid", userId);
            auditMap.put("actiontaken", actionTaken);
            audittrailService.saveAuditTrail(auditMap);
        }
    }

    /*
     * Secured endpoint
     */
    @GetMapping(value = "/home")
    public String renderHomePage(Model model, HttpServletRequest request) throws AuthenticationException {
        MT_Userlogin mtUserlogin;
        try {
            mtUserlogin = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }
        System.out.println("::::::::::" + mtUserlogin.getRole().getRoleCode().toUpperCase());

        switch (mtUserlogin.getRole().getRoleCode().toUpperCase()) {
            case "A":
                model.addAttribute("user", mtUserlogin);
                model.addAttribute("profilepic", ImageUtil.convertToBase64(mtUserlogin.getUserphotograph()));
                model.addAttribute("recentlycompletedphases",
                        mPhasesService.getDashboardRecentlyCompletedPhasesList(3, 2));
                model.addAttribute("Myprogramsaccepted",
                        mProgramsService.getDashboardProgramsByUser(mtUserlogin.getUsercode(), 3));
                model.addAttribute("Myprogramsrejected",
                        mProgramsService.getDashboardProgramsByUserRejected(mtUserlogin.getUsercode(), 3));
                model.addAttribute("All", mProgramsService.getDashboardAll(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));
                model.addAttribute("Ongoing", mProgramsService.getDashboardOngoing(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));
                model.addAttribute("Upcoming", mProgramsService.getDashboardUpcoming(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));
                model.addAttribute("Completed", mProgramsService.getDashboardCompleted(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));
                model.addAttribute("Closed", mProgramsService.getDashboardClosed(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));
                model.addAttribute("Archived", mProgramsService.getDashboardArchived(mtUserlogin.getUsercode(),
                        mtUserlogin.getMoffices().getOfficecode(), mtUserlogin.getUserrole()));

                return "pages/local-admin/home";
            case "T": // Student
                T_Students tStudents = tStudentsService.findByUsercode(mtUserlogin);
                Set<T_Notifications> tNotifications = tNotificationsService.findByReceivertype("Students");
                List<Object[]> pendingAssignments = tStudentAssignmentService
                        .findAssignmentDetailsByUsercode(mtUserlogin.getUsercode());

                model.addAttribute("myinfo", tStudents);
                model.addAttribute("studentImage", ImageUtil.convertToBase64(mtUserlogin.getUserphotograph()));
                model.addAttribute("notifications", !tNotifications.isEmpty() ? tNotifications : List.of());
                model.addAttribute("pendingAssignments",
                        !pendingAssignments.isEmpty() ? pendingAssignments : List.of());

                return "pages/t_students/home";
            case "P":
                try {
                    model.addAttribute("myinfo", mtUserlogin);
                    byte[] photoBytes = mtUserlogin.getUserphotograph();
                    String base64Image = null; // Default to null
                    if (photoBytes != null && photoBytes.length > 0) {
                        try {
                            base64Image = ImageUtil.convertToBase64(photoBytes);
                        } catch (Exception imgEx) {
                            imgEx.printStackTrace();
                        }
                    }
                    model.addAttribute("participantImage", base64Image);

                    return "pages/t_participants/home";
                } catch (Exception e) {
                    model.addAttribute("errorMessage", "Could not load participant dashboard data.");
                    return "pages/error/404";
                }
            case "S":
                try {
                    model.addAttribute("LOGIN", mtUserlogin);
                    model.addAttribute("profilepic", ImageUtil.convertToBase64(mtUserlogin.getUserphotograph()));
                    List<Object[]> officeWiseCountProgramList = mProgramsService.getOfficeWiseCountProgram();
                    model.addAttribute("OfficeWiseCountProgramList", officeWiseCountProgramList);
                    return "pages/admin/home";
                } catch (Exception e) {
                    return "pages/error/404";
                }
            case "U":
                try {
                    model.addAttribute("LOGIN", mtUserlogin);
                    model.addAttribute("profilepic", ImageUtil.convertToBase64(mtUserlogin.getUserphotograph()));
                    model.addAttribute("recentlycompletedphases", mProgramsService
                            .getDashboardRecentlyCompletedPhasesListByUser(mtUserlogin.getUsercode(), 3, 2));
                    model.addAttribute("Myprogramsaccepted",
                            mProgramsService.getDashboardProgramsByUser(mtUserlogin.getUsercode(), 3));
                    model.addAttribute("Myprogramsrejected",
                            mProgramsService.getDashboardProgramsByUserRejected(mtUserlogin.getUsercode(), 3));
                    model.addAttribute("All", mProgramsService.getCoordinatorDashboardAll(mtUserlogin.getUsercode(),
                            mtUserlogin.getMoffices().getOfficecode()));
                    model.addAttribute("Ongoing", mProgramsService.getCoordinatorDashboardOngoing(
                            mtUserlogin.getUsercode(), mtUserlogin.getMoffices().getOfficecode()));
                    model.addAttribute("Upcoming", mProgramsService.getCoordinatorDashboardUpcoming(
                            mtUserlogin.getUsercode(), mtUserlogin.getMoffices().getOfficecode()));
                    model.addAttribute("Completed", mProgramsService.getCoordinatorDashboardCompleted(
                            mtUserlogin.getUsercode(), mtUserlogin.getMoffices().getOfficecode()));
                    model.addAttribute("Closed", mProgramsService.getCoordinatorDashboardClosed(
                            mtUserlogin.getUsercode(), mtUserlogin.getMoffices().getOfficecode()));
                    model.addAttribute("Archived", mProgramsService.getCoordinatorDashboardArchived(
                            mtUserlogin.getUsercode(), mtUserlogin.getMoffices().getOfficecode()));
                    return "pages/coordinator-faculty/home";
                } catch (Exception e) {
                    return "pages/error/404";
                }
            case "Z":
                model.addAttribute("myinfo", mtUserlogin);
                model.addAttribute("profilePicture", ImageUtil.convertToBase64(mtUserlogin.getUserphotograph()));
                model.addAttribute("facultiesCount",
                        mOfficesService.getOfficeFacultiesCount(mtUserlogin.getMoffices().getOfficecode()));
                model.addAttribute("studentsCount",
                        mOfficesService.getOfficeStudentsCount(mtUserlogin.getMoffices().getOfficecode()));
                model.addAttribute("alumniCount",
                        mOfficesService.getOfficeAlumniCount(mtUserlogin.getMoffices().getOfficecode()));
                model.addAttribute("participantsCount", mOfficesService.getOfficeParticipantsCount());
                model.addAttribute("departmentsCount", mDepartmentsService.getDepartmentsCount());
                model.addAttribute("coordinatorsCount",
                        mtUserloginService.getCoordinatorsCount(mtUserlogin.getMoffices().getOfficecode()));
                model.addAttribute("completedofficeprograms",
                        mProgramsService.getCompletedProgramCount(mtUserlogin.getMoffices().getOfficecode()));
                model.addAttribute("closedofficeprograms",
                        mProgramsService.getClosedProgramCount(mtUserlogin.getMoffices().getOfficecode()));
                return "pages/principal-director/home";
            case "R":
                LocalDate currentDate = LocalDate.now();
                LocalTime currentTime = LocalTime.now();
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

                model.addAttribute("serverDate", currentDate.toString());
                model.addAttribute("serverTime", currentTime.format(timeFormatter));
                return "pages/resource-person/home";
            default:
                throw new MyAuthorizationDeniedException(
                        ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
                                mtUserlogin.getUserid()),
                        "json");
        }
    }

    @GetMapping("/users/dynamic-menu")
    public String renderDynamicMenu(Model model, HttpServletRequest request) {
        MT_Userlogin user;
        try {
            user = mtUserloginService.getUserloginFromAuthentication();
        } catch (Exception ex) {
            throw new MyAuthenticationCredentialsNotFoundException(
                    ExceptionUtil.generateUnAuthenticatedMessage(request.getRequestURI(), request.getMethod()), "json");
        }

        if (!List.of("A", "U", "Z").contains(user.getRole().getRoleCode().toUpperCase())) {
            throw new MyAuthorizationDeniedException(
                    ExceptionUtil.generateAuthorizationDeniedMessage(request.getRequestURI(), request.getMethod(),
                            user.getUserid()),
                    "json");
        }

        List<Object[]> userprocess = mProcessesService.getProcessesForUserNavigation(user.getUsercode());

        Map<String, List<Object[]>> groupedMenu = userprocess.stream()
                .collect(Collectors.groupingBy(
                        item -> (String) item[1], // Group by mainmenuname (st[1])
                        LinkedHashMap::new, // Factory for a LinkedHashMap to preserve order
                        Collectors.toList() // Collect the sub-menu items into a List
                ));

        model.addAttribute("userrole", user.getRole().getRoleCode());
        model.addAttribute("groupedUserProcess", groupedMenu);

        return "pages/dynamic-menu";
    }
}
