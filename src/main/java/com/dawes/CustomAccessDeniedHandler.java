package com.dawes;

import java.io.IOException;
import org.springframework.security.access.AccessDeniedException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
	 @Override
	    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
	            throws IOException, ServletException {	        
	        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	        if (auth != null) {	          
	            String userRole = auth.getAuthorities().stream()
	                                  .map(grantedAuthority -> grantedAuthority.getAuthority())
	                                  .findFirst()
	                                  .orElse("USER");
	            request.setAttribute("userRole", userRole);
	        }	       
	        RequestDispatcher dispatcher = request.getRequestDispatcher("/403");
	        dispatcher.forward(request, response);
	    }
}
