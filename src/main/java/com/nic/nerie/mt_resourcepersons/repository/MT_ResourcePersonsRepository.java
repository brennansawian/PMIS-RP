package com.nic.nerie.mt_resourcepersons.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.nic.nerie.mt_resourcepersons.model.MT_ResourcePersons;

import jakarta.transaction.Transactional;

public interface MT_ResourcePersonsRepository extends JpaRepository<MT_ResourcePersons, String> {
        @Query("FROM MT_ResourcePersons")
        List<MT_ResourcePersons> findAll();

        @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
                        "FROM MT_ResourcePersons r WHERE UPPER(r.rpemailid) = :rpemailid")
        boolean existsByRpemailid(@Param("rpemailid") String rpemailid);

        @Query(value = "SELECT DISTINCT rp.rpslno, rp.rpemailid, rp.rpname, q.qualificationname, rp.rpspecialization, "
                        +
                        "d.designationname, rp.rpinstitutename, rp.rpofficeaddress, p.phaseid " +
                        "FROM nerie.mt_resourcepersons rp " +
                        "LEFT JOIN nerie.mt_resourcepersoncoursemap rpm ON rp.rpslno = rpm.rpslno AND rpm.phaseid = :phaseid "
                        +
                        "LEFT JOIN nerie.m_phases p ON p.phaseid = rpm.phaseid " +
                        "LEFT JOIN nerie.m_qualifications q ON q.qualificationcode = rp.qualificationcode " +
                        "LEFT JOIN nerie.m_designations d ON d.designationcode = rp.designationcode " +
                        "ORDER BY rp.rpname", nativeQuery = true)
        List<Object[]> getAllResourcePersonsWithPhase(@Param("phaseid") String phaseid);

        @Query(value = """
                        SELECT rc.rpslno, p.rpname
                        FROM nerie.mt_resourcepersoncoursemap rc
                        INNER JOIN nerie.mt_resourcepersons p ON rc.rpslno = p.rpslno
                        WHERE rc.phaseid = :phaseid
                        ORDER BY p.rpname
                        """, nativeQuery = true)
        List<Object[]> getResourcePersonsPhaseid(@Param("phaseid") String phaseid);

        @Query("SELECT MAX(CAST(rp.rpslno as int)) FROM MT_ResourcePersons rp")
        Integer getLastUsedRpslno();

        @Transactional
        @Modifying
        @Query(value = "DELETE FROM nerie.mt_resourcepersoncoursemap rp where rp.phaseid = :phaseid", nativeQuery = true)
        void deleteResourcePersonCourseEntryByPhaseid(@Param("phaseid") String phaseid);

        @Transactional
        @Modifying
        @Query(value = "INSERT INTO nerie.mt_resourcepersoncoursemap(phaseid, rpslno) " +
                        "VALUES(:phaseid, :resourceperson) ", nativeQuery = true)
        void createResourcePersonCourseEntry(@Param("phaseid") String phaseid,
                        @Param("resourceperson") String resourceperson);

        @Query(value = "SELECT DISTINCT ph.phaseno, ph.phasedescription, p.programname, p.programid, p.programdescription,pd.startdate,pd.enddate, rpcm.phaseid,p.programcode FROM nerie.mt_userlogin ul "
                        + "INNER JOIN nerie.mt_resourcepersons rp ON ul.userid = rp.rpemailid "
                        + "INNER JOIN nerie.mt_resourcepersoncoursemap rpcm ON rp.rpslno = rpcm.rpslno "
                        + "INNER JOIN nerie.m_phases ph ON rpcm.phaseid = ph.phaseid "
                        + "INNER JOIN nerie.m_programs p ON ph.programcode = p.programcode "
                        + "INNER JOIN nerie.mt_programdetails pd ON rpcm.phaseid = pd.phaseid "
                        + "WHERE ul.userid = :emailid", nativeQuery = true)
        List<Object[]> findRPPrograms(@Param("emailid") String emailid);

}
