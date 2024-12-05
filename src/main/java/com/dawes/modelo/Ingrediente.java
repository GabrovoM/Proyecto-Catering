package com.dawes.modelo;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name="ingredientes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ingrediente {	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(unique=true)
	private String nombre;
	@ManyToMany(fetch =FetchType.EAGER)
    @JoinTable(
        name = "platos_ingredientes", 
        joinColumns = { @JoinColumn(name = "id_ingrediente") }, 
        inverseJoinColumns = { @JoinColumn(name = "id_plato") }
    )
	@JsonBackReference
	@ToString.Exclude	
	private Set<Plato> platos;
	
	@Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + id;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Ingrediente other = (Ingrediente) obj;
        return id == other.id;
    }
    
    @Override
    public String toString() {
        return "Ingrediente{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                '}';
    } 
   
    public void addPlato(Plato plato) {
    	platos.add(plato);
        plato.getIngredientes().add(this);
    }
     
    public void removePlato(Plato plato) {
    	platos.remove(plato);
        plato.getIngredientes().remove(this);
    }

}

