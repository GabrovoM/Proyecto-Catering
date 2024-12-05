package com.dawes.repositorio;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.Usuario;

public interface PedidoRepository extends JpaRepository<Pedido, Integer> {
	Optional<List<Pedido>> findByUsuario(Usuario usuario);		
	@Query("SELECT p FROM Pedido p LEFT JOIN FETCH p.lineas_pedido WHERE p.id = :id")
	Optional<Pedido> findByIdWithItems(@Param("id") Integer id);	
	@Query("SELECT p FROM Pedido p WHERE p.usuario.id = :usuarioId AND p.pordefecto=true ORDER BY p.id DESC")
    List<Pedido> buscarUltimaDireccion(@Param("usuarioId") Integer usuarioId);	
	@Query("SELECT p FROM Pedido p WHERE p.usuario = :usuario AND p.pordefecto = true ORDER BY p.id DESC")
	List<Pedido> findDireccionMasReciente(@Param("usuario") Usuario usuario);	
	Optional<List<Pedido>> findByFechaCreacionBetween(LocalDate fechaini, LocalDate fechafin);	
	List<Pedido> findByEstadoEnum(EstadoPedidoEnum estadoEnum);		
	@Query("SELECT p FROM Pedido p " +
//	           "WHERE (p.estadoEnum IN ('PENDIENTE', 'PARCIAL') " +
			   "WHERE (p.estadoEnum = 'PARCIAL' " +
	           "AND (p.fechaPrimerEnvio = :nextDay OR " +
	           "EXISTS (SELECT pmp FROM PlatoMenuPedido pmp WHERE pmp.pedido.id = p.id AND pmp.fechaEnvioSemanal = :sixDaysAgo)))")
	List<Pedido> findPendingOrPartialWithWarnings(@Param("nextDay") LocalDate nextDay, 
	                                                  @Param("sixDaysAgo") LocalDate sixDaysAgo);
	 
	@Query("SELECT p, MAX(pmp.fechaEnvioSemanal) " +
	           "FROM Pedido p " +
	           "JOIN p.lineas_pedido pmp " +
	           "WHERE p.estadoEnum = :estado " +
	           "GROUP BY p " +
	           "HAVING MAX(pmp.fechaEnvioSemanal) = :fechaEnvioSemanal")
	 List<Object[]> findPedidosWithLastFechaEnvioSemanal(
	            @Param("estado") EstadoPedidoEnum estado, 
	            @Param("fechaEnvioSemanal") LocalDate fechaEnvioSemanal);
	 Page<Pedido> findByEstadoEnum(EstadoPedidoEnum estadoEnum, Pageable pageable); 	
	 Optional<List<Pedido>> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate fecha_ini, LocalDate fecha_fin);		    
	 Page<Pedido> findByFechaCreacionBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable); 
	 Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable);	    
	 Page<Pedido> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate startDate, LocalDate endDate, Pageable pageable);
		    
	 @Query("SELECT p FROM Pedido p " +
	    	       "WHERE (p.estadoEnum = 'PARCIAL' " +
	    	       "AND (p.fechaPrimerEnvio = :nextDay OR " +
	    	       "EXISTS (SELECT pmp FROM PlatoMenuPedido pmp " +
	    	       "WHERE pmp.pedido.id = p.id AND pmp.fechaEnvioSemanal = :fiveDaysAgo)))")
	 List<Pedido> findPartialWithFiveDaysWarning(@Param("nextDay") LocalDate nextDay, @Param("fiveDaysAgo") LocalDate fiveDaysAgo);
 
	    
}
