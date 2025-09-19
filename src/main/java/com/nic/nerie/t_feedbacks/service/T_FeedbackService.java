package com.nic.nerie.t_feedbacks.service;

import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.t_feedbacks.model.T_Feedbacks;
import com.nic.nerie.t_feedbacks.repository.T_FeedbacksRepository;
import com.nic.nerie.t_feedbacksday.model.T_Feedbacksday; // Import day feedback model
import com.nic.nerie.t_feedbacksday.repository.T_FeedbacksdayRepository; // Import day feedback repo
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import com.nic.nerie.mt_userlogin.service.MT_UserloginService; // To get user object
import com.nic.nerie.t_programtimetable.model.T_ProgramTimeTable; // Import timetable model
import com.nic.nerie.t_programtimetable.service.T_ProgramTimeTableService; // To get timetable object
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class T_FeedbackService {

    private final T_FeedbacksRepository feedbackRepository;
    private final T_FeedbacksdayRepository feedbackdayRepository; // Inject Day repo
    private final MT_UserloginService userloginService; // To fetch user
    private final T_ProgramTimeTableService timeTableService; // To fetch timetable entry

    @Autowired
    public T_FeedbackService(T_FeedbacksRepository feedbackRepository, T_FeedbacksdayRepository feedbackdayRepository, MT_UserloginService userloginService, T_ProgramTimeTableService timeTableService) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackdayRepository = feedbackdayRepository;
        this.userloginService = userloginService;
        this.timeTableService = timeTableService;
    }

    public List<Object[]> getParticipantFeedbackAsObjectArray(String phaseid, String usercode) {
        return feedbackRepository.findParticipantFeedbackAsObjectArray(phaseid, usercode);
    }


    @Transactional
    public boolean saveOrUpdateFeedback(T_Feedbacks tfeedback) {
        if (tfeedback == null || tfeedback.getPhaseid() == null || !StringUtils.hasText(tfeedback.getPhaseid().getPhaseid()) || tfeedback.getMtuserlogin() == null || !StringUtils.hasText(tfeedback.getMtuserlogin().getUsercode())) { return false; }
        try { if (tfeedback.getEntrydate() == null) { tfeedback.setEntrydate(new Date()); }
            if (!StringUtils.hasText(tfeedback.getFeedbackslno())) { Optional<Integer> maxIdOpt = feedbackRepository.findMaxFeedbackSlnoAsInteger(); int nextId = maxIdOpt.map(maxId -> maxId + 1).orElse(1); tfeedback.setFeedbackslno(String.valueOf(nextId)); }
            feedbackRepository.save(tfeedback); return true;
        } catch (Exception e) { System.err.println("Error saving/updating feedback: " + e.getMessage()); return false; }
    }

    // Service method to save day-wise feedback
    @Transactional
    public boolean saveDailyFeedback(String timetableCode, String userCode, String feedbackText) {
        try {
            Optional<T_ProgramTimeTable> timetableOpt = timeTableService.findById(timetableCode);
            MT_Userlogin user = userloginService.findByUsercode(userCode);
            if (timetableOpt.isEmpty() || user == null) {
                System.err.println("Cannot save day feedback: Timetable or User not found.");
                return false;
            }

            T_Feedbacksday fbd = new T_Feedbacksday();
            fbd.setFeedback(feedbackText);
            fbd.setProgramtimetablecode(timetableOpt.get());
            fbd.setUsercode(user);
            fbd.setEntrydate(new Date());

            if (!StringUtils.hasText(fbd.getFeedbackdayid())) {
                Optional<Integer> maxIdOpt = feedbackdayRepository.findMaxFeedbackdayIdAsInteger(); // Add this query to repo
                int nextId = maxIdOpt.map(maxId -> maxId + 1).orElse(1);
                fbd.setFeedbackdayid(String.valueOf(nextId));
            }

            feedbackdayRepository.save(fbd);
            return true;
        } catch (Exception e) {
            System.err.println("Error saving daily feedback: " + e.getMessage());
            return false;
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getDayPrograms(Date attendanceDate, String phaseId, String usercode) {
        if (attendanceDate == null || phaseId == null || usercode == null) {
            return Collections.emptyList();
        }
        try {
            return feedbackRepository.findSubjectsAttendedOnDate(attendanceDate, phaseId, usercode);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
