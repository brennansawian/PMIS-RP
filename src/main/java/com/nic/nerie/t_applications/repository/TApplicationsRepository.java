package com.nic.nerie.t_applications.repository;

import com.nic.nerie.t_applications.model.T_Applications;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TApplicationsRepository extends JpaRepository<T_Applications, String> {

    @Modifying
    @Transactional
    @Query("UPDATE T_Applications ta SET ta.remarks = :remarks, ta.status = :status " +
            "WHERE ta.mtuserlogin.usercode = :usercode AND ta.phaseid.phaseid = :phaseid")
    int updateStatusAndRemarksByUsercodeAndPhaseid(
            @Param("usercode") String usercode,
            @Param("phaseid") String phaseid,
            @Param("remarks") String remarks,
            @Param("status") String status
    );

    @Query("SELECT CASE WHEN COUNT(ta) > 0 THEN true ELSE false END " +
           "FROM T_Applications ta " +
           "WHERE ta.mtuserlogin.usercode = :usercode " + 
           "AND ta.phaseid.phaseid = :phaseid")
    boolean existsByUsercodeAndPhaseid(@Param("usercode") String usercode, @Param("phaseid") String phaseid);

    @Query(value = "SELECT u.usercode, u.username, a.applicationcode, pa.attendance " +
           "FROM nerie.t_applications a " +
           "INNER JOIN nerie.mt_userlogin u ON u.usercode = a.usercode " +
           "LEFT JOIN nerie.t_participantattendance pa ON pa.pusercode = a.usercode AND pa.programtimetablecode = :programtimetablecode " +
           "WHERE a.status = 'A' AND a.phaseid = :phaseid", nativeQuery = true)
    List<Object[]> findParticipantsInSession(
            @Param("programtimetablecode") String programtimetablecode,
            @Param("phaseid") String phaseid
    );
}