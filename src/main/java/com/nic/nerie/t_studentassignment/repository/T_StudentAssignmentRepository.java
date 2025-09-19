package com.nic.nerie.t_studentassignment.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.t_studentassignment.model.T_StudentAssignment;

public interface T_StudentAssignmentRepository extends JpaRepository<T_StudentAssignment, String> {
        @Query(value = "SELECT s.subjectname, a.title, a.uploaddate, a.submissiondate, " +
                        "(SELECT assignmentmark FROM nerie.t_studentassignment x WHERE usercode = :usercode AND x.assignmenttestid = a.assignmenttestid LIMIT 1) AS assignmentmarks, "
                        +
                        "(SELECT studentassignmentid FROM nerie.t_studentassignment x WHERE x.assignmenttestid = a.assignmenttestid LIMIT 1) AS studentassignmentid "
                        +
                        "FROM nerie.t_student_subject t, nerie.m_subjects s, nerie.t_assignmenttest a " +
                        "WHERE t.usercode = :usercode AND t.subjectcode = s.subjectcode AND s.subjectcode = a.subjectcode "
                        +
                        "ORDER BY assignmentmarks", nativeQuery = true)
        List<Object[]> findAssignmentDetailsByUsercode(@Param("usercode") String usercode);

        @Query("SELECT a FROM T_StudentAssignment a WHERE a.studentassignmentid = :studentassignmentid")
        Optional<T_StudentAssignment> findByStudentassignmentid(
                        @Param("studentassignmentid") String studentassignmentid);

        @Query(value = "SELECT t1.assignmenttestid, t1.subjectcode, subs.subjectname, t1.reldoc, t1.title, t1.uploaddate, t1.submissiondate, "
                        +
                        "       (SELECT assignmentmark FROM nerie.t_studentassignment x " +
                        "        WHERE x.usercode = :usercode AND x.assignmenttestid = t1.assignmenttestid LIMIT 1) AS assignmentmarks, "
                        +
                        "       (SELECT studentassignmentid FROM nerie.t_studentassignment x " +
                        "        WHERE x.usercode = :usercode AND x.assignmenttestid = t1.assignmenttestid LIMIT 1) AS studentassignmentid, "
                        +
                        "       t1.description, t1.fullmark, t1.submissiontype " +
                        "FROM nerie.t_assignmenttest t1 " +
                        "JOIN nerie.m_subjects subs ON t1.subjectcode = subs.subjectcode " +
                        "WHERE t1.subjectcode IN (SELECT s.subjectcode FROM nerie.m_subjects s " +
                        "                         WHERE s.coursecode = (SELECT coursecode FROM nerie.t_students s WHERE s.usercode = :usercode) "
                        +
                        "                         AND s.sphaseid = (SELECT sphaseid FROM nerie.t_students s WHERE s.usercode = :usercode AND isoptional = '0') "
                        +
                        "                         UNION " +
                        "                         SELECT s.subjectcode FROM nerie.t_student_subject s WHERE s.usercode = :usercode)", nativeQuery = true)
        List<Object[]> getSubmitAssignmentList(String usercode);

        @Query(value = "SELECT * FROM nerie.t_studentassignment WHERE assignmenttestid = :asid", nativeQuery = true)
        List<T_StudentAssignment> getSubmittedAssignmentsByAssignmentTestId(@Param("asid") String asid);

        @Query(value = "SELECT s.rollno, s.fname, s.lname " +
                        "FROM nerie.T_StudentAssignment t " +
                        "JOIN nerie.T_Students s ON t.usercode = s.usercode " +
                        "WHERE t.assignmenttestid = :asid " +
                        "ORDER BY t.usercode", nativeQuery = true)
        List<Object[]> getSubmittedAssignmentStudents(@Param("asid") String asid);

        @Query(value = "SELECT * FROM t_studentassignment " +
                        "WHERE assignmenttestid = :fid AND usercode = :sid", nativeQuery = true)
        Optional<T_StudentAssignment> findStudentAssignmentDocument(@Param("fid") String assignmentTestId, @Param("sid") String userCode);

        @Query("from T_StudentAssignment where assignmenttestid.assignmenttestid=:assignmenttestid and usercode.usercode=:usercode")
        Optional<T_StudentAssignment> findByAssignmentidAndUsercode(@Param("assignmentid") String assignmentid, @Param("usercode") String usercode);

        @Query("select max(cast(studentassignmentid as int)) from T_StudentAssignment")
        Integer getLastUsedAssignmentid();
}
