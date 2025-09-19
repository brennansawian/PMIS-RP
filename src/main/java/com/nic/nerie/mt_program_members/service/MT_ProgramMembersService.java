package com.nic.nerie.mt_program_members.service;

import com.nic.nerie.mt_program_members.model.MT_ProgramMembers;
import com.nic.nerie.mt_program_members.repository.MT_ProgramMembersRepository;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
public class MT_ProgramMembersService {
    private final MT_ProgramMembersRepository mtProgramMembersRepository;

    public MT_ProgramMembersService(MT_ProgramMembersRepository mtProgramMembersRepository) {
        this.mtProgramMembersRepository = mtProgramMembersRepository;
    }

    public List<MT_ProgramMembers> getProgramMembers(String programCode, String phaseId) {
        return mtProgramMembersRepository.getProgramMembers(programCode,phaseId);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void insertProgramMembersFromArraylist(@NotNull @NotEmpty List<String> coordinators, @NotNull @NotBlank String programcode, 
        @NotNull @NotBlank String phaseid) {
        boolean headCoordinatorSaved = false;

        try {
            for (String coordinator : coordinators) {
                if (!headCoordinatorSaved) {
                    // first coordinator in coordinators list is the head co-ordinator
                    mtProgramMembersRepository.createProgramMembersEntry(programcode, coordinator, phaseid, "1", "0");
                    headCoordinatorSaved = true;
                } else
                    mtProgramMembersRepository.createProgramMembersEntry(programcode, coordinator, phaseid, "0", "0");
            }
        } catch (Exception ex) {
            throw new RuntimeException("Error creating m_program_memebers entry", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteByProgramcode(@NotNull @NotBlank String programcode) {
        try {
            if (mtProgramMembersRepository.deleteByProgramcode(programcode.trim()) == 0)
                throw new Exception();
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting MT_ProgramMembers entity by programcode " + programcode, ex);
        }
    }

    public List<Object[]> getMembersByProgramAndPhase(String pcode, String phaseid) {
        try {
            return mtProgramMembersRepository.findMembersByProgramAndPhase(pcode, phaseid);
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String setCoordinatorAsLocalCoordinator(String programMemberIdStr) {
        try {
            if (programMemberIdStr == null || programMemberIdStr.trim().isEmpty()) {
                return "-1";
            }
            Integer programMemberId = Integer.parseInt(programMemberIdStr.trim());

            int rowsUpdated = mtProgramMembersRepository.setAsLocalCoordinator(programMemberId);

            if (rowsUpdated > 0) {
                return "1"; // Success
            } else {
                return "-1"; // Failure (record not found)
            }

        } catch (NumberFormatException e) {
            return "-1"; // Failure (invalid input)
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        }
    }
}
