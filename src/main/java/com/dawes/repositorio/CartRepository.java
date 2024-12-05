package com.dawes.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.Usuario;

public interface CartRepository extends JpaRepository<Cart, Integer> {
	List<Cart> findByUsuario(Usuario usuario);	
}
