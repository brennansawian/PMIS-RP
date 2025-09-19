package com.nic.nerie.mt_userloginrole.repository;

import com.nic.nerie.mt_userloginrole.model.MT_UserloginRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MT_UserloginRoleRepository extends JpaRepository<MT_UserloginRole, Integer> {
    Optional<MT_UserloginRole> findByRoleCode(String roleCode);
}