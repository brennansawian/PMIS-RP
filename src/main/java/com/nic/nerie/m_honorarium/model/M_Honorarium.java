package com.nic.nerie.m_honorarium.model;

import java.time.LocalDate;

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
@Table(name = "m_honorarium")
public class M_Honorarium {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="phaseid")
    private M_Phases phase;

    private String venue;
    private LocalDate fromdate;
    private LocalDate todate;
    private int numberofdays;
    private double rateperday;
    private double totalamount;

    private String amountinwords;

    private String nameofficial;
    private String designation;
    private String address;

    private String accountnumber;
    private String bankname;
    private String branch;
    private String ifsc;
    private String pancardnumber;

    @ManyToOne
    private MT_Userlogin rpUserlogin;

    public M_Honorarium() {
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

    public int getNumberofdays() {
        return numberofdays;
    }

    public void setNumberofdays(int numberofdays) {
        this.numberofdays = numberofdays;
    }

    public double getRateperday() {
        return rateperday;
    }

    public void setRateperday(double rateperday) {
        this.rateperday = rateperday;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public String getAmountinwords() {
        return amountinwords;
    }

    public void setAmountinwords(String amountinwords) {
        this.amountinwords = amountinwords;
    }

    public String getNameofficial() {
        return nameofficial;
    }

    public void setNameofficial(String nameofficial) {
        this.nameofficial = nameofficial;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public MT_Userlogin getRpUserlogin() {
        return rpUserlogin;
    }

    public void setRpUserlogin(MT_Userlogin rpUserlogin) {
        this.rpUserlogin = rpUserlogin;
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

    public M_Phases getPhase() {
        return phase;
    }

    public void setPhase(M_Phases phase) {
        this.phase = phase;
    }
 

}