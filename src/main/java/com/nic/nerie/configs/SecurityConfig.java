package com.nic.nerie.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.nic.nerie.configs.security.filters.JwtFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
        private final JwtFilter jwtFilter;

        @Autowired
        public SecurityConfig(JwtFilter jwtFilter) {
                this.jwtFilter = jwtFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                return http
                                .csrf(csrf -> csrf.disable())
                                .authorizeHttpRequests(request -> request
                                                // Single Authority Routes
                                                // Local Admin
                                                .requestMatchers("/nerie/alumni/manage", "/nerie/alumni/save")
                                                .hasAuthority("A")
                                                // Participant
                                                .requestMatchers(
                                                                "/nerie/participant/edit-profile",
                                                                "/participant/update-profile",
                                                                "/nerie/program/accepted-rejected-programs",
                                                                "/nerie/program/save-accept-program",
                                                                "/nerie/program/save-reject-program",
                                                                "/nerie/program/my-programs",
                                                                "/nerie/participant/feedback/write-overall-feedback",
                                                                "/nerie/participant/feedback/save-overall-feedback")
                                                .hasAuthority("P")
                                                // Resource Person
                                                .requestMatchers(
                                                                "/nerie/resource-persons/**")
                                                .hasAuthority("R")
                                                // Admin
                                                .requestMatchers(
                                                                "/nerie/offices/createoffice",
                                                                "/nerie/offices/saveOfficeDetails",
                                                                "/nerie/reports/report-office-list",
                                                                "/nerie/reports/report-coordinator-list",
                                                                "/nerie/reports/report-course-list",
                                                                "/nerie/admin/audittrail")
                                                .hasAuthority("S")
                                                // Student
                                                .requestMatchers(
                                                                "/nerie/subjects/viewsubjects",
                                                                "/nerie/feedbacks/postsubjectfeedback",
                                                                "/nerie/assignments/viewassignments",
                                                                "/nerie/assignments/viewassignmentsubmission",
                                                                "/nerie/assignments/edit-student-assignment",
                                                                "/nerie/subjects/viewstudymaterials",
                                                                "/nerie/attendance/viewattendance",
                                                                "/nerie/attendance/getattendance",
                                                                "/nerie/student-leaves/application",
                                                                "/nerie/student-leaves/submit-application",
                                                                "/nerie/students/profile", "/nerie/students/info")
                                                .hasAuthority("T")
                                                // Principal-Director
                                                .requestMatchers(
                                                                "/nerie/program/principal-director/manage",
                                                                "/nerie/program-details/principal-director/accept",
                                                                "/nerie/program-details/principal-director/reject",
                                                                "/nerie/program-details/principal-director/delete",
                                                                "/nerie/notifications/manage")
                                                .hasAuthority("Z")
                                                // Multiple Authority Routes
                                                // Local Admin + Coordinator-Faculty
                                                .requestMatchers(
                                                                "/nerie/course-categories/manage",
                                                                "/nerie/course-categories/save",
                                                                "/nerie/qualification-subjects/manage",
                                                                "/nerie/qualification-subjects/save",
                                                                "/nerie/qualification-subjects/map",
                                                                "/nerie/qualification-subjects/map/save",
                                                                "/nerie/venue-rooms/manage", "/nerie/venue-rooms/save",
                                                                "/nerie/holidays/init", "/nerie/holidays/save",
                                                                "/nerie/holidays/remove",
                                                                "/nerie/program/manage", "/nerie/program/inst/save",
                                                                "/nerie/program/batch/save",
                                                                "/nerie/activities/save",
                                                                "/nerie/resource-persons/create",
                                                                "/nerie/resource-persons/save",
                                                                "/nerie/resource-persons/map",
                                                                "/nerie/resource-persons/map/save",
                                                                "/nerie/program-materials/manage",
                                                                "/nerie/program-materials/save",
                                                                "/nerie/program-members/get-program-members",
                                                                "/nerie/program-details/update",
                                                                "/nerie/program-details/save", // New Endpoints
                                                                "/nerie/timetable/program-timetable/create",
                                                                "/nerie/timetable/program-timetable/save",
                                                                "/nerie/participant/manage",
                                                                "/nerie/participant/create",
                                                                "/nerie/participant/remove",
                                                                "/nerie/participant/attendance/manage",
                                                                "/nerie/participant/attendance/save",
                                                                "/nerie/reports/report-attendance-list",
                                                                "/nerie/reports/ReportAttendance",
                                                                "/nerie/course-categories/create-academic-courses",
                                                                "/nerie/course-academics/saveMapDepartmentCourse",
                                                                "/nerie/faculties/register-faculties",
                                                                "/nerie/faculties/createEditFaculty",
                                                                "/nerie/students/manage", "/nerie/students/save",
                                                                "/nerie/students/promotion-student-list",
                                                                "/nerie/students/promoteStudent",
                                                                "/nerie/student-leaves/student-leave-reports",
                                                                "/nerie/student-leaves/ReportStudentLeaveList",
                                                                "/nerie/attendance/upload-attendance",
                                                                "/nerie/attendance/view-student-attendance",
                                                                "/nerie/study-materials/upload-study-materials",
                                                                "/nerie/tests/create-tests",
                                                                "/nerie/tests/saveTestDetails",
                                                                "/nerie/internal-evaluation-marks/upload-internal-evaluation-marks",
                                                                "/nerie/internal-evaluation-marks/saveTestDetails",
                                                                "/nerie/assignments/upload-assignment",
                                                                "/nerie/assignments/editAssignment",
                                                                "/nerie/assignments/view-submitted-assignment",
                                                                "/nerie/assignments/viewStudentUploadAssignmentDocument",
                                                                "/nerie/assignments/saveStudentAssignmentMarks")
                                                .hasAnyAuthority("A", "U")
                                                // Local Admin + Admin
                                                .requestMatchers("/nerie/designations/manage",
                                                                "/nerie/designations/save")
                                                .hasAnyAuthority("A", "S")
                                                // Student + Coordinator-Faculty
                                                .requestMatchers("/nerie/assignments/viewAssignmentDocument")
                                                .hasAnyAuthority("T", "U")
                                                // Local Admin + Principal-Director
                                                .requestMatchers(
                                                                "/nerie/program/close",
                                                                "/nerie/program-details/phase/close",
                                                                "/nerie/program/reopen", "/nerie/program/reopen-phase")
                                                .hasAnyAuthority("A", "Z")
                                                // Local Admin + Admin + Coordinator-Faculty
                                                .requestMatchers(
                                                                "/nerie/venues/manage", "/nerie/venues/save",
                                                                "/nerie/qualifications/manage",
                                                                "/nerie/qualifications/save",
                                                                "/nerie/subjects/create-academic-subjects",
                                                                "/nerie/subjects/saveNewSubject",
                                                                "/nerie/reports/Report",
                                                                "/nerie/qualification-subjects/get-mp-subjects")
                                                .hasAnyAuthority("A", "S", "U")
                                                // Local Admin + Coordinator-Faculty + Principal-Director
                                                .requestMatchers(
                                                                "/nerie/student-leaves/approve-student-leave",
                                                                "/nerie/student-leaves/approveLeaveApplication",
                                                                "/nerie/student-leaves/rejectLeaveApplication",
                                                                "/nerie/student-leaves/view-leave-application-details",
                                                                "/nerie/student-leaves/ReportStudentLeaveList",
                                                                "/nerie/reports/report-program-schedule",
                                                                "/nerie/reports/scheduleReport",
                                                                "/nerie/reports/report-participant-resource-list",
                                                                "/nerie/reports/ReportLA")
                                                .hasAnyAuthority("A", "U", "Z")
                                                // Local Admin + Admin + Principal-Director
                                                .requestMatchers("/nerie/users/manage", "/nerie/users/save")
                                                .hasAnyAuthority("A", "S", "Z")
                                                // Admin + Student + Coordinator-Faculty
                                                .requestMatchers("/nerie/study-materials/viewStudyMaterialDocument")
                                                .hasAnyAuthority("S", "T", "U")
                                                // Local Admin + Student + Coordinator-Faculty + Principal-Director
                                                .requestMatchers("/nerie/student-leaves/view-approval")
                                                .hasAnyAuthority("A", "T", "U", "Z")
                                                // Coordinator-Faculty + Participant
                                                .requestMatchers("/nerie/participant/feedback/daily-feedback/list",
                                                                "/nerie/participant/feedback/daily-feedback/get",
                                                                "/nerie/participant/feedback/view-overall-feedback",
                                                                "/nerie/participant/feedback/overall-feedback/list")
                                                .hasAnyAuthority("U", "P")
                                                // Public Routes
                                                // Landing Routes
                                                .requestMatchers(
                                                                "/nerie/index", "/nerie/about", "/nerie/login", 
                                                                "/nerie/forgotpassword","/nerie/loginresetpassword",
                                                                "/nerie/about/blog*", "/captcha/**",
                                                                "/nerie/participants/**",
                                                                "/nerie/program-details/getMoreOngoingProgramList",
                                                                "/nerie/program-details/getMoreUpcomingProgramList",
                                                                "/nerie/program-details/getMoreUpcomingProgramList",
                                                                "/nerie/reports/publicReport",
                                                                "/nerie/program-details/getMoreCompletedProgramList")
                                                .permitAll()
                                                // Static Resources
                                                .requestMatchers("/resources/**", "/static/**", "/lily/**",
                                                                "/tempscripts/**", "/vendor/**",
                                                                "/fontawesome-free", "/webfonts/**", "/assets/**",
                                                                "/css/**", "/images/**", "/js/**")
                                                .permitAll()
                                                // Error routes
                                                .requestMatchers("/nerie/error/**").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(formLogin -> formLogin.disable())
                                .httpBasic(httpBasic -> httpBasic.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                                .build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder(12);
        }
}
