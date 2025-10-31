package com.nic.nerie.m_taform.model;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.nic.nerie.m_phases.model.M_Phases;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_taform")
public class M_Taform {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="phaseid")
    private M_Phases phase;

    private String venue;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate fromdate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate todate;

    private String namerecord;
    private String designation;
    private double basicpay;
    private String address;
    private String city;
    private String pincode;

    private String resaddress;
    private String rcity;
    private String rpincode;

    private String accountnumber;
    private String bankname;
    private String branch;
    private String ifsc;
    private String pancardnumber;
    private boolean islocal;

    @ManyToOne
    @JoinColumn(name = "rp_userlogin_id")
    private MT_Userlogin rpUserlogin;

    public M_Taform() {
    }

    public boolean isIslocal() {
        return islocal;
    }

    public void setIslocal(boolean islocal) {
        this.islocal = islocal;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDate getFromdate() {
        return fromdate;
    }

    public void setFromdate(LocalDate fromdate) {
        this.fromdate = fromdate;
    }

    public LocalDate getTodate() {
        return todate;
    }

    public void setTodate(LocalDate todate) {
        this.todate = todate;
    }

    public String getNamerecord() {
        return namerecord;
    }

    public void setNamerecord(String namerecord) {
        this.namerecord = namerecord;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public double getBasicpay() {
        return basicpay;
    }

    public void setBasicpay(double basicpay) {
        this.basicpay = basicpay;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getResaddress() {
        return resaddress;
    }

    public void setResaddress(String resaddress) {
        this.resaddress = resaddress;
    }

    public String getRcity() {
        return rcity;
    }

    public void setRcity(String rcity) {
        this.rcity = rcity;
    }

    public String getRpincode() {
        return rpincode;
    }

    public void setRpincode(String rpincode) {
        this.rpincode = rpincode;
    }

    public String getAccountnumber() {
        return accountnumber;
    }

    public void setAccountnumber(String accountnumber) {
        this.accountnumber = accountnumber;
    }

    public String getBankname() {
        return bankname;
    }

    public void setBankname(String bankname) {
        this.bankname = bankname;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getIfsc() {
        return ifsc;
    }

    public void setIfsc(String ifsc) {
        this.ifsc = ifsc;
    }

    public String getPancardnumber() {
        return pancardnumber;
    }

    public void setPancardnumber(String pancardnumber) {
        this.pancardnumber = pancardnumber;
    }

    public MT_Userlogin getRpUserlogin() {
        return rpUserlogin;
    }

    public void setRpUserlogin(MT_Userlogin rpUserlogin) {
        this.rpUserlogin = rpUserlogin;
    }

    public M_Phases getPhase() {
        return phase;
    }

    public void setPhase(M_Phases phase) {
        this.phase = phase;
    }

    
}
