package com.org.scraper_bkd_security.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@AllArgsConstructor
public class OtpSendException  extends RuntimeException{

    private HttpStatus status;
    private String responseBody;

    public OtpSendException(String message,HttpStatus httpStatus,String responseBody){
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
