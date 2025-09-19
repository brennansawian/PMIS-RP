package com.nic.nerie.t_applications.service;

import com.nic.nerie.t_applications.model.T_Applications;
import com.nic.nerie.t_applications.repository.TApplicationsRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class TApplicationsService {

    private final TApplicationsRepository tApplicationsRepository;

    @Autowired
    public TApplicationsService(TApplicationsRepository tApplicationsRepository) {
        this.tApplicationsRepository = tApplicationsRepository;
    }

    public T_Applications findByApplicationcode(@NotNull @NotBlank String applicationcode) {
        try {
            return tApplicationsRepository.findById(applicationcode).get();
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Transactional(readOnly = false)
    public boolean updateProgramInvitationStatus(String phaseid, String usercode, String remarks, String status) {
        try {
            int updatedRows = tApplicationsRepository.updateStatusAndRemarksByUsercodeAndPhaseid(
                    usercode, phaseid, remarks, status);

            return updatedRows > 0;
        } catch (Exception e) {
            return false;
        }
    }

    @Transactional(readOnly = false)
    public boolean acceptProgramInvitation(String phaseid, String usercode, String remarks) {
        return updateProgramInvitationStatus(phaseid, usercode, remarks, "A");
    }

    @Transactional(readOnly = false)
    public boolean rejectProgramInvitation(String phaseid, String usercode, String remarks) {
        return updateProgramInvitationStatus(phaseid, usercode, remarks, "R");
    }

    public boolean checkParticipantApplicationExists(@NotNull @NotBlank String usercode, @NotNull @NotBlank String phaseid) {
        try {
            return tApplicationsRepository.existsByUsercodeAndPhaseid(usercode, phaseid);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public T_Applications saveApplications(@NotNull T_Applications newApplications) {
        try {
            return tApplicationsRepository.save(newApplications);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_Applications entity | Exception = " + ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> listParticipantsInSession(@NotNull @NotBlank String programtimetablecode, @NotNull @NotBlank String phaseid) {
        try {
            return tApplicationsRepository.findParticipantsInSession(programtimetablecode, phaseid);
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching participants in session | Exception = " + ex);
        }
    }

    @Transactional(readOnly = false)
    public void deleteApplicationByApplicationcode(@NotNull @NotBlank String applicationcode) {
        try {
            tApplicationsRepository.deleteById(applicationcode.trim());
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting T_Applications entity", ex);
        }
    }
}