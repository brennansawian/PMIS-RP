package com.nic.nerie.t_conveyancecharge.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nic.nerie.m_taform.model.M_Taform;
import com.nic.nerie.m_taform.repository.M_TaformRepository;
import com.nic.nerie.t_conveyancecharge.model.T_ConveyanceCharge;
import com.nic.nerie.t_conveyancecharge.repository.T_ConveyanceChargeRepository;

@Service
public class T_ConveyanceChargeService {

    private final T_ConveyanceChargeRepository tconveyanceChargeRepository;

        @Autowired
        private T_ConveyanceChargeService(T_ConveyanceChargeRepository tconveyanceChargeRepository) {
        this.tconveyanceChargeRepository= tconveyanceChargeRepository;
        }

        // save a new form
        public void saveForm(T_ConveyanceCharge form) {
        tconveyanceChargeRepository.save(form);
        }

        // get 1 receipt of the current logged in user
        public T_ConveyanceCharge getById(Long id) {
        return tconveyanceChargeRepository.findById(id).get();
        }
        
    }




