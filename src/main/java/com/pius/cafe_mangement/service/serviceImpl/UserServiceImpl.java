package com.pius.cafe_mangement.service.serviceImpl;

import com.pius.cafe_mangement.contants.CafeContants;
import com.pius.cafe_mangement.entity.User;
import com.pius.cafe_mangement.jwt.CustomerUsersDetailsService;
import com.pius.cafe_mangement.jwt.JwtUtil;
import com.pius.cafe_mangement.repository.UserRespository;
import com.pius.cafe_mangement.service.UserService;
import com.pius.cafe_mangement.utils.Utils;
import jdk.jshell.execution.Util;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRespository userRespository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("inside signUp {}" + requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userRespository.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userRespository.save(getUserFromMap(requestMap));
                    return Utils.getResponseEntity("Successfully Registered", HttpStatus.OK);
                } else {
                    return Utils.getResponseEntity("Email already exits", HttpStatus.BAD_REQUEST);
                }
            } else {
                return Utils.getResponseEntity(CafeContants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return Utils.getResponseEntity(CafeContants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

    }

    private boolean validateSignUpMap(Map<String, String> requestMap) {
        if (
                requestMap.containsKey("name") && requestMap.containsKey("email") && requestMap.containsKey("password")
                        && requestMap.containsKey("contactNumber")) {
            return true;
        }
        return false;

    }

    // extract user from the map, set value and return object

    private User getUserFromMap(Map<String, String> requestMap) {
        User user = new User();
        user.setName(requestMap.get("name"));
        user.setEmail(requestMap.get("email"));
        user.setContactNumber(requestMap.get("contactNumber"));
        user.setPassword(requestMap.get("password"));
        user.setStatus("false");
        user.setRole("user");
        return user;
    }

    @Override
    public ResponseEntity<String> login(Map<String, String> requestMap) {
        log.info("Inside login");
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            requestMap.get("email"), requestMap.get("password")
                    )
            );
            if (auth.isAuthenticated()) {
                if (customerUsersDetailsService.getUserDetail().getStatus().equalsIgnoreCase("true")) {

                    return new ResponseEntity<String>("{\"token\":\"" +
                            jwtUtil.generateToken(customerUsersDetailsService.getUserDetail().getEmail(),
                                    customerUsersDetailsService.getUserDetail().getRole()) + "\"}", HttpStatus.OK);
                } else {
                    return new ResponseEntity<String>("{\"message\":\"" + "Wait for admin approval." + "\"}",
                            HttpStatus.BAD_REQUEST);
                }
            }

        } catch (Exception ex) {
            log.error("{}", ex);
        }
        return new ResponseEntity<String>("{\"message\":\"" + "Invalid Credentials." + "\"}",
                HttpStatus.BAD_REQUEST);
    }

}
