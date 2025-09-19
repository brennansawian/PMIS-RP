package com.nic.nerie.t_studentsattendance.repository;

import com.nic.nerie.t_studentsattendance.model.T_StudentsAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface T_StudentsAttendanceRepository extends JpaRepository<T_StudentsAttendance, String> {
    @Query(value = "SELECT at.attendancestatus, to_char(at.attendancedate, 'dd-mm-YYYY') AS attendancedate, " +
            "to_char(at.starttime, 'HH12:MI AM') AS starttime, to_char(at.endtime, 'HH12:MI AM') AS endtime, " +
            "s.usercode, at.subjectcode " +
            "FROM nerie.t_studentsattendance at, nerie.t_students s " +
            "WHERE s.usercode = :usercode AND at.subjectcode = :subjectcode AND s.studentid = at.studentid " +
            "AND (:month IS NULL OR cast(EXTRACT(MONTH FROM at.attendancedate) AS varchar) = :month)",
            nativeQuery = true)
    List<Object[]> getStudentAttendanceList(@Param("usercode") String usercode,
                                            @Param("subjectcode") String subjectcode,
                                            @Param("month") String month);
    @Query(value = "SELECT COALESCE(MAX(CAST(studentattendanceid AS integer)), 0) FROM nerie.t_studentsattendance",
            nativeQuery = true)
    Integer getNextAttendanceId();

    @Query(value = "SELECT DISTINCT(CONCAT(to_char(at.starttime,'HH12:MI AM'),'-',to_char(at.endtime,'HH12:MI AM'))) as timelist " +
            "FROM nerie.t_studentsattendance at",
            nativeQuery = true)
    List<String> getTimeList();

    @Query(value = "SELECT at.studentid, CONCAT(s.fname,' ', s.mname,' ', s.lname) as Name, " +
            "array_to_string(array_agg(concat(attendancestatus,'$$',to_char(at.attendancedate,'dd'),'$$', " +
            "(CONCAT(to_char(at.starttime,'HH12:MI AM'),'-',to_char(at.endtime,'HH12:MI AM'))))), ',') " +
            "FROM nerie.t_studentsattendance at " +
            "JOIN nerie.t_students s ON s.studentid = at.studentid " +
            "WHERE at.subjectcode = :subjectcode AND at.usercode = :usercode " +
            "AND (:month IS NULL OR cast(EXTRACT(MONTH FROM at.attendancedate) AS varchar) = :month) " +
            "AND (:time IS NULL OR concat(to_char(at.starttime,'HH12:MI AM'), '-', to_char(at.endtime,'HH12:MI AM')) = :time) " +
            "GROUP BY at.studentid, Name ORDER BY Name",
            nativeQuery = true)
    List<Object[]> getStudentAttendanceDetails(@Param("usercode") String usercode,
                                               @Param("subjectcode") String subjectcode,
                                               @Param("month") String month,
                                               @Param("time") String time);

}
