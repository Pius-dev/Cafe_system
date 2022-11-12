package com.pius.cafe_mangement.repository;

import com.pius.cafe_mangement.entity.User;
import com.pius.cafe_mangement.wrapper.UserWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Integer> {

    // create named query
    User findByEmailId(@Param("email") String email);

    List<UserWrapper> getAllUsers();

    @Transactional
    @Modifying
    Integer updateStatus(@Param("status")String status, @Param("id") Integer id);

    List<String> getAllAdmin();
}
