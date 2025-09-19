package com.nic.nerie.t_students.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.t_students.model.T_Students;

public interface T_StudentsRepository extends JpaRepository<T_Students, String> {
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM T_Students s WHERE s.studentid = :studentid")
    boolean existsByStudentid(@Param("studentid") String studentid);

    @Query("SELECT s FROM T_Students s WHERE s.usercode = :usercode")
    Optional<T_Students> findByUsercode(MT_Userlogin usercode);
    
    @Query(value = "SELECT * FROM t_students WHERE usercode = :usercode", nativeQuery = true)
    Optional<T_Students> findByUsercode(String usercode)    ;

    @Query(value = "SELECT s.subjectname, s.subjectcode, " +
                   "COUNT(CASE WHEN att.attendancestatus='P' THEN 1 END) as present_count, " +
                   "COUNT(CASE WHEN att.attendancestatus='A' THEN 1 END) as absent_count, " +
                   "COUNT(att.attendancestatus) as total_count, " +
                   "ROUND(COUNT(CASE WHEN att.attendancestatus='P' THEN 1 END) * 100.0 / COUNT(att.attendancestatus), 2) " +
                   "as attendance_percentage " +
                   "FROM nerie.m_subjects s " +
                   "JOIN nerie.t_studentsattendance att ON att.subjectcode=s.subjectcode " +
                   "WHERE s.semestercode=:semesterscode AND s.coursecode=:coursecode AND att.studentid=:studentid " +
                   "GROUP BY s.subjectcode, s.subjectname", nativeQuery = true)
    List<Object[]> getMyHomePageAttendance(String semesterscode, String coursecode, String studentid);

    @Query(value = "SELECT s FROM T_Students s WHERE s.studentid = :studentid")
    Optional<T_Students> findByStudentid(@Param("studentid") String studentid);

    @Query(value = "SELECT s.usercode, s.studentid, s.fname, s.mname, s.lname, d.departmentname, " +
           "array_to_string(ARRAY(SELECT t.subjectname FROM nerie.m_subjects t, nerie.t_student_subject q " +
           "WHERE q.usercode=s.usercode AND q.subjectcode=t.subjectcode), ',') AS subjects, " +
           "s.email, u.username, u.userid, u.enabled, s.semestercode, s.sphaseid, ms.semestername, " +
           "msp.sphasename, s.isshortterm, s.rollno " +
           "FROM nerie.t_students s " +
           "LEFT JOIN nerie.m_departments d ON s.departmentcode=d.departmentcode " +
           "LEFT JOIN nerie.t_student_subject ss ON s.usercode=ss.usercode " +
           "LEFT JOIN nerie.m_subjects su ON ss.subjectcode=su.subjectcode " +
           "LEFT JOIN nerie.mt_userlogin u ON u.usercode=s.usercode " +
           "LEFT JOIN nerie.m_semesters ms ON ms.semestercode = s.semestercode " +
           "LEFT JOIN nerie.m_shortterm_phases msp ON msp.sphaseid = s.sphaseid " +
           "WHERE iscurrent = '1' " +
           "GROUP BY s.studentid, s.fname, s.mname, s.lname, d.departmentcode, d.departmentname, " +
           "s.usercode, s.email, u.username, u.userid, u.enabled, s.semestercode, s.sphaseid, " +
           "ms.semestername, msp.sphasename, s.isshortterm, s.rollno", nativeQuery = true)
    List<Object[]> getStudentList();

    @Query(value = "SELECT s.rollno FROM T_Students s ORDER BY s.rollno")  
    List<String> getRollnoList();

    @Query("SELECT MAX(CAST(SUBSTRING(s.rollno, 9) AS int)) FROM T_Students s WHERE SUBSTRING(s.rollno, 1, 8) = :prefix")
    Integer findMaxRollNumberSuffix(@Param("prefix") String prefix);

    @Query(value = "SELECT DISTINCT(s.rollno), s.fname, s.mname, s.lname, s.departmentcode, s.semestercode, s.usercode " +
            "FROM nerie.t_students s " +
            "INNER JOIN ( " +
            "    SELECT departmentcode, sphaseid " +
            "    FROM nerie.m_subjects " +
            "    WHERE subjectcode = :subjectcode AND isoptional = '0' " +
            ") AS b ON s.departmentcode = b.departmentcode AND s.sphaseid = b.sphaseid AND s.iscurrent = '1' " +
            "ORDER BY s.rollno", nativeQuery = true)
    List<Object[]> getGeneralPhaseSubjectStudents(@Param("subjectcode") String subjectcode);

    @Query(value = "SELECT DISTINCT(s.rollno), s.fname, s.mname, s.lname, s.departmentcode, s.semestercode, s.usercode " +
            "FROM nerie.t_students s " +
            "INNER JOIN ( " +
            "    SELECT ts.usercode " +
            "    FROM nerie.m_subjects m " +
            "    INNER JOIN nerie.t_student_subject ts ON m.subjectcode = ts.subjectcode " +
            "    WHERE m.subjectcode = :subjectcode AND m.isoptional = '1' " +
            ") AS b ON s.usercode = b.usercode AND s.iscurrent = '1' " +
            "ORDER BY s.rollno", nativeQuery = true)
    List<Object[]> getOptionalSemesterSubjectStudents(@Param("subjectcode") String subjectcode);

    @Query(value = "SELECT DISTINCT(s.rollno), s.fname, s.mname, s.lname, s.departmentcode, s.semestercode, s.usercode " +
            "FROM nerie.t_students s " +
            "INNER JOIN ( " +
            "    SELECT departmentcode, semestercode " +
            "    FROM nerie.m_subjects " +
            "    WHERE subjectcode = :subjectcode AND isoptional = '0' " +
            ") AS b ON s.departmentcode = b.departmentcode AND s.semestercode = b.semestercode AND s.iscurrent = '1' " +
            "ORDER BY s.rollno", nativeQuery = true)
    List<Object[]> getGeneralSemesterSubjectStudents(@Param("subjectcode") String subjectcode);

    @Query(value = "SELECT s.studentid, s.fname, s.mname, s.lname, s.departmentcode, s.semestercode, s.usercode, " +
            "m.subjectcode, ie.testid, ie.internalevaluationid, m.subjectname, ie.marks " +
            "FROM nerie.t_students s " +
            "INNER JOIN nerie.t_student_subject ts ON ts.usercode = s.usercode " +
            "INNER JOIN nerie.m_subjects m ON m.subjectcode = ts.subjectcode " +
            "LEFT JOIN nerie.t_internalevaluationmarks ie ON ie.studentid = s.studentid AND ie.testid = :testid " +
            "WHERE m.subjectcode = :subjectcode",
            nativeQuery = true)
    List<Object[]> getStudentsList(@Param("subjectcode") String subjectcode,
                                   @Param("testid") String testid);

    @Query(value = "SELECT s.studentid, CONCAT(s.fname,' ', s.mname,' ', s.lname) AS Name, d.departmentname, c.coursename, s.email, s.rollno " +
            "FROM t_students s " +
            "JOIN m_departments d ON s.departmentcode = d.departmentcode " +
            "JOIN m_course_academics c ON s.coursecode = c.coursecode " +
            "JOIN m_semesters sem ON s.semestercode = sem.semestercode " +
            "WHERE s.semestercode = :semphase " +
            "AND s.departmentcode = :dcode " +
            "AND s.coursecode = :ccode " +
            "AND s.iscurrent = '1' " +
            "GROUP BY s.studentid, s.fname, s.mname, s.lname, d.departmentname, c.coursename",
            nativeQuery = true)
    List<Object[]> getSubjectListOfStudentsSemester(@Param("dcode") String dcode,
                                                    @Param("ccode") String ccode,
                                                    @Param("semphase") String semphase);

    @Query(value = "SELECT s.studentid, CONCAT(s.fname, ' ', s.mname, ' ', s.lname) AS Name, d.departmentname, c.coursename, s.email, s.rollno " +
            "FROM t_students s " +
            "JOIN m_departments d ON s.departmentcode = d.departmentcode " +
            "JOIN m_course_academics c ON s.coursecode = c.coursecode " +
            "JOIN m_shortterm_phases ph ON s.sphaseid = ph.sphaseid " +
            "WHERE s.sphaseid = :semphase " +
            "AND s.departmentcode = :dcode " +
            "AND s.coursecode = :ccode " +
            "AND s.iscurrent = '1' " +
            "GROUP BY s.studentid, s.fname, s.mname, s.lname, d.departmentname, c.coursename",
            nativeQuery = true)
    List<Object[]> getSubjectListOfStudentsPhase(@Param("dcode") String dcode,
                                                 @Param("ccode") String ccode,
                                                 @Param("semphase") String semphase);

}

