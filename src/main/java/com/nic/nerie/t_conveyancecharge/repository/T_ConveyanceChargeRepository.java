package com.nic.nerie.t_conveyancecharge.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.nic.nerie.t_conveyancecharge.model.T_ConveyanceCharge;
import java.util.List;
import com.nic.nerie.m_taform.model.M_Taform;



public interface T_ConveyanceChargeRepository extends JpaRepository<T_ConveyanceCharge, Long> {
    List<T_ConveyanceCharge> findByTaformOrderByIdAsc(M_Taform taform);
}