package com.nic.nerie.t_studentleave.service;

import com.nic.nerie.t_studentleave.model.T_StudentLeave;
import com.nic.nerie.t_studentleave.repository.T_StudentLeaveRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class T_StudentLeaveService {
    private final T_StudentLeaveRepository tStudentLeaveRepository;

    @Autowired
    public T_StudentLeaveService(T_StudentLeaveRepository tStudentLeaveRepository) {
        this.tStudentLeaveRepository = tStudentLeaveRepository;
    }

    public T_StudentLeave findByStudentleaveid(String studentleaveid) {
        Optional<T_StudentLeave> studentLeave = tStudentLeaveRepository.findByStudentleaveid(studentleaveid);

        if (studentLeave.isPresent())
            return studentLeave.get();

        return null;
    }

    public List<T_StudentLeave> getOwnLeaveApplications(String studentid) {
        return tStudentLeaveRepository.getByStudentid(studentid);
    }

    @Transactional(readOnly = false)
    public T_StudentLeave saveLeaveApplication(T_StudentLeave tStudentLeave) {
        try {
            return tStudentLeaveRepository.save(tStudentLeave);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_StudentLeave entity", ex);
        }
    }

    public List<T_StudentLeave> getAllStudentLeaveApplications() {
        return tStudentLeaveRepository.getAllStudentLeaveApplications();
    }

    public List<T_StudentLeave> getPStudentLeaveApplications() {
        return tStudentLeaveRepository.getPStudentLeaveApplications();
    }

    public List<T_StudentLeave> getMStudentLeaveApplications() {
        return tStudentLeaveRepository.getMStudentLeaveApplications();
    }

    public List<T_StudentLeave> getFStudentLeaveApplications() {
        return tStudentLeaveRepository.getFStudentLeaveApplications();
    }

    public List<T_StudentLeave> getDStudentLeaveApplications() {
        return tStudentLeaveRepository.getDStudentLeaveApplications();
    }

    public List<T_StudentLeave> getCWStudentLeaveApplications() {
        return tStudentLeaveRepository.getCWStudentLeaveApplications();
    }

    @Transactional(readOnly = false)
    public T_StudentLeave saveStudentLeave(@NotNull @NotBlank T_StudentLeave studentLeave) {
        try {
            if (studentLeave.getStudentleaveid() == null || studentLeave.getStudentleaveid().isBlank())
                studentLeave.setStudentleaveid(generateNextStudentleaveid());
            else
                System.out.println("[Test] it didn't work");

            return tStudentLeaveRepository.save(studentLeave);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_StudentLeave entity", ex);
        }
    }

    @Transactional
    public String uploadStudentLeaveApplication(T_StudentLeave sl) {
        try {
            if (sl.getStudentleaveid() == null || sl.getStudentleaveid().isEmpty()) {
                Integer maxId = tStudentLeaveRepository.findMaxStudentLeaveId();
                if (maxId == null) {
                    maxId = 0; // Start from 0 so first ID will be 1
                }
                sl.setStudentleaveid(String.valueOf(maxId + 1));
            }

            T_StudentLeave savedLeave = tStudentLeaveRepository.save(sl);
            return savedLeave.getStudentleaveid();
        } catch (Exception e) {
            System.out.println("E::uploadStudentLeaveApplication::" + e);
            return "-1";
        }
    }

    public List<Object[]> getFilteredStudentLeaveList(String status, String fystart, String fyend,
                                                      String sphaseid, String semester, String course,
                                                      String approvedstatus) {
        System.out.println("HERE IN SERVICE");
        List<Object[]> sla = null;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date startDate;
        Date endDate;

        try {
            // Handle date parsing
            if (fystart.equals("all") && fyend.equals("all")) {
                startDate = dateFormat.parse("2000-01-01");
                endDate = dateFormat.parse("2100-12-31");
            } else {
                fystart = fystart + "-01";
                fyend = fyend + "-01";
                startDate = dateFormat.parse(fystart);
                endDate = dateFormat.parse(fyend);
            }

            // Filter logic based on status and approval status
            if ("shortterm".equalsIgnoreCase(status)) {
                if ("APPROVED".equalsIgnoreCase(approvedstatus)) {
                    if ("All".equalsIgnoreCase(sphaseid)) {
                        sla = tStudentLeaveRepository.findApprovedStudentLeavesByCourseAndDateRange(course, startDate, endDate);
                    } else {
                        sla = tStudentLeaveRepository.findStudentLeaveDetailsByCourseDateAndPhase(course, startDate, endDate, sphaseid);
                    }
                } else if ("NOTAPPROVED".equalsIgnoreCase(approvedstatus)) {
                    sla = tStudentLeaveRepository.findNotApprovedStudentLeavesByCourseAndDateRange(course, startDate, endDate);
                } else {
                    if ("All".equalsIgnoreCase(sphaseid)) {
                        sla = tStudentLeaveRepository.findStudentLeaveDetailsByCourseAndDateRange(course, startDate, endDate);
                    } else {
                        sla = tStudentLeaveRepository.findStudentLeaveDetailsByCourseDateAndPhase(course, startDate, endDate, sphaseid);
                    }
                }
            } else if ("longterm".equalsIgnoreCase(status)) {
                if ("APPROVED".equalsIgnoreCase(approvedstatus)) {
                    sla = tStudentLeaveRepository.findApprovedStudentLeavesByCourseDateAndSemester(course, startDate, endDate);
                } else if ("NOTAPPROVED".equalsIgnoreCase(approvedstatus)) {
                    sla = tStudentLeaveRepository.findUnapprovedStudentLeavesByCourseAndDate(course, startDate, endDate);
                } else {
                    if ("All".equalsIgnoreCase(semester)) {
                        sla = tStudentLeaveRepository.findStudentLeavesWithSemesterByCourseAndDateRange(course, startDate, endDate);
                    } else {
                        sla = tStudentLeaveRepository.findStudentLeavesByCourseDateAndSemester(course, startDate, endDate, semester);
                    }
                }
            } else if ("all".equalsIgnoreCase(status)) {
                if ("APPROVED".equalsIgnoreCase(approvedstatus)) {
                    sla = tStudentLeaveRepository.findApprovedStudentLeavesByDateRange(startDate, endDate);
                } else if ("NOTAPPROVED".equalsIgnoreCase(approvedstatus)) {
                    sla = tStudentLeaveRepository.findUnapprovedStudentLeavesByDateRange(startDate, endDate);
                } else {
                    sla = tStudentLeaveRepository.findStudentLeavesByDateRange(startDate, endDate);
                }
            }

        } catch (Exception e) {
            System.err.println("E:: CC:: getFilteredStudentLeaveList:: " + e + " " + e.getMessage());
            e.printStackTrace();
        }

        return sla;
    }

    public T_StudentLeave getStudentLeaveDetails(String fid) {
        Optional<T_StudentLeave> result = tStudentLeaveRepository.findStudentLeaveById(fid);
        return result.orElse(null); // Or throw an exception if preferred
    }

    @Transactional(readOnly = true)
    private String generateNextStudentleaveid() {
        try {
            Integer lastUsedStudentleaveid = tStudentLeaveRepository.findLastUsedStudentleaveid();

            System.out.println("[test] studentleaveid " + lastUsedStudentleaveid);

            return lastUsedStudentleaveid != null ? String.valueOf(lastUsedStudentleaveid + 1) : "1";
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error generating next studentleaveid", ex);
        }
    } 
}
