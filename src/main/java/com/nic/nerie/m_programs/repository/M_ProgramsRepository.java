package com.nic.nerie.m_programs.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.nic.nerie.m_programs.model.M_Programs;

public interface M_ProgramsRepository extends JpaRepository<M_Programs, String> {
       @Query("SELECT p FROM M_Programs p WHERE p.programcode = :programcode")
       Optional<M_Programs> findByProgramcode(@Param("programcode") String programcode);

       @Query(value = "SELECT distinct(p.programcode), p.programname || '(' || TO_CHAR(min(pd.startdate),'dd/MM/yyyy') || '-' || TO_CHAR(max(pd.enddate),'dd/MM/yyyy') || ')' AS value  "
                + "FROM nerie.m_programs p,nerie.mt_programdetails pd  "
                + "WHERE p.closed='N' and pd.finalized != 'R' and pd.finalized != 'N' and p.programcode = pd.programcode "
                + "group by p.programcode,pd.enddate,pd.entrydate,pd.startdate", nativeQuery = true)
       List<Object[]> getOngoingPrograms();

       @Query(value = "SELECT p.programcode AS KEY, MAX(p.programname) as val, MAX(pd.entrydate) as entrydate, p.programname AS value  "
                + "FROM nerie.m_programs p,nerie.mt_programdetails pd, nerie.mt_program_members pm  " //, nerie.mt_program_members pm
                + "WHERE p.closed='N' and pd.finalized != 'R' and pd.finalized != 'N' and p.programcode = pd.programcode "
                + " and (p.programcode IN (SELECT distinct(pm.programcode) WHERE pm.usercode = :usercode) "
                + " OR p.enteredby = :usercode) " 
                + "group by p.programcode", nativeQuery = true)
       List<Object[]> getOngoingProgramsByUsercode(@Param("usercode") String usercode);

       @Query(value = "SELECT DISTINCT p.programcode, " +
                     "       MAX(ph.phaseno) AS number_of_phases, " +
                     "       p.programname, " +
                     "       p.programdescription, " +
                     "       p.programid " +
                     "FROM nerie.m_programs p " +
                     "JOIN nerie.mt_programdetails pd ON p.programcode = pd.programcode " +
                     "JOIN nerie.m_phases ph ON pd.phaseid = ph.phaseid " +
                     "WHERE pd.closed IS NOT NULL " +
                     "  AND pd.finalized = 'Y' " +
                     "  AND (p.enteredby = :ucode OR " +
                     "       p.programcode IN (SELECT pm.programcode FROM nerie.mt_program_members pm WHERE pm.usercode = :ucode)) "
                     +
                     "  AND (:coursetype = 0 OR :coursetype = 3) " +
                     "  AND pd.finalized != 'R' " +
                     "GROUP BY p.programcode, p.programname, p.programdescription, p.programid", nativeQuery = true)
       List<Object[]> getDashboardProgramsByUser(@Param("ucode") String ucode, @Param("coursetype") Integer coursetype);

       @Query(value = "SELECT DISTINCT p.programcode, MAX(ph.phaseno) AS number_of_phases, p.programname, " +
                     "p.programdescription, p.programid " +
                     "FROM nerie.m_programs p " +
                     "JOIN nerie.mt_programdetails pd ON p.programcode = pd.programcode " +
                     "JOIN nerie.m_phases ph ON pd.phaseid = ph.phaseid " +
                     "WHERE p.enteredby = :ucode " +
                     "AND (:coursetype = 0 OR :coursetype = 3) " +
                     "AND pd.finalized = 'R' " +
                     "GROUP BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardProgramsByUserRejected(@Param("ucode") String ucode,
                     @Param("coursetype") Integer coursetype);

       @Query(value = "SELECT p.programcode, p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p " +
                     "WHERE p.officecode = :oc " +
                     "  AND p.programcode IN (SELECT DISTINCT d2.programcode FROM nerie.mt_programdetails d2) " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardAll(@Param("uc") String uc, @Param("oc") String oc);

       @Query(value = "SELECT p.programcode, p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p " +
                     "WHERE p.officecode = :oc " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "  AND p.closed = 'N' " +
                     "  AND p.programcode IN ( " +
                     "      SELECT DISTINCT programcode " +
                     "      FROM nerie.mt_programdetails " +
                     "      WHERE finalized = 'Y' " +
                     "        AND startdate <= CURRENT_DATE " +
                     "        AND enddate >= CURRENT_DATE " +
                     "  ) " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardOngoing(@Param("uc") String uc, @Param("oc") String oc);

       @Query(value = "SELECT p.programcode, p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p " +
                     "WHERE p.officecode = :oc " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "  AND p.closed = 'N' " +
                     "  AND p.programcode IN ( " +
                     "      SELECT DISTINCT programcode " +
                     "      FROM nerie.mt_programdetails " +
                     "      WHERE finalized = 'Y' " +
                     "        AND startdate > CURRENT_DATE " +
                     "  ) " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardUpcoming(@Param("uc") String uc, @Param("oc") String oc);

       @Query(nativeQuery = true, value = "SELECT p.programcode,p.programname,p.programid, p.programdescription, pd.startdate,pd.enddate,pd.lastdate,O.officename,p.coursecodecategory,CC.coursecategoryname, "
                     +
                     "STRING_AGG(distinct v.venuename,',') as venuename,STRING_AGG(distinct u.username,',') as coordinator, ph.phaseid,ph.phaseno "
                     +
                     "from nerie.m_programs p " +
                     "inner join nerie.m_phases ph on ph.programcode = p.programcode " +
                     "inner join nerie.mt_programdetails pd on pd.phaseid= ph.phaseid " +
                     "inner join nerie.mt_programvenues pv on ph.phaseid=pv.phaseid " +
                     "inner join nerie.m_venues v on pv.venuecode=v.venuecode " +
                     "inner join nerie.mt_program_members pm on pm.phaseid=ph.phaseid " +
                     "inner join nerie.mt_userlogin u on u.usercode=pm.usercode " +
                     "INNER JOIN nerie.m_coursecategories CC ON p.coursecodecategory =CC.coursecategorycode " +
                     "INNER JOIN nerie.m_offices O ON O.officecode=p.officecode " +
                     "INNER JOIN nerie.t_applications a ON a.phaseid=ph.phaseid " +
                     "WHERE a.usercode=:usercode AND a.status='P' AND a.usercodewhoapplied IS NOT NULL " +
                     "group by p.programcode,p.programname,p.programid, p.programdescription, pd.startdate,pd.enddate,pd.lastdate,O.officename,a.applicationcode,ph.phaseid,ph.phaseno,CC.coursecategoryname "
                     +
                     "ORDER BY p.programcode , a.entrydate DESC")
       List<Object[]> findInviteCourseList(String usercode);

       @Query(value = "SELECT DISTINCT p.programcode, p.programid, p.programname, p.programdescription, pd.startdate, pd.enddate "
                     +
                     "FROM nerie.m_programs p " +
                     "JOIN nerie.mt_programdetails pd ON p.programcode = pd.programcode " +
                     "WHERE p.officecode = :oc " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "  AND p.closed = 'N' " +
                     "  AND TO_CHAR(pd.startdate, 'yyyy-MM') BETWEEN '2022-04' AND '2023-03' " +
                     "  AND p.programcode IN ( " +
                     "      SELECT DISTINCT programcode " +
                     "      FROM nerie.mt_programdetails " +
                     "      WHERE finalized = 'Y' " +
                     "        AND enddate < CURRENT_DATE " +
                     "  ) " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardCompleted(@Param("uc") String uc, @Param("oc") String oc);

       @Query(nativeQuery = true, value = """
                  SELECT p.programcode, p.programname, p.programid, p.programdescription,
                         pd.startdate, pd.enddate, pd.entrydate, O.officename, a.applicationcode,
                         STRING_AGG(DISTINCT v.venuename, ', ') AS venuename,
                         STRING_AGG(DISTINCT u.username, ', ') AS coordinator,
                         ph.phaseid, ph.phaseno,
                         CASE 
                             WHEN EXISTS (
                                 SELECT 1 
                                 FROM nerie.t_participantfeedbacks pf 
                                 WHERE pf.usercode = :usercode 
                                   AND pf.phaseid = ph.phaseid 
                             ) THEN 1 
                             ELSE 0 
                         END AS existsinfeedbacks,
                         CASE 
                             WHEN EXISTS (
                                 SELECT 1 
                                 FROM nerie.t_phase_preposttest pppt 
                                 WHERE pppt.phaseid = ph.phaseid AND pppt.testtype = 'APP'
                             ) THEN 1 
                             ELSE 0 
                         END AS haspreposttest,
                         CASE 
                             WHEN EXISTS (
                                 SELECT 1 
                                 FROM nerie.t_participantanswerkey_preposttest pak 
                                 JOIN nerie.t_phase_preposttest pppt 
                                 ON pak.testid = pppt.testid 
                                 WHERE pak.participantusercode = :usercode 
                                   AND pak.testtype = 'PRE' 
                                   AND pppt.phaseid = ph.phaseid 
                             ) THEN 1 
                             ELSE 0 
                         END AS hasdonepretest,
                         CASE 
                             WHEN EXISTS (
                                 SELECT 1 
                                 FROM nerie.t_participantanswerkey_preposttest pak 
                                 JOIN nerie.t_phase_preposttest pppt 
                                 ON pak.testid = pppt.testid 
                                 WHERE pak.participantusercode = :usercode 
                                   AND pak.testtype = 'POST' 
                                   AND pppt.phaseid = ph.phaseid 
                             ) THEN 1 
                             ELSE 0 
                         END AS hasdoneposttest,
                         CASE 
                             WHEN EXISTS (
                                 SELECT 1 
                                 FROM nerie.t_phase_preposttest pppt 
                                 WHERE pppt.phaseid = ph.phaseid AND pppt.testtype = 'LINK'
                             ) THEN 1 
                             ELSE 0 
                         END AS hasgooglelinkpreposttest,
                         COALESCE(pppt.testurl,'') as testurl
                  FROM nerie.m_programs p
                  INNER JOIN nerie.m_phases ph ON ph.programcode = p.programcode
                  INNER JOIN nerie.mt_programdetails pd ON pd.phaseid = ph.phaseid
                  LEFT JOIN nerie.mt_programvenues pv ON ph.phaseid = pv.phaseid
                  LEFT JOIN nerie.m_venues v ON pv.venuecode = v.venuecode
                  LEFT JOIN nerie.mt_program_members pm ON pm.phaseid = ph.phaseid
                  LEFT JOIN nerie.mt_userlogin u ON u.usercode = pm.usercode
                  INNER JOIN nerie.m_coursecategories CC ON p.coursecodecategory = CC.coursecategorycode
                  INNER JOIN nerie.m_offices O ON O.officecode = p.officecode
                  INNER JOIN nerie.t_applications a ON a.phaseid = ph.phaseid
                  LEFT JOIN nerie.t_phase_preposttest pppt ON pppt.phaseid = ph.phaseid
                  WHERE a.usercode = :usercode AND a.status = 'A'
                  GROUP BY p.programcode, p.programname, p.programid, p.programdescription,
                           pd.startdate, pd.enddate, pd.entrydate, O.officename, a.applicationcode,
                           ph.phaseid, ph.phaseno, pppt.testurl
                  ORDER BY p.programcode, pd.entrydate DESC
                  """)
       List<Object[]> findParticipantProgramsList(@Param("usercode") String usercode);

       @Query(value = "SELECT p.programcode, p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p " +
                     "WHERE p.officecode = :oc " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "  AND p.closed = 'Y' " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardClosed(@Param("uc") String uc,
                     @Param("oc") String oc);

       @Query(value = "SELECT DISTINCT p.programcode, p.programid, p.programname, p.programdescription, pd.startdate, pd.enddate "
                     +
                     "FROM nerie.m_programs p " +
                     "JOIN nerie.mt_programdetails pd ON p.programcode = pd.programcode " +
                     "WHERE p.officecode = :oc " +
                     "  AND (:uc = 'ALL' OR p.enteredby = :uc) " +
                     "  AND p.closed = 'N' " +
                     "  AND TO_CHAR(pd.startdate, 'yyyy-MM') < '2022-04' " +
                     "  AND p.programcode IN ( " +
                     "      SELECT DISTINCT programcode " +
                     "      FROM nerie.mt_programdetails " +
                     "      WHERE finalized = 'Y' " +
                     "        AND enddate < CURRENT_DATE " +
                     "  ) " +
                     "ORDER BY p.programcode", nativeQuery = true)
       List<Object[]> getDashboardArchived(@Param("uc") String uc, @Param("oc") String oc);

       @Query(value = """
                            SELECT o.officename,
                                   SUM(CASE WHEN pd.finalized = 'Y' AND TO_CHAR(pd.startdate, 'yyyy-MM-dd') <= TO_CHAR(now(), 'yyyy-MM-dd') AND TO_CHAR(pd.enddate, 'yyyy-MM-dd') >= TO_CHAR(now(), 'yyyy-MM-dd') THEN 1 ELSE 0 END) AS ongoing,
                                   SUM(CASE WHEN pd.finalized = 'Y' AND TO_CHAR(pd.startdate, 'yyyy-MM-dd') > TO_CHAR(now(), 'yyyy-MM-dd') THEN 1 ELSE 0 END) AS upcoming,
                                   SUM(CASE WHEN pd.finalized = 'Y' AND TO_CHAR(pd.enddate, 'yyyy-MM-dd') < TO_CHAR(now(), 'yyyy-MM-dd') THEN 1 ELSE 0 END) AS completed
                            FROM nerie.m_offices o
                            LEFT OUTER JOIN nerie.m_programs p ON o.officecode = p.officecode
                            LEFT OUTER JOIN nerie.mt_programdetails pd ON pd.programcode = p.programcode
                            GROUP BY o.officename
                            ORDER BY o.officename
                     """, nativeQuery = true)
       List<Object[]> getOfficeWiseCountProgram();

       @Query(value = "SELECT p.programname, " +
                     "       p.programdescription, " +
                     "       ph.phaseno, " +
                     "       ph.phasedescription, " +
                     "       TRIM(TO_CHAR(pd.enddate, 'DDth Month')) || TO_CHAR(pd.enddate, ' YYYY') AS enddate, " +
                     "       TRIM(TO_CHAR(pd.startdate, 'DDth Month')) || TO_CHAR(pd.startdate, ' YYYY') AS startdate, "
                     +
                     "       p.programid " +
                     "FROM nerie.mt_programdetails pd " +
                     "INNER JOIN nerie.m_programs p ON pd.programcode = p.programcode " +
                     "INNER JOIN nerie.m_phases ph ON pd.phaseid = ph.phaseid " +
                     "WHERE pd.finalized = 'Y' " +
                     "  AND pd.closed = 'Y' " +
                     "  AND pd.enddate < CURRENT_DATE " +
                     "  AND (:coursetype = 0 OR :coursetype = 3) " +
                     "  AND (p.enteredby = :ucode OR " +
                     "        p.programcode IN (SELECT pm.programcode FROM nerie.mt_program_members pm WHERE pm.usercode = :ucode)) "
                     +
                     "ORDER BY pd.enddate DESC " +
                     "LIMIT :limit", nativeQuery = true)
       List<Object[]> getDashboardRecentlyCompletedPhasesListByUser(@Param("ucode") String ucode,
                     @Param("coursetype") Integer coursetype,
                     @Param("limit") Integer limit);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p, nerie.mt_program_members m " +
                     "WHERE p.programcode = m.programcode AND " +
                     "p.officecode = :oc AND " +
                     "p.programcode IN (SELECT DISTINCT(d2.programcode) FROM nerie.mt_programdetails d2) AND " +
                     "((m.usercode = :uc OR p.enteredby = :uc) OR p.programcode IN " +
                     "(SELECT DISTINCT(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :uc)) " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardAll(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p, nerie.mt_program_members m " +
                     "WHERE p.programcode = m.programcode AND " +
                     "p.officecode = :oc AND " +
                     "((m.usercode = :uc OR p.enteredby = :uc) OR p.programcode IN " +
                     "(SELECT DISTINCT(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :uc)) AND "
                     +
                     "p.closed = 'N' AND " +
                     "p.programcode IN (" +
                     "SELECT DISTINCT(programcode) " +
                     "FROM nerie.mt_programdetails " +
                     "WHERE finalized = 'Y' AND " +
                     "TO_CHAR(startdate,'yyyy-MM-dd') <= TO_CHAR(now(),'yyyy-MM-dd') AND " +
                     "TO_CHAR(enddate,'yyyy-MM-dd') >= TO_CHAR(now(),'yyyy-MM-dd')" +
                     ") " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardOngoing(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p, nerie.mt_program_members m " +
                     "WHERE p.programcode = m.programcode AND " +
                     "p.officecode = :oc AND " +
                     "((m.usercode = :uc OR p.enteredby = :uc) OR p.programcode IN " +
                     "(SELECT DISTINCT(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :uc)) AND "
                     +
                     "p.closed = 'N' AND " +
                     "p.programcode IN (" +
                     "SELECT DISTINCT(programcode) " +
                     "FROM nerie.mt_programdetails " +
                     "WHERE finalized = 'Y' AND " +
                     "TO_CHAR(startdate,'yyyy-MM-dd') > TO_CHAR(now(),'yyyy-MM-dd')" +
                     ") " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardUpcoming(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription, pd.startdate, pd.enddate "
                     +
                     "FROM nerie.m_programs p, nerie.mt_program_members m, nerie.mt_programdetails pd " +
                     "WHERE p.programcode = m.programcode AND pd.programcode = p.programcode AND " +
                     "p.officecode = :oc AND TO_CHAR(pd.startdate,'yyyy-MM') BETWEEN '2022-04' AND '2023-03' AND " +
                     "((m.usercode = :uc OR p.enteredby = :uc) OR p.programcode IN " +
                     "(SELECT DISTINCT(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :uc)) AND "
                     +
                     "p.closed = 'N' AND " +
                     "p.programcode IN (" +
                     "SELECT DISTINCT(programcode) " +
                     "FROM nerie.mt_programdetails " +
                     "WHERE finalized = 'Y' AND " +
                     "TO_CHAR(enddate,'yyyy-MM-dd') < TO_CHAR(now(),'yyyy-MM-dd')" +
                     ") " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardCompleted(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription " +
                     "FROM nerie.m_programs p, nerie.mt_program_members m " +
                     "WHERE p.programcode = m.programcode AND " +
                     "p.officecode = :oc AND " +
                     "((m.usercode = :uc OR p.enteredby = :uc) OR p.programcode IN " +
                     "(SELECT DISTINCT(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :uc)) AND "
                     +
                     "p.closed = 'Y' " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardClosed(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT DISTINCT(p.programcode), p.programid, p.programname, p.programdescription, pd.startdate, pd.enddate "
                     +
                     "FROM nerie.m_programs p, nerie.mt_program_members m, nerie.mt_programdetails pd " +
                     "WHERE p.programcode = m.programcode AND pd.programcode = p.programcode AND " +
                     "p.officecode = :oc AND TO_CHAR(pd.startdate,'yyyy-MM') < '2022-04' AND " +
                     "(m.usercode = :uc OR p.enteredby = :uc) AND " +
                     "p.closed = 'N' AND " +
                     "p.programcode IN (" +
                     "SELECT DISTINCT(programcode) " +
                     "FROM nerie.mt_programdetails " +
                     "WHERE finalized = 'Y' AND " +
                     "TO_CHAR(enddate,'yyyy-MM-dd') < TO_CHAR(now(),'yyyy-MM-dd')" +
                     ") " +
                     "ORDER BY programcode", nativeQuery = true)
       List<Object[]> getCoordinatorDashboardArchived(@Param("uc") String userCode, @Param("oc") String officeCode);

       @Query(value = "SELECT p.programcode, ph.phaseid, O.officename, ph.phasedescription, " +
                     "STRING_AGG(distinct v.venuename,',') as venuename , STRING_AGG(distinct COALESCE(rp.rpname, 'No Resource Person'),',') as rpname "
                     +
                     "FROM nerie.m_programs p " +
                     "INNER JOIN nerie.m_phases ph ON ph.programcode = p.programcode " +
                     "INNER JOIN nerie.mt_programdetails pd ON pd.phaseid = ph.phaseid " + // Removed extra space
                     "INNER JOIN nerie.mt_programvenues pv ON ph.phaseid = pv.phaseid " +
                     "INNER JOIN nerie.m_venues v ON pv.venuecode = v.venuecode " +
                     "INNER JOIN nerie.m_offices O ON O.officecode = p.officecode " +
                     "LEFT JOIN nerie.mt_resourcepersoncoursemap rpm ON rpm.phaseid = ph.phaseid " +
                     "LEFT JOIN nerie.mt_resourcepersons rp ON rp.rpslno = rpm.rpslno " +
                     "WHERE p.programcode = :programcode AND ph.phaseid = :phaseid " +
                     "GROUP BY p.programcode, p.programname, p.programid, p.programdescription, O.officename, ph.phaseid, ph.phaseno "
                     +
                     "ORDER BY p.programcode DESC, ph.phaseid ASC", nativeQuery = true)
       List<Object[]> getProgramVenuesAndRP(@Param("programcode") String programcode, @Param("phaseid") String phaseid);

       @Query("SELECT p FROM M_Programs p WHERE p.programcode = :programcode")
       M_Programs getProgram(@Param("programcode") String programcode);

       @Query(value = """
                            SELECT p.programcode AS p0, p.programname AS p1, p.programdescription AS p2, pd.lastdate AS p3, p.programid AS p4,
                                   pd.startdate AS p5, pd.enddate AS p6, pd.finalized AS p7, pd.closed AS p8, pd.ttfinalized AS p9,
                                   pd.courseclosedate AS p10, pd.approvedusercode AS p11, p.officecode AS p12, pd.approvaldate AS p13,
                                   pd.rejectionremarks AS p14, OCTET_LENGTH(pd.approvalletter) AS p15,
                                   STRING_AGG(DISTINCT v.venuename, ',') AS p16, STRING_AGG(DISTINCT u.username, ',') AS p17,
                                   p.officecode AS p18, p.enteredby AS p19, cc.coursecategorycode AS p20, cc.coursecategoryname AS p21,
                                   ph.phasedescription AS p22, o.officename AS p23
                            FROM nerie.m_programs p
                            LEFT OUTER JOIN nerie.mt_programdetails pd ON pd.programcode = p.programcode
                            LEFT OUTER JOIN nerie.m_phases ph ON ph.phaseid = pd.phaseid
                            LEFT OUTER JOIN nerie.mt_program_members pm ON pm.programcode = pd.programcode
                            LEFT OUTER JOIN nerie.mt_userlogin u ON u.usercode = pm.usercode
                            LEFT OUTER JOIN nerie.mt_programvenues vc ON vc.programcode = pd.programcode
                            LEFT OUTER JOIN nerie.m_coursecategories cc ON cc.coursecategorycode = p.coursecodecategory
                            LEFT OUTER JOIN nerie.m_venues v ON vc.venuecode = v.venuecode
                            LEFT OUTER JOIN nerie.m_offices o ON o.officecode = p.officecode
                            WHERE p.officecode = :officecode AND pd.finalized = :finalized AND p.closed = 'N'
                            GROUP BY p.programcode, p.programname, p.programdescription, pd.lastdate, p.programid, pd.startdate, pd.enddate,
                                   pd.finalized, pd.closed, pd.ttfinalized, pd.courseclosedate, pd.approvedusercode, p.officecode,
                                   pd.approvaldate, pd.rejectionremarks, p.officecode, p.enteredby, cc.coursecategorycode,
                                   cc.coursecategoryname, pd.approvalletter, ph.phasedescription, o.officename
                     """, nativeQuery = true)
       List<Object[]> getOfficeCourseList(@Param("officecode") String officecode, @Param("finalized") String finalized);

       @Query(value = """
                     SELECT p.programname, p.programdescription, p.programid,
                            COALESCE(c.coursecategoryname, 'No Category') AS coursecategoryname,
                            pd.startdate, pd.enddate, pd.lastdate, ph.phaseid, ph.phaseno, ph.phasedescription,
                            STRING_AGG(DISTINCT mv.venuename, ',') AS venues,
                            STRING_AGG(DISTINCT u.username, ',') AS coordinators,
                            p.coursecodecategory, pd.courseclosedate, pd.approvalletter, pd.approvaldate,
                            p.programcode, STRING_AGG(DISTINCT v.venuecode, ',') AS venuecodes,
                            STRING_AGG(DISTINCT m.usercode, ',') AS usercodes, pd.programdetailid
                     FROM nerie.m_phases ph
                     INNER JOIN nerie.m_programs p ON ph.programcode = p.programcode
                     INNER JOIN nerie.mt_programdetails pd ON pd.phaseid = ph.phaseid
                     LEFT JOIN nerie.mt_programvenues v ON ph.phaseid = v.phaseid
                     LEFT JOIN nerie.m_venues mv ON mv.venuecode = v.venuecode
                     INNER JOIN nerie.mt_program_members m ON ph.phaseid = m.phaseid
                     INNER JOIN nerie.mt_userlogin u ON u.usercode = m.usercode
                     LEFT JOIN nerie.m_coursecategories c ON c.coursecategorycode = p.coursecodecategory
                     WHERE p.officecode = :officecode AND pd.finalized = :finalized AND pd.closed = 'N'
                     GROUP BY ph.phaseid, p.programname, p.programdescription, p.programid,
                            c.coursecategoryname, p.programcode, pd.lastdate, pd.startdate, pd.enddate,
                            p.coursecodecategory, pd.courseclosedate, pd.approvalletter, pd.approvaldate,
                            pd.programdetailid
                     ORDER BY p.programcode""", nativeQuery = true)
       List<Object[]> getPhaseCourseList(@Param("officecode") String officecode, @Param("finalized") String finalized);

       @Query("SELECT MAX(CAST(p.programcode as int)) from M_Programs p")
       Integer getLastUsedProgramcode();

       @Query(value = """
                            SELECT p.programdescription, p.programid, c.coursecategoryname,
                                   (CAST((SELECT MAX(CAST(ph2.phaseno AS INT))
                                          FROM nerie.m_phases ph2
                                          WHERE ph2.programcode = p.programcode) AS INT) + 1) AS nextphase
                            FROM nerie.m_programs p
                            JOIN nerie.m_coursecategories c ON p.coursecodecategory = c.coursecategorycode
                            JOIN nerie.m_phases ph ON ph.programcode = p.programcode
                            JOIN nerie.mt_programdetails pd ON ph.phaseid = pd.phaseid
                            WHERE p.programcode = :pcode
                            AND pd.closed = 'Y'
                            ORDER BY ph.phaseno DESC
                            LIMIT 1
                     """, nativeQuery = true)
       List<Object[]> getProgramDetailsBasedOnCode(@Param("pcode") String pcode);

       @Query(value = """
                            Select distinct(p.programcode), p.programname
                            from nerie.m_programs p, nerie.mt_programdetails pd
                            where pd.programcode = p.programcode and officecode = :officecode
                            and pd.closed !='Y' and pd.finalized != 'R'
                            and TO_CHAR(pd.startdate,'yyyy-MM') between :fystart and :fyend
                     """, nativeQuery = true)
       List<Object[]> getProgramsByOfficecodeFinancialyear(@Param("officecode") String officecode,
                     @Param("fystart") String fystart, @Param("fyend") String fyend);

       @Query(value = """
                            Select distinct(p.programcode), p.programname
                            from nerie.m_programs p, nerie.mt_programdetails pd
                            where pd.programcode = p.programcode and officecode=:officecode
                            and pd.closed !='Y' and pd.finalized != 'R'
                            and TO_CHAR(pd.startdate,'yyyy-MM') between :fystart and :fyend
                            AND p.enteredby = :usercode
                     """, nativeQuery = true)
       List<Object[]> getProgramsByOfficecodeFinancialyearAndUsercode(@Param("officecode") String officecode,
                     @Param("fystart") String fystart, @Param("fyend") String fyend,
                     @Param("usercode") String usercode);

       @Query(value = "SELECT DISTINCT p.programcode, p.programname " +
               "FROM nerie.m_programs p " +
               "JOIN nerie.mt_programdetails pd ON pd.programcode = p.programcode " +
               "WHERE p.officecode = :officecode " +
               "AND pd.closed != 'Y' " +
               "AND pd.finalized != 'R' " +
               "AND pd.approvedusercode IS NOT NULL " +
               "AND (pd.startdate IS NULL AND TO_CHAR(pd.entrydate, 'yyyy-MM') BETWEEN :fystart AND :fyend)",
               nativeQuery = true)
       List<Object[]> findProgramsByFiscalYear(
               @Param("officecode") String officecode,
               @Param("fystart") String fystart,
               @Param("fyend") String fyend);

       @Query(value = "SELECT DISTINCT p.programcode, p.programname " +
               "FROM nerie.m_programs p " +
               "JOIN nerie.mt_programdetails pd ON pd.programcode = p.programcode " +
               "JOIN nerie.mt_program_members pm ON pm.programcode = p.programcode " +
               "WHERE p.officecode = :officecode " +
               "AND pd.closed != 'Y' " +
               "AND pd.finalized != 'R' AND pd.approvedusercode IS NOT NULL " +
               "AND (pd.startdate IS NULL AND TO_CHAR(pd.entrydate, 'yyyy-MM') BETWEEN :fystart AND :fyend) " +
               "AND (p.enteredby = :usercode " +
               "OR (pm.usercode = :usercode " +
               "AND (pm.isheadcoordinator = '1' OR pm.isdelegated = '1')))",
               nativeQuery = true)
       List<Object[]> findProgramsByFyAndUserCode(
               @Param("officecode") String officecode,
               @Param("fystart") String fystart,
               @Param("fyend") String fyend,
               @Param("usercode") String usercode);

       @Transactional
       @Modifying
       @Query(value = "INSERT INTO nerie.mt_programvenues(programcode, venuecode, phaseid)" +
                     "VALUES(:programcode, :venuecode, :phaseid)", nativeQuery = true)
       void createProgramvenuesEntry(@Param("programcode") String programcode, @Param("venuecode") String venuecode,
                     @Param("phaseid") String phaseid);

       @Transactional
       @Modifying
       @Query(value = "DELETE FROM nerie.mt_programvenues pv WHERE pv.phaseid = :phaseid", nativeQuery = true)
       void deleteProgramVenuesEntryByPhaseid(@Param("phaseid") String phaseid);

       @Transactional
       @Modifying
       @Query(value = "INSERT INTO nerie.mt_program_members(programcode, usercode, phaseid) VALUES(:programcode, :usercode, :phaseid)", nativeQuery = true)
       void createProgramMembersEntry(@Param("programcode") String programcode, @Param("usercode") String usercode,
                     @Param("phaseid") String phaseid);

       @Transactional
       @Modifying
       @Query(value = "DELETE FROM nerie.mt_program_members pm WHERE pm.phaseid = :phaseid", nativeQuery = true)
       void deleteProgramMembersEntryByPhaseid(@Param("phaseid") String phaseid);

       @Query(value = "select  distinct(p.programcode), p.programname,p.programid,p.programdescription, count(ph.phaseid) as noofphase "
                + "from nerie.m_programs p, nerie.mt_programdetails pd, nerie.m_phases ph "
                + "where p.programcode = pd.programcode and p.programcode = ph.programcode "
                + "and p.officecode = :officecode "
                + "and (TO_CHAR(pd.enddate,'yyyy-MM-dd')<TO_CHAR(now(),'yyyy-MM-dd')) "
                + "and (TO_CHAR(pd.startdate,'yyyy-MM')>=:fystart AND TO_CHAR(pd.startdate,'yyyy-MM')<=:fyend) "
                + "and p.closed='N' and  "
                + "pd.finalized != 'R' "
                + "group by p.programname, p.programcode,pd.entrydate ", nativeQuery = true)
       List<Object[]> getUnCloseCourseList(@Param("officecode") String officecode, @Param("fystart") String fystart, @Param("fyend") String fyend);

       @Transactional
       @Modifying
       @Query(value = "UPDATE nerie.mt_programdetails SET closed = 'Y' WHERE programcode = :programcode", nativeQuery = true)
       void closeAllProgramDetails(@Param("programcode") String programcode);

       @Transactional
       @Modifying
       @Query(value = "UPDATE nerie.m_programs SET closed = 'Y', closingreport = :closingreport WHERE programcode = :programcode", nativeQuery = true)
       void closeProgram(@Param("programcode") String programcode, @Param("closingreport") String closingreport);

       @Query(value = "SELECT distinct(pd.phaseid ), p.programcode, p.programname,p.programdescription, p.programid , ph.phaseno,ph.phasedescription "
              + "FROM nerie.m_phases ph, nerie.mt_programdetails pd, nerie.m_programs p  "
              + "WHERE ph.programcode = p.programcode and pd.phaseid = ph.phaseid  "
              + "and p.officecode = :officecode "
              + "and (TO_CHAR(pd.enddate,'yyyy-MM-dd')<TO_CHAR(now(),'yyyy-MM-dd')) "
              + "and (TO_CHAR(pd.startdate, 'yyyy-MM') >= :fystart AND TO_CHAR(pd.startdate, 'yyyy-MM') <= :fyend) "
              + " and pd.closed = 'Y' and "
              + " pd.finalized != 'R'  "
              + "ORDER BY p.programname ", nativeQuery = true)
       List<Object[]> getCloseCourseList(@Param("officecode") String officecode, @Param("fystart") String fystart, @Param("fyend") String fyend);

       @Transactional
       @Modifying
       @Query(value = "UPDATE nerie.mt_programdetails SET closed = 'N' WHERE phaseid = :phaseid", nativeQuery = true)
       void unclosePhaseDetails(@Param("phaseid") String phaseid);

       @Transactional
       @Modifying
       @Query(value = "UPDATE nerie.m_programs SET closed = 'N' WHERE programcode = (SELECT programcode FROM nerie.m_phases WHERE phaseid = :phaseid)", nativeQuery = true)
       void uncloseProgram(@Param("phaseid") String phaseid);

       @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
                     "FROM M_Programs p WHERE p.programname = :programname")
       boolean existsByProgramname(@Param("programname") String programname);

       @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END " +
                     "FROM M_Programs p WHERE p.programid = :programid")
       boolean existsByProgramid(@Param("programid") String programid);

       @Query(value = "SELECT COUNT(DISTINCT p.programcode)  "
                + "FROM nerie.m_programs p,nerie.mt_programdetails pd   "
                + "WHERE pd.programcode=p.programcode AND p.officecode=:officecode AND TO_CHAR(pd.startdate,'yyyy-MM') between '2022-04' and '2023-03' "
                + "AND p.closed = 'N' AND pd.finalized='Y' AND TO_CHAR(enddate,'yyyy-MM-dd')<TO_CHAR(now(),'yyyy-MM-dd')", nativeQuery = true)
       Integer getCompletedProgramCount(@Param("officecode") String officecode);

       @Query(value = "SELECT COUNT(DISTINCT pd.programcode)  "
                + "FROM nerie.mt_programdetails pd, nerie.m_programs p  "
                + "WHERE pd.programcode=p.programcode AND TO_CHAR(pd.startdate,'yyyy-MM') between '2022-04' and '2023-03' AND  "
                + "pd.closed = 'Y' AND p.officecode=:officecode AND pd.finalized='Y'", nativeQuery = true)
       Integer getClosedProgamCount(@Param("officecode") String officecode);

       @Modifying
       @Query("DELETE FROM M_Programs WHERE programcode = :programcode")
       int deleteByProgramcode(@Param("programcode") String programcode);

       @Query(value = "select p.programdescription, p.programid,  " 
              +"(CAST((SELECT MAX(CAST(ph2.phaseno AS INT))FROM m_phases ph2 WHERE ph2.programcode = p.programcode"
              +" ) AS INT) + 1) AS nextphase "
              + "from m_programs p,   m_phases ph, mt_programdetails pd  " 
              + "where p.programcode = :pcode and ph.phaseid = pd.phaseid and ph.programcode = p.programcode order by ph.phaseno desc limit 1",
              nativeQuery = true)
       List<Object[]> getProgramDetailsBasedOnCodeToPopulateForm(@Param("pcode") String pcode);

       @Query(value = "select p.programname ,p.programdescription,p.programid,c.coursecategoryname,pd.startdate,pd.enddate, pd.lastdate,ph.phaseid,ph.phaseno,ph.phasedescription, "
                + "string_agg(distinct(mv.venuename), ',') as venues, string_agg(distinct(u.username), ',') as coordinators,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,p.programcode, string_agg(distinct(v.venuecode), ',') as venuecodes, "
                + "string_agg(distinct(m.usercode), ',') as usercodes, pd.programdetailid "
                + "from nerie.m_phases ph "
                + "inner join nerie.m_programs p on ph.programcode = p.programcode "
                + "inner join nerie.mt_programdetails pd on pd.phaseid = ph.phaseid "
                + "inner join nerie.mt_programvenues v on ph.phaseid = v.phaseid "
                + "inner join nerie.m_venues mv on mv.venuecode = v.venuecode "
                + "inner join nerie.mt_program_members m on ph.phaseid = m.phaseid "
                + "inner join nerie.mt_userlogin u on u.usercode = m.usercode "
                + "inner join nerie.m_coursecategories c on c.coursecategorycode = p.coursecodecategory "
                + "where p.officecode=:officecode and pd.finalized=:finalized AND " //and pd.closed='N'
                + "(TO_CHAR(pd.startdate,'yyyy-MM')) between :fystart and :fyend "
                + "AND p.enteredby=:usercode "
                + "group by ph.phaseid,p.programname,p.programdescription,p.programid,c.coursecategoryname,p.programcode,pd.lastdate,pd.startdate,pd.enddate,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,pd.programdetailid "
                + "order by p.programcode", nativeQuery = true)
       List<Object[]> getApprovedProgramsByUser(@Param("officecode") String ocode, @Param("fystart") String fystart, @Param("fyend") String fyend, @Param("finalized") String finalized, @Param("usercode") String usercode);

       @Query(value = "select p.programname ,p.programdescription,p.programid,c.coursecategoryname,pd.startdate,pd.enddate, pd.lastdate,ph.phaseid,ph.phaseno,ph.phasedescription, "
                + "string_agg(distinct(mv.venuename), ',') as venues, string_agg(distinct(u.username), ',') as coordinators,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,p.programcode, string_agg(distinct(v.venuecode), ',') as venuecodes, "
                + "string_agg(distinct(m.usercode), ',') as usercodes, pd.programdetailid "
                + "from nerie.m_phases ph "
                + "inner join nerie.m_programs p on ph.programcode = p.programcode "
                + "inner join nerie.mt_programdetails pd on pd.phaseid = ph.phaseid "
                + "inner join nerie.mt_programvenues v on ph.phaseid = v.phaseid "
                + "inner join nerie.m_venues mv on mv.venuecode = v.venuecode "
                + "inner join nerie.mt_program_members m on ph.phaseid = m.phaseid "
                + "inner join nerie.mt_userlogin u on u.usercode = m.usercode "
                + "inner join nerie.m_coursecategories c on c.coursecategorycode = p.coursecodecategory "
                + "where p.officecode=:officecode and pd.finalized=:finalized and pd.closed='N' AND "
                + "(TO_CHAR(pd.startdate,'yyyy-MM')) between :fystart and :fyend "
                + "group by ph.phaseid,p.programname,p.programdescription,p.programid,c.coursecategoryname,p.programcode,pd.lastdate,pd.startdate,pd.enddate,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,pd.programdetailid "
                + "order by p.programcode", nativeQuery = true)
       List<Object[]> getApprovedPrograms(@Param("officecode") String ocode, @Param("fystart") String fystart, @Param("fyend") String fyend, @Param("finalized") String finalized);

       @Query(value = "select p.programname ,p.programdescription,p.programid,c.coursecategoryname,pd.startdate,pd.enddate, pd.lastdate,ph.phaseid,ph.phaseno,ph.phasedescription, "
                + "string_agg(distinct(mv.venuename), ',') as venues, string_agg(distinct(u.username), ',') as coordinators,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,p.programcode, string_agg(distinct(v.venuecode), ',') as venuecodes, "
                + "string_agg(distinct(m.usercode), ',') as usercodes, pd.programdetailid, pd.rejectionremarks, pd.rejectionletter, pd.rejectiondate "
                + "from nerie.m_phases ph "
                + "inner join nerie.m_programs p on ph.programcode = p.programcode "
                + "inner join nerie.mt_programdetails pd on pd.phaseid = ph.phaseid "
                + "inner join nerie.mt_programvenues v on ph.phaseid = v.phaseid "
                + "inner join nerie.m_venues mv on mv.venuecode = v.venuecode "
                + "inner join nerie.mt_program_members m on ph.phaseid = m.phaseid "
                + "inner join nerie.mt_userlogin u on u.usercode = m.usercode "
                + "inner join nerie.m_coursecategories c on c.coursecategorycode = p.coursecodecategory "
                + "where p.officecode=:officecode and pd.finalized=:finalized and pd.closed='N' AND "
                //+ "(p.programcode IN (SELECT distinct(pm.programcode) FROM nerie.mt_program_members pm WHERE pm.usercode = :usercode) "
                + "p.enteredby=:usercode AND "
                + "(TO_CHAR(pd.startdate,'yyyy-MM')) between :fystart and :fyend "
                + "group by ph.phaseid,p.programname,p.programdescription,p.programid,c.coursecategoryname,p.programcode,pd.lastdate,pd.startdate,pd.enddate,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,pd.programdetailid "
                + "order by p.programcode", nativeQuery = true)
       List<Object[]> getRejectedProgramsByUser(@Param("officecode") String ocode, @Param("fystart") String fystart, @Param("fyend") String fyend, @Param("finalized") String finalized, @Param("usercode") String usercode);

       @Query(value = "select p.programname ,p.programdescription,p.programid,c.coursecategoryname,pd.startdate,pd.enddate, pd.lastdate,ph.phaseid,ph.phaseno,ph.phasedescription, "
                + "string_agg(distinct(mv.venuename), ',') as venues, string_agg(distinct(u.username), ',') as coordinators,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,p.programcode, string_agg(distinct(v.venuecode), ',') as venuecodes, "
                + "string_agg(distinct(m.usercode), ',') as usercodes, pd.programdetailid, pd.rejectionremarks, pd.rejectionletter, pd.rejectiondate "
                + "from nerie.m_phases ph "
                + "inner join nerie.m_programs p on ph.programcode = p.programcode "
                + "inner join nerie.mt_programdetails pd on pd.phaseid = ph.phaseid "
                + "inner join nerie.mt_programvenues v on ph.phaseid = v.phaseid "
                + "inner join nerie.m_venues mv on mv.venuecode = v.venuecode "
                + "inner join nerie.mt_program_members m on ph.phaseid = m.phaseid "
                + "inner join nerie.mt_userlogin u on u.usercode = m.usercode "
                + "inner join nerie.m_coursecategories c on c.coursecategorycode = p.coursecodecategory "
                + "where p.officecode=:officecode and pd.finalized=:finalized and pd.closed='N' AND "
                + "(TO_CHAR(pd.startdate,'yyyy-MM')) between :fystart and :fyend "
                + "group by ph.phaseid,p.programname,p.programdescription,p.programid,c.coursecategoryname,p.programcode,pd.lastdate,pd.startdate,pd.enddate,p.coursecodecategory,pd.courseclosedate,pd.approvalletter,pd.approvaldate,pd.programdetailid "
                + "order by p.programcode", nativeQuery = true)
       List<Object[]> getRejectedPrograms(@Param("officecode") String ocode, @Param("fystart") String fystart, @Param("fyend") String fyend, @Param("finalized") String finalized);
}

