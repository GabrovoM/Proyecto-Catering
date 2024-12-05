package com.dawes.modelo;

import java.util.Objects;

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
@Table(name="menus_platos")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class MenuPlato {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;	
	@ManyToOne
	@JoinColumn(name="idmenu")
	private Menu menu;	
	@ManyToOne
	@JoinColumn(name="idplato")
	private Plato plato;	
	private Integer dia;
	private int orden;	
	
	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MenuPlato that = (MenuPlato) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "MenuPlato{" +
                "id=" + id +
                ", plato=" + plato +  
                ", menuId=" + (menu != null ? menu.getId() : null) +  
                '}';
    }

	
}
