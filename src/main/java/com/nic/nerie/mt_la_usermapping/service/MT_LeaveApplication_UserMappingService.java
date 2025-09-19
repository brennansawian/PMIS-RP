package com.nic.nerie.mt_la_usermapping.service;

import com.nic.nerie.mt_la_usermapping.model.MT_LeaveApplication_UserMapping;
import com.nic.nerie.mt_la_usermapping.repository.MT_LeaveApplication_UserMappingRepository;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class MT_LeaveApplication_UserMappingService {
    private final MT_LeaveApplication_UserMappingRepository mtLeaveApplicationUserMappingRepository;

    @Autowired
    public MT_LeaveApplication_UserMappingService(MT_LeaveApplication_UserMappingRepository mtLeaveApplicationUserMappingRepository) {
        this.mtLeaveApplicationUserMappingRepository = mtLeaveApplicationUserMappingRepository;
    }

    public Integer getLAUserMapRolecode(String uid) {
        Optional<Integer> roleCodeOpt = mtLeaveApplicationUserMappingRepository.findRoleCodeByUserCode(uid);
        Integer roleCode = roleCodeOpt.orElse(null);
        return roleCode;
    }

    @Transactional(propagation = Propagation.REQUIRED, readOnly = false)
    public boolean saveUserSlaRole(MT_Userlogin userlogin, String slarole) {
        try {
            if (userlogin == null || userlogin.getUsercode() == null || slarole == null || slarole.isBlank()) {
                throw new IllegalArgumentException("User or SLA role is invalid");
            }

            String usercode = userlogin.getUsercode();
            int newLarolecode = Integer.parseInt(slarole);

            Optional<MT_LeaveApplication_UserMapping> existingMappingOpt = mtLeaveApplicationUserMappingRepository.findByUsercode(usercode);

            if (existingMappingOpt.isPresent()) {
                MT_LeaveApplication_UserMapping existingMapping = existingMappingOpt.get();
                existingMapping.setLarolecode(newLarolecode);
                mtLeaveApplicationUserMappingRepository.save(existingMapping); // update
            } else {
                MT_LeaveApplication_UserMapping newMapping = new MT_LeaveApplication_UserMapping();

                Integer maxId = mtLeaveApplicationUserMappingRepository.findMaxLausermapcode().orElse(0);
                newMapping.setLausermapcode(maxId + 1);

                newMapping.setUsercode(userlogin);
                newMapping.setLarolecode(newLarolecode);

                mtLeaveApplicationUserMappingRepository.save(newMapping); //  insert
            }

            return true;

        } catch (NumberFormatException ex) {
            throw new RuntimeException("Invalid SLA role code (must be numeric): " + slarole, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Error saving user SLA role mapping", ex);
        }
    }
}
