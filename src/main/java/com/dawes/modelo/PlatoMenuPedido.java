package com.dawes.modelo;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="platos_menus_pedidos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class PlatoMenuPedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private Integer cantidad;
	private double totalLinea;
	private double precio_plato;
	private double precio_menu;
	private LocalDate fechaEnvioSemanal;	
	private boolean estadoSemanal;
	
	@ManyToOne
	@JoinColumn(name="idplato")
	@JsonBackReference
	private Plato plato;
	
	@ManyToOne
	@JoinColumn(name="idmenu")
	@JsonBackReference
	private Menu menu;
	
	@ManyToOne
	@JoinColumn(name="idpedido")
	@JsonBackReference
	private Pedido pedido;
	
	@Override
	public String toString() {
	    String platoId = (plato != null) ? plato.getId().toString() : "0"; 
	    String menuId = (menu != null) ? menu.getId().toString() : "0"; 

	    return "PlatoMenuPedido{" +
	            "platoId=" + platoId +
	            ", menuId=" + menuId +
	            ", cantidad=" + cantidad +
	            ", totalLinea=" + totalLinea +
	            '}';
	}
}
