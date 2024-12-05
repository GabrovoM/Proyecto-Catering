package com.dawes.controladores;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Menu;
import com.dawes.modelo.MenuPlato;
import com.dawes.modelo.Plato;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.CartItemRepository;
import com.dawes.repositorio.MenuPlatoRepository;
import com.dawes.servicios.ServicioMenu;
import com.dawes.serviciosImpl.ServicioCartImpl;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.ServicioIngredienteImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;
import com.dawes.serviciosImpl.UsuarioSesionService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/menus")
public class MenuController {
	@Autowired
	private ServicioMenu smi;
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioCategoriaImpl sci;	
	@Autowired
	private MenuPlatoRepository mpr;	
	private final UsuarioSesionService uss;	
    public MenuController(UsuarioSesionService uss) {
        this.uss = uss;
    }
	
	 @ModelAttribute
	 public void addTituloAttribute(Model model) {
	     model.addAttribute("titulo", "Menús");
	 }	 

	 @GetMapping("")
	 public String home(@RequestParam(required = false) String nombre, 
	                    Model model, 
	                    Pageable pageable) { 
	     List<Menu> menus;
	     Page<Menu> paginatedMenus;	     
	     if (nombre != null && !nombre.isEmpty()) {	        
	         menus = smi.findAll().stream()
	             .filter(menu -> menu.getNombre().toLowerCase().contains(nombre.toLowerCase()))
	             .collect(Collectors.toList()); 	      
	         int start = (int) pageable.getOffset();
	         int end = Math.min((start + pageable.getPageSize()), menus.size());
	         menus = menus.subList(start, end);	      
	         paginatedMenus = new PageImpl<>(menus, pageable, menus.size()); 
	         model.addAttribute("menusFiltered", paginatedMenus);    
	     } else {
	         paginatedMenus = smi.findAll(pageable);	        
	         model.addAttribute("menusAll", paginatedMenus);
	     }   	   
	     model.addAttribute("platos", spi.findAll(pageable));        
	     List<Plato> allPlatos = spi.findAll();
	     model.addAttribute("allPlatos", allPlatos);
	     model.addAttribute("currentPage", "menus");
	     model.addAttribute("nombre", nombre); 
	     model.addAttribute("page", pageable.getPageNumber()+1);	     
	     return "admin/menus/listado-menus";
	 }
	
	@GetMapping("/nuevomenu")
	public String create(Model model) {
	    model.addAttribute("nuevoMenu", new Menu());
	    List<Plato> platosLista = spi.findAll();
	    model.addAttribute("platosLista", platosLista);	
	    List<Categoria> categoriasLista = sci.findAll();
	    model.addAttribute("categoriasLista", categoriasLista);	
	    return "admin/menus/form_add_menu";
	}		
	
	@PostMapping("/savemenu")
	public String saveMenu(
	        @ModelAttribute("nuevoMenu") Menu menu,
	        @RequestParam Map<String, String> allRequestParams,
	        RedirectAttributes redirectAttributes) {
	    int dayIndex = 1;
	    while (allRequestParams.containsKey("dias[" + dayIndex + "].numerodia")) {
	        int numerodia = Integer.parseInt(allRequestParams.get("dias[" + dayIndex + "].numerodia"));
	        int plato1Id = Integer.parseInt(allRequestParams.get("dias[" + dayIndex + "].platos1"));
	        int plato2Id = Integer.parseInt(allRequestParams.get("dias[" + dayIndex + "].platos2"));	       
	        if (plato1Id == plato2Id) {
	            redirectAttributes.addFlashAttribute("errorMessage", "El primero y el segundo plato no peuden ser iguales para el mismo dia." + numerodia + ".");
	            return "redirect:/nuevomenu";  
	        }            
	        Plato plato1 = spi.findById(plato1Id)
	                .orElseThrow(() -> new RuntimeException("Plato not found with id " + plato1Id));
	        Plato plato2 = spi.findById(plato2Id)
	                .orElseThrow(() -> new RuntimeException("Plato not found with id " + plato2Id));
	        MenuPlato mp1 = new MenuPlato();
	        mp1.setOrden(1);
//	        mp1.setOrden(plato1.getOrden());
	        mp1.setPlato(plato1);
	        mp1.setDia(numerodia);
	        mp1.setMenu(menu);

	        MenuPlato mp2 = new MenuPlato();
	        mp2.setOrden(2);
//	        mp2.setOrden(plato2.getOrden());
	        mp2.setPlato(plato2);
	        mp2.setDia(numerodia);
	        mp2.setMenu(menu);

	        menu.addPlato(mp1);
	        menu.addPlato(mp2);
	        dayIndex++;
	    }
	    double totalPrice = menu.calcularPrecio();
	    menu.setPrecio(totalPrice);
	    try {
	    	smi.save(menu);
	    } catch (DataIntegrityViolationException e) {		        
	        System.err.println("Clave unica duplicada " + e.getLocalizedMessage());		        
	        redirectAttributes.addFlashAttribute("errorMessage", "Clave única Nombre menú duplicada. No se puede guardar el registro.");
	    }		    
	    return "redirect:/admin/menus";
	}			

	@GetMapping("/{id}")
	public String showMenuDetails(@PathVariable("id") int id, 
	        @RequestParam(defaultValue = "0") int page,
	        HttpServletRequest request, 
	        HttpSession session,
	        Model model) {
	    String referer = request.getHeader("Referer");
	    System.err.println("REFERER " + referer);
	    String refererUrl = "/admin/menus"; 

	    if (referer != null && referer.contains("/admin/menus")) {
	        refererUrl = referer;  	       
	        String query = referer.split("\\?", 2).length > 1 ? referer.split("\\?", 2)[1] : "";
	        Map<String, String> params = Arrays.stream(query.split("&"))
	            .map(param -> param.split("=", 2))
	            .collect(Collectors.toMap(
	                arr -> arr[0],
	                arr -> arr.length > 1 ? arr[1] : ""
	            ));

	        if (params.containsKey("nombre")) {
	            model.addAttribute("nombre", params.get("nombre")); 
	        }
	    }
	    session.setAttribute("refererUrl", refererUrl);
	    model.addAttribute("refererUrl", refererUrl);
	    model.addAttribute("sesion", session.getAttribute("idusuario"));
	    model.addAttribute("page", Optional.ofNullable(page).orElse(0));        

	    Optional<Menu> optionalMenu = smi.findById(id);    
	    if (optionalMenu.isPresent()) {
	        Menu m = optionalMenu.get(); 
	        m.getPlatos().sort(Comparator.comparing(MenuPlato::getDia).thenComparing(MenuPlato::getOrden));
	        model.addAttribute("menu", m);
	        return "admin/menus/detail_menu";
	    }
	    return "redirect:/admin/menus";
	}
	
	@GetMapping("/editMenu/{id}")
	public String editMenu(@PathVariable("id") Integer id, 
			@RequestParam(defaultValue = "0") int page, 
			HttpServletRequest request, Model model) {	
		String referer = request.getHeader("Referer");
		if (referer != null && referer.contains("?")) {
	        String query = referer.split("\\?", 2)[1];
	        Map<String, String> params = Arrays.stream(query.split("&"))
	            .map(param -> param.split("=", 2))
	            .collect(Collectors.toMap(
	                arr -> arr[0],
	                arr -> arr.length > 1 ? arr[1] : ""
	            ));
	        if (params.containsKey("page")) {
	            page = Integer.parseInt(params.get("page"));
	        }
	    }
	    model.addAttribute("page", page);	    
	    Optional<Menu> optMenu = smi.findById(id);
	    if (!optMenu.isPresent()) {	       
	        return "redirect:/admin/menus";
	    }
	    Menu menu = optMenu.get();	    
	    List<MenuPlato> menuPlatos = mpr.findByIdMenu(id); 
	    model.addAttribute("menuEdit", menu);
	    List<Plato> primerPlatos = spi.findByOrden(1).stream()
	                                  .filter(plato -> plato.getId() != 0)
	                                  .collect(Collectors.toList());
	    List<Plato> segundoPlatos = spi.findByOrden(2).stream()
	                                   .filter(plato -> plato.getId() != 0)
	                                   .collect(Collectors.toList());
	    model.addAttribute("primerPlatos", primerPlatos);
	    model.addAttribute("segundoPlatos", segundoPlatos);
	    Map<Integer, List<MenuPlato>> groupedByDia = new LinkedHashMap<>();
	    for (MenuPlato menuPlato : menuPlatos) {
	        groupedByDia.computeIfAbsent(menuPlato.getDia(), k -> new ArrayList<>()).add(menuPlato);
	    }
	    model.addAttribute("groupedByDia", groupedByDia);
	    List<Plato> platosLista = spi.findAll().stream()
	                                 .filter(plato -> plato.getId() != 0)
	                                 .collect(Collectors.toList());
	    model.addAttribute("platosLista", platosLista);
	    List<Categoria> categoriasLista = sci.findAll();
	    model.addAttribute("categoriasLista", categoriasLista);
	    Map<Categoria, List<Plato>> platosByCategoria = platosLista.stream()
	        .collect(Collectors.groupingBy(Plato::getCategoria));
	    model.addAttribute("platosByCategoria", platosByCategoria);
	    
	    return "admin/menus/form_edit_menu";
	}
	
	@PostMapping("/updatemenu")
	public String updateMenu(
	    @ModelAttribute("menuEdit") Menu menu,
	    HttpServletRequest request,
	    RedirectAttributes redirectAttributes) {	   
	    Enumeration<String> parameterNames = request.getParameterNames();
	    Map<String, String[]> paramsMap = new HashMap<>();
	    while (parameterNames.hasMoreElements()) {
	        String paramName = parameterNames.nextElement();
	        String[] paramValues = request.getParameterValues(paramName);
	        paramsMap.put(paramName, paramValues);
	        for (String value : paramValues) {
	            System.err.println(paramName + " = " + value);
	        }
	    }	  
	    if (menu.getId() == null || menu.getId() <= 0) {
	        redirectAttributes.addFlashAttribute("errorMessage", "ID menú no válido.");
	        return "redirect:/admin/menus/editMenu/{id}";
	    }
	    Menu existingMenu = smi.findById(menu.getId())
	        .orElseThrow(() -> new RuntimeException("Menu no encontrado"));
	    existingMenu.setNombre(menu.getNombre());
	    existingMenu.getPlatos().clear();
	    List<MenuPlato> newPlatos = new ArrayList<>();
	    Set<Integer> daysSet = new HashSet<>(); 
	    Pattern pattern = Pattern.compile("dias\\[(\\d+)]\\.numerodia");
	    for (String paramName : paramsMap.keySet()) {
	        Matcher matcher = pattern.matcher(paramName);
	        if (matcher.matches()) {
	            int index = Integer.parseInt(matcher.group(1));
	            String diaKey = "dias[" + index + "].numerodia";
	            String plato1Key = "dias[" + index + "].platos1";
	            String plato2Key = "dias[" + index + "].platos2";
	            String diaStr = request.getParameter(diaKey);
	            if (diaStr == null) {
	                continue;
	            }	            
	            int dia;
	            try {
	                dia = Integer.parseInt(diaStr);
	            } catch (NumberFormatException e) {
	                redirectAttributes.addFlashAttribute("errorMessage", "Valor de día inválido: " + diaStr);
	                return "redirect:/admin/menus/editMenu/" + menu.getId();
	            } 
	            if (!daysSet.add(dia)) {
	                redirectAttributes.addFlashAttribute("errorMessage", "El día " + dia + " ya está presente. Por favor elija otro día.");
	                return "redirect:/admin/menus/editMenu/" + menu.getId(); 
	            }
	            String plato1IdStr = request.getParameter(plato1Key);
	            String plato2IdStr = request.getParameter(plato2Key);

	            // comrpobar selects vacíos
	            if ((plato1IdStr == null || plato1IdStr.isEmpty()) || (plato2IdStr == null || plato2IdStr.isEmpty())) {
	                redirectAttributes.addFlashAttribute("errorMessage", "Debe elegir un plato para cada opción en el día " + dia + ".");
	                return "redirect:/admin/menus/editMenu/" + menu.getId(); 
	            }
	            if (plato1IdStr != null && !plato1IdStr.isEmpty()) {
	                try {
	                    int plato1Id = Integer.parseInt(plato1IdStr);	
	                    Plato plato1 = spi.findById(plato1Id)
                				.orElseThrow(() -> new RuntimeException("Plato no encontrado con id " + plato1Id));
	                    MenuPlato menuPlato1 = new MenuPlato();
	                    menuPlato1.setDia(dia);
	                    menuPlato1.setOrden(1);
	                    menuPlato1.setPlato(plato1);
	                    menuPlato1.setMenu(existingMenu);
	                    newPlatos.add(menuPlato1);
	                } catch (NumberFormatException e) {
	                    redirectAttributes.addFlashAttribute("errorMessage", "ID de primer plato no válido: " + plato1IdStr);
	                    return "redirect:/admin/menus/editMenu/" + menu.getId();
	                }
	            }	         
	            if (plato2IdStr != null && !plato2IdStr.isEmpty()) {
	                try {
	                    int plato2Id = Integer.parseInt(plato2IdStr);
	                    Plato plato2 = spi.findById(plato2Id)
                				.orElseThrow(() -> new RuntimeException("Plato no encontrado con id " + plato2Id));
    		            MenuPlato menuPlato2 = new MenuPlato();
	                    menuPlato2.setDia(dia);
	                    menuPlato2.setOrden(2);
	                    menuPlato2.setPlato(plato2);
	                    menuPlato2.setMenu(existingMenu);
	                    newPlatos.add(menuPlato2);
	                } catch (NumberFormatException e) {
	                    redirectAttributes.addFlashAttribute("errorMessage", "ID de segundo plato no válido: " + plato2IdStr);
	                    return "redirect:/admin/menus/editMenu/" + menu.getId();
	                }
	            }	         
	            if ((plato1IdStr != null && plato2IdStr != null) && plato1IdStr.equals(plato2IdStr)) {
	                redirectAttributes.addFlashAttribute("errorMessage", "El primer y segundo plato no pueden ser el mismo para el día " + dia + ".");
	                return "redirect:/admin/menus/editMenu/" + menu.getId(); 
	            }
	        }
	    }	  
	    existingMenu.getPlatos().addAll(newPlatos);	    
	    double totalPrice = existingMenu.calcularPrecio(); 
	    existingMenu.setPrecio(totalPrice);	   
	    smi.save(existingMenu);
	    redirectAttributes.addFlashAttribute("message", "Menú actualizado con éxito");
	    return "redirect:/admin/menus/" + existingMenu.getId();
	}	
	
	@GetMapping("/cambiarplato/{menuId}/{platoId}")
	public String cambiar(
	        @PathVariable("menuId") Integer menuId, 
	        @PathVariable("platoId") Integer platoId,
	        @RequestParam(name = "returnUrl", required = false) String returnUrl,
	        @RequestParam(name = "idCategoria", required = false) Integer idCategoria,
	        @RequestParam(name = "page", defaultValue = "0") int page,
	        @RequestParam(name = "size", defaultValue = "12") int size, 
	        @RequestParam(name = "dia", required = false) Integer dia,	        
	        HttpSession session,	        
	        Model model) {	
		if (dia != null) {
	        session.setAttribute("dia", dia);
	    }
		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
	    model.addAttribute("categorias", sci.findAll());
	    Page<Plato> platosPage;	  
	    if (idCategoria == null || idCategoria <= 0) {
	        platosPage = spi.findAll(pageable);
	        model.addAttribute("idCategoria", null);
	    } else {
	        platosPage = spi.findByCategoriaId(idCategoria, pageable);
	        model.addAttribute("idCategoria", idCategoria);
	    }	
	    Plato selectedPlato = spi.findById(platoId).orElse(null);
	    Integer selectedOrden = selectedPlato != null ? selectedPlato.getOrden() : null;	   
	    model.addAttribute("selectedOrden", selectedOrden);
	    model.addAttribute("platos", platosPage.getContent());
	    model.addAttribute("platosPage", platosPage);
	    model.addAttribute("size", size); 
	    model.addAttribute("menuId", menuId);
	    model.addAttribute("platoId", platoId);	    
	    model.addAttribute("returnUrl", returnUrl);	    
	    model.addAttribute("dia", session.getAttribute("dia"));	    
	    model.addAttribute("page", page);	    
	    model.addAttribute("currentPage", "cambiar_plato");	
	    return "admin/menus/cambiar_plato";
	}
	
	@PostMapping("/cambiarplato/submit")
	public String cambiarPlatoSubmit(
	        @RequestParam("menuId") Integer menuId,
	        @RequestParam("platoId") Integer platoId,
	        @RequestParam("selectedPlato") Integer selectedPlatoId,
	        @RequestParam("returnUrl") String returnUrl,
	        @RequestParam(name = "page", defaultValue = "0") int page, 	        
	        HttpSession session,  	        
	        Model model,
	        RedirectAttributes redirectAttributes) {		
		Integer dia = (Integer) session.getAttribute("dia"); 		
	    Menu menuR = smi.findById(menuId).orElseThrow(() -> new IllegalArgumentException("Menu ID no válido"));
	    Plato platoR = spi.findById(platoId).orElseThrow(() -> new IllegalArgumentException("Plato ID no válido"));	    
	    MenuPlato mpl = mpr.buscarOtroPlatoDelDia(dia, menuR, platoR);		    
	    if (mpl != null && mpl.getPlato().getId().equals(selectedPlatoId)) {
	        redirectAttributes.addFlashAttribute("errorMessage", "El plato seleccionado ya está en el menú. Por favor, elija un plato diferente.");
	        if (returnUrl.equals("editMenu")) {
	            return "redirect:/admin/menus/editMenu/" + menuId;
	        } else {
	            return "redirect:/admin/menus/" + menuId;
	        }
	    }	    
	    try {
	        Menu menu = smi.findById(menuId).orElseThrow(() -> new RuntimeException("Menu no encontrado"));
	        Plato currentPlato = spi.findById(platoId).orElseThrow(() -> new RuntimeException("Plato no encontrado"));
	        Plato newPlato = spi.findById(selectedPlatoId).orElseThrow(() -> new RuntimeException("Plato no encontrado"));
	        MenuPlato menuPlato = menu.getPlatos().stream()
	                .filter(mp -> mp.getPlato().equals(currentPlato) && mp.getDia().equals(dia))
	                .findFirst()
	                .orElseThrow(() -> new RuntimeException("MenuPlato no encontrado"));
	        menuPlato.setPlato(newPlato);
	        double totalPrice = menu.calcularPrecio();
	        menu.setPrecio(totalPrice);	        
	        boolean todos_disponibles = menu.getPlatos().stream()
	                .allMatch(mp -> !mp.getPlato().isNoDisponible()); 	        
	        menu.setNoDisponible(!todos_disponibles);	        
	        smi.save(menu);	     
	        if (returnUrl.equals("editMenu")) {
	            return "redirect:/admin/menus/" + returnUrl + "/" + menuId;
	        } else {
	        	 return "redirect:/admin/menus/" + menuId;
	        }	        
	    } catch (Exception e) {
	        model.addAttribute("error", "Error al cambiar el plato: " + e.getMessage());
	        return "redirect:/admin/menus/cambiarplato/" + menuId + "/" + platoId;
	    }
	}	
	
	@GetMapping("/borrar/{idmp}")
	public String borrarMenuPlato(@PathVariable("idmp") Integer idmp, Model model) {
	    Optional<MenuPlato> mpOptional = mpr.findById(idmp);
	    Integer idmenu = 0;
	    if (mpOptional.isPresent()) {
	        MenuPlato mp = mpOptional.get();
	        idmenu = mp.getMenu().getId();
	        Integer dia = mp.getDia(); 	       
	        List<MenuPlato> platosToDelete = mpr.findByMenuIdAndDia(idmenu, dia);
	        mpr.deleteAll(platosToDelete);	      
	        model.addAttribute("message", "Todos los platos per día " + dia + " borrados con éxito.");
	    } else {
	        model.addAttribute("error", "Plato no encontrado.");
	    }	   
	    return "redirect:/admin/menus/editMenu/" + idmenu; 
	}
	
	@GetMapping({"/delete/{id}"})
	public String borrarPlato(@PathVariable int id, RedirectAttributes redirectAttributes)  {
		try {			
			List<EstadoPedidoEnum> estados = Arrays.asList(EstadoPedidoEnum.PENDIENTE, EstadoPedidoEnum.PARCIAL);
			long countPedidosMenus = smi.countPedidosByMenuIdAndEstados(id, estados);			
			if (countPedidosMenus > 0) {
				redirectAttributes.addFlashAttribute("errorMessage", "Este menú está asociado a pedidos pendientes o parciales.");
				return "redirect:/admin/menus";
			}			
			smi.deleteById(id);	
		} catch (NoSuchElementException e) {
			System.err.println("No existe menu con id "+id + e.getLocalizedMessage());
			redirectAttributes.addFlashAttribute("errorMessage", "No existe menu con este id");			
		}				
		return "redirect:/admin/menus";	
	}
	
	@PostMapping("/cambiar_plato/search")
	public String searchInCambiarPlato(
	        @RequestParam(name = "page", required = false, defaultValue = "0") int page,
	        @RequestParam(defaultValue = "12") int size,
	        @RequestParam(defaultValue = "") String nombre,
	        @RequestParam(required = false) Integer idCategoria,
	        @RequestParam String sourceView,
	        @RequestParam String currentPage,
	        @RequestParam(value = "menuId", required = false) Integer menuId,
	        @RequestParam(value = "platoId", required = false) Integer platoId,
	        @RequestParam(value = "dia", required = false) Integer dia,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl,
	        Model model,
	        @AuthenticationPrincipal UserDetails userDetails,
	        HttpServletRequest request) {
	    List<Plato> allPlatos = spi.findAll();	  
	    List<Plato> filteredPlatos = allPlatos.stream()
	            .filter(p -> nombre.isEmpty() || p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
	            .filter(p -> idCategoria == null || p.getCategoria().getId() == idCategoria)
	            .collect(Collectors.toList());
	    if (filteredPlatos.isEmpty()) {
	        model.addAttribute("noResultsMessage", "No se encontraron platos para los criterios de búsqueda proporcionados.");	       
	        Pageable pageable = PageRequest.of(page, size);
	        Page<Plato> platosPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
	        model.addAttribute("platosPage", platosPage); 
	        model.addAttribute("platos", platosPage.getContent()); 
	    } else {	       
	        Pageable pageable = PageRequest.of(page, size);
	        int start = (int) pageable.getOffset();
	        if (start >= filteredPlatos.size()) {
	            start = filteredPlatos.size(); 
	        }
	        int end = Math.min((start + pageable.getPageSize()), filteredPlatos.size());
	        Page<Plato> platosPage = new PageImpl<>(filteredPlatos.subList(start, end), pageable, filteredPlatos.size());
	        model.addAttribute("platos", platosPage.getContent());
	        model.addAttribute("platosPage", platosPage);
	    }	    
	    model.addAttribute("nombre", nombre);
	    model.addAttribute("size", size);
	    model.addAttribute("idCategoria", idCategoria);
	    model.addAttribute("menuId", menuId);
	    model.addAttribute("platoId", platoId);
	    model.addAttribute("returnUrl", returnUrl);
	    model.addAttribute("currentPage", currentPage);
	    model.addAttribute("sourceView", sourceView);
	    model.addAttribute("categorias", sci.findAll());
	    model.addAttribute("page", page);	
	    if (platoId != null) {
	        Plato selectedPlato = spi.findById(platoId).orElse(null);
	        Integer selectedOrden = selectedPlato != null ? selectedPlato.getOrden() : null;
	        model.addAttribute("selectedOrden", selectedOrden);
	    }
	   
	    return "admin/menus/cambiar_plato";
	}				
	   
   @PostMapping("/search")
	public String searchMenu(@RequestParam String nombre, Model model, Pageable pageable) {	  
	    List<Menu> menusLista = smi.findAll().stream()
	        .filter(c -> c.getNombre().toLowerCase().contains(nombre.toLowerCase()))
	        .collect(Collectors.toList());

	    Page<Menu> menus = new PageImpl<>(menusLista, pageable, menusLista.size());
	    model.addAttribute("menus", menus);  	
	    model.addAttribute("currentPage", "menus");	
	    return "redirect:/admin/menus?nombre=" + nombre;
	}
	
}



