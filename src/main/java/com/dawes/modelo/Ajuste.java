package com.dawes.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name="ajustes")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Ajuste {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	@Column(columnDefinition = "double default 0.00")
	private double precioEnvioZona1;
	@Column(columnDefinition = "double default 0.00")
	private double precioEnvioZona2;
	private String direccionEmpresa;
	private String num;
	private String piso;
	private String cp;
	private String localidad;
	private String emailEmpresa;
	private String numeracionPedido;
	
}
