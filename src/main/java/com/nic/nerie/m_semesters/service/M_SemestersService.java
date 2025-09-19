package com.nic.nerie.m_semesters.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import com.nic.nerie.m_semesters.model.M_Semesters;
import com.nic.nerie.m_semesters.repository.M_SemestersRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

@Service
public class M_SemestersService {
    private final M_SemestersRepository mSemestersRepository;

    @Autowired
    public M_SemestersService(M_SemestersRepository mSemestersRepository) {
        this.mSemestersRepository = mSemestersRepository;
    }

    public M_Semesters getSemesterBySemestercode(@NotNull @NotBlank String semestercode) {
        semestercode = semestercode.trim();

        try {
            Optional<M_Semesters> semesterOptional = mSemestersRepository.findById(semestercode);
            return semesterOptional.isPresent() ? semesterOptional.get() : null;
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching M_Semesters entity", ex);
        }
    }

    public Boolean checkSemesterExists(@NotNull @NotBlank String semestercode) {
        return mSemestersRepository.existsById(semestercode);
    }

    public List<M_Semesters> getSemesterList() {
        return mSemestersRepository.findAllByOrderBySemestercodeAscSemesternameAsc();
    }

    public List<Object[]> getMasterSemesters() {
        return mSemestersRepository.getMasterSemesters();
    }
}


