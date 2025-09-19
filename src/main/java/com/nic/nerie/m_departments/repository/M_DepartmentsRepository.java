package com.nic.nerie.m_departments.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.m_departments.model.M_Departments;

public interface M_DepartmentsRepository extends JpaRepository<M_Departments, String> {
    @Query("FROM M_Departments ORDER BY departmentcode, departmentname")
    List<M_Departments> findAllOrderByDepartmentcodeAscAndDepartmentnameAsc();

    @Query(value = "SELECT * FROM m_departments ORDER BY departmentcode, departmentname", nativeQuery = true)
    List<M_Departments> getDepartmentList();
    
    @Query(value = "SELECT * FROM nerie.m_departments WHERE UPPER(departmentname) = UPPER(:dname)", nativeQuery = true)
    Optional<M_Departments> getDepartmentByName(@Param("dname") String departmentName);

    @Query(value = "SELECT * FROM nerie.m_departments WHERE UPPER(departmentname) = UPPER(:dname) AND departmentcode != :dcode", nativeQuery = true)
    Optional<M_Departments> getDepartmentByNameExcludingCode(@Param("dname") String departmentName, @Param("dcode") String departmentCode);

    @Query(value = "SELECT MAX(CAST(departmentcode AS INTEGER)) FROM nerie.m_departments", nativeQuery = true)
    Integer getMaxDepartmentCode();

    @Query(value = "SELECT * FROM nerie.m_departments WHERE departmentcode = :dcode", nativeQuery = true)
    Optional<M_Departments> getDepartmentByCode(@Param("dcode") String departmentcode);

    @Query(value = "SELECT * FROM m_departments", nativeQuery = true)
    List<M_Departments> findAllDepartments();

    @Query(value = "SELECT COUNT(*) FROM nerie.m_departments", nativeQuery = true)
    Integer getDepartmentsCount();
}
