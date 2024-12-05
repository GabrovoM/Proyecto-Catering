package com.dawes;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ResourceWebConfiguration implements WebMvcConfigurer {
	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// local
//		registry.addResourceHandler("/images/**").addResourceLocations("file:images/");
		
		// despliegue
		registry.addResourceHandler("/images/**").addResourceLocations("file:/opt/images/");
	}

}

