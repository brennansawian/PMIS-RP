package com.nic.nerie.mt_userloginrole.service;

import com.nic.nerie.mt_userloginrole.model.MT_UserloginRole;
import com.nic.nerie.mt_userloginrole.repository.MT_UserloginRoleRepository;

import groovyjarjarantlr4.v4.runtime.misc.NotNull;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MT_UserloginRoleService {
    private final MT_UserloginRoleRepository mtUserloginRoleRepository;

    @Autowired
    public MT_UserloginRoleService(MT_UserloginRoleRepository mtUserloginRoleRepository) {
        this.mtUserloginRoleRepository = mtUserloginRoleRepository;
    }

    public MT_UserloginRole findByRoleCode(@NotNull @NotBlank String roleCode) {
        try {
            Optional<MT_UserloginRole> role = mtUserloginRoleRepository.findByRoleCode(roleCode);
            return role.isPresent() ? role.get() : null;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("rolecode cannot be null or blank", ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error fetching MT_UserloginRole entity for role code: " + roleCode, ex);
        }
    }
}
