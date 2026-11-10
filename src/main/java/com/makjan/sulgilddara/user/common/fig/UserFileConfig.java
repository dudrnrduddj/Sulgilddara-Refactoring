package com.makjan.sulgilddara.user.common.fig;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class UserFileConfig implements WebMvcConfigurer{
	private String webPath = "/user-images/**";
	private String realPath = "file:C:/uploadFile/user/";

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler(webPath)
				.addResourceLocations(realPath);
	}
}
