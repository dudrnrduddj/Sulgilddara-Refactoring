package com.makjan.sulgilddara.liquor.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class LiquorFileConfig implements WebMvcConfigurer {
	private String webPath = "/liquor-images/**"; 			 // /images/**
	private String realPath = "file:C:/uploadFile/liquor/"; // file:C:/uploadFile/notice
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(webPath)
			.addResourceLocations(realPath);
	}
}
