package com.nic.nerie.m_financialyear.service;

import com.nic.nerie.m_financialyear.repository.M_FinancialYearRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class M_FinancialYearService {
    private final M_FinancialYearRepository mFinancialYearRepository;

    @Autowired
    public M_FinancialYearService(M_FinancialYearRepository mFinancialYearRepository) {
        this.mFinancialYearRepository = mFinancialYearRepository;
    }

    @Transactional(readOnly = true)
    public List<Object[]> getfy() {
        try {
            return mFinancialYearRepository.getAllFinancialYear();
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving all M_FinancialYear", ex);
        }
    }

    public List<Object[]> getFyByUsercode(@NotNull @NotBlank String usercode) {
        return mFinancialYearRepository.getFinancialYearByUsercode(usercode);
    }
}
