package com.nic.nerie.t_internalevaluationmarks.repository;

import com.nic.nerie.t_internalevaluationmarks.model.T_InternalEvaluationMarks;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface T_InternalEvaluationMarksRepository extends JpaRepository<T_InternalEvaluationMarks, String> {
    @Query(value = "SELECT COALESCE(MAX(CAST(internalevaluationid AS INTEGER)), 0) FROM T_InternalEvaluationMarks", nativeQuery = true)
    Integer findMaxInternalEvaluationId();
}
