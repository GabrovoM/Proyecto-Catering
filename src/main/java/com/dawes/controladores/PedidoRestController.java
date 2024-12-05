package com.dawes.controladores;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.serviciosImpl.ServicioPedidoImpl;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoRestController {
	@Autowired
    private ServicioPedidoImpl pedidoService;

    @GetMapping
    public Map<String, Integer> getPedidosCountByFechaEnvio(@RequestParam EstadoPedidoEnum estado) {
    	Map<String, Integer> pedidosCount = pedidoService.getPedidosCountByFechaEnvio(estado);
        return pedidoService.getPedidosCountByFechaEnvio(estado);
    }	    
	    
}
