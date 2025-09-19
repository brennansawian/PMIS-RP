package com.nic.nerie.mt_resourcepersons.service;

import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.mt_resourcepersons.model.MT_ResourcePersons;
import com.nic.nerie.mt_resourcepersons.repository.MT_ResourcePersonsRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.apache.commons.collections.functors.ExceptionPredicate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MT_ResourcePersonsService {
    private final MT_ResourcePersonsRepository mtResourcePersonsRepository;

    @Autowired
    public MT_ResourcePersonsService(MT_ResourcePersonsRepository mtResourcePersonsRepository) {
        this.mtResourcePersonsRepository = mtResourcePersonsRepository;
    }

    public List<MT_ResourcePersons> getAllResourcePersons() {
        return mtResourcePersonsRepository.findAll();
    }

    @Transactional(readOnly = true)
    public boolean checkEmailAvailability(@NotNull @NotBlank String emailid) {
        try {
            return !mtResourcePersonsRepository.existsByRpemailid(emailid.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error checking email availability for " + emailid, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllResourcePersonsWithPhase(@NotNull @NotBlank String phaseid) {
        try {
            return mtResourcePersonsRepository.getAllResourcePersonsWithPhase(phaseid);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Resource persons with phaseid " + phaseid,
                    ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getResourcePersonsByPhaseid(@NotNull @NotBlank String phaseid) {
        try {
            return mtResourcePersonsRepository.getResourcePersonsPhaseid(phaseid);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Resource person with phaseid " + phaseid,
                    ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public MT_ResourcePersons saveResourcePersons(@NotNull MT_ResourcePersons newResourcePersons) {
        if (newResourcePersons.getRpslno() == null || newResourcePersons.getRpslno().isBlank())
            newResourcePersons.setRpslno(generateNextRpslno());

        try {
            return mtResourcePersonsRepository.save(newResourcePersons);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving MT_ResourcePersons entity | Exception = " + ex);
        }
    }

    @Transactional
    public Boolean saveResourcePersonsCourseMap(@NotNull M_Programs program, @NotNull @NotBlank String phaseid,
            @NotNull List<String> resourcePersons) {
        try {
            mtResourcePersonsRepository.deleteResourcePersonCourseEntryByPhaseid(phaseid);
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting mt_resourcepersoncoursemap entry | Exception = " + ex);
        }

        for (String resourcePerson : resourcePersons) {
            try {
                mtResourcePersonsRepository.createResourcePersonCourseEntry(phaseid, resourcePerson);
            } catch (Exception ex) {
                throw new RuntimeException("Error creating mt_resourcepersoncoursemap entry | Exception = " + ex);
            }
        }

        return true;
    }

    private String generateNextRpslno() {
        Integer lastUsedRpslno = mtResourcePersonsRepository.getLastUsedRpslno();
        return lastUsedRpslno == null ? "1" : String.valueOf(lastUsedRpslno + 1);
    }

    public List<Object[]> findRPPrograms(String email) {
        return mtResourcePersonsRepository.findRPPrograms(email);
    }
}
