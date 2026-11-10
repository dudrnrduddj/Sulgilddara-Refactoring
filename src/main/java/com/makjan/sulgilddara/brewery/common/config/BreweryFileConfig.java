package com.makjan.sulgilddara.brewery.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BreweryFileConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 양조장 이미지 설정
        registry.addResourceHandler("/brewery-images/**")
                .addResourceLocations("file:C:/uploadFile/brewery/");
        
        // 생산제품 이미지 설정
        registry.addResourceHandler("/liquor-images/**")
                .addResourceLocations("file:C:/uploadFile/liquor/");
    }
}