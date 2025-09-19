package com.nic.nerie.m_honorarium.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nic.nerie.m_honorarium.model.M_Honorarium;
import com.nic.nerie.m_honorarium.repository.M_HonorariumRepository;

@Service
public class M_HonorariumService {
    private final M_HonorariumRepository mHonorariumRepository;

    @Autowired
    private M_HonorariumService(M_HonorariumRepository mHonorariumRepository) {
        this.mHonorariumRepository = mHonorariumRepository;
    }

    // save a new form
    public void saveForm(M_Honorarium form) {
        mHonorariumRepository.save(form);
    }

    // get all submitted forms
    public List<M_Honorarium> getAllForms() {
        return mHonorariumRepository.findAll();
    }

    // get 1 receipt of the current logged in user
    public M_Honorarium getById(String id) {
        return mHonorariumRepository.findById(id).get();
    }
}
