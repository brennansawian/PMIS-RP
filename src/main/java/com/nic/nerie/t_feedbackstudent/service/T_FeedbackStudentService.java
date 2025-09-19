package com.nic.nerie.t_feedbackstudent.service;

import com.nic.nerie.t_feedbackstudent.model.T_FeedbackStudent;
import com.nic.nerie.t_feedbackstudent.repository.T_FeedbackStudentRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Service
@Validated
public class T_FeedbackStudentService {
    private final T_FeedbackStudentRepository tFeedbackStudentRepository;

    @Autowired
    public T_FeedbackStudentService(T_FeedbackStudentRepository tFeedbackStudentRepository) {
        this.tFeedbackStudentRepository = tFeedbackStudentRepository;
    }

    @Transactional(readOnly = false)
    public T_FeedbackStudent saveFeedbackStudent(@NotNull T_FeedbackStudent tFeedbackStudent) {
        try {
            return tFeedbackStudentRepository.save(tFeedbackStudent);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving T_FeedbackStudent entity", ex);
        }
    }

    public List<Object[]> getSubjectsListFeed(String usercode) {
        return tFeedbackStudentRepository.getSubjectsListFeed(usercode);
    }

    public List<Object[]> getStudentsFeedbackList(String subjectcode, String usercode) {
        return tFeedbackStudentRepository.getStudentsFeedbackList(subjectcode, usercode);
    }
}
