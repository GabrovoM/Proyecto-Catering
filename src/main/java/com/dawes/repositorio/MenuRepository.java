package com.dawes.repositorio;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Menu;

public interface MenuRepository extends JpaRepository<Menu, Integer> {
	@Query("SELECT COUNT(pmp) FROM PlatoMenuPedido pmp " +
		       "JOIN pmp.pedido pd " +
		       "WHERE pmp.menu.id = :menuId AND pd.estadoEnum IN :estados")
	long countPedidosByMenuIdAndEstados(@Param("menuId") Integer menuId, @Param("estados") List<EstadoPedidoEnum> estados);
	List<Menu> findByNombreContainingIgnoreCase(String nombre);
}
