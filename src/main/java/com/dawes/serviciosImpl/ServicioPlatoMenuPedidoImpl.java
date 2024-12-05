package com.dawes.serviciosImpl;

import java.time.LocalDate;
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

import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.repositorio.PlatoMenuPedidoRepository;
import com.dawes.servicios.ServicioPlatoMenuPedido;

@Service
public class ServicioPlatoMenuPedidoImpl implements ServicioPlatoMenuPedido {

	@Autowired
	private PlatoMenuPedidoRepository pmpr;
	
	@Override
	public List<PlatoMenuPedido> findByFechaEnvioSemanalGreaterThanEqual(LocalDate date) {		
		return pmpr.findByFechaEnvioSemanalGreaterThanEqual(date);
	}

	public List<PlatoMenuPedido> getPedidosConFechaEnvioSemanalHoyYSieteDiasAntes() {
	    LocalDate fechaHoy = LocalDate.now();
	    LocalDate sevenDaysAgo = LocalDate.now().minusDays(7);
	    return pmpr.findByFechaEnvioSemanal(fechaHoy, sevenDaysAgo);
	}	
	
	public LocalDate getNextDispatchDate(Integer pedidoId) {
	    List<LocalDate> dispatchDates = pmpr.findDispatchDatesByPedidoId(pedidoId); 
	    if (dispatchDates.contains(null)) {	        
	        // envíos semanales pendientes
	        return null;
	    } else {    
	        // todos los envíos completados, devuelve la última fecha
	        return dispatchDates.stream().max(LocalDate::compareTo).orElse(null);
	    }
	}

	@Override
	public <S extends PlatoMenuPedido> S save(S entity) {
		return pmpr.save(entity);
	}

	@Override
	public <S extends PlatoMenuPedido> List<S> saveAll(Iterable<S> entities) {
		return pmpr.saveAll(entities);
	}

	@Override
	public <S extends PlatoMenuPedido> Optional<S> findOne(Example<S> example) {
		return pmpr.findOne(example);
	}

	@Override
	public List<PlatoMenuPedido> findAll(Sort sort) {
		return pmpr.findAll(sort);
	}

	@Override
	public void flush() {
		pmpr.flush();
	}

	@Override
	public Page<PlatoMenuPedido> findAll(Pageable pageable) {
		return pmpr.findAll(pageable);
	}

	@Override
	public <S extends PlatoMenuPedido> S saveAndFlush(S entity) {
		return pmpr.saveAndFlush(entity);
	}

	@Override
	public <S extends PlatoMenuPedido> List<S> saveAllAndFlush(Iterable<S> entities) {
		return pmpr.saveAllAndFlush(entities);
	}

	@Override
	public List<PlatoMenuPedido> findAll() {
		return pmpr.findAll();
	}

	@Override
	public List<PlatoMenuPedido> findAllById(Iterable<Integer> ids) {
		return pmpr.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<PlatoMenuPedido> entities) {
		pmpr.deleteInBatch(entities);
	}

	@Override
	public <S extends PlatoMenuPedido> Page<S> findAll(Example<S> example, Pageable pageable) {
		return pmpr.findAll(example, pageable);
	}

	@Override
	public Optional<PlatoMenuPedido> findById(Integer id) {
		return pmpr.findById(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<PlatoMenuPedido> entities) {
		pmpr.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return pmpr.existsById(id);
	}

	@Override
	public <S extends PlatoMenuPedido> long count(Example<S> example) {
		return pmpr.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		pmpr.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends PlatoMenuPedido> boolean exists(Example<S> example) {
		return pmpr.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		pmpr.deleteAllInBatch();
	}

	@Override
	public PlatoMenuPedido getOne(Integer id) {
		return pmpr.getOne(id);
	}

	@Override
	public <S extends PlatoMenuPedido, R> R findBy(Example<S> example,
			Function<FetchableFluentQuery<S>, R> queryFunction) {
		return pmpr.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return pmpr.count();
	}

	@Override
	public void deleteById(Integer id) {
		pmpr.deleteById(id);
	}

	@Override
	public PlatoMenuPedido getById(Integer id) {
		return pmpr.getById(id);
	}

	@Override
	public void delete(PlatoMenuPedido entity) {
		pmpr.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		pmpr.deleteAllById(ids);
	}

	@Override
	public PlatoMenuPedido getReferenceById(Integer id) {
		return pmpr.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends PlatoMenuPedido> entities) {
		pmpr.deleteAll(entities);
	}

	@Override
	public <S extends PlatoMenuPedido> List<S> findAll(Example<S> example) {
		return pmpr.findAll(example);
	}

	@Override
	public <S extends PlatoMenuPedido> List<S> findAll(Example<S> example, Sort sort) {
		return pmpr.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		pmpr.deleteAll();
	}

										
}
