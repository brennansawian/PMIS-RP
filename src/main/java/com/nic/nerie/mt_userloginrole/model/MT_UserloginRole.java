package com.nic.nerie.mt_userloginrole.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nic.nerie.mt_userlogin.model.MT_Userlogin;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Entity
@Table(name = "mt_userloginrole")
public class MT_UserloginRole {
    @Id
    private Integer roleId;

    @NotBlank
    @Column(name = "role_name", length=25, nullable = false)
    private String roleName;

    @NotBlank
    @Column(name = "role_code", length=2, unique=true, nullable = false)
    private String roleCode;

    @OneToMany(mappedBy = "role", cascade = CascadeType.PERSIST, orphanRemoval = false)
    @JsonIgnore
    private List<MT_Userlogin> users;

    public MT_UserloginRole() {}

    public MT_UserloginRole(Integer roleId, String roleName, String roleCode) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.roleCode = roleCode;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Override
    public String toString() {
        return "MT_UserloginRole{" +
                "roleId=" + roleId +
                ", roleName='" + roleName + '\'' +
                ", roleCode='" + roleCode + '\'' +
                '}';
    }
}
