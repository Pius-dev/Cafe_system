package com.pius.cafe_mangement.service.serviceImpl;

import com.google.common.base.Strings;
import com.pius.cafe_mangement.contants.CafeContants;
import com.pius.cafe_mangement.entity.User;
import com.pius.cafe_mangement.jwt.CustomerUsersDetailsService;
import com.pius.cafe_mangement.jwt.JwtFilter;
import com.pius.cafe_mangement.jwt.JwtUtil;
import com.pius.cafe_mangement.repository.UserRepository;
import com.pius.cafe_mangement.service.UserService;
import com.pius.cafe_mangement.utils.EmailUtils;
import com.pius.cafe_mangement.utils.Utils;
import com.pius.cafe_mangement.wrapper.UserWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    CustomerUsersDetailsService customerUsersDetailsService;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    JwtFilter jwtFilter;

    @Autowired
    EmailUtils emailUtils;

    @Override
    public ResponseEntity<String> signUp(Map<String, String> requestMap) {
        log.info("inside signUp {}" + requestMap);
        try {
            if (validateSignUpMap(requestMap)) {
                User user = userRepository.findByEmailId(requestMap.get("email"));
                if (Objects.isNull(user)) {
                    userRepository.save(getUserFromMap(requestMap));
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

    @Override
    public ResponseEntity<List<UserWrapper>> getAllUsers() {
        try {
            if(jwtFilter.isAdmin()){
                return new ResponseEntity<>(userRepository.getAllUsers(), HttpStatus.OK);

            }else {
                return new ResponseEntity<>(new ArrayList<>(), HttpStatus.UNAUTHORIZED);
            }


        }catch (Exception ex){
            ex.printStackTrace();
        }

        return new ResponseEntity<>(new ArrayList<>(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> update(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()){
                Optional<User> optional = userRepository.findById(Integer.parseInt(requestMap.get("id")));
                if (!optional.isEmpty()){
                    userRepository.updateStatus(requestMap.get("status"), Integer.parseInt(requestMap.get("id")));
                    sendMailToAllAdmin(requestMap.get("status"), optional.get().getEmail(), userRepository.getAllAdmin());
                    return Utils.getResponseEntity("User status updated successfully.", HttpStatus.OK);
                }else {
                    return Utils.getResponseEntity("User ID does not exit", HttpStatus.OK);
                }


            }else {
                return Utils.getResponseEntity(CafeContants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return Utils.getResponseEntity(CafeContants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    private void sendMailToAllAdmin(String status, String user, List<String> allAdmin) {
        allAdmin.remove(jwtFilter.getCurrentUser());
        if (status != null && status.equalsIgnoreCase("true")){
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),
                    "Account Approved",
                    "User:-"+user+"\n is approved by \nADMIN:-"+jwtFilter.getCurrentUser(), allAdmin);


        }else {
            emailUtils.sendSimpleMessage(jwtFilter.getCurrentUser(),
                    "Account Disabled",
                    "User:-"+user+"\n is disabled by \nADMIN:-"+jwtFilter.getCurrentUser(), allAdmin);

        }
    }

    @Override
    public ResponseEntity<String> checkToken() {
        return Utils.getResponseEntity("true", HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> changePassword(Map<String, String> requestMap) {
        try {
            User userObj = userRepository.findByEmail(jwtFilter.getCurrentUser());
            if (!userObj.equals(null)){
                if (userObj.getPassword().equals(requestMap.get("oldPassword"))){
                    userObj.setPassword(requestMap.get("newPassword"));
                    userRepository.save(userObj);
                    return Utils.getResponseEntity("Password updated Successfully", HttpStatus.OK);
                }
                return Utils.getResponseEntity("Incorrect Old Password", HttpStatus.BAD_REQUEST);

            }
            return Utils.getResponseEntity(CafeContants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return Utils.getResponseEntity(CafeContants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> forgotPassword(Map<String, String> requestMap) {
        try {
            User user = userRepository.findByEmail(requestMap.get("email"));
            if (!Objects.isNull(user) && !Strings.isNullOrEmpty(user.getEmail()))
                emailUtils.forgotPasswordMail(user.getEmail(),"Requested Login Credentials by Cafe management System", user.getPassword());
            return Utils.getResponseEntity("Check Your Email for credentials.", HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return Utils.getResponseEntity(CafeContants.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
