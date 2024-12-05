package com.dawes.controladores;

import java.util.List;
import java.util.Optional;
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

import com.dawes.modelo.Categoria;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;

@Controller
@RequestMapping("/admin/categoria")
public class CategoriaController {
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioCategoriaImpl sci;
	
	@ModelAttribute
	public void addTituloAttribute(Model model) {
	    model.addAttribute("titulo", "Categorías");
	}
	
	@GetMapping("")
	public String listCategorias(Model model, Pageable pageable) {
		Page<Categoria> categoriasPage = sci.findAll(pageable);	
	    System.err.println("Total Pages: " + categoriasPage.getTotalPages());
	    model.addAttribute("categoriasPage", categoriasPage);
		model.addAttribute("currentPage", "categorias");
		return "admin/categorias/listado-categorias";
	}
	
	@GetMapping("/nuevacategoria")
	public String addCategoria(Model model) {
		model.addAttribute("nuevaCat", new Categoria());
		return "admin/categorias/form_add_categoria";
	}
	
	@PostMapping("/addCategoriaSubmit")
	public String addCatSubmit(@ModelAttribute("nuevaCat") Categoria c, 
			RedirectAttributes redirectAttributes, BindingResult bindingResult) {		
		try {
		   sci.save(c);
	    } catch (DataIntegrityViolationException e) {		        
	       System.err.println("Clave unica duplicada " + e.getLocalizedMessage());		        
	       redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre categoría duplicada. No se puede guardar el registro.");
	    }
		return "redirect:/admin/categoria";
	}
	
	@GetMapping("/editCategoria/{id}")
	public String editarCategoria(@PathVariable("id") Integer id, Model model) {
		Categoria categoria = sci.findById(id).orElse(null);
		if (categoria != null) {
			model.addAttribute("editCat", categoria);
			return "admin/categorias/form_edit_categoria";
		} else {			
			return "redirect:/admin/categoria";
		}
	}
	
	@PostMapping("/editCategoriaSubmit")
	public String editarCategoriaSubmit(@ModelAttribute("editCat") Categoria categoria,
			RedirectAttributes redirectAttributes, BindingResult bindingResult) {
		System.err.println("Categoria recibida via @ModelAttribute en PostMapping "+categoria);
		if (bindingResult.hasErrors()) {
	        return "admin/categorias/form_edit_categoria"; 
	    }
		Categoria catEditar = sci.findById(categoria.getId()).orElse(null);
		System.err.println("Categoria a Editar "+catEditar);		
		if (catEditar != null) {
	        catEditar.setNombre(categoria.getNombre());
	        try {
	        	sci.save(catEditar);
	        } catch (DataIntegrityViolationException e) {
		        redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre categoría duplicada. No se puede guardar el registro.");
		    } catch (Exception e) {
		        redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar categoría.");
		    }	        
	    }			
		return "redirect:/admin/categoria";
	}	
	
	@GetMapping("/delete/{id}")
	public String borrarCategoria(@PathVariable("id") Integer id, Model model, RedirectAttributes redirectAttributes) {
	    Optional<Categoria> optionalCategoria = sci.findById(id);
	    if (optionalCategoria.isPresent()) {
	        Categoria categoria = optionalCategoria.get();
	        if (spi.findNumPlatosByCategoria(categoria) == 0) {
	            sci.delete(categoria);
	            return "redirect:/admin/categoria";
	        } else {
	            redirectAttributes.addFlashAttribute("errorMessage", "No se puede borrar una categoría que tiene platos asociados.");
	            return "redirect:/admin/categoria";
	        }
	    } else {
	        redirectAttributes.addFlashAttribute("errorMessage", "Categoría no encontrada.");
	        return "redirect:/admin/categoria";
	    }
	}
	
	@PostMapping("/search")
	public String searchCategoria(@RequestParam String nombre, Model model, Pageable pageable) {	  
	    List<Categoria> categorias = sci.findAll().stream()
	        .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
	        .collect(Collectors.toList());	   
	    Page<Categoria> categoriasPage = new PageImpl<>(categorias, pageable, categorias.size());	  
	    model.addAttribute("categoriasPage", categoriasPage);  
	    model.addAttribute("categorias", categoriasPage.getContent());  
	    model.addAttribute("currentPage", "categorias");	  
	    return "admin/categorias/listado-categorias"; 
	}
 	
}
