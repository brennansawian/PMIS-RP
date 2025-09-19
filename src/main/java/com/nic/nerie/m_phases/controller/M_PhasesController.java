package com.nic.nerie.m_phases.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nic.nerie.m_phases.service.M_PhasesService;


@Controller
@RequestMapping("/nerie/phases")
public class M_PhasesController {
    private final M_PhasesService mPhasesService;
   
    public M_PhasesController(M_PhasesService mPhasesService) {
        this.mPhasesService = mPhasesService;
    }

    @PostMapping("/list")
    public ResponseEntity<List<Object[]>> getPhasesByProgramcode(@RequestParam("programcode") String programcode) {
        return ResponseEntity.ok(mPhasesService.getPhasesByProgramcode(programcode));
    }

    @PostMapping("/unclose/list")
    public ResponseEntity<List<Object[]>> getUnClosePhasesList(@RequestParam("pcode") String programcode) {
        return ResponseEntity.ok(mPhasesService.getUnClosePhasesList(programcode));
    }

    @PostMapping("/getPhasesBasedOnProgram")
    @ResponseBody
    public List<Object[]> getpahsesbasedonprogramcodep(@RequestParam(value = "programcode", required = false) String programcode) {
        List<Object[]> plist = null;
        plist = mPhasesService.getPhasesBasedOnProgramCode(programcode);
        return plist;
    }
}
