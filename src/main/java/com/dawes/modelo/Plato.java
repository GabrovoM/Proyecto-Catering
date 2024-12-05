package com.dawes.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="platos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Plato {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(unique=true, length=40)
	private String nombre;
	private double precio;
	private String imagen;
	@Column(length = 2000)
	private String descripcion;	
	private Integer orden;	
	private boolean noDisponible;
	private int oferta;
	private boolean de_oferta;
	private double precio_of;
	
	@ManyToOne(fetch = FetchType.EAGER)	
	@JoinColumn(name="id_categoria", nullable=true)
	@JsonIgnore	
	@ToString.Exclude	
	private Categoria categoria;
	
	@ManyToMany(mappedBy = "platos", fetch = FetchType.EAGER)
	@JsonManagedReference
	@ToString.Exclude		
	private Set<Ingrediente> ingredientes;
	
	@OneToMany(mappedBy="plato")	
	@ToString.Exclude	
	 @JsonIgnore
	private Set<MenuPlato> menus;
	
	@OneToMany(mappedBy="plato")
	@JsonManagedReference
	private List<PlatoMenuPedido> lineas_pedido = new ArrayList<>();	

	@ManyToMany(mappedBy="platos")
	@JsonBackReference
	private Set<Usuario> usuarios = new HashSet<>();	

	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (id == null ? 0 : id.hashCode());
        result = prime * result + (nombre == null ? 0 : nombre.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Plato other = (Plato) obj;
        return id != null && id.equals(other.id);
    }
    
    @Override
    public String toString() {
        return "Plato{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", precio=" + precio +
                ", imagen='" + imagen + '\'' +
                ", descripción='" + descripcion + '\'' +
                ", categoría='" + (categoria != null ? categoria.getId() + " " + categoria.getNombre() : "null") + '\'' +
                '}';
    }
    
    public void actualizarPrecio() {
		if (this.oferta>0) {
			this.precio_of=this.precio-(this.precio*this.oferta)/100;
		}
		else {
			this.precio_of = 0.0;
		}			
	}
}
