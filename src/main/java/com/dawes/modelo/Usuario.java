package com.dawes.modelo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "usuarios")
public class Usuario {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String nombre;
	@Column(unique=true)
	private String username;
	@Column(unique=true)
	private String email;
	private String localidad;
	private String password;
	@Column(name = "reset_password_token")
    private String resetPasswordToken; 
	
	@OneToMany(mappedBy="usuario", fetch=FetchType.EAGER, cascade=CascadeType.ALL, orphanRemoval=true)
	private List<UsuarioRol> roles = new ArrayList<>();
	 
	@OneToMany(mappedBy="usuario")
	private List<Pedido> pedidos  = new ArrayList<>();
	
	@ManyToMany(cascade = {
		    CascadeType.PERSIST,
		    CascadeType.MERGE
		})
	@JoinTable(name = "favoritos",
		    joinColumns = @JoinColumn(name = "usuario_id"),
		    inverseJoinColumns = @JoinColumn(name="plato_id"))
	@JsonManagedReference		
	private Set<Plato> platos = new HashSet<>();	
	
	@Column(name = "Enabled", length = 1, nullable = false)
	private boolean enabled;
	
	@Column(name="account_No_Expired")
	private Boolean accountNoExpired;
	
	@Column(name="account_No_Locked")
	private boolean accountNoLocked;
	
	@Column(name="credentials_No_Expired")
	private boolean credentialsNoExpired;
	
	@Override
	public String toString() {
	    return "Usuario{id=" + id + ", nombre=" + nombre + "}"; 
	}	
	
	public boolean hasRole(String roleName) {
	    return roles.stream()
	                .anyMatch(usuarioRol -> usuarioRol.getRol().getRoleName().equalsIgnoreCase(roleName));
	}
	
	public void addPlatoFavorito(Plato p) {
	    if (!this.platos.contains(p)) {
	        this.platos.add(p);
	        p.getUsuarios().add(this);
	    }
	}
	
	public void removePlatoFavorito(Plato p) {
	    if (this.platos.contains(p)) {
	        p.getUsuarios().remove(this);
	        this.platos.remove(p);
	    }
	}
	 
}
