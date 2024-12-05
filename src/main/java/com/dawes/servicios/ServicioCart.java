package com.dawes.servicios;

import java.util.List;

import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.Usuario;

public interface ServicioCart {
	List<Cart> findByUsuario(Usuario usuario);	
	void addItem(CartItem item);
    List<CartItem> getCartItems();
    void clearCart();
}
