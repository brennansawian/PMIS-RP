package com.nic.nerie.t_conveyancecharge.model;

import java.sql.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.nic.nerie.m_taform.model.M_Taform;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
@Table(name = "t_conveyancecharge")
public class T_ConveyanceCharge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String placeofdeparture;
    private String placeofarrival;
    private Double kms;
    private String modeofconveyance;
    private Double amount;
    private Date dateofdeparture;
    private Date dateofarrival;
    private String timeofdeparture;
    private String timeofarrival;
    private String detailsoftravel;
    private String nonlocalpartno;

    @ManyToOne
    @JoinColumn(name = "taform_id")
    public M_Taform taform;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date date;

    public T_ConveyanceCharge() {
    }

    public Date getDateofdeparture() {
        return dateofdeparture;
    }

    public void setDateofdeparture(Date dateofdeparture) {
        this.dateofdeparture = dateofdeparture;
    }

    public Date getDateofarrival() {
        return dateofarrival;
    }

    public void setDateofarrival(Date dateofarrival) {
        this.dateofarrival = dateofarrival;
    }

    public String getTimeofdeparture() {
        return timeofdeparture;
    }

    public void setTimeofdeparture(String timeofdeparture) {
        this.timeofdeparture = timeofdeparture;
    }

    public String getTimeofarrival() {
        return timeofarrival;
    }

    public void setTimeofarrival(String timeofarrival) {
        this.timeofarrival = timeofarrival;
    }

    public String getDetailsoftravel() {
        return detailsoftravel;
    }

    public void setDetailsoftravel(String detailsoftravel) {
        this.detailsoftravel = detailsoftravel;
    }

    public String getNonlocalpartno() {
        return nonlocalpartno;
    }

    public void setNonlocalpartno(String nonlocalpartno) {
        this.nonlocalpartno = nonlocalpartno;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getKms() {
        return kms;
    }

    public void setKms(Double kms) {
        this.kms = kms;
    }

    public String getPlaceofdeparture() {
        return placeofdeparture;
    }

    public void setPlaceofdeparture(String placeofdeparture) {
        this.placeofdeparture = placeofdeparture;
    }

    public String getPlaceofarrival() {
        return placeofarrival;
    }

    public void setPlaceofarrival(String placeofarrival) {
        this.placeofarrival = placeofarrival;
    }

    public String getModeofconveyance() {
        return modeofconveyance;
    }

    public void setModeofconveyance(String modeofconveyance) {
        this.modeofconveyance = modeofconveyance;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public M_Taform getTaform() {
        return taform;
    }

    public void setTaform(M_Taform taform) {
        this.taform = taform;
    }

}