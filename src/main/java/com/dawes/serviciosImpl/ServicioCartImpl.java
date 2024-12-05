package com.dawes.serviciosImpl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.CartItemRepository;
import com.dawes.repositorio.CartRepository;
import com.dawes.servicios.ServicioCart;

@Service
public class ServicioCartImpl implements ServicioCart{
	@Autowired
	private CartRepository cr;
	@Autowired
	private CartItemRepository cir;
	@Autowired
    private ServicioUsuarioImpl sui;	
    private final List<CartItem> cartItems = new ArrayList<>();
	
	@Override
	public List<Cart> findByUsuario(Usuario usuario) {		
		return cr.findByUsuario(usuario);
	}
	
    @Override
    public void addItem(CartItem item) {
        cartItems.add(item);
    }

    @Override
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems); 
    }

    @Override
    @Transactional
    public void clearCart() {
        cartItems.clear();
    }    
  
    public Integer getNumeroItemsCarrito(String username) {
        Usuario usuario = sui.findByUsername(username).orElse(null);
        if (usuario == null) {
            return 0; 
        }
        return cir.numeroItemsCart(usuario.getId()); 
    } 
    
    public boolean isCartParcial(Cart cart) {
        if (cart == null) {
            return false; 
        }
        return cir.findByCart(cart).stream().allMatch(CartItem::isSemanal);
    }

}
