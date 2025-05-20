package com.org.scraper_bkd.exception;

import org.springframework.http.HttpStatus;

public class BadClient_Request extends  RuntimeException {
    private final HttpStatus status;
    private  final String responseBody;

    public BadClient_Request(String message, HttpStatus httpStatus,String responseBody) {
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
