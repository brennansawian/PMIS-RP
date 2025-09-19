package com.nic.nerie.m_honorarium.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.nic.nerie.m_honorarium.model.M_Honorarium;

@Repository
public interface M_HonorariumRepository extends JpaRepository<M_Honorarium, String> {

}