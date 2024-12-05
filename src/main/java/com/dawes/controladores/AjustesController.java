package com.dawes.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dawes.repositorio.AjusteRepository;

@Controller
@RequestMapping("/admin/ajustes")
public class AjustesController {
	@Autowired
	private AjusteRepository ar;
	
	@GetMapping("")
	public String ajustes(Model model) {
		model.addAttribute("ajustes", ar.findAll());
		return "admin/ajustes";
	}
	
	@PostMapping("/update-ajustes")
	public String updateAjustes(@RequestParam Integer id,  
	                            @RequestParam String direccionEmpresa,
	                            @RequestParam String num,
	                            @RequestParam(required = false) String piso,
	                            @RequestParam String cp,
	                            @RequestParam String localidad,
	                            @RequestParam String emailEmpresa,
	                            @RequestParam String precioEnvioZona1,
	                            @RequestParam String precioEnvioZona2,
	                            @RequestParam String numeracionPedido) {	    
	    
	    ar.updateAjustes(id, direccionEmpresa, num, piso, cp, localidad, emailEmpresa, 
	    				 precioEnvioZona1, precioEnvioZona2, numeracionPedido);	    
	    
	    return "redirect:/admin/ajustes";
	}	

}
