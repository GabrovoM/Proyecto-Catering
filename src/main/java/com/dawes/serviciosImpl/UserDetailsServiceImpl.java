package com.dawes.serviciosImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.dawes.modelo.Usuario;
import com.dawes.modelo.UsuarioRol;
import com.dawes.repositorio.UsuarioRepository;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {
	
	@Autowired
	private UsuarioRepository ur;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Usuario u = ur.findByUsername(username).get();
		if (u == null) {
			System.out.println("Usuario no encontrado " + username);
			throw new UsernameNotFoundException("Usuario " + username + " no existe en la base de datos");
		}
		System.err.println("Usuario encontrado: " + u);		
		List<GrantedAuthority> grantList = new ArrayList<GrantedAuthority>();
		if (u.getRoles() != null) {
			for (UsuarioRol role : u.getRoles()) {
				GrantedAuthority authority = new SimpleGrantedAuthority(role.getRol().getRoleName());
				grantList.add(authority);				
			}
		}		
		UserDetails userDetails = (UserDetails) new User(u.getUsername(), 
	               u.getPassword(), grantList);		
		return userDetails;
	}

}
