package com.dawes.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Plato;

public interface PlatoRepository extends JpaRepository<Plato, Integer> {
	Optional<Plato> findById(int id);
	Optional<Plato> findByNombre(String nombre);
//	Optional<Plato> findByCategoria(Categoria categoria);	
	List<Plato> findByNombreContainsIgnoreCase(String cadena);	
	@Query("select p from Plato p where p.categoria.id = ?1")
	public List<Plato> findByCategoriaId(int categoriaId);	
	public List<Plato> findByCategoria(Categoria categoria);	
	List<Plato> findByOrden(Integer orden);	
	Page<Plato> findByCategoriaId(int idCategoria, Pageable pageable);	
	@Query("select p.id from Plato p")
	public List<Integer> obtenerIds();	
	@Query("select count(p) from Plato p where p.categoria = ?1")
	int findNumPlatosByCategoria(Categoria categoria);		
	Page<Plato> findAllByNombreContainingIgnoreCase(String nombre, Pageable pageable);	
	@Query("SELECT COUNT(pmp) FROM PlatoMenuPedido pmp " +
	           "JOIN pmp.pedido pd " +
	           "WHERE pmp.plato.id = :platoId AND pd.estadoEnum = :estado")
	long countPedidosByPlatoIdAndEstado(@Param("platoId") Integer platoId, @Param("estado") EstadoPedidoEnum estado);
    @Query("SELECT COUNT(m) FROM MenuPlato m WHERE m.plato.id = :platoId")
    long countMenusByPlatoId(@Param("platoId") int platoId);
}
