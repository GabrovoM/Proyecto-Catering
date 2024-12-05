package com.dawes.modelo;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="pedidos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Pedido {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String numero;
	private LocalDate fechaCreacion;
	private double total;	
	private String direccion;
	private String num;
	private String piso;
	private String cp;
	private String localidad;
	private boolean pordefecto;
	private String numeroFactura;	
	@Enumerated(EnumType.STRING)
	private EstadoPedidoEnum estadoEnum;	
	private LocalDate fechaPrimerEnvio;
	private LocalDate fechaEnvio;
	@Column(name = "fecha_entrega")
	private LocalDate fechaEntrega;		
	private String currency  = "EUR";
	private String method;
	
	@ManyToOne
	private Usuario usuario;
	
	@OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@JsonManagedReference
	private List<PlatoMenuPedido> lineas_pedido = new ArrayList<>();	
	
	public Pedido(double total) {
		this.total=total;
	}
	
	@Override
	public String toString() {
	    return "Pedido{" +
	            "id=" + id +
	            ", numero='" + numero + '\'' +
	            ", fechaCreacion=" + fechaCreacion +
	            ", total=" + total +
	            ", currency='" + currency + '\'' +
	            ", method='" + method + '\'' +
	            ", usuario=" + (usuario != null ? usuario.getId() : "null") +
	            '}';
	}
	 
	 public LocalDate getCalculatedFechaEntrega() {	        
		    if (fechaEntrega != null) {		    	
		        return fechaEntrega;
		    }	       
		    if (fechaCreacion == null) {		    	
		        return null; 
		    }
		    LocalDate calculatedEntrega = fechaCreacion.plusDays(2); 
		    if (fechaCreacion.getDayOfWeek() == DayOfWeek.FRIDAY) {		    	
		        calculatedEntrega = fechaCreacion.plusDays(3);
		    }
		    return calculatedEntrega;
		}	 
	 
	 @Override
	 public boolean equals(Object o) {
	     if (this == o) return true;
	     if (!(o instanceof Pedido)) return false;
	     Pedido pedido = (Pedido) o;
	     return id != null && id.equals(pedido.id);
	 }

	 @Override
	 public int hashCode() {
	     return getClass().hashCode() + (id == null ? 0 : id.hashCode());
	 }
	
}
