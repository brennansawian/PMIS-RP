package com.nic.nerie.m_subjects.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.m_subjects.model.M_Subjects;

public interface M_SubjectRepository extends JpaRepository<M_Subjects, String> {
        @Query(value = "SELECT s.subjectname, " +
                        "string_agg(DISTINCT(f.fname || ' ' || f.mname || ' ' || f.lname), ',') as teachers, " +
                        "f.facultyid, s.subjectcode, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'P' THEN 1 ELSE NULL END) as present_count, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'A' THEN 1 ELSE NULL END) as absent_count, " +
                        "COUNT(att.attendancestatus) as total_count " +
                        "FROM nerie.m_subjects s " +
                        "JOIN nerie.t_faculty_subject fs ON fs.subjectcode = s.subjectcode " +
                        "JOIN nerie.t_faculties f ON fs.usercode = f.usercode " +
                        "JOIN nerie.t_students st ON st.studentid = :studentid " +
                        "LEFT JOIN nerie.t_studentsattendance att ON att.usercode = fs.usercode AND att.studentid = :studentid "
                        +
                        "WHERE s.semestercode = :semestercode AND s.coursecode = :coursecode AND s.isoptional = '0' " +
                        "GROUP BY s.subjectname, f.facultyid, s.subjectcode", nativeQuery = true)
        List<Object[]> getGeneralStudentFacultySubjectListLongterm(@Param("semestercode") String semestercode,
                        @Param("coursecode") String coursecode,
                        @Param("studentid") String studentid);

        @Query(value = "SELECT s.subjectname, " +
                        "string_agg(DISTINCT(f.fname || ' ' || f.mname || ' ' || f.lname), ',') as teachers, " +
                        "f.facultyid, s.subjectcode, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'P' THEN 1 ELSE NULL END) as present_count, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'A' THEN 1 ELSE NULL END) as absent_count, " +
                        "COUNT(att.attendancestatus) as total_count " +
                        "FROM nerie.t_student_subject ts, nerie.m_subjects s, nerie.t_faculty_subject fs, nerie.t_faculties f, nerie.t_studentsattendance att "
                        +
                        "WHERE ts.usercode = :usercode AND ts.subjectcode = s.subjectcode AND ts.isactive = '1' AND " +
                        "fs.subjectcode = s.subjectcode AND fs.usercode = f.usercode AND att.usercode = fs.usercode AND att.studentid = :studentid "
                        +
                        "GROUP BY s.subjectcode, s.subjectname, f.facultyid", nativeQuery = true)
        List<Object[]> getoptionalstudentfacultysubjectlist(@Param("usercode") String usercode,
                        @Param("studentid") String studentid);

        @Query(value = "SELECT s.subjectname, s.subjectcode " +
                        "FROM nerie.m_subjects s, nerie.t_student_subject t " +
                        "WHERE t.subjectcode = s.subjectcode AND t.usercode = :usercode", nativeQuery = true)
        List<Object[]> getStudentSubjectsList(@Param("usercode") String usercode);

        @Query("FROM M_Subjects ORDER BY subjectcode")
        List<M_Subjects> findAllByOrderBySubjectcodeAsc();

        @Query("FROM M_Subjects WHERE departmentcode.departmentcode = :departmentcode " +
                        "AND semestercode.semestercode = :semestercode " +
                        "AND coursecode.coursecode = :coursecode " +
                        "ORDER BY subjectcode")
        List<M_Subjects> findByDepartmentAndSemesterAndCourse(
                        @Param("departmentcode") String departmentcode,
                        @Param("semestercode") String semestercode,
                        @Param("coursecode") String coursecode);

        @Query("FROM M_Subjects WHERE departmentcode.departmentcode = :departmentcode " +
                        "AND sphaseid.sphaseid = :sphaseid " +
                        "AND coursecode.coursecode = :coursecode " +
                        "ORDER BY subjectcode")
        List<M_Subjects> findByDepartmentAndPhaseAndCourse(
                        @Param("departmentcode") String departmentcode,
                        @Param("sphaseid") String sphaseid,
                        @Param("coursecode") String coursecode);

        @Query("FROM M_Subjects m WHERE m.departmentcode.departmentcode = :departmentcode " +
                        "AND m.semestercode.semestercode = :spcode AND m.isoptional = '1'")
        List<M_Subjects> findNextSemesterOptionalSubjects(@Param("departmentcode") String departmentcode,
                        @Param("spcode") String spcode);

        @Query("FROM M_Subjects m WHERE m.departmentcode.departmentcode = :departmentcode " +
                        "AND m.sphaseid.sphaseid = :spcode AND m.isoptional = '1'")
        List<M_Subjects> findNextPhaseOptionalSubjects(@Param("departmentcode") String departmentcode,
                        @Param("spcode") String spcode);

        @Query(value = "SELECT DISTINCT s.subjectcode, s.subjectname " +
                        "FROM nerie.m_subjects s " +
                        "JOIN nerie.t_faculty_subject t ON s.subjectcode = t.subjectcode " +
                        "WHERE t.usercode = :usercode", nativeQuery = true)
        List<Object[]> getSubjectsList(@Param("usercode") String usercode);

        @Query(value = "SELECT * FROM nerie.m_subjects WHERE subjectcode = :subjectcode", nativeQuery = true)
        M_Subjects getSubjectBySubjectCode(@Param("subjectcode") String subjectcode);

        @Query(value = "SELECT " +
                        "s.subjectcode, s.subjectname, d.departmentname, st.semestername, " +
                        "s.departmentcode, s.semestercode, stp.sphasename, s.sphaseid, " +
                        "s.isshortterm, s.coursecode, c.coursename, s.isoptional " +
                        "FROM nerie.m_subjects s " +
                        "INNER JOIN nerie.m_departments d ON d.departmentcode = s.departmentcode " +
                        "LEFT JOIN nerie.m_semesters st ON st.semestercode = s.semestercode " +
                        "LEFT JOIN nerie.m_shortterm_phases stp ON stp.sphaseid = s.sphaseid " +
                        "LEFT JOIN nerie.m_course_academics c ON c.coursecode = s.coursecode " +
                        "WHERE s.subjectname IS NOT NULL " +
                        "AND s.departmentcode = :dcode " +
                        "AND s.sphaseid = :sphase " +
                        "AND c.coursecode = :coursecode " +
                        "ORDER BY s.subjectcode", nativeQuery = true)
        List<Object[]> getSubjectListByPhaseid(@Param("dcode") String departmentCode,
                        @Param("sphase") String shortTermPhaseId,
                        @Param("coursecode") String courseCode);

        @Query(value = "SELECT " +
                        "s.subjectcode, s.subjectname, d.departmentname, st.semestername, " +
                        "s.departmentcode, s.semestercode, stp.sphasename, s.sphaseid, " +
                        "s.isshortterm, s.coursecode, c.coursename, s.isoptional " +
                        "FROM nerie.m_subjects s " +
                        "INNER JOIN nerie.m_departments d ON d.departmentcode = s.departmentcode " +
                        "LEFT JOIN nerie.m_semesters st ON st.semestercode = s.semestercode " +
                        "LEFT JOIN nerie.m_shortterm_phases stp ON stp.sphaseid = s.sphaseid " +
                        "LEFT JOIN nerie.m_course_academics c ON c.coursecode = s.coursecode " +
                        "WHERE s.subjectname IS NOT NULL " +
                        "AND s.departmentcode = :dcode " +
                        "AND s.semestercode = :scode " +
                        "AND c.coursecode = :coursecode " +
                        "ORDER BY s.subjectcode", nativeQuery = true)
        List<Object[]> getSubjectListBySemestercode(@Param("dcode") String departmentCode,
                        @Param("scode") String semesterCode,
                        @Param("coursecode") String courseCode);

        @Query(value = "SELECT MAX(CAST(subjectcode AS INTEGER)) FROM nerie.m_subjects", nativeQuery = true)
        Integer getMaxSubjectCode();

        @Query(value = "SELECT * FROM m_subjects ORDER BY subjectcode", nativeQuery = true)
        List<M_Subjects> findAllSubjectsList();

        @Query(value = "SELECT s.subjectcode, s.subjectname " + // Only subjectcode and subjectname are strictly needed
                                                                // by JS for display, but subjectcode is key
                        "FROM nerie.t_faculty_subject tfs " +
                        "JOIN nerie.m_subjects s ON tfs.subjectcode = s.subjectcode " +
                        // "JOIN nerie.m_semesters e ON s.semestercode = e.semestercode " +
                        "WHERE tfs.usercode = :usercode " +
                        "ORDER BY s.subjectcode", nativeQuery = true)
        List<Object[]> findSubjectsByFacultyUsercode(@Param("usercode") String usercode);

        @Query(value = "SELECT s.subjectname, " +
                        "string_agg(DISTINCT(f.fname || ' ' || f.mname || ' ' || f.lname), ',') as teachers, " +
                        "f.facultyid, s.subjectcode, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'P' THEN 1 ELSE NULL END) as present_count, " +
                        "COUNT(CASE WHEN att.attendancestatus = 'A' THEN 1 ELSE NULL END) as absent_count, " +
                        "COUNT(att.attendancestatus) as total_count " +
                        "FROM nerie.m_subjects s " +
                        "JOIN nerie.t_faculty_subject fs ON fs.subjectcode = s.subjectcode " +
                        "JOIN nerie.t_faculties f ON fs.usercode = f.usercode " +
                        "JOIN nerie.t_students st ON st.studentid = :studentid " +
                        "LEFT JOIN nerie.t_studentsattendance att ON att.usercode = fs.usercode AND att.studentid = :studentid "
                        +
                        "WHERE s.sphaseid = :sphaseid AND s.coursecode = :coursecode AND s.isoptional = '0' " +
                        "GROUP BY s.subjectname, f.facultyid, s.subjectcode", nativeQuery = true)
        List<Object[]> getGeneralStudentFacultySubjectListShortterm(@Param("sphaseid") String sphaseid,
                        @Param("coursecode") String coursecode,
                        @Param("studentid") String studentid);

        // @Query(value = "SELECT * FROM m_subjects WHERE departmentcode =
        // :departmentcode AND semestercode = :spcode AND isoptional = '1'", nativeQuery
        // = true)
        // List<M_Subjects> findNextSemesterOptionalSubjects(String departmentcode,
        // String spcode);

        // @Query(value = "SELECT * FROM m_subjects WHERE departmentcode =
        // :departmentcode AND sphaseid = :spcode AND isoptional = '1'", nativeQuery =
        // true)
        // List<M_Subjects> findNextPhaseOptionalSubjects(@Param("departmentcode")
        // String departmentcode,
        // @Param("spcode") String spcode);
}
