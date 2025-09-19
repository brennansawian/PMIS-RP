package com.nic.nerie.t_internalevaluationmarks.service;

import com.nic.nerie.t_internalevaluationmarks.model.T_InternalEvaluationMarks;
import com.nic.nerie.t_internalevaluationmarks.repository.T_InternalEvaluationMarksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class T_InternalEvaluationMarksService {

    private final T_InternalEvaluationMarksRepository repository;

    @Autowired
    public T_InternalEvaluationMarksService(T_InternalEvaluationMarksRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public String saveStudentInternalEvaluationMarks(T_InternalEvaluationMarks ie) {
        String res = "-1";
        try {
            // Check if internalevaluationid is null or empty
            if (ie.getInternalevaluationid() == null || ie.getInternalevaluationid().trim().isEmpty() || "null".equalsIgnoreCase(ie.getInternalevaluationid())) {
                Integer maxId = repository.findMaxInternalEvaluationId();
                Integer newId = (maxId == null || maxId == 0) ? 1 : maxId + 1;
                ie.setInternalevaluationid(newId.toString());
            }

            repository.save(ie);

            res = ie.getInternalevaluationid();

        } catch (Exception e) {
            e.printStackTrace();
            res = "-1"; // error
        }
        return res;
    }
}
