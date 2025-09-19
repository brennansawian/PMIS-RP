package com.nic.nerie.t_studentassignment.service;

import com.nic.nerie.t_studentassignment.model.T_StudentAssignment;
import com.nic.nerie.t_studentassignment.repository.T_StudentAssignmentRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class T_StudentAssignmentService {
    private final T_StudentAssignmentRepository tStudentAssignmentRepository;

    @Autowired
    public T_StudentAssignmentService(T_StudentAssignmentRepository tStudentAssignmentRepository) {
        this.tStudentAssignmentRepository = tStudentAssignmentRepository;
    }

    public List<Object[]> findAssignmentDetailsByUsercode(String usercode) {
        return tStudentAssignmentRepository.findAssignmentDetailsByUsercode(usercode);
    }

    public List<Object[]> getSubmitAssignmentList(String usercode) {
        return tStudentAssignmentRepository.getSubmitAssignmentList(usercode);
    }

    public T_StudentAssignment getAssignmentSubmissionDetails(String studentassignmentid) {
        Optional<T_StudentAssignment> tStudentAssignment = tStudentAssignmentRepository
                .findByStudentassignmentid(studentassignmentid);

        if (tStudentAssignment.isPresent())
            return tStudentAssignment.get();

        return null;
    }

    public List<T_StudentAssignment> getSubmittedAssignments(String assignmentTestId) {
        return tStudentAssignmentRepository.getSubmittedAssignmentsByAssignmentTestId(assignmentTestId);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getSubmittedAssignmentsStudentsName(String assignmentTestId) {
        try {
            return tStudentAssignmentRepository.getSubmittedAssignmentStudents(assignmentTestId);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Submitted Assignments by assignmentTestId " + assignmentTestId, ex);
        } 
    }

    public T_StudentAssignment getStudentAssignmentDocument(String fid, String sid) {
        Optional<T_StudentAssignment> assignment = tStudentAssignmentRepository
                .findStudentAssignmentDocument(fid, sid);

        return assignment.orElse(null); // or throw exception if not found
    }

    @Transactional(readOnly = true)
    public T_StudentAssignment getStudentAssignmentByAssignmentidAndUsercode(@NotNull @NotBlank String assignmentid, @NotNull @NotBlank String usercode) {
        try {
            Optional<T_StudentAssignment> studentAssignment = tStudentAssignmentRepository
                    .findByAssignmentidAndUsercode(assignmentid.trim(), usercode.trim());
            return studentAssignment.orElse(null); // Return null if not found
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching T_StudentAssignment entity", ex);
        }
    }

    @Transactional
    public String saveStudentAssignmentMarks(String studentassignmentid, String mark) {
        try {
            // Find the existing assignment
            Optional<T_StudentAssignment> optionalAssignment = tStudentAssignmentRepository
                    .findByStudentassignmentid(studentassignmentid);

            if (!optionalAssignment.isPresent()) {
                return "-1"; // Assignment not found
            }

            // Parse and validate the mark
            float markValue = Float.parseFloat(mark);
            if (markValue < 0) {
                return "-1"; // Invalid mark value
            }

            // Update and save the entity
            T_StudentAssignment assignment = optionalAssignment.get();
            assignment.setAssignmentmark(markValue);
            tStudentAssignmentRepository.save(assignment);

            return "1"; // Success
        } catch (NumberFormatException e) {
            return "-1"; // Invalid number format
        }
    }

    @Transactional(readOnly = false)
    public T_StudentAssignment saveStudentAssignment(@NotNull T_StudentAssignment assignment) {
        try {
            if (assignment.getStudentassignmentid() == null || assignment.getStudentassignmentid().isEmpty()) 
                assignment.setStudentassignmentid(getNextAssignmentid());
            return tStudentAssignmentRepository.save(assignment);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_StudentAssignment entity", ex);
        }
    }

    @Transactional(readOnly = true)
    private String getNextAssignmentid() {
        try {
            Integer lastUsedAssignmentid = tStudentAssignmentRepository.getLastUsedAssignmentid();
            return lastUsedAssignmentid != null ? String.valueOf(lastUsedAssignmentid + 1) : "1";
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error fetching last used assignment ID", ex);
        }
    }
}
