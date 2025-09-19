package com.nic.nerie.m_phasemoredetails.service;

import com.nic.nerie.m_phasemoredetails.model.M_PhaseMoreDetails;
import com.nic.nerie.m_phasemoredetails.repository.M_PhaseMoreDetailsRepository;
import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.m_phases.repository.M_PhasesRepository;
import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.m_programs.repository.M_ProgramsRepository;
import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class M_PhaseMoreDetailsService {

    private final M_PhaseMoreDetailsRepository phaseMoreDetailsRepository;
    private final M_ProgramsRepository programsRepository;
    private final M_PhasesRepository phasesRepository;

    @Autowired
    public M_PhaseMoreDetailsService(M_PhaseMoreDetailsRepository phaseMoreDetailsRepository, M_ProgramsRepository programsRepository, M_PhasesRepository phasesRepository) {
        this.phaseMoreDetailsRepository = phaseMoreDetailsRepository;
        this.programsRepository = programsRepository;
        this.phasesRepository = phasesRepository;
    }

    public M_PhaseMoreDetails getPhaseMoreDetailsByPhaseId(String phaseid) {
        return phaseMoreDetailsRepository.getPhaseMoreDetailsByPhaseId(phaseid);
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public boolean savePhaseMoreDetails(String[] focusareas, String[] targetgroup, String[] stages,
                                        String budget, String objectives, String methodology, String tools,
                                        String kpindicators, String outcomes, String pcode, String phaseid) {
        try {
            Integer maxId = phaseMoreDetailsRepository.findMaxPhasedetailsId();
            int nextId = (maxId == null) ? 1 : maxId + 1;

            M_Programs program = programsRepository.findById(pcode)
                    .orElseThrow(() -> new EntityNotFoundException("Program not found with code: " + pcode));

            M_Phases phase = phasesRepository.findById(phaseid)
                    .orElseThrow(() -> new EntityNotFoundException("Phase not found with ID: " + phaseid));

            M_PhaseMoreDetails newDetails = new M_PhaseMoreDetails();
            newDetails.setPhasedetailsid(String.valueOf(nextId));
            newDetails.setProgramcode(program);
            newDetails.setPhaseid(phase);
            newDetails.setBudgetproposed(budget);
            newDetails.setMethodology(methodology);
            newDetails.setObjectives(objectives);
            newDetails.setOutcomes(outcomes);
            newDetails.setTools(tools);
            newDetails.setKpindicators(kpindicators);

            if (focusareas != null && focusareas.length > 0) {
                newDetails.setFocusareas(String.join(",", focusareas));
            }
            if (targetgroup != null && targetgroup.length > 0) {
                newDetails.setTargetgroup(String.join(",", targetgroup));
            }
            if (stages != null && stages.length > 0) {
                newDetails.setStage(String.join(",", stages));
            }

            phaseMoreDetailsRepository.save(newDetails);

            return true;

        } catch (EntityNotFoundException e) {
            System.err.println("Entity not found: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("Unexpected error while saving phase details: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to save phase details", e);
        }
    }
}
