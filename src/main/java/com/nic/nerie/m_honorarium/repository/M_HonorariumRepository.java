package com.nic.nerie.m_honorarium.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.nic.nerie.m_honorarium.model.M_Honorarium;

@Repository
public interface M_HonorariumRepository extends JpaRepository<M_Honorarium, Long> {
    @Query("SELECT new com.nic.nerie.m_honorarium.dto.HonorariumFormDTO(h.id,h.phase.programcode.programname, h.venue, h.fromdate, h.todate) " +
           "FROM M_Honorarium h " +
           "WHERE h.rpUserlogin.id = :usercode")
    List<com.nic.nerie.m_honorarium.dto.HonorariumFormDTO> findHonorariumFormsByUser(@Param("usercode") String usercode);
}