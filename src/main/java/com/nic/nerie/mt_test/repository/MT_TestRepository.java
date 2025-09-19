package com.nic.nerie.mt_test.repository;

import com.nic.nerie.mt_test.model.MT_Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MT_TestRepository extends JpaRepository<MT_Test, String> {
    @Query(value = "SELECT t.testid, t.testno, t.testdate, t.testname, t.passmark, t.fullmark, " +
            "s.subjectcode, s.subjectname, se.semestername " +
            "FROM nerie.t_faculty_subject f " +
            "JOIN nerie.m_subjects s ON f.subjectcode = s.subjectcode " +
            "JOIN nerie.m_semesters se ON s.semestercode = se.semestercode " +
            "JOIN nerie.mt_test t ON t.subjectcode = s.subjectcode AND t.usercode = f.usercode " +
            "WHERE f.usercode = :usercode " +
            "ORDER BY se.semestercode",
            nativeQuery = true)
    List<Object[]> getTestList(String usercode);

    @Query(value = "SELECT COALESCE(MAX(CAST(testid AS INTEGER)), 0) FROM nerie.mt_test", nativeQuery = true)
    Integer getMaxTestId();
}
