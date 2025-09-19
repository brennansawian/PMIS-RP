package com.nic.nerie.m_course_academics.service;

import com.nic.nerie.m_course_academics.model.M_Course_Academics;
import com.nic.nerie.m_course_academics.repository.M_Course_AcademicsRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

@Service
public class M_Course_AcademicsService {
    private final M_Course_AcademicsRepository courseAcademicsRepository;

    @Autowired
    public M_Course_AcademicsService(M_Course_AcademicsRepository courseAcademicsRepository) {
        this.courseAcademicsRepository = courseAcademicsRepository;
    }

    public M_Course_Academics getCourseAcademicsByCoursecode(@NotNull @NotBlank String coursecode) {
        coursecode = coursecode.trim();

        try {
            Optional<M_Course_Academics> courseAcademicsOptional = courseAcademicsRepository.findById(coursecode);
            return courseAcademicsOptional.isPresent() ? courseAcademicsOptional.get() : null;
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching M_Course_Academics entity", ex);
        }
    }

    public List<Object[]> getCourseAcademicsByDepartmentcode(@NotNull @NotBlank String departmentcode) {
        departmentcode = departmentcode.trim();

        try {
            return courseAcademicsRepository.getByDepartmentcodeOrderByCoursecodeCoursenameAsc(departmentcode);
        } catch (Exception ex) {
            throw new RuntimeException("Error retrieving M_Course_Academics list by departmentcode = " + departmentcode, ex);
        }
    }

    public Boolean checkCourseExists(@NotNull @NotBlank String coursecode) {
        return courseAcademicsRepository.existsById(coursecode);
    }

    public List<M_Course_Academics> getcoursesbasedondepartment(String departmentcode, String isshortterm) {
        if (!isshortterm.equals("0") && !isshortterm.equals("1")) {
            isshortterm = null;
        }
        return courseAcademicsRepository.findByDepartmentCodeAndShortTerm(departmentcode, isshortterm);
    }
    
    public List<M_Course_Academics> getListOfCoursesForDept(String departmentcode) {
        return courseAcademicsRepository.findByDepartmentCode(departmentcode);
    }
    
    public List<Object[]> getCoursesBasedOnDepartment(String departmentcode, String isshortterm) {
        String shortTermParam = ("0".equals(isshortterm) || "1".equals(isshortterm)) ? isshortterm : null;

        return courseAcademicsRepository.findCoursesByDepartmentAndShortTerm(departmentcode, shortTermParam);
    }

    public List<Object[]> getCourseList(String departmentCode) {
        return courseAcademicsRepository.findCoursesWithDepartmentDetails(departmentCode);
    }

    public List<M_Course_Academics> getCourseList2() {
        return courseAcademicsRepository.findAllOrderedByCourseCodeAndName();
    }

    public boolean checkAcademicCourseExist(M_Course_Academics mcourse) {
        return courseAcademicsRepository.existsByCourseNameAndDepartmentCode(
                mcourse.getCoursename().trim(),
                mcourse.getDepartmentcode().getDepartmentcode()
        );
    }

    public String saveOrUpdateCourse(M_Course_Academics course) {
        try {
            if (course.getCoursecode() == null || course.getCoursecode().trim().isEmpty()) {
                Integer maxCode = courseAcademicsRepository.getMaxCourseCode();
                int newCode = (maxCode == null) ? 1 : maxCode + 1;
                course.setCoursecode(String.valueOf(newCode));
            }

            courseAcademicsRepository.save(course);

            return "2"; // Success
        } catch (Exception ex) {
            return "1"; // Failure
        }
    }
    public M_Course_Academics getCourseByCode(String coursecode) {
        return courseAcademicsRepository.getCourseByCode(coursecode)
                .orElseThrow(() -> new EntityNotFoundException("Course not found with code: " + coursecode));
    }

    public boolean isCourseNameTakenByOtherCourse(String courseName, String departmentCode, String courseCode) {
        return courseAcademicsRepository.isCourseNameTakenByOtherCourse(courseName, departmentCode, courseCode);
    }

    public List<Object[]> getCoursesBasedOnDepartmentFaculty(String departmentcode) {
        return courseAcademicsRepository.getCoursesBasedOnDepartmentFaculty(departmentcode);
    }

    public List<M_Course_Academics> getAllCourseAcademics() {
        return courseAcademicsRepository.findAllCoursesAcademics();
    }
}
