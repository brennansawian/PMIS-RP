package com.nic.nerie.m_taform.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nic.nerie.m_taform.dto.TaFormDTO;
import com.nic.nerie.m_taform.model.M_Taform;
import com.nic.nerie.m_taform.repository.M_TaformRepository;

@Service
public class M_TaformService {
    private final M_TaformRepository mTaformRepository;

    @Autowired
    private M_TaformService(M_TaformRepository mTaformRepository) {
    this.mTaformRepository = mTaformRepository;
    }

    // save a new form
    public void saveForm(M_Taform form) {
    mTaformRepository.save(form);
    }

    // get all the submitted forms
    public List<M_Taform> getAllForms() {
    return mTaformRepository.findAll();
    }

    // get 1 receipt of the current logged in user
    public M_Taform getById(Long id) {
    return mTaformRepository.findById(id).get();
    }

    public List<TaFormDTO> getTaFormsByUserAndType(String usercode, boolean islocal) {
        return mTaformRepository.findTaFormsByUserAndType(usercode, islocal);
    }
}
