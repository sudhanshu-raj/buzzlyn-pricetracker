package com.org.scraper_bkd.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
public class WebPushMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    public String title;
    public String clickTarget;
    public String message;
    public String icon;
    public String image;

    public WebPushMessage(String title,String message, String clickTarget,String icon,String image){
        this.title=title;
        this.message=message;
        this.clickTarget=clickTarget;
        this.icon=icon;
        this.image=image;
    }
}
