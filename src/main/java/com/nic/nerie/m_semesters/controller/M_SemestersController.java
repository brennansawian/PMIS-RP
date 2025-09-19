package com.nic.nerie.m_semesters.controller;

import com.nic.nerie.m_semesters.model.M_Semesters;
import com.nic.nerie.m_semesters.service.M_SemestersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/neire/semesters")
public class M_SemestersController {
    private final M_SemestersService mSemestersService;

    @Autowired
    public M_SemestersController(M_SemestersService mSemestersService) {
        this.mSemestersService = mSemestersService;
    }

    @PostMapping("/getSemestersBasedOnCourse")
    @ResponseBody
    public List<M_Semesters> getsemestersbasedoncourse(@RequestParam(value = "fystart", required = false) String fystart, @RequestParam(value = "fyend", required = false) String fyend) {
        List<M_Semesters> ilist = null;
        ilist = mSemestersService.getSemesterList();
        return ilist;
    }
}
