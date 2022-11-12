package com.pius.cafe_mangement.entity;


import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.io.Serializable;

@NamedQuery(name = "User.findByEmailId", query = "select u  from User u where u.email=:email")

@NamedQuery(name = "User.getAllUsers", query = "select new com.pius.cafe_mangement.wrapper.UserWrapper(u.id, u.name,u.email,u.contactNumber,u.status) from User u where u.role='user'")

@NamedQuery(name = "User.updateStatus", query = "update User u set u.status=:status where u.id=:id")

@NamedQuery(name = "User.getAllUsers", query = "select u.email from User u where u.role='admin'")


@Entity
@Data
@DynamicUpdate
@DynamicInsert
@Table(name = "system_user")
public class User implements Serializable {

    private static final Long serialVersionUID =1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name= "id")
    private  Integer id;

    @Column(name = "name")
    private String name;

    @Column(name = "contactNumber")
    private String contactNumber;

    @Column(name = "email")
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "status")
    private String status;

    @Column(name = "role")
    private String role;



}
