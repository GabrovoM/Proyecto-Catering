package com.dawes.serviciosImpl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import com.dawes.modelo.Cart;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.CartItemRepository;

@Service
public class UsuarioSesionService {
	
	 private final ServicioUsuarioImpl sui;
     private final ServicioCartImpl csi;
     private final CartItemRepository cir;

     public UsuarioSesionService(ServicioUsuarioImpl sui, ServicioCartImpl csi, CartItemRepository cir) {
        this.sui = sui;
        this.csi = csi;
        this.cir = cir;
     }     
     
     public void addUserDetailsToModel(Model model, @AuthenticationPrincipal UserDetails userDetails) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    boolean isAuthenticated = auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
	    boolean userAdmin = false;    	   
	    if (isAuthenticated) {
	        for (GrantedAuthority authority : auth.getAuthorities()) {
//	            if (authority.getAuthority().equals("ROLE_ADMIN")) {
	        	String role = authority.getAuthority();
	        	if (role.equals("ROLE_ADMIN") || role.equals("ROLE_GESTOR") || role.equals("ROLE_EDITOR")) {
	                userAdmin = true;
	                break;
	            }
	        }    	      
	        if (userDetails != null) {
	            Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	            if (usuario != null) {
	                Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);
	                Integer numeroItemsCarrito = cir.numeroItemsCart(usuario.getId());
	                model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
	                if (numeroItemsCarrito != null && numeroItemsCarrito > 0) {
	                    model.addAttribute("carritoMsg", "Tienes más de 1 artículo en el carrito.");
	                }
	            } else {    	              
	                model.addAttribute("numeroItemsCarrito", 0);
	                model.addAttribute("carritoMsg", "No tienes artículos en el carrito.");
	            }
	        } else {    	          
	            model.addAttribute("numeroItemsCarrito", 0);
	            model.addAttribute("carritoMsg", "Inicia sesión para ver tu carrito.");
	        }
	    } else {    	      
	        model.addAttribute("numeroItemsCarrito", 0);
	        model.addAttribute("carritoMsg", "Inicia sesión para ver tu carrito.");
	    }
	    model.addAttribute("isAuthenticated", isAuthenticated);
	    model.addAttribute("userAdmin", userAdmin);
	}

    public static String encodeURIComponent(String value) {
        try {
            return URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

}
