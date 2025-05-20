package com.org.scraper_bkd.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.org.scraper_bkd.constants.AppConstant.PYTHON_SCRAPER_SECRETKEY;
import static com.org.scraper_bkd.constants.AppConstant.PYTHON_SCRAPER_URL;

//@Data
@Getter
@Component
public class AppConfig {

    private final String scraperUrl=PYTHON_SCRAPER_URL;

    private final String scraperApiKey=PYTHON_SCRAPER_SECRETKEY;


}
