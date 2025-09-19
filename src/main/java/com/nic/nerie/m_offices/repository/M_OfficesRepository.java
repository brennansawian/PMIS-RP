package com.nic.nerie.m_offices.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.m_offices.model.M_Offices;

public interface M_OfficesRepository extends JpaRepository<M_Offices, String> {
    @Query("FROM M_Offices ORDER BY officename")
    List<M_Offices> findAllOrderByOfficename();

    @Query("FROM M_Offices ORDER BY officename ASC")
    List<M_Offices> findAllByOrderByOfficenameAsc();

    @Query("SELECT MAX(CAST(o.officecode AS integer)) FROM M_Offices o")
    Integer findMaxOfficeCodeAsInteger();

    @Query(value = "SELECT COUNT(*) FROM nerie.mt_userlogin WHERE isfaculty='1' AND officecode=:officecode", nativeQuery = true)
    Integer getOfficeFacultiesCount(@Param("officecode") String officecode);

    @Query(value = "SELECT COUNT(*) FROM nerie.mt_userlogin u "
                + "INNER JOIN nerie.t_students s ON s.usercode=u.usercode "
                + "WHERE u.userrole='T' AND s.officecode=:officecode", nativeQuery = true)
    Integer getOfficeStudentsCount(@Param("officecode") String officecode);

    @Query(value = "SELECT COUNT(distinct rollno)  "
                + "FROM nerie.t_alumni WHERE officecode=:officecode", nativeQuery = true)
    Integer getOfficeAlumniCount(@Param("officecode") String officecode);

    @Query(value = "SELECT COUNT(*) FROM nerie.mt_userlogin WHERE userrole='P'", nativeQuery = true)
    Integer getOfficeParticipantsCount();
}