package com.dawes.repositorio;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.dawes.modelo.Ajuste;

public interface AjusteRepository extends JpaRepository<Ajuste, Integer> {
	
	 	@Modifying
	    @Transactional
	    @Query("UPDATE Ajuste a SET a.direccionEmpresa = :direccionEmpresa, " +
	    	   "a.num = :num, " +
	    	   "a.piso = :piso, " +
	    	   "a.cp = :cp, " +
	    	   "a.localidad = :localidad, "+
	    	   "a.emailEmpresa = :emailEmpresa, " +
	           "a.precioEnvioZona1 = :precioEnvioZona1, " +
	           "a.precioEnvioZona2 = :precioEnvioZona2, " +
	           "a.numeracionPedido = :numeracionPedido " +
	           "WHERE a.id = :id")
	    int updateAjustes(Integer id, String direccionEmpresa, String num, String piso, 
	    				  String cp, String localidad, String emailEmpresa, String precioEnvioZona1, 
	                      String precioEnvioZona2, String numeracionPedido);	 	
	 	
	 	boolean existsByNumeracionPedido(String numeracionPedido);
	
}
