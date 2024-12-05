package com.dawes;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.dawes.modelo.Ajuste;
import com.dawes.modelo.Rol;
import com.dawes.repositorio.AjusteRepository;
import com.dawes.repositorio.RolRepository;

@SpringBootApplication
public class ProbaCateringApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProbaCateringApplication.class, args);
	}
	
	@Bean
	public CommandLineRunner loadData(RolRepository rolRepository) {
	    return args -> {
	        createRoleIfNotFound("ROLE_USER", rolRepository);
	        createRoleIfNotFound("ROLE_ADMIN", rolRepository);
	        createRoleIfNotFound("ROLE_GESTOR", rolRepository);
	        createRoleIfNotFound("ROLE_EDITOR", rolRepository);
	        createRoleIfNotFound("ROLE_ANONYMOUS", rolRepository);
	    };
	}

	private void createRoleIfNotFound(String roleName, RolRepository rolRepository) {
	    if (rolRepository.findByRoleName(roleName) == null) {
	        Rol role = new Rol();
	        role.setRoleName(roleName);
	        rolRepository.save(role);
	        System.out.println(roleName + " creado");
	    }
	}
	
	@Bean
	public CommandLineRunner loadAjustes(AjusteRepository ajusteRepository) {
	    return args -> {
	        if (!ajusteRepository.existsByNumeracionPedido("#")) { 
	            Ajuste ajuste = new Ajuste();
	            ajuste.setPrecioEnvioZona1(0.00);
	            ajuste.setPrecioEnvioZona2(0.00);
	            ajuste.setNumeracionPedido("#");
	            ajusteRepository.save(ajuste);
	            System.out.println("Ajuste por defeco creado");
	        }
	    };
	}

}
