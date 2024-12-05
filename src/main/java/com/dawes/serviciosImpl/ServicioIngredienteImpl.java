package com.dawes.serviciosImpl;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.stereotype.Service;

import com.dawes.modelo.Ingrediente;
import com.dawes.repositorio.IngredienteRepository;
import com.dawes.repositorio.PlatoRepository;
import com.dawes.servicios.ServicioIngrediente;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

@Service
public class ServicioIngredienteImpl implements ServicioIngrediente {
	@Autowired
	private IngredienteRepository ir;	
	@Autowired
	private PlatoRepository pr;	 

	@Override
	public <S extends Ingrediente> S save(S entity) {
		return ir.save(entity);
	}

	@Override
	public <S extends Ingrediente> List<S> saveAll(Iterable<S> entities) {
		return ir.saveAll(entities);
	}

	@Override
	public <S extends Ingrediente> Optional<S> findOne(Example<S> example) {
		return ir.findOne(example);
	}

	@Override
	public List<Ingrediente> findAll(Sort sort) {
		return ir.findAll(sort);
	}

	@Override
	public void flush() {
		ir.flush();
	}

	@Override
	public Page<Ingrediente> findAll(Pageable pageable) {
		return ir.findAll(pageable);
	}

	@Override
	public <S extends Ingrediente> S saveAndFlush(S entity) {
		return ir.saveAndFlush(entity);
	}

	@Override
	public <S extends Ingrediente> List<S> saveAllAndFlush(Iterable<S> entities) {
		return ir.saveAllAndFlush(entities);
	}

	@Override
	public List<Ingrediente> findAll() {
		return ir.findAll();
	}

	@Override
	public List<Ingrediente> findAllById(Iterable<Integer> ids) {
		return ir.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<Ingrediente> entities) {
		ir.deleteInBatch(entities);
	}

	@Override
	public <S extends Ingrediente> Page<S> findAll(Example<S> example, Pageable pageable) {
		return ir.findAll(example, pageable);
	}

	@Override
	public Optional<Ingrediente> findById(Integer id) {
		return ir.findById(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<Ingrediente> entities) {
		ir.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return ir.existsById(id);
	}

	@Override
	public <S extends Ingrediente> long count(Example<S> example) {
		return ir.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		ir.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends Ingrediente> boolean exists(Example<S> example) {
		return ir.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		ir.deleteAllInBatch();
	}

	@Override
	public Ingrediente getOne(Integer id) {
		return ir.getOne(id);
	}

	@Override
	public <S extends Ingrediente, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return ir.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return ir.count();
	}

	@Override
	@Transactional
	public void deleteById(Integer id) {		
		 Ingrediente ingrediente = ir.findById(id)
	                .orElseThrow(() -> new EntityNotFoundException("Ingrediente no encontrado"));
		 if (ingrediente.getPlatos().isEmpty()) {
			 ir.deleteById(id);
		 } else {
            throw new IllegalStateException("Ingrediente tiene platos asociados y no se puede borrar.");
        }		 
//		ir.deleteById(id);
	}

	@Override
	public Ingrediente getById(Integer id) {
		return ir.getById(id);
	}

	@Override
	public void delete(Ingrediente entity) {
		ir.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		ir.deleteAllById(ids);
	}

	@Override
	public Ingrediente getReferenceById(Integer id) {
		return ir.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends Ingrediente> entities) {
		ir.deleteAll(entities);
	}

	@Override
	public <S extends Ingrediente> List<S> findAll(Example<S> example) {
		return ir.findAll(example);
	}

	@Override
	public <S extends Ingrediente> List<S> findAll(Example<S> example, Sort sort) {
		return ir.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		ir.deleteAll();
	}
	
	
}
