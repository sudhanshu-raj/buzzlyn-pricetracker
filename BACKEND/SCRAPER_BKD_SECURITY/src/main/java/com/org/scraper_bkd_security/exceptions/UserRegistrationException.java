package com.org.scraper_bkd_security.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class UserRegistrationException extends RuntimeException {

    private HttpStatus status;
    private String responseBody;

    public UserRegistrationException(String message,HttpStatus httpStatus,String responseBody){
        super(message);
        this.status=httpStatus;
        this.responseBody=responseBody;
    }

    public HttpStatus getStatus() {
        return status;
    }
    public String getResponseBody() {
        return responseBody;
    }

}
