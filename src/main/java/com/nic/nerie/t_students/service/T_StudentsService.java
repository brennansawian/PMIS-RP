package com.nic.nerie.t_students.service;

import com.nic.nerie.m_course_academics.model.M_Course_Academics;
import com.nic.nerie.m_course_academics.service.M_Course_AcademicsService;
import com.nic.nerie.m_semesters.model.M_Semesters;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.t_students.model.T_Students;
import com.nic.nerie.t_students.repository.T_StudentsRepository;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class T_StudentsService {
    private final T_StudentsRepository tStudentsRepository;
    private final M_Course_AcademicsService mCourseAcademicsService;

    @Autowired
    public T_StudentsService(T_StudentsRepository tStudentsRepository, M_Course_AcademicsService mCourseAcademicsService) {
        this.tStudentsRepository = tStudentsRepository;
        this.mCourseAcademicsService = mCourseAcademicsService;
    }

    public Boolean existsByStudentid(@NotNull @NotBlank String studentid) {
        studentid = studentid.trim();

        try {
            return tStudentsRepository.existsByStudentid(studentid);
        } catch (Exception ex) {
            throw new RuntimeException("Error checking existence of student with ID: " + studentid, ex);
        }
    }

    public T_Students findByUsercode(MT_Userlogin usercode) {
        Optional<T_Students> tStudents = tStudentsRepository.findByUsercode(usercode);

        if (tStudents.isPresent())
            return tStudents.get();

        return null;
    }

    @Transactional(readOnly = true)
    public T_Students findByUsercode(@NotNull @NotBlank String usercode) {
        try {
            Optional<T_Students> tStudents = tStudentsRepository.findByUsercode(usercode);
            return tStudents.isPresent() ? tStudents.get() : null;
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error fetching T_Students entity by usercode", ex);
        }
    }

    public T_Students findByStudentid(@NotNull @NotBlank String studentid) {
        try {
            Optional<T_Students> studentOptional = tStudentsRepository.findByStudentid(studentid);
            return studentOptional.isPresent() ? studentOptional.get() : null;
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching T_Students entity", ex);
        }
    }

    /*
     * This method retrieves all the rollno of students in a list
     * TODO: Change List<T_Students> to List<Strings> because it doesn't make any sense.
     */
    public List<String> getRollnoList() {
        try {
            return tStudentsRepository.getRollnoList();
        } catch (Exception ex) {
            throw new RuntimeException("Error retrieving T_Students rollno list", ex);
        }
    }

    public List<Object[]> getMyHomePageAttendance(M_Semesters semesterscode,
                                                  M_Course_Academics coursecode,
                                                  String studentid) {
        return tStudentsRepository.getMyHomePageAttendance(semesterscode.getSemestercode(), coursecode.getCoursecode(), studentid);
    }

    public List<Object[]> getStudentList() {
        return tStudentsRepository.getStudentList();
    }

    /*
     * This method is used to configure rollno & student id and save a new T_Students entity.
     * @param newStudent The T_Students entity to be configured and saved.
     * @return The saved T_Students entity with configured rollno and studentid.
     * @throws RuntimeException if there is an error during the configuration or save operation.
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public T_Students configureAndSaveTStudentsEntity(@NotNull T_Students newStudent) {
        String temporaryRollno = "";

        // setting rollno for new student based on isshortterm and courseAcademics
        try {
            M_Course_Academics courseAcademics = mCourseAcademicsService.getCourseAcademicsByCoursecode(newStudent.getCoursecode().getCoursecode());
            if (newStudent.getIsshortterm().equals("1")) {
                newStudent.setSemestercode(null);
                temporaryRollno = "NE" + courseAcademics.getCourseid() + newStudent.getAcademicyear().substring(2, 4);
            } else if (newStudent.getIsshortterm().equals("0")) {
                newStudent.setSphaseid(null);
                temporaryRollno = newStudent.getAcademicyear().substring(2, 4) + "NER" + courseAcademics.getCourseid();
            } else
                throw new RuntimeException("Invalid ishortterm flag");

            // assigning suffix to rollno and setting it to studentid and rollno
            try {
                Integer maxId = tStudentsRepository.findMaxRollNumberSuffix(temporaryRollno);
                int nextId = (maxId == null) ? 1 : maxId + 1;
                String suffix;

                if (newStudent.getIsshortterm().equals("1"))
                    suffix = String.format("%02d", nextId);
                else
                    suffix = String.format("%03d", nextId);

                String finalId = temporaryRollno + suffix;
                newStudent.setStudentid(finalId);
                newStudent.setRollno(finalId);
            } catch (Exception ex) {
                throw new RuntimeException("Error fetching max roll number suffix for prefix: " + temporaryRollno, ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error configuring roll number for new student - " + ex.getMessage(), ex);
        }

        // saving student instance
        try {
            return tStudentsRepository.save(newStudent);
        } catch (RuntimeException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }

    /*
     * This method is used to update or save a T_Students entity.
     * @param newStudent The T_Students entity to be updated or saved.
     * @return The updated or saved T_Students entity.
     * @throws IllegalArgumentException if the newStudent is null.
     * @throws RuntimeException if there is an error during the save operation.
    */
    @Transactional(propagation = Propagation.REQUIRED)
    public T_Students updateOrSaveTStudentsEntity(@NotNull T_Students newStudent) {
        try {
            return tStudentsRepository.save(newStudent);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_Students entity", ex);
        }
    }

    public List<Object[]> getGeneralPhaseSubjectStudents(String subjectcode) {
        return tStudentsRepository.getGeneralPhaseSubjectStudents(subjectcode);
    }

    public List<Object[]> getOptionalSemesterSubjectStudents(String subjectcode) {
        return tStudentsRepository.getOptionalSemesterSubjectStudents(subjectcode);
    }

    public List<Object[]> getGeneralSemesterSubjectStudents(String subjectcode) {
        return tStudentsRepository.getGeneralSemesterSubjectStudents(subjectcode);
    }

    public List<Object[]> getStudentsList(String subjectcode, String testid) {
        return tStudentsRepository.getStudentsList(subjectcode, testid);
    }

    public List<Object[]> getSubjectListOfStudentsSemester(String dcode, String ccode, String semphase) {
        return tStudentsRepository.getSubjectListOfStudentsSemester(dcode, ccode, semphase);
    }

    public List<Object[]> getSubjectListOfStudentsPhase(String dcode, String ccode, String semphase) {
        return tStudentsRepository.getSubjectListOfStudentsPhase(dcode, ccode, semphase);
    }

    public String updateStudent2(T_Students student) {
        try {
            tStudentsRepository.save(student); // saveOrUpdate equivalent in JPA
            return "1"; // Success
        } catch (Exception e) {
            e.printStackTrace();
            return "-1"; // Error
        }
    }
}
