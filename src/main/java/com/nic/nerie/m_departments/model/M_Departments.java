package com.nic.nerie.m_departments.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "m_departments")
public class M_Departments {
    @Id
    private String departmentcode;
    private String departmentname;

    public M_Departments() {
    }

    public M_Departments(String departmentcode, String departmentname) {
        this.departmentcode = departmentcode;
        this.departmentname = departmentname;
    }

    public String getDepartmentcode() {
        return departmentcode;
    }

    public void setDepartmentcode(String departmentcode) {
        this.departmentcode = departmentcode;
    }

    public String getDepartmentname() {
        return departmentname;
    }

    public void setDepartmentname(String departmentname) {
        this.departmentname = departmentname;
    }

    @Override
    public String toString() {
        return "M_Departments{" +
                "departmentcode='" + departmentcode + '\'' +
                ", departmentname='" + departmentname + '\'' +
                '}';
    }
}
