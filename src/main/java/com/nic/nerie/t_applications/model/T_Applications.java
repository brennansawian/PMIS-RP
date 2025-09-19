package com.nic.nerie.t_applications.model;

import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "t_applications")
public class T_Applications implements Serializable {

    @Id
    private String applicationcode;
    private String emailsent;
    @ManyToOne
    @JoinColumn(name = "usercode")
    public MT_Userlogin mtuserlogin;
    public String status;
    public String remarks;
    @ManyToOne
    @JoinColumn(name = "usercodewhoapplied")
    public MT_Userlogin mtuserloginapplied;
    @ManyToOne
    @JoinColumn(name = "phaseid")
    public M_Phases phaseid;

    public T_Applications() {
    }

    public T_Applications(String applicationcode, String emailsent, MT_Userlogin mtuserlogin, String status, String remarks, MT_Userlogin mtuserloginapplied, M_Phases phaseid) {
        this.applicationcode = applicationcode;
        this.emailsent = emailsent;
        this.mtuserlogin = mtuserlogin;
        this.status = status;
        this.remarks = remarks;
        this.mtuserloginapplied = mtuserloginapplied;
        this.phaseid = phaseid;
    }

    public String getApplicationcode() {
        return applicationcode;
    }

    public void setApplicationcode(String applicationcode) {
        this.applicationcode = applicationcode;
    }

    public String getEmailsent() {
        return emailsent;
    }

    public void setEmailsent(String emailsent) {
        this.emailsent = emailsent;
    }

    public MT_Userlogin getMtuserlogin() {
        return mtuserlogin;
    }

    public void setMtuserlogin(MT_Userlogin mtuserlogin) {
        this.mtuserlogin = mtuserlogin;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public MT_Userlogin getMtuserloginapplied() {
        return mtuserloginapplied;
    }

    public void setMtuserloginapplied(MT_Userlogin mtuserloginapplied) {
        this.mtuserloginapplied = mtuserloginapplied;
    }

    public M_Phases getPhaseid() {
        return phaseid;
    }

    public void setPhaseid(M_Phases phaseid) {
        this.phaseid = phaseid;
    }

    @Override
    public String toString() {
        return "T_Applications{" + "applicationcode=" + applicationcode + ", emailsent=" + emailsent + ", mtuserlogin=" + mtuserlogin + ", status=" + status + ", remarks=" + remarks + ", mtuserloginapplied=" + mtuserloginapplied + ", phaseid=" + phaseid + '}';
    }
}
