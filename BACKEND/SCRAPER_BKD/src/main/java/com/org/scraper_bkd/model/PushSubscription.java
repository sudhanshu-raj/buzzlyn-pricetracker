package com.org.scraper_bkd.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PushSubscription {

    private Map<String, String> keys;
    private String endpoint;
    private String expirationTime;
}
