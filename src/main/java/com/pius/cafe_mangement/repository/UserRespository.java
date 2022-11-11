package com.pius.cafe_mangement.repository;

import com.pius.cafe_mangement.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;


public interface UserRespository extends JpaRepository<User, Integer> {

    // create named query
    User findByEmailId(@Param("email") String email);
}
