package com.nic.nerie.m_shortterm_phases.controller;

import com.nic.nerie.m_shortterm_phases.model.M_ShortTerm_Phases;
import com.nic.nerie.m_shortterm_phases.service.M_ShortTerm_PhasesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/nerie/short-term-phases")
public class M_ShortTerm_PhasesController {
    private final M_ShortTerm_PhasesService mShortTermPhasesService;

    @Autowired
    public M_ShortTerm_PhasesController(M_ShortTerm_PhasesService mShortTermPhasesService) {
        this.mShortTermPhasesService = mShortTermPhasesService;
    }

    @PostMapping("/getPhasesBasedOnCourse")
    @ResponseBody
    public List<M_ShortTerm_Phases> getphasesbasedoncourse(@RequestParam(value = "fystart", required = false) String fystart, @RequestParam(value = "fyend", required = false) String fyend) {
        List<M_ShortTerm_Phases> ilist = null;
        ilist = mShortTermPhasesService.getSPhaseList();
        return ilist;
    }
}
