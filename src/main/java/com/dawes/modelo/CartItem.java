package com.dawes.modelo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class CartItem {
	 @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Integer id;
	    @ManyToOne
	    private Cart cart;
	    @ManyToOne
	    private Plato plato;
	    @ManyToOne
//	    @JoinColumn(name = "menu_id", referencedColumnName = "id", insertable = false, updatable = false)
	    private Menu menu; 	    
	    private int cantidadCart;	    
	    private boolean semanal;	      
	    
	    @Override
	    public String toString() {
	        return "CartItem{" +
	               "plato=" + plato +  
	               "menu=" + menu +
	               '}';
	    }

}
