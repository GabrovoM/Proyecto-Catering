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

import com.dawes.modelo.Categoria;
import com.dawes.repositorio.CategoriaRepository;
import com.dawes.servicios.ServicioCategoria;

@Service
public class ServicioCategoriaImpl implements ServicioCategoria {

	@Autowired
	private CategoriaRepository cr;

	@Override
	public Optional<Categoria> findById(int id) {
		return cr.findById(id);
	}

	@Override
	public Optional<Categoria> findByNombre(String nombre) {
		return cr.findByNombre(nombre);
	}

	@Override
	public <S extends Categoria> S save(S entity) {
		return cr.save(entity);
	}

	@Override
	public <S extends Categoria> List<S> saveAll(Iterable<S> entities) {
		return cr.saveAll(entities);
	}

	@Override
	public <S extends Categoria> Optional<S> findOne(Example<S> example) {
		return cr.findOne(example);
	}

	@Override
	public List<Categoria> findAll(Sort sort) {
		return cr.findAll(sort);
	}

	@Override
	public void flush() {
		cr.flush();
	}

	@Override
	public Page<Categoria> findAll(Pageable pageable) {
		return cr.findAll(pageable);
	}

	@Override
	public <S extends Categoria> S saveAndFlush(S entity) {
		return cr.saveAndFlush(entity);
	}

	@Override
	public <S extends Categoria> List<S> saveAllAndFlush(Iterable<S> entities) {
		return cr.saveAllAndFlush(entities);
	}

	@Override
	public List<Categoria> findAll() {
		return cr.findAll();
	}

	@Override
	public List<Categoria> findAllById(Iterable<Integer> ids) {
		return cr.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<Categoria> entities) {
		cr.deleteInBatch(entities);
	}

	@Override
	public <S extends Categoria> Page<S> findAll(Example<S> example, Pageable pageable) {
		return cr.findAll(example, pageable);
	}

	@Override
	public Optional<Categoria> findById(Integer id) {
		return cr.findById(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<Categoria> entities) {
		cr.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return cr.existsById(id);
	}

	@Override
	public <S extends Categoria> long count(Example<S> example) {
		return cr.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		cr.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends Categoria> boolean exists(Example<S> example) {
		return cr.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		cr.deleteAllInBatch();
	}

	@Override
	public Categoria getOne(Integer id) {
		return cr.getOne(id);
	}

	@Override
	public <S extends Categoria, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return cr.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return cr.count();
	}

	@Override
	public void deleteById(Integer id) {
		cr.deleteById(id);
	}

	@Override
	public Categoria getById(Integer id) {
		return cr.getById(id);
	}

	@Override
	public void delete(Categoria entity) {
		cr.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		cr.deleteAllById(ids);
	}

	@Override
	public Categoria getReferenceById(Integer id) {
		return cr.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends Categoria> entities) {
		cr.deleteAll(entities);
	}

	@Override
	public <S extends Categoria> List<S> findAll(Example<S> example) {
		return cr.findAll(example);
	}

	@Override
	public <S extends Categoria> List<S> findAll(Example<S> example, Sort sort) {
		return cr.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		cr.deleteAll();
	}
	
	
}
