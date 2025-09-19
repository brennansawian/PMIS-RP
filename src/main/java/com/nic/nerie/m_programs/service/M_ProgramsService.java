package com.nic.nerie.m_programs.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.m_phases.service.M_PhasesService;
import com.nic.nerie.m_programs.model.M_Programs;
import com.nic.nerie.m_programs.repository.M_ProgramsRepository;
import com.nic.nerie.mt_programdetails.model.MT_ProgramDetails;
import com.nic.nerie.mt_programdetails.service.MT_ProgramDetailsService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Service
@Validated
public class M_ProgramsService {
    private final M_ProgramsRepository mProgramsRepository;
    private final M_PhasesService mPhasesService;
    private final MT_ProgramDetailsService mtProgramDetailsService;

    @Autowired
    public M_ProgramsService(M_ProgramsRepository mProgramsRepository, M_PhasesService mPhasesService,
                             MT_ProgramDetailsService mtProgramDetailsService) {
        this.mProgramsRepository = mProgramsRepository;
        this.mPhasesService = mPhasesService;
        this.mtProgramDetailsService = mtProgramDetailsService;
    }

    @Transactional(readOnly = true)
    public Boolean existsByProgramcode(@NotNull @NotBlank String programcode) {
        try {
            return mProgramsRepository.existsById(programcode.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error checking M_Prorgrams Existence by programcode " + programcode, ex);
        }
    }

    @Transactional(readOnly = true)
    public Boolean existsByProgramid(@NotNull @NotBlank String programid) {
        try {
            return mProgramsRepository.existsByProgramid(programid.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error checking M_Prorgrams Existence by programid " + programid, ex);
        }
    } 

    @Transactional(readOnly = true)
    public Boolean existsByProgramname(@NotNull @NotBlank String programname) {
        try {
            return mProgramsRepository.existsByProgramname(programname.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error checking M_Prorgrams Existence by programname " + programname, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getOngoingPrograms() {
        try {
            return mProgramsRepository.getOngoingPrograms();
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retieving ongoing programs", ex);
        }
    }
    
    @Transactional(readOnly = true)
    public List<Object[]> getOngoingProgramsByUsercode(@NotNull @NotBlank String usercode) {
        try {
            return mProgramsRepository.getOngoingProgramsByUsercode(usercode);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retieving ongoing programs by usercode " + usercode, ex);
        }
    }

    /*
     * This is crazy...
     * Refactor: Implement and use each entity's configure and save method
     * Just bored...
     */
    @Transactional(readOnly = false)
    public void saveProgramPhaseVenuesCoordinatorsProgramDetails(
        M_Programs program, 
        List<String> venues, 
        List<String> coordinators, 
        String hasphase, 
        String phasedescription, 
        String startdate, 
        String enddate, 
        String lastdate, 
        String courseclosedate
    ) {
        try {
            // saving M_Program
            program = saveProgramDetails(program);
            
            if (program == null)
                throw new Exception();

            // saving M_Phases
            M_Phases phase = new M_Phases();
            phase.setPhaseid(mPhasesService.generateNextPhaseid());
            phase.setProgramcode(program);
            phase.setPhaseno("1");
            
            if (hasphase.equals("Yes"))
                phase.setPhasedescription(phasedescription);
            
            phase = mPhasesService.savePhaseDetails(phase);
            if (phase == null)
                throw new Exception("Error saving M_Phases");

            // saving M_Venues
            if (venues != null)
                for (String venue : venues) 
                    mProgramsRepository.createProgramvenuesEntry(program.getProgramcode(), venue, phase.getPhaseid());

            // saving MT_Program_Members
            if (coordinators != null) 
                for (String coordinator : coordinators) 
                    mProgramsRepository.createProgramMembersEntry(program.getProgramcode(), coordinator, phase.getPhaseid());

            // saving MT_ProgramDetails
            // last one I swear...
            MT_ProgramDetails programDetails = new MT_ProgramDetails();
            programDetails.setStartdate(new SimpleDateFormat("dd-MM-yyyy").parse(startdate));   // relax its inside try-catch
            programDetails.setEnddate(new SimpleDateFormat("dd-MM-yyyy").parse(enddate));
            programDetails.setLastdate(new SimpleDateFormat("dd-MM-yyyy").parse(lastdate));
            programDetails.setCourseclosedate(new SimpleDateFormat("dd-MM-yyyy").parse(courseclosedate));
            programDetails.setEntrydate(new Date());
            programDetails.setPhaseid(phase);
            programDetails.setProgramcode(program);
            programDetails.setClosed("N");
            programDetails.setFinalized("N");
            programDetails.setTtfinalized("N");
            programDetails.setClosingreport("");
            
            programDetails = mtProgramDetailsService.saveProgramDetails(programDetails);
            if (programDetails == null)
                throw new Exception("Error saving MT_ProgramDetails");
        } catch (Exception ex) {
            throw new RuntimeException("Error saving M_Program and other related entities", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean savePhaseAndProgramDetails(String[] venues, String[] coordinators, String phasedescription,
                                    String startdate, String enddate, String lastdate, String courseclosedate,
                                    String pcode, String programtypeco) {

        M_Phases newPhase = new M_Phases();
        M_Programs newPhaseProgram = new M_Programs();

        System.out.println("[Test] pcode = " + pcode);

        newPhaseProgram.setProgramcode(pcode);
        newPhase.setPhaseid(mPhasesService.generateNextPhaseid());
        newPhase.setPhaseno(mPhasesService.generateNextPhaseno(pcode));
        newPhase.setProgramcode(newPhaseProgram);
        newPhase.setPhasedescription(phasedescription);

        try {
            mPhasesService.savePhaseDetails(newPhase);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return false;
        }

        String phaseid = newPhase.getPhaseid();
        if (venues != null) {
            for (String venue : venues) {
                try {
                    mProgramsRepository.createProgramvenuesEntry(pcode, venue, phaseid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }

        if (coordinators != null) {
            for (String coordinator : coordinators) {
                try {
                    mProgramsRepository.createProgramMembersEntry(pcode, coordinator, phaseid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }

        MT_ProgramDetails newProgramDetails = new MT_ProgramDetails();
        newProgramDetails.setProgramdetailid(mtProgramDetailsService.generateNextProgramdetailid());

        try {
            newProgramDetails.setStartdate(new SimpleDateFormat("dd-MM-yyyy").parse(startdate));
            newProgramDetails.setEnddate(new SimpleDateFormat("dd-MM-yyyy").parse(enddate));
            newProgramDetails.setLastdate(new SimpleDateFormat("dd-MM-yyyy").parse(lastdate));
            newProgramDetails.setCourseclosedate(new SimpleDateFormat("dd-MM-yyyy").parse(courseclosedate));
        } catch (ParseException ex) {
            ex.printStackTrace();
            return false;
        }

        newProgramDetails.setEntrydate(new Date());
        newProgramDetails.setPhaseid(newPhase);
        newProgramDetails.setProgramcode(newPhaseProgram);
        newProgramDetails.setProgramtype(programtypeco);
        newProgramDetails.setClosed("N");
        newProgramDetails.setFinalized("N");
        newProgramDetails.setTtfinalized("N");

        try {
            mtProgramDetailsService.saveProgramDetails(newProgramDetails);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public Boolean updateProgramPhaseProgramDetails(M_Programs updatedProgram, List<String> venues, List<String> coordinators,
                                        String phaseDescription, String startDate, String endDate, String lastDate,
                                        String courseClosedDate, String phaseid) {
        try {
            saveProgramDetails(updatedProgram);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            return false;
        }

        M_Phases updatedPhase = new M_Phases();
        updatedPhase.setPhaseid(phaseid);
        updatedPhase.setProgramcode(updatedProgram);
        updatedPhase.setPhaseno(mPhasesService.getPhasenoByPhaseid(phaseid));
        updatedPhase.setPhasedescription(phaseDescription != null ? phaseDescription.trim() : "");

        try {
            mPhasesService.savePhaseDetails(updatedPhase);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        if (venues != null) {
            mProgramsRepository.deleteProgramVenuesEntryByPhaseid(phaseid);
            for (String venue : venues) {
                try {
                    mProgramsRepository.createProgramvenuesEntry(updatedProgram.getProgramcode(), venue, phaseid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }

        if (coordinators != null) {
            mProgramsRepository.deleteProgramMembersEntryByPhaseid(phaseid);
            for (String coordinator : coordinators) {
                try {
                    mProgramsRepository.createProgramMembersEntry(updatedProgram.getProgramcode(), coordinator, phaseid);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }

        MT_ProgramDetails updatedProgramDetails = new MT_ProgramDetails();
        try {
            updatedProgramDetails.setStartdate(new SimpleDateFormat("dd-MM-yyyy").parse(startDate));
            updatedProgramDetails.setEnddate(new SimpleDateFormat("dd-MM-yyyy").parse(endDate));
            updatedProgramDetails.setLastdate(new SimpleDateFormat("dd-MM-yyyy").parse(lastDate));
            updatedProgramDetails.setCourseclosedate(new SimpleDateFormat("dd-MM-yyyy").parse(courseClosedDate));
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        String programdetailsid = mtProgramDetailsService.getProgramdetailsidByProgramcodeAndPhaseid(
                updatedProgram.getProgramcode(), phaseid);
        updatedProgramDetails.setProgramdetailid(programdetailsid);
        updatedProgramDetails.setProgramcode(updatedProgram);
        updatedProgramDetails.setPhaseid(updatedPhase);
        updatedProgramDetails.setEntrydate(new Date());
        updatedProgramDetails.setClosed("N");
        updatedProgramDetails.setFinalized("N");
        updatedProgramDetails.setTtfinalized("N");

        try {
            mtProgramDetailsService.saveProgramDetails(updatedProgramDetails);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean closeProgram(String programcode, String closingreport) {
        try {
            mProgramsRepository.closeAllProgramDetails(programcode);
            mProgramsRepository.closeProgram(programcode, closingreport);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean unclosePhase(@NotNull String phaseid) {
        try {
            mProgramsRepository.unclosePhaseDetails(phaseid);
            mProgramsRepository.uncloseProgram(phaseid);
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }

        return true;
    }

    public List<Object[]> getDashboardProgramsByUser(String ucode, Integer coursetype) {
        return mProgramsRepository.getDashboardProgramsByUser(ucode, coursetype);
    }

    public List<Object[]> getDashboardProgramsByUserRejected(String ucode, Integer coursetype) {
        return mProgramsRepository.getDashboardProgramsByUserRejected(ucode, coursetype);
    }

    public List<Object[]> getDashboardAll(String uc, String oc, String ur) {
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;
        return mProgramsRepository.getDashboardAll(effectiveUc, oc);
    }

    public List<Object[]> getDashboardOngoing(String uc, String oc, String ur) {
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;
        return mProgramsRepository.getDashboardOngoing(effectiveUc, oc);
    }

    public List<Object[]> getDashboardUpcoming(String uc, String oc, String ur) {
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;
        return mProgramsRepository.getDashboardUpcoming(effectiveUc, oc);
    }

    public List<Object[]> getDashboardCompleted(String uc, String oc, String ur) {
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;
        return mProgramsRepository.getDashboardCompleted(effectiveUc, oc);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getInviteCourseList(String usercode) {
        try {
            return mProgramsRepository.findInviteCourseList(usercode.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Invite Course List by usercode " + usercode, ex);
        }
    }

    public List<Object[]> getDashboardClosed(String uc, String oc, String ur) {
        // Handle user role logic: if 'A', set uc to 'ALL'
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;

        return mProgramsRepository.getDashboardClosed(effectiveUc, oc);
    }

    public List<Object[]> getParticipantProgramsList(String usercode) {
        return mProgramsRepository.findParticipantProgramsList(usercode);
    }

    public List<Object[]> getDashboardArchived(String uc, String oc, String ur) {
        String effectiveUc = "A".equals(ur) ? "ALL" : uc;
        return mProgramsRepository.getDashboardArchived(effectiveUc, oc);
    }

    public List<Object[]> getOfficeWiseCountProgram() {
        return mProgramsRepository.getOfficeWiseCountProgram();
    }

    public List<Object[]> getDashboardRecentlyCompletedPhasesListByUser(String userCode, int courseType, int limit) {
        return mProgramsRepository.getDashboardRecentlyCompletedPhasesListByUser(userCode, courseType, limit);
    }

    public List<Object[]> getCoordinatorDashboardAll(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardAll(userCode, officeCode);
    }
    public List<Object[]> getCoordinatorDashboardOngoing(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardOngoing(userCode, officeCode);
    }

    public List<Object[]> getCoordinatorDashboardUpcoming(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardUpcoming(userCode, officeCode);
    }

    public List<Object[]> getCoordinatorDashboardCompleted(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardCompleted(userCode, officeCode);
    }

    public List<Object[]> getCoordinatorDashboardClosed(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardClosed(userCode, officeCode);
    }

    public List<Object[]> getCoordinatorDashboardArchived(String userCode, String officeCode) {
        return mProgramsRepository.getCoordinatorDashboardArchived(userCode, officeCode);
    }

    public List<Object[]> getProgramVenuesAndRP(String programCode, String phaseId) {
        return mProgramsRepository.getProgramVenuesAndRP(programCode, phaseId);
    }

    @Transactional(readOnly = true)
    public M_Programs getProgram(@NotNull @NotBlank String programCode) {
        try {
            return mProgramsRepository.getProgram(programCode);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving M_Programs with programcode " + programCode, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getOfficeCourseList(String officecode, String finalized) {
        try {
            return mProgramsRepository.getOfficeCourseList(officecode, finalized);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Office Course list by officecode " + officecode + " and finalized " + finalized, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getPhaseCourseList(String officecode, String finalized) {
        try {
            return mProgramsRepository.getPhaseCourseList(officecode, finalized);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Phase Course list by officecode " + officecode + " and finalized " + finalized, ex);
        }
    }

    public List<Object[]> getProgramsByFinancialyear(@NotNull @NotBlank String officecode, @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend) {
        return mProgramsRepository.getProgramsByOfficecodeFinancialyear(officecode, fystart, fyend);
    }

    public List<Object[]> getProgramsByOfficecodeFinancialyearAndUsercode(@NotNull @NotBlank String officecode,
                                                                          @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend, @NotNull @NotBlank String usercode) {
        return mProgramsRepository.getProgramsByOfficecodeFinancialyearAndUsercode(officecode, fystart, fyend, usercode);
    }

    public List<Object[]> getAcceptedProgramsBasedOnFy(@NotNull @NotBlank String officecode, @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend) {
        return mProgramsRepository.findProgramsByFiscalYear(officecode, fystart, fyend);
    }

    public List<Object[]> getAcceptedProgramsBasedOnFyAndUsercode(@NotNull @NotBlank String officecode,
                                                                          @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend, @NotNull @NotBlank String usercode) {
        return mProgramsRepository.findProgramsByFyAndUserCode(officecode, fystart, fyend, usercode);
    }


    @Transactional(readOnly = true)
    public List<Object[]> getUnCloseCourseList(@NotNull @NotBlank String officecode, @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend) {
        try {
            return mProgramsRepository.getUnCloseCourseList(officecode, fystart, fyend);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Unclose Course List", ex);
        }
    }

    public List<Object[]> getCloseCourseList(@NotNull @NotBlank String officecode, @NotNull @NotBlank String fystart, @NotNull @NotBlank String fyend) {
        return mProgramsRepository.getCloseCourseList(officecode, fystart, fyend);
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public M_Programs saveProgramDetails(@NotNull M_Programs newProgram) {
        if (newProgram.getProgramcode() == null || newProgram.getProgramcode().isBlank())
            newProgram.setProgramcode(generateNewProgramcode());

        try {
            return mProgramsRepository.save(newProgram);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving M_Programs entity", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getProgramDetailsBasedOnCode(@NotNull @NotBlank String pcode) {
        try {
            return mProgramsRepository.getProgramDetailsBasedOnCode(pcode);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Program details by programcode " + pcode, ex);
        }
    }

    @Transactional(readOnly = true)
    private String generateNewProgramcode() {
        try {
            Integer lastUsedProgramcode = mProgramsRepository.getLastUsedProgramcode();
            return lastUsedProgramcode == null ? "1" : String.valueOf(lastUsedProgramcode + 1);
        } catch (Exception ex) {
            throw new RuntimeException("Error generating new programcode", ex);
        }
    }

    @Transactional(readOnly = true)
    public String getCompletedProgramCount(@NotNull @NotBlank String officecode) {
        try {
            return String.valueOf(mProgramsRepository.getCompletedProgramCount(officecode.trim()));
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving completed program count for office " + officecode, ex);
        }
    }

    @Transactional(readOnly = true)
    public String getClosedProgramCount(@NotNull @NotBlank String officecode) {
        try {
            return String.valueOf(mProgramsRepository.getClosedProgamCount(officecode.trim()));
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving closed program count", ex);
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public void deleteByProgramcode(@NotNull @NotBlank String programcode) {
        try {
            if (mProgramsRepository.deleteByProgramcode(programcode.trim()) == 0)
                throw new Exception();
        } catch (Exception ex) {
            throw new RuntimeException("Error deleting M_Program entity by programcode " + programcode, ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getProgramDetailsBasedOnCodeToPopulateForm(@NotNull @NotBlank String programcode) {
        try {
            return mProgramsRepository.getProgramDetailsBasedOnCodeToPopulateForm(programcode.trim());
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Program Details Based On Code ToPopulate Form", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getApprovedProgramsByUser(@NotNull @NotBlank String ocode, @NotNull @NotBlank String fystart, 
        @NotNull @NotBlank String fyend, @NotNull @NotBlank String finalized, @NotNull @NotBlank String ucode) {
        try {
            return mProgramsRepository.getApprovedProgramsByUser(ocode, fystart, fyend, finalized, ucode);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Approved programs by user", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getApprovedPrograms(@NotNull @NotBlank String ocode, @NotNull @NotBlank String fystart, 
        @NotNull @NotBlank String fyend, @NotNull @NotBlank String finalized) {
        try {
            return mProgramsRepository.getApprovedPrograms(ocode, fystart, fyend, finalized);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Approved programs by user", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getRejectedProgramByUser(@NotNull @NotBlank String ocode, @NotNull @NotBlank String fystart, 
        @NotNull @NotBlank String fyend, @NotNull @NotBlank String finalized, @NotNull @NotBlank String ucode) {
        try {
            return mProgramsRepository.getRejectedProgramsByUser(ocode, fystart, fyend, finalized, ucode);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Rejected programs by user", ex);
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getRejectedPrograms(@NotNull @NotBlank String ocode, @NotNull @NotBlank String fystart, 
        @NotNull @NotBlank String fyend, @NotNull @NotBlank String finalized) {
        try {
            return mProgramsRepository.getRejectedPrograms(ocode, fystart, fyend, finalized);
        } catch (Exception ex) {
            throw new DataAccessResourceFailureException("Error retrieving Rejected programs by user", ex);
        }
    }
}

