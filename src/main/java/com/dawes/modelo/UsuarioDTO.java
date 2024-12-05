package com.dawes.modelo;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
public class UsuarioDTO {
	 private String nombre;
	    private String username;
	    private String dni;
	    private String email;
	    private String direccion;
	    private String password;
	    private List<Integer> selectedRoleIds; 

}
