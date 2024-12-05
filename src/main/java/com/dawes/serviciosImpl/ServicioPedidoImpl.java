package com.dawes.serviciosImpl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.AjusteRepository;
import com.dawes.repositorio.PedidoRepository;
import com.dawes.servicios.ServicioPedido;

@Service
public class ServicioPedidoImpl implements ServicioPedido {
	@Autowired
	private PedidoRepository pedr;
	@Autowired
	private AjusteRepository ar;
	
	public String generarNumeroOrden() {
		int numero=0;
		String numeroConcatenado="";		
		List<Pedido> pedidos = findAll();		
		List<Integer> numeros= new ArrayList<Integer>();		
		pedidos.stream().forEach(o -> numeros.add(Integer.parseInt(o.getNumero().replace("#", ""))));		
		if (pedidos.isEmpty()) {
			numero=1;
		}else {			
			numero= numeros.stream().max(Integer::compare).get();
			numero++;
		}		
		String inicio = ar.findById(1).get().getNumeracionPedido();
		if (numero<10) { 
			numeroConcatenado=inicio +"00000000"+String.valueOf(numero);
		}else if(numero<100) {
			numeroConcatenado=inicio +"0000000"+String.valueOf(numero);
		}else if(numero<1000) {
			numeroConcatenado=inicio +"000000"+String.valueOf(numero);
		}else if(numero<10000) {
			numeroConcatenado=inicio +"00000"+String.valueOf(numero);
		}			
		return numeroConcatenado;
	}

	@Override
	public Optional<List<Pedido>> findByUsuario(Usuario usuario) {		
		return pedr.findByUsuario(usuario);
	}

	@Override
	public List<Pedido> buscarUltimaDireccion(@Param("usuarioId") Integer usuarioId) {		
		return pedr.buscarUltimaDireccion(usuarioId);
	}

	@Override
	public Optional<List<Pedido>> findByFechaCreacionBetween(LocalDate fechaini, LocalDate fechafin) {		
		return pedr.findByFechaCreacionBetween(fechaini, fechafin);
	}

	@Override
	public List<Pedido> findByEstadoEnum(EstadoPedidoEnum estadoEnum) {		
		return pedr.findByEstadoEnum(estadoEnum);		
	}	
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");	
	public Map<String, Integer> getPedidosCountByFechaEnvio(EstadoPedidoEnum estado) {
	    List<Pedido> pedidos = pedr.findByEstadoEnum(estado);
	    Map<String, Integer> countByFechaEnvio = new HashMap<>();
	    for (Pedido pedido : pedidos) {
	        LocalDate fechaEnvio = pedido.getFechaEnvio(); 
	        String fechaEnvioStr = fechaEnvio.format(DateTimeFormatter.ISO_LOCAL_DATE); 
	        countByFechaEnvio.put(fechaEnvioStr, countByFechaEnvio.getOrDefault(fechaEnvioStr, 0) + 1);
	    }	   
	    return countByFechaEnvio;
	}	
	
	public List<Pedido> getPendingOrPartialWithWarnings() {
        LocalDate today = LocalDate.now();
        LocalDate nextDay = today.plusDays(1); 
        LocalDate sixDaysAgo = today.minusDays(6); 
        return pedr.findPendingOrPartialWithWarnings(nextDay, sixDaysAgo);
    }	

	@Override
	public List<Pedido> findDireccionMasReciente(Usuario usuario) {		
		return pedr.findDireccionMasReciente(usuario);
	}	
	
	public List<Pedido> getPedidosWithLastFechaEnvioSemanal() {
	    LocalDate sixDaysAgo = LocalDate.now().minusDays(6);
	    LocalDate dispatchDate = LocalDate.now().plusDays(1); 	  
	    List<Object[]> results = pedr.findPedidosWithLastFechaEnvioSemanal(
	            EstadoPedidoEnum.PARCIAL, sixDaysAgo);  	 
	    List<Pedido> pedidos = new ArrayList<>();
	    for (Object[] result : results) {
	        Pedido pedido = (Pedido) result[0]; 
	        LocalDate maxFechaEnvioSemanal = (LocalDate) result[1];  	       
	        if (maxFechaEnvioSemanal != null && maxFechaEnvioSemanal.equals(sixDaysAgo)) {	           
	            pedido.setFechaEnvio(dispatchDate); 
	        } else {	          
	            pedido.setFechaEnvio(pedido.getFechaPrimerEnvio()); 
	        }	       
	        pedidos.add(pedido);
	    }	    
	    return pedidos; 
	}
	
	public Page<Pedido> findByEstadoEnum(EstadoPedidoEnum estadoEnum, Pageable pageable) {
        return pedr.findByEstadoEnum(estadoEnum, pageable);
    }

	@Override
	public Optional<List<Pedido>> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate fecha_ini,
			LocalDate fecha_fin) {	
		return pedr.findByEstadoEnumAndFechaCreacionBetween(estado, fecha_ini, fecha_fin);
	}
	
	@Override
	public Page<Pedido> findByFechaCreacionBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {		
		return pedr.findByFechaCreacionBetween(fechaInicio, fechaFin, pageable);
	}

	@Override
	public Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable) {	
		return pedr.findByUsuario(usuario, pageable);
	}

	@Override
	public Optional<List<Pedido>> findByEstadoAndFechaCreacionBetween(LocalDate fechaInicio, LocalDate fechaFin) {		
		return pedr.findByFechaCreacionBetween(fechaInicio, fechaFin);
	}

	@Override
	public Page<Pedido> findByEstadoEnumAndFechaCreacionBetween(EstadoPedidoEnum estado, LocalDate startDate,
			LocalDate endDate, Pageable pageable) {	
		return pedr.findByEstadoEnumAndFechaCreacionBetween(estado, startDate, endDate, pageable);
	}

	@Override
	public List<Pedido> findPartialWithFiveDaysWarning(LocalDate nextDay, LocalDate fiveDaysAgo) {		
		return pedr.findPartialWithFiveDaysWarning(nextDay, fiveDaysAgo);
	}
		
	@Override
	public <S extends Pedido> S save(S entity) {
		return pedr.save(entity);
	}

	@Override
	public <S extends Pedido> List<S> saveAll(Iterable<S> entities) {
		return pedr.saveAll(entities);
	}

	@Override
	public <S extends Pedido> Optional<S> findOne(Example<S> example) {
		return pedr.findOne(example);
	}

	@Override
	public List<Pedido> findAll(Sort sort) {
		return pedr.findAll(sort);
	}

	@Override
	public void flush() {
		pedr.flush();
	}

	@Override
	public Page<Pedido> findAll(Pageable pageable) {
		return pedr.findAll(pageable);
	}

	@Override
	public <S extends Pedido> S saveAndFlush(S entity) {
		return pedr.saveAndFlush(entity);
	}

	@Override
	public <S extends Pedido> List<S> saveAllAndFlush(Iterable<S> entities) {
		return pedr.saveAllAndFlush(entities);
	}

	@Override
	public List<Pedido> findAll() {
		return pedr.findAll();
	}

	@Override
	public List<Pedido> findAllById(Iterable<Integer> ids) {
		return pedr.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<Pedido> entities) {
		pedr.deleteInBatch(entities);
	}

	@Override
	public <S extends Pedido> Page<S> findAll(Example<S> example, Pageable pageable) {
		return pedr.findAll(example, pageable);
	}

	@Override
	public Optional<Pedido> findById(Integer id) {
		 return pedr.findByIdWithItems(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<Pedido> entities) {
		pedr.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return pedr.existsById(id);
	}

	@Override
	public <S extends Pedido> long count(Example<S> example) {
		return pedr.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		pedr.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends Pedido> boolean exists(Example<S> example) {
		return pedr.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		pedr.deleteAllInBatch();
	}

	@Override
	public Pedido getOne(Integer id) {
		return pedr.getOne(id);
	}

	@Override
	public <S extends Pedido, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return pedr.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return pedr.count();
	}

	@Override
	public void deleteById(Integer id) {
		pedr.deleteById(id);
	}

	@Override
	public Pedido getById(Integer id) {
		return pedr.getById(id);
	}

	@Override
	public void delete(Pedido entity) {
		pedr.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		pedr.deleteAllById(ids);
	}

	@Override
	public Pedido getReferenceById(Integer id) {
		return pedr.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends Pedido> entities) {
		pedr.deleteAll(entities);
	}

	@Override
	public <S extends Pedido> List<S> findAll(Example<S> example) {
		return pedr.findAll(example);
	}

	@Override
	public <S extends Pedido> List<S> findAll(Example<S> example, Sort sort) {
		return pedr.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		pedr.deleteAll();
	}	
	
}
