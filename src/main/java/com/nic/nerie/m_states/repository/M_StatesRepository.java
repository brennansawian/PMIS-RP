package com.nic.nerie.m_states.repository;

import com.nic.nerie.m_states.model.M_States;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface M_StatesRepository extends JpaRepository<M_States, String> {
    // TODO @Toiar: Implement @Query
    List<M_States> findAllByOrderByStatename();
}