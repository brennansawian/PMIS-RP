package com.nic.nerie.t_participantfeedbacks.service;

import com.nic.nerie.t_participantfeedbacks.model.T_ParticipantFeedbacks;
import com.nic.nerie.t_participantfeedbacks.repository.T_ParticipantFeedbacksRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class T_ParticipantFeedbacksService {
    private final T_ParticipantFeedbacksRepository tParticipantFeedbacksRepository;

    @Autowired
    public T_ParticipantFeedbacksService(T_ParticipantFeedbacksRepository tParticipantFeedbacksRepository) {
        this.tParticipantFeedbacksRepository = tParticipantFeedbacksRepository;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseEntity<String> saveOverallFeedback(T_ParticipantFeedbacks tpfeedback) {
        try {
            if (tpfeedback == null) {
                throw new IllegalArgumentException("Feedback data cannot be null");
            }

            if (tpfeedback.getPfeedbackno() == null || tpfeedback.getPfeedbackno().isEmpty()) {
                Integer maxId = tParticipantFeedbacksRepository.findMaxPfeedbackNo();
                int newId = (maxId == null) ? 1 : maxId + 1;
                tpfeedback.setPfeedbackno(String.valueOf(newId));
            }

            if (tpfeedback.getEntrydate() == null) {
                tpfeedback.setEntrydate(new Date());
            }

            T_ParticipantFeedbacks savedFeedback = tParticipantFeedbacksRepository.save(tpfeedback);

            return ResponseEntity.ok(savedFeedback.getPfeedbackno());

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to save participant feedback", e);
        }
    }

    public T_ParticipantFeedbacks getFeedbackByPhaseIdAndUserCode(String phaseid, String usercode) {
        try {
            return tParticipantFeedbacksRepository.getByPhaseIdAndUserCode(phaseid, usercode);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch participant feedback using native query", e);
        }
    }

    public List<T_ParticipantFeedbacks> getFeedbacksByPhaseId(String phaseid) {
        return tParticipantFeedbacksRepository.findFeedbacksByPhaseId(phaseid);
    }
}
