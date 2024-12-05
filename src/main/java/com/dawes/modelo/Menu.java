package com.dawes.modelo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="menus")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Menu {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique=true)
	private String nombre;
	private Double precio;
	private boolean noDisponible;
	
	@OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MenuPlato> platos = new ArrayList<>();
	 
	@OneToMany(mappedBy="menu")
	@JsonManagedReference
	private List<PlatoMenuPedido> lineas_pedido = new ArrayList<>();
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Menu menu = (Menu) o;
        return id == menu.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Menu{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio='" + precio + '\'' +	                
                '}';
    } 	
		 
    public void addPlato(MenuPlato menuPlato) {
        if (this.platos == null) {
            this.platos = new ArrayList<>();
        }
        this.platos.add(menuPlato);
        menuPlato.setMenu(this);
    }

    public void removePlato(MenuPlato menuPlato) {
        if (this.platos != null) {
            this.platos.remove(menuPlato);
            menuPlato.setMenu(null);
        }
    }
	    
	public double calcularPrecio() {
	    Double precioTotal = 0.0; 	    	
        for (MenuPlato mp : this.platos) {	          
            Plato plato = mp.getPlato();
            if (plato.getPrecio_of() != 0) {
                precioTotal += plato.getPrecio_of();
            } else {
                precioTotal += plato.getPrecio();
            }
        }	        
	        return precioTotal;
	    }
		    
}
