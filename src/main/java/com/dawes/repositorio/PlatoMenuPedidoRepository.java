package com.dawes.repositorio;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;

public interface PlatoMenuPedidoRepository extends JpaRepository<PlatoMenuPedido, Integer> {	
	List<PlatoMenuPedido> findByFechaEnvioSemanalGreaterThanEqual(LocalDate date);	
	@Query(value = "SELECT * FROM PlatoMenuPedido pmp WHERE pmp.idpedido = :idpedido AND pmp.fechaEnvioSemanal = DATE_SUB(CURDATE(), INTERVAL 7 DAY)", nativeQuery = true)
	List<PlatoMenuPedido> findByPedidoAndDate(@Param("idpedido") Integer idpedido);
	@Query("SELECT pmp FROM PlatoMenuPedido pmp " +
		       "JOIN pmp.pedido p " +
		       "WHERE (p.fechaPrimerEnvio = :fechaHoy OR pmp.fechaEnvioSemanal = :sevenDaysAgo) " +
		       "AND p.estadoEnum = 'PARCIAL'")
	List<PlatoMenuPedido> findByFechaEnvioSemanal(@Param("fechaHoy") LocalDate fechaHoy, @Param("sevenDaysAgo") LocalDate sevenDaysAgo);
	@Query("SELECT pmp.fechaEnvioSemanal FROM PlatoMenuPedido pmp WHERE pmp.pedido.id = :pedidoId ORDER BY pmp.fechaEnvioSemanal ASC")
	List<LocalDate> findDispatchDatesByPedidoId(Integer pedidoId);	
}
