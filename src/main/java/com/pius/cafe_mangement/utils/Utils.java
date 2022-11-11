package com.pius.cafe_mangement.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class Utils {

    // default constructor
    private Utils(){

    }

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus){
        return new ResponseEntity<String> ("{\"message\":\""+responseMessage+"\"}", httpStatus);
    }
}
