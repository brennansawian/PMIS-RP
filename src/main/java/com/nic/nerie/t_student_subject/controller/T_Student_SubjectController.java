package com.nic.nerie.t_student_subject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.nic.nerie.t_student_subject.model.T_Student_Subject;
import com.nic.nerie.t_student_subject.service.T_Student_SubjectService;

@Controller
@RequestMapping("/nerie/student-subject")
public class T_Student_SubjectController {
    private final T_Student_SubjectService studentSubjectService;

    @Autowired
    public T_Student_SubjectController(T_Student_SubjectService studentSubjectService) {
        this.studentSubjectService = studentSubjectService;
    }

    /*
     * Fetches T_Student_Subject by usercode and returns it on success
     * @param usercode The usercode associated with the T_Student_Subject entity
     * @return ResponseEntity OK encapsulating T_Student_Subject entity on success
     * @return ResponseEntity NotFound when T_Student_Subject doesn't exist
     * @return ResponseEntity InternalServerError on database errors
     */
    @GetMapping("/{usercode}")
    public ResponseEntity<?> getTStudentSubjectByUsercode(@PathVariable("usercode") String usercode) {
        try {
            T_Student_Subject studentSubject = studentSubjectService.getTStudentSubjectByUsercode(usercode);
            return studentSubject != null ? ResponseEntity.ok(studentSubject) : ResponseEntity.notFound().build();
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body("Something went wrong");
        }
    }
}
