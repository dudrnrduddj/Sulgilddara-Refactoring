package com.makjan.sulgilddara.tour.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TourFileConfig implements WebMvcConfigurer {
	private String webPath = "/tour-images/**";
	private String realPath = "file:C:/uploadFile/tour/";
//	private String realPath = "file:/Users/eom-eunji/Downloads/uploadFile/tour/";
	
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(webPath)
			.addResourceLocations(realPath);
	}
}
