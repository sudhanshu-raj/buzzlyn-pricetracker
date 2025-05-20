package com.org.scraper_bkd.exception;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;


public class PriceTrackerExistedAlready extends RuntimeException{

    private final HttpStatus status;
    private  final String responseBody;

    public PriceTrackerExistedAlready(String message, HttpStatus httpStatus,String responseBody) {
        super(message);
        this.status = httpStatus;
        this.responseBody = responseBody;
    }
    public HttpStatus getStatus() {
        return status;
    }
    public String getResponseBody() {
        return responseBody;
    }
}
