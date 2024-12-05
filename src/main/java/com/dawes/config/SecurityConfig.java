package com.dawes.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.dawes.CustomAccessDeniedHandler;
import com.dawes.serviciosImpl.UserDetailsServiceImpl;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	@Autowired
	private UserDetailsServiceImpl userDetailsService;
	
	@Bean
	public UserDetailsServiceImpl userDetailsService(){
	    return new UserDetailsServiceImpl();
	}
	
	@Bean
	 public BCryptPasswordEncoder passwordEncoder(){
	     return new BCryptPasswordEncoder();
	 }
	
	@Bean
	 public DaoAuthenticationProvider authenticationProvider(){
	     DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
	     authenticationProvider.setUserDetailsService(userDetailsService);
	     authenticationProvider.setPasswordEncoder(passwordEncoder());
	     return authenticationProvider;
	 }
	 
	 @Bean
	 public AuthenticationManager authenticationManager(HttpSecurity httpSecurity) throws Exception {
	     AuthenticationManagerBuilder authenticationManagerBuilder = httpSecurity.getSharedObject(AuthenticationManagerBuilder.class);	        
	     authenticationManagerBuilder.authenticationProvider(authenticationProvider());
	     return authenticationManagerBuilder.build();
	 }
	 
	 @Bean
     public CustomAccessDeniedHandler customAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
     }
	 
	 @Bean
	    protected SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
	        httpSecurity
	            .authorizeHttpRequests(request -> request	              
	                .requestMatchers("/", "/index", "/public/**", "/webjars/**", "/css/**", "/js/**", "/images/**", "/img/**", "/platos/**", "/menus/**", "/contact", "/api/platos/**").permitAll()
	                .requestMatchers("/forgot_password", "/reset_password/**").permitAll()	                
	                .requestMatchers("/api/pedidos/**").permitAll()	               
	                .requestMatchers("/login", "/logout", "/registrarse").permitAll() 	              
	                .requestMatchers("/admin/home.html", "/admin/usuario/**").hasRole("ADMIN")	              
	                .requestMatchers("/admin/pedidos/**").hasAnyRole("GESTOR", "ADMIN")
	                .requestMatchers("/admin/categoria/**", "/admin/ingredientes/**", "/admin/menus/**", "/admin/plato/**").hasAnyRole("ADMIN", "EDITOR")
	                .requestMatchers("/admin/**").hasAnyRole("ADMIN", "GESTOR", "EDITOR")  	           
	                .requestMatchers("/registrado").hasAnyRole("USER", "ADMIN")	          
	                .anyRequest().authenticated()
	            )	        
	            .exceptionHandling(handling -> handling.accessDeniedHandler(customAccessDeniedHandler()))	          
	            .formLogin(form -> form
	                .loginProcessingUrl("/j_spring_security_check")  
	                .loginPage("/login")                            
	                .failureUrl("/login?error=true")                
	                .usernameParameter("username")                  
	                .passwordParameter("password")                  
	                .defaultSuccessUrl("/redirectByRole", true)     
	            )	         
	            .logout(logout -> logout
	                .logoutUrl("/logout")                           
	                .logoutSuccessUrl("/")                          
	                .permitAll()                                   
	            );	           
//	            .csrf(csrf -> csrf.disable());

	        return httpSecurity.build();
	    }
	 

}

