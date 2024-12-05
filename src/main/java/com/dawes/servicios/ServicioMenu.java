package com.dawes.servicios;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Menu;

public interface ServicioMenu {	
	long countPedidosByMenuIdAndEstados(@Param("menuId") Integer menuId, @Param("estados") List<EstadoPedidoEnum> estados);
	List<Menu> findByNombreContainingIgnoreCase(String nombre);

	<S extends Menu> S save(S entity);
	<S extends Menu> List<S> saveAll(Iterable<S> entities);
	<S extends Menu> Optional<S> findOne(Example<S> example);
	List<Menu> findAll(Sort sort);
	void flush();
	Page<Menu> findAll(Pageable pageable);
	<S extends Menu> S saveAndFlush(S entity);
	<S extends Menu> List<S> saveAllAndFlush(Iterable<S> entities);
	List<Menu> findAll();
	List<Menu> findAllById(Iterable<Integer> ids);
	void deleteInBatch(Iterable<Menu> entities);
	<S extends Menu> Page<S> findAll(Example<S> example, Pageable pageable);
	Optional<Menu> findById(Integer id);
	void deleteAllInBatch(Iterable<Menu> entities);
	boolean existsById(Integer id);
	<S extends Menu> long count(Example<S> example);
	void deleteAllByIdInBatch(Iterable<Integer> ids);
	<S extends Menu> boolean exists(Example<S> example);
	void deleteAllInBatch();
	Menu getOne(Integer id);
	<S extends Menu, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction);
	long count();
	void deleteById(Integer id);
	Menu getById(Integer id);
	void delete(Menu entity);
	void deleteAllById(Iterable<? extends Integer> ids);
	Menu getReferenceById(Integer id);
	void deleteAll(Iterable<? extends Menu> entities);
	<S extends Menu> List<S> findAll(Example<S> example);
	<S extends Menu> List<S> findAll(Example<S> example, Sort sort);
	void deleteAll();	
	
}