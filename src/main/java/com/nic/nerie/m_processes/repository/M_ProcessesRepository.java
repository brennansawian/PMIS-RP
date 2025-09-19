package com.nic.nerie.m_processes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nic.nerie.m_processes.model.M_Processes;

public interface M_ProcessesRepository extends JpaRepository<M_Processes, Integer> {
        @Query(value = "SELECT M.mainmenucode, M.mainmenuname, P.menuname, P.newpageurl "
                        + " FROM nerie.m_processes P "
                        + " INNER JOIN nerie.mt_userprocesses U ON P.processcode = U.processcode "
                        + " INNER JOIN nerie.m_mainmenu M ON M.mainmenucode = P.mainmenucode "
                        + " WHERE U.usercode = :ucode"
                        + " ORDER BY M.mainmenucode, P.processcode", nativeQuery = true)
        List<Object[]> getProcessesForUserNavigation(@Param("ucode") String ucode);

        @Query("SELECT m FROM M_Processes m WHERE m.processcode IN :codes ORDER BY m.processcode")
        List<M_Processes> getPrincipalProcesses(@Param("codes") List<Integer> codes);

        @Query(value = "SELECT processcode, COALESCE(processname, '') " +
                        "FROM nerie.m_processes " +
                        "WHERE processcode IN (1, 7, 10, 17, 19, 24) " +
                        "ORDER BY processcode", nativeQuery = true)
        List<Object[]> getPrincipalProcessesFixed(); // Changed return type to List<Object[]>

        @Query(value = "SELECT m.processcode, m.processname " +
                        "FROM nerie.m_processes m " +
                        "WHERE m.processcode NOT IN (7, 25, 42, 43) " +
                        "ORDER BY m.processcode", nativeQuery = true)
        List<Object[]> getAllProcesses();

        @Transactional
        @Query(value = """
                SELECT DISTINCT p.processcode, n.processname
                FROM nerie.mt_userprocesses p
                JOIN nerie.mt_userlogin u ON u.usercode = p.usercode
                JOIN nerie.m_processes n ON n.processcode = p.processcode
                WHERE p.processcode NOT IN (1, 5, 7, 17, 25, 32, 42, 43)
                """, nativeQuery = true)
        List<Object[]> getLocalAdminProcesses(@Param("usercode") String usercode);

        @Transactional
        @Query(value = "SELECT usercode, processcode FROM nerie.mt_userprocesses WHERE usercode = :usercode", nativeQuery = true)
        List<Object[]> getUserProcesses(@Param("usercode") String usercode);

        @Transactional
        @Query(value = "SELECT processcode FROM nerie.mt_userprocesses WHERE usercode = :usercode", nativeQuery = true)
        List<String> getProcessesFromUsercode(@Param("usercode") String usercode);

        @Transactional
        @Modifying
        @Query(value = "INSERT INTO nerie.mt_userprocesses(usercode, processcode) " +
                        "VALUES(:usercode, :processcode)", nativeQuery = true)
        void createUserProcessesEntry(@Param("usercode") String usercode, @Param("processcode") Integer processcode);

        @Query(value = "SELECT processcode FROM nerie.m_processes WHERE mainmenucode = :mainmenucode", nativeQuery = true)
        List<Integer> findProcessCodesByMainMenuCode(@Param("mainmenucode") int mainmenucode);

        @Modifying
        @Query(value = "DELETE FROM nerie.mt_userprocesses up WHERE up.usercode = :usercode", nativeQuery = true)
        void removeUserProcessEntry(@Param("usercode") String usercode);

        @Modifying
        @Query(value = "INSERT INTO nerie.mt_userprocesses (usercode, processcode) VALUES (:usercode, :processcode)", nativeQuery = true)
        void insertUserProcess(@Param("usercode") String usercode, @Param("processcode") int processcode);

        @Query(value = "SELECT EXISTS (SELECT 1 FROM nerie.mt_userprocesses up WHERE up.usercode = :usercode AND up.processcode = :processcode)", nativeQuery = true)
        boolean userProcessExists(@Param("usercode") String usercode, @Param("processcode") Integer processcode);
}
