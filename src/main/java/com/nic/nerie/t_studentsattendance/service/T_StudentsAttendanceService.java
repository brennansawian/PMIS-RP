package com.nic.nerie.t_studentsattendance.service;

import com.nic.nerie.t_studentsattendance.model.T_StudentsAttendance;
import com.nic.nerie.t_studentsattendance.repository.T_StudentsAttendanceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class T_StudentsAttendanceService {
    private final T_StudentsAttendanceRepository tStudentsAttendanceRepository;

    @Autowired
    public T_StudentsAttendanceService(T_StudentsAttendanceRepository tStudentsAttendanceRepository) {
        this.tStudentsAttendanceRepository = tStudentsAttendanceRepository;
    }

    public List<Object[]> getStudentAttendanceList(String usercode, String subjectcode, String month) {
        return tStudentsAttendanceRepository.getStudentAttendanceList(usercode, subjectcode, month);
    }

    public List<String> getTimeList() {
        return tStudentsAttendanceRepository.getTimeList();
    }

    @Transactional
    public String saveStudentAttendance(T_StudentsAttendance attendance) {
        try {
            if (attendance.getStudentattendanceid() == null || attendance.getStudentattendanceid().isEmpty()) {
                Integer nextId = tStudentsAttendanceRepository.getNextAttendanceId();
                attendance.setStudentattendanceid(String.valueOf(nextId + 1));
            }

            tStudentsAttendanceRepository.save(attendance);
            return attendance.getStudentattendanceid();
        } catch (Exception e) {
            return "-1";
        }
    }

    public List<Object[]> getStudentAttendanceDetails(String usercode, String subjectcode, String month, String time) {
        return tStudentsAttendanceRepository.getStudentAttendanceDetails(usercode, subjectcode, month, time);
    }

}
