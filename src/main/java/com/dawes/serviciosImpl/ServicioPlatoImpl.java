package com.dawes.serviciosImpl;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Ingrediente;
import com.dawes.modelo.Plato;
import com.dawes.repositorio.IngredienteRepository;
import com.dawes.repositorio.PlatoRepository;
import com.dawes.servicios.ServicioPlato;


@Service
public class ServicioPlatoImpl implements ServicioPlato {

	@Autowired
	private PlatoRepository pr;
	@Autowired
	private IngredienteRepository ir;
	
	public List<Plato> findAllByCategoria(int categoriaId) {
		return pr.findByCategoriaId(categoriaId);
	}

	@Override
	public List<Plato> findByCategoria(Categoria categoria) {
		return pr.findByCategoria(categoria);
	}

	@Override
	public Page<Plato> findByCategoriaId(int idCategoria, Pageable pageable) {
		 return pr.findByCategoriaId(idCategoria, pageable);
	}

	@Override
	public int findNumPlatosByCategoria(Categoria categoria) {		
		return pr.findNumPlatosByCategoria(categoria);
	}

	@Override
	public Page<Plato> findAllByNombreContainingIgnoreCase(String nombre, Pageable pageable) {
		return pr.findAllByNombreContainingIgnoreCase(nombre, pageable);
	}	
	
	public List<Plato> findByIds(Collection<Integer> ids) {
		return pr.findAllById(ids);
	}

	@Override
	public List<Plato> findByOrden(Integer orden) {		
		return pr.findByOrden(orden);
	}

	@Override
	public long countPedidosByPlatoIdAndEstado(Integer platoId, EstadoPedidoEnum estado) {		
		return pr.countPedidosByPlatoIdAndEstado(platoId, estado);
	}

	@Override
	public long countMenusByPlatoId(int platoId) {		
		return pr.countMenusByPlatoId(platoId);
	}	

	@Override
	public Optional<Plato> findById(int id) {
		return pr.findById(id);
	}

	@Override
	public Optional<Plato> findByNombre(String nombre) {
		return pr.findByNombre(nombre);
	}

	@Override
	public List<Plato> findByNombreContainsIgnoreCase(String cadena) {
		return pr.findByNombreContainsIgnoreCase(cadena);
	}

	@Override
	public <S extends Plato> S save(S entity) {
		return pr.save(entity);
	}

	@Override
	public <S extends Plato> List<S> saveAll(Iterable<S> entities) {
		return pr.saveAll(entities);
	}

	@Override
	public <S extends Plato> Optional<S> findOne(Example<S> example) {
		return pr.findOne(example);
	}

	@Override
	public List<Plato> findAll(Sort sort) {
		return pr.findAll(sort);
	}

	@Override
	public void flush() {
		pr.flush();
	}

	@Override
	public Page<Plato> findAll(Pageable pageable) {
	Page<Plato> platos = pr.findAll(pageable);       
        if (platos == null) {
            platos = Page.empty(pageable);
        }        
        return platos;		
//		return pr.findAll(pageable);
	}

	@Override
	public <S extends Plato> S saveAndFlush(S entity) {
		return pr.saveAndFlush(entity);
	}

	@Override
	public <S extends Plato> List<S> saveAllAndFlush(Iterable<S> entities) {
		return pr.saveAllAndFlush(entities);
	}

	@Override
	public List<Plato> findAll() {
		return pr.findAll();
	}

	@Override
	public List<Plato> findAllById(Iterable<Integer> ids) {
		return pr.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<Plato> entities) {
		pr.deleteInBatch(entities);
	}

	@Override
	public <S extends Plato> Page<S> findAll(Example<S> example, Pageable pageable) {
		return pr.findAll(example, pageable);
	}

	@Override
	public Optional<Plato> findById(Integer id) {
		return pr.findById(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<Plato> entities) {
		pr.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return pr.existsById(id);
	}

	@Override
	public <S extends Plato> long count(Example<S> example) {
		return pr.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		pr.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends Plato> boolean exists(Example<S> example) {
		return pr.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		pr.deleteAllInBatch();
	}

	@Override
	public Plato getOne(Integer id) {
		return pr.getOne(id);
	}

	@Override
	public <S extends Plato, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return pr.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return pr.count();
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {		
		Plato plato = findById(id).get();
		if (plato != null) {
			Set<Ingrediente> ingredientes = new HashSet<>(plato.getIngredientes());
			 // eliminar la associación con el plato
	        for (Ingrediente ingrediente : ingredientes) {
	            ingrediente.removePlato(plato);
	            // guardar el ingrediente actualizado
	            ir.save(ingrediente); 
	        }	     
	        pr.deleteById(id);
		}		
	}

	@Override
	public Plato getById(Integer id) {
		return pr.getById(id);
	}

	@Override
	public void delete(Plato entity) {
		pr.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		pr.deleteAllById(ids);
	}

	@Override
	public Plato getReferenceById(Integer id) {
		return pr.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends Plato> entities) {
		pr.deleteAll(entities);
	}

	@Override
	public <S extends Plato> List<S> findAll(Example<S> example) {
		return pr.findAll(example);
	}

	@Override
	public <S extends Plato> List<S> findAll(Example<S> example, Sort sort) {
		return pr.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		pr.deleteAll();
	}
	
	
}
