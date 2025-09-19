package com.nic.nerie.t_faculties.service;

import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.repository.MT_UserloginRepository;
import com.nic.nerie.t_faculties.model.T_Faculties;
import com.nic.nerie.t_faculties.repository.T_FacultiesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class T_FacultiesService {
    private final T_FacultiesRepository tFacultiesRepository;
    private final MT_UserloginRepository mtUserloginRepository;

    public T_FacultiesService(T_FacultiesRepository tFacultiesRepository, MT_UserloginRepository mtUserloginRepository) {
        this.tFacultiesRepository = tFacultiesRepository;
        this.mtUserloginRepository = mtUserloginRepository;
    }

    public List<Object[]> getDeptAndFacultyDetails(String usercode) {
        return tFacultiesRepository.getDeptAndFacultyDetails(usercode);
    }

    public T_Faculties getFaculty(String usercode) {
        return tFacultiesRepository.getFacultyByUsercode(usercode);
    }

    public List<Object[]> getFacultySubjectsListByUser(String usercode) {
        try {
            return tFacultiesRepository.findFacultySubjectsAndCoursesByUsercode(usercode);
        } catch (Exception e) {
            return null;
        }
    }

    public List<Object[]> getFacultySubjectsList() {
        try {
            return tFacultiesRepository.findAllFacultySubjectsAndCourses();
        } catch (Exception e) {
            return null;
        }
    }

    public List<Object[]> getFacultyDetails(String usercode) {
        try {
            return tFacultiesRepository.findFacultyDetailsByUsercode(usercode);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Transactional
    public String createFaculty(T_Faculties fac) {
        try {
            if (fac.getFacultyid() == null || fac.getFacultyid().isEmpty()) {
                Integer maxId = tFacultiesRepository.findMaxFacultyId();
                int nextId = (maxId == null) ? 1 : maxId + 1;
                fac.setFacultyid(String.valueOf(nextId));
            }

            // Save or update faculty
            T_Faculties saved = tFacultiesRepository.save(fac);

            // Update mt_userlogin.isfaculty
            mtUserloginRepository.updateIsFaculty(String.valueOf(saved.getUsercode()));

            return saved.getFacultyid();

        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    @Transactional
    public String saveFacultySubjects(String usercode, String[] subjects) {
        try {
            // Delete existing entries
            tFacultiesRepository.deleteFacultySubjectByUsercode(usercode);

            // Insert new subjects
            for (String subjectCode : subjects) {
                tFacultiesRepository.insertFacultySubject(usercode, subjectCode);
            }

            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    @Transactional
    public String saveFacultyCourses(String usercode, String[] courses) {
        try {
            // Delete existing entries
            tFacultiesRepository.deleteFacultyCoursesByUsercode(usercode);

            // Insert new courses
            for (String courseCode : courses) {
                tFacultiesRepository.insertFacultyCourse(usercode, courseCode);
            }

            return "1";
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }

    public T_Faculties getFacultyByFacultyID(String facultyid) {
        return tFacultiesRepository.findFacultyByFacultyId(facultyid);
    }
}
