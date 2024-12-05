package com.dawes.controladores;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Ingrediente;
import com.dawes.modelo.Plato;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.ServicioIngredienteImpl;
import com.dawes.serviciosImpl.ServicioMenuImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;
import com.dawes.serviciosImpl.UploadFileService;

@Controller
@RequestMapping("/admin/plato")
public class PlatoController {
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioCategoriaImpl sci;
	@Autowired
	private ServicioIngredienteImpl sii;
	@Autowired
	private ServicioMenuImpl smi;
	@Autowired
	private UploadFileService upload;
	
	@ModelAttribute("categorias")
	public List<Categoria> todasCategorias() {
		return sci.findAll();
	}
	
	@ModelAttribute
	public void addTituloAttribute(Model model) {
	    model.addAttribute("titulo", "Platos");
	}	
	
	@GetMapping("")
	public String home(@RequestParam(name = "idCategoria", required = false) Integer idCategoria, 
	                   Model model, Pageable pageable) {	    
	    model.addAttribute("categorias", sci.findAll());
	    Page<Plato> platosPage;	    
	    if (idCategoria == null || idCategoria <= 0) {  
	        platosPage = spi.findAll(pageable);
	        model.addAttribute("idCategoria", null);  
	    } else {  
	        platosPage = spi.findByCategoriaId(idCategoria, pageable);
	        model.addAttribute("idCategoria", idCategoria);
	    }
	    model.addAttribute("platos", platosPage.getContent());
	    model.addAttribute("platosPage", platosPage);	    
	    model.addAttribute("isIndexView", false);	    			
	    model.addAttribute("currentPage", "listado-platos");   
	    return "admin/platos/listado-platos";
	}	

	@GetMapping("/nuevoplato")
	public String create(Model model) {
		model.addAttribute("nuevoPlato", new Plato());
		model.addAttribute("catLista", sci.findAll());
		model.addAttribute("ingrLista", sii.findAll());
		return "admin/platos/form_add_plato";
	}

	@PostMapping("/saveplato")	
	public String savePlato(@ModelAttribute("nuevoPlato") Plato plato, 
			RedirectAttributes redirectAttributes, BindingResult bindingResult,
			@RequestParam("img") MultipartFile file) throws IOException {
		Integer ordenValue = plato.getOrden();
		if (plato.getId() == null) { // cuando se crea un nuevo plato
			String nombreImagen = upload.saveImage(file);
			plato.setImagen(nombreImagen);
		}
		for (Ingrediente i : plato.getIngredientes()) {
			i.getPlatos().add(plato);
			plato.getIngredientes().add(i);
		}
		try {
		   spi.save(plato);
	    } catch (DataIntegrityViolationException e) {		        
	       System.err.println("Clave unica duplicada " + e.getLocalizedMessage());		        
	       redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre plato duplicada. No se puede guardar el registro.");
	    }		
		return "redirect:/admin/plato";
	}
	
	@GetMapping("/editPlato/{id}")
	public String editarPlato(@PathVariable("id") Integer id, Model model) {
		Plato plato = spi.findById(id).orElse(null);
		if (plato != null) {
			model.addAttribute("editedPlato", plato);
			model.addAttribute("catLista", sci.findAll());
			model.addAttribute("ingrLista", sii.findAll());
			return "admin/platos/form_edit_plato";
		} else {
			return "redirect:/admin/plato";
		}					
	}	
	
	@PostMapping("/updateplato/submit")
	public String editPlatoSubmit(@ModelAttribute("editedPlato") Plato plato,
	        @RequestParam("categoria") Integer cat,
	        @RequestParam("ingredientes") List<Integer> ingredientesIds,
	        @RequestParam("img") MultipartFile file,
	        RedirectAttributes redirectAttributes, 
	        BindingResult bindingResult) throws IOException {
	    Plato platoEditar = spi.findById(plato.getId())
	            .orElseThrow(() -> new IllegalArgumentException("Plato ID no válido: " + plato.getId()));
	    boolean isOfertaChanged = platoEditar.getOferta() != plato.getOferta();
	    boolean isNoDisponibleChanged = platoEditar.isNoDisponible() != plato.isNoDisponible();
	    platoEditar.setNombre(plato.getNombre());
	    platoEditar.setPrecio(plato.getPrecio());
	    platoEditar.setOrden(plato.getOrden());
	    Categoria selectedCat = sci.findById(cat)
	            .orElseThrow(() -> new IllegalArgumentException("Categoria ID no válido: " + cat));
	    platoEditar.setCategoria(selectedCat);
	    platoEditar.setDescripcion(plato.getDescripcion());	
	    Set<Ingrediente> currentIngredientes = new HashSet<>(platoEditar.getIngredientes());
	    Set<Ingrediente> newIngredientes = ingredientesIds.stream()
	            .map(id -> sii.findById(id)
	                    .orElseThrow(() -> new IllegalArgumentException("Ingrediente ID no válido: " + id)))
	            .collect(Collectors.toSet());
	    currentIngredientes.stream().filter(ing -> !newIngredientes.contains(ing)).forEach(ing -> {
	        platoEditar.getIngredientes().remove(ing);
	        ing.getPlatos().remove(platoEditar);
	    });
	    newIngredientes.stream().filter(ing -> !currentIngredientes.contains(ing)).forEach(ing -> {
	        platoEditar.getIngredientes().add(ing);
	        ing.getPlatos().add(platoEditar);
	    });	  
	    if (isOfertaChanged) {
	        platoEditar.setOferta(plato.getOferta());
	        platoEditar.setDe_oferta(plato.getOferta() > 0);
	        platoEditar.actualizarPrecio();
	    }	   
	    if (isNoDisponibleChanged) {
	        platoEditar.setNoDisponible(plato.isNoDisponible());
	    }	    
	    if (!file.isEmpty()) {
	        if (platoEditar.getImagen() != null && !platoEditar.getImagen().equals("default.jpg")) {
	            upload.deleteImage(platoEditar.getImagen());
	        }
	        String nombreImagen = upload.saveImage(file);
	        platoEditar.setImagen(nombreImagen);
	    } else if (platoEditar.getImagen() == null || platoEditar.getImagen().equals("default.jpg")) {
	        platoEditar.setImagen("default.jpg");
	    }
	    try {
	        spi.save(platoEditar);
	        if (isOfertaChanged) {
	            smi.updateMenusPriceForPlato(platoEditar);
	        }
	        if (isNoDisponibleChanged) {
	            smi.updateMenusAvailabilityForPlato(platoEditar);
	        }
	    } catch (DataIntegrityViolationException e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre plato duplicada. No se puede guardar el registro.");
	    } catch (Exception e) {
	        redirectAttributes.addFlashAttribute("errorMessage", "Error al actualizar el plato.");
	    }
	    return "redirect:/admin/plato";
	}
	
	@GetMapping({"/delete/{id}"})
	public String borrarPlato(@PathVariable int id, RedirectAttributes redirectAttributes) {
	    try {	      
	        long countPedidos = spi.countPedidosByPlatoIdAndEstado(id, EstadoPedidoEnum.PENDIENTE);	        
	        long countPedidosParcial = spi.countPedidosByPlatoIdAndEstado(id, EstadoPedidoEnum.PARCIAL);
	        
	        if (countPedidos > 0 || countPedidosParcial > 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Este plato está asociado a pedidos pendientes.");
	            return "redirect:/admin/plato";  
	        }	      
	        long countMenus = spi.countMenusByPlatoId(id);
	        if (countMenus > 0) {
	            redirectAttributes.addFlashAttribute("errorMessage", "Este plato tiene menús asociados.");
	            return "redirect:/admin/plato";  
	        }	       
	        spi.deleteById(id);
	    } catch (NoSuchElementException e) {
	        System.err.println("No existe plato con id " + id + e.getLocalizedMessage());
	        redirectAttributes.addFlashAttribute("errorMessage", "No existe plato con este id");
	    } catch (DataIntegrityViolationException e) {
	        System.err.println("Error al borrar plato " + e.getLocalizedMessage());
	        redirectAttributes.addFlashAttribute("errorMessage", "Error al borrar el plato.");
	    }
	    return "redirect:/admin/plato";		 
	}	
	
}
