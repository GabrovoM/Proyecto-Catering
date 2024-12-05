package com.dawes.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.Menu;
import com.dawes.modelo.Plato;

public interface CartItemRepository extends JpaRepository<CartItem, Integer> {	
	Optional<CartItem> findByCartAndPlato(Cart cart, Plato plato);
	Optional<CartItem> findByCartAndMenu(Cart cart, Menu menu);
	List<CartItem> findByCart(Cart cart);	
	@Query("SELECT COALESCE(SUM(ci.cantidadCart), 0) FROM CartItem ci JOIN ci.cart c WHERE c.usuario.id = :usuarioId")
	Integer numeroItemsCart(@Param("usuarioId") Integer usuarioId);	
	void deleteByCart(Cart cart);
}
