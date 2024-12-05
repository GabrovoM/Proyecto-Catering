package com.dawes.controladores;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.modelo.Ingrediente;
import com.dawes.modelo.Plato;
import com.dawes.serviciosImpl.ServicioIngredienteImpl;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/admin/ingredientes")
public class IngredienteController {
	@Autowired
	private ServicioIngredienteImpl sii;
	
	@ModelAttribute
	public void addTituloAttribute(Model model) {
        model.addAttribute("titulo", "Ingredientes");
    }
	
	@GetMapping("")
	public String home(Model model, Pageable pageable) {  
	    Page<Ingrediente> ingrLista = sii.findAll(pageable); 
	    model.addAttribute("ingredientes", ingrLista.getContent());
	    model.addAttribute("ingrLista", ingrLista);
	    model.addAttribute("currentPage", "ingredientes");	  
	    return "admin/ingredientes/listado-ingredientes";
	}	

	@GetMapping("/nuevoingr")
	public String addIngrediente(Model model) {
		model.addAttribute("nuevoIngr", new Ingrediente());		
		return "admin/ingredientes/form_add_ingrediente";
	}

	@PostMapping("/saveingrediente")
	public String saveIngrediente(@ModelAttribute("nuevoIngr") Ingrediente ingrediente, 
			RedirectAttributes redirectAttributes, BindingResult bindingResult) {		
		try {
			sii.save(ingrediente);
	    } catch (DataIntegrityViolationException e) {		        
	       System.err.println("Clave unica duplicada " + e.getLocalizedMessage());		        
	       redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre ingrediente duplicada. No se puede guardar el registro.");
	    }
		return "redirect:/admin/ingredientes";
	}	
	
	@GetMapping("/editIngr/{id}")
	public String editarIngrediente(@PathVariable("id") Integer id, Model model) {
		Ingrediente editIngr = sii.findById(id).orElse(null);
		if (editIngr != null) {
			model.addAttribute("editIngr", editIngr);
			return "admin/ingredientes/form_edit_ingrediente";
		} else {
			return "redirect:/admin/ingredientes";
		}	
	}
	
	@PostMapping("/saveEditedIngrediente")
	public String saveEditedIngrediente(@ModelAttribute Ingrediente editIngr,
										RedirectAttributes redirectAttributes) {
		Ingrediente editadoIngr = sii.findById(editIngr.getId()).get();
		editadoIngr.setNombre(editIngr.getNombre());		
		try {
			sii.save(editadoIngr);
        } catch (DataIntegrityViolationException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre ingrediente duplicada. No se puede guardar el registro.");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar ingrediente.");
	    }	
		return "redirect:/admin/ingredientes";
	}
	
	@GetMapping({"/delete/{id}"})
	public String borrarIngrediente(@PathVariable int id, RedirectAttributes redirectAttributes) {			
		try {
			sii.deleteById(id);
		} catch (IllegalStateException e) {
			System.err.println("tiene platos " + e.getLocalizedMessage());		        
		    redirectAttributes.addFlashAttribute("errorMessage", "Este ingrediente tiene platos asociados.");
		}
		return "redirect:/admin/ingredientes";
	}
	
	@PostMapping("/search")
	public String searchIngrediente(@RequestParam String nombre, Model model, Pageable pageable) { 
		System.err.println("sii.findAll() result: " + sii.findAll());
	    List<Ingrediente> ingredientes = sii.findAll().stream()
	        .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
	        .collect(Collectors.toList());	  
	    Page<Ingrediente> ingrLista = new PageImpl<>(ingredientes, pageable, ingredientes.size());
	    model.addAttribute("ingrLista", ingrLista);  
	    model.addAttribute("ingredientes", ingrLista.getContent());  
	    model.addAttribute("currentPage", "ingredientes");	  
	    return "admin/ingredientes/listado-ingredientes"; 
	}
		
}
