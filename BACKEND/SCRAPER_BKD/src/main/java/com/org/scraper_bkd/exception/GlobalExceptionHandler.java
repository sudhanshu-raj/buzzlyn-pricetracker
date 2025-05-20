package com.org.scraper_bkd.exception;

import com.org.scraper_bkd.service.ProductScraperService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidPythonAPI_Request.class)
    public ResponseEntity<String> handleInvalidPythonAPIRequest(final InvalidPythonAPI_Request ex) {
        logger.error("Python API Error - Status: {}, Message: {}",
                ex.getStatus(),
                ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ex.getMessage());
    }

    @ExceptionHandler(BadClient_Request.class)
    public ResponseEntity<String> handleBadClientRequest(final BadClient_Request ex) {
        logger.warn("Unsupported Request - Status: {}, Message: {}",
                ex.getStatus(),
                ex.getMessage());
        return ResponseEntity.status(ex.getStatus())
                .body(ex.getMessage());
    }

    @ExceptionHandler(PriceTrackerExistedAlready.class)
    public ResponseEntity<String> handlePriceTrackerExisted(final PriceTrackerExistedAlready ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ex.getMessage());
    }

    @ExceptionHandler(PriceTrackerDBError.class)
    public ResponseEntity<String> handlePriceTrackerDBError(final PriceTrackerDBError ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ex.getMessage());
    }


}
