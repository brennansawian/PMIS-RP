package com.nic.nerie.m_financialyear.repository;

import com.nic.nerie.m_financialyear.model.M_FinancialYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface M_FinancialYearRepository extends JpaRepository<M_FinancialYear, String> {
    @Query(value = "SELECT * FROM nerie.m_financialyear", nativeQuery = true)
    List<Object[]> getAllFinancialYear();

    @Query(value = "SELECT * FROM nerie.m_financialyear, nerie.mt_userlogin u WHERE u.usercode = :usercode", nativeQuery = true)
    List<Object[]> getFinancialYearByUsercode(@Param("usercode") String usercode);
}
