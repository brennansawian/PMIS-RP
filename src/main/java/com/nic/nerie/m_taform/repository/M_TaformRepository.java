package com.nic.nerie.m_taform.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.nic.nerie.m_taform.model.M_Taform;

@Repository
public interface M_TaformRepository extends JpaRepository<M_Taform, Long> {
     @Query("SELECT new com.nic.nerie.m_taform.dto.TaFormDTO(t.id,t.phase.programcode.programname, t.venue, t.fromdate, t.todate) " +
           "FROM M_Taform t " +
           "WHERE t.rpUserlogin.id = :usercode AND t.islocal = :islocal")
    List<com.nic.nerie.m_taform.dto.TaFormDTO> findTaFormsByUserAndType(@Param("usercode") String usercode,
                                             @Param("islocal") boolean islocal);

}