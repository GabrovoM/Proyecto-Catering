package com.dawes.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dawes.modelo.Plato;
import com.dawes.serviciosImpl.ServicioPlatoImpl;

@RestController
@RequestMapping("/api/platos")
public class PlatoRestController {
	
	@Autowired
	private ServicioPlatoImpl spi;
	
	@GetMapping("/{id}")
    public ResponseEntity<Plato> getPlatoDetails(@PathVariable("id") Integer id) {
        Plato p = spi.findById(id).orElse(null);
        if (p != null) {
            return ResponseEntity.ok(p); 
        }
        return ResponseEntity.notFound().build();
    }

}
