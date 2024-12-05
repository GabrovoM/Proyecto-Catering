package com.dawes.controladores;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.Principal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.modelo.Ajuste;
import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.Categoria;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Menu;
import com.dawes.modelo.MenuPlato;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.Plato;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.AjusteRepository;
import com.dawes.repositorio.CartItemRepository;
import com.dawes.repositorio.CartRepository;
import com.dawes.repositorio.UsuarioRepository;
import com.dawes.serviciosImpl.EmailService;
import com.dawes.serviciosImpl.PDFGeneratorService;
import com.dawes.serviciosImpl.ServicioCartImpl;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.ServicioIngredienteImpl;
import com.dawes.serviciosImpl.ServicioMenuImpl;
import com.dawes.serviciosImpl.ServicioPedidoImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;
import com.dawes.serviciosImpl.ServicioPlatoMenuPedidoImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;
import com.dawes.serviciosImpl.UploadFileService;
import com.dawes.serviciosImpl.UsuarioSesionService;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Controller
@SessionAttributes({"idCategoria", "page"})
public class MainController {	
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioMenuImpl smi;
	@Autowired
	private ServicioCategoriaImpl sci;		
	@Autowired
	private ServicioPedidoImpl spdi;	
	@Autowired
	private ServicioPlatoMenuPedidoImpl sppi;	
	@Autowired
	private ServicioUsuarioImpl sui;	
	@Autowired
	private ServicioCartImpl csi;		
	@Autowired
	private UsuarioRepository usuarioRepository;
	@Autowired
	private CartRepository cr;
	@Autowired
	private CartItemRepository cir;
	@Autowired
	private AjusteRepository ar;
	@Autowired
	private PDFGeneratorService pdfGeneratorService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;	
	
	private final UsuarioSesionService uss;
    public MainController(UsuarioSesionService uss) {
        this.uss = uss;
    }
	
	// para almacenar los detalles del pedido
	List<PlatoMenuPedido> detalles = new ArrayList<PlatoMenuPedido>();
	// datos del pedido
	Pedido pedido = new Pedido();
	
	@ModelAttribute("categorias")
	public List<Categoria> todasCategorias() {
		return sci.findAll();
	}		
	
	@ModelAttribute("favoritos")
	public Set<Integer> favoritos() {
	    Set<Integer> result = new HashSet<>();
	   
	    if (SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {	       
	        String username = SecurityContextHolder.getContext().getAuthentication().getName();	        
	        Optional<Usuario> optionalUsuario = usuarioRepository.findByUsername(username);
	        if (optionalUsuario.isPresent()) {
	            Usuario usuario = optionalUsuario.get();
	            result = usuario.getPlatos().stream()
	                            .mapToInt(Plato::getId)
	                            .collect(HashSet::new, HashSet::add, HashSet::addAll);
	        } else {	           
	            System.err.println("Usuario no encontrado: " + username);
	        }
	    } else {	       
	        System.err.println("Usuario no autenticado.");
	    }
	    return result;
	}
	
	@ModelAttribute("numeroItemsCarrito")
	public Integer numeroItemsCarrito(@AuthenticationPrincipal UserDetails userDetails) {
	    if (userDetails == null) {
	        return 0; 
	    }
	    return csi.getNumeroItemsCarrito(userDetails.getUsername());
	}
	
	@ModelAttribute
	public void addCommonAttributes(Model model) {
	    Ajuste ajuste = ar.findById(1).orElseThrow(() -> new IllegalArgumentException("Ajuste ID no válido."));
	    model.addAttribute("precioEnvio", ajuste.getPrecioEnvioZona1());	       
	}
	
	@GetMapping(value={"","/"})
	public String welcome(HttpServletRequest request, 
			@AuthenticationPrincipal UserDetails userDetails, 
			Model model) {
		uss.addUserDetailsToModel(model, userDetails);
		model.addAttribute("isIndexView", true);
		model.addAttribute("currentUri", request.getRequestURI()); 
		return "public/index";
	}	
	
	@GetMapping("/platos")
	public String home(@RequestParam(name = "idCategoria", required = false) Integer idCategoria, 
	                   Model model, Pageable pageable, 
	                   HttpServletRequest request,
	                   @AuthenticationPrincipal UserDetails userDetails) {	
		uss.addUserDetailsToModel(model, userDetails);	    
	    model.addAttribute("categorias", sci.findAll());
	    Page<Plato> platosPage;
	    boolean noPlatosInCategory = false;
	    String categoryName = null;	  
	    Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(Sort.Direction.DESC, "id"));

	    if (idCategoria == null || idCategoria <= 0) {
	        platosPage = spi.findAll(sortedPageable);
	        model.addAttribute("idCategoria", null);  
	    } else {
	        platosPage = spi.findByCategoriaId(idCategoria, sortedPageable);
	        if (platosPage.isEmpty()) {
	            noPlatosInCategory = true;  
	        }
	        model.addAttribute("idCategoria", idCategoria);
	        Categoria categoria = sci.findById(idCategoria).orElse(null);
	        if (categoria != null) {
	            categoryName = categoria.getNombre();  
	        }
	    }
	    model.addAttribute("platos", platosPage.getContent());
	    model.addAttribute("platosPage", platosPage != null ? platosPage : new PageImpl<>(new ArrayList<>()));
	    model.addAttribute("isFavoritesView", false);
	    model.addAttribute("currentPage", "platos");
	    model.addAttribute("noPlatosInCategory", noPlatosInCategory); 
	    model.addAttribute("categoryName", categoryName);
	    model.addAttribute("currentUri", request.getRequestURI());    	    
	    return "public/platos";
	}   

	@GetMapping("/menus")
	public String homeMenus(Model model, Pageable pageable,
			HttpServletRequest request,
			@AuthenticationPrincipal UserDetails userDetails) {	
		uss.addUserDetailsToModel(model, userDetails);	    
	    Page<Menu> menusPage = smi.findAll(pageable);	   
	    for (Menu menu : menusPage.getContent()) {
	        menu.getPlatos().sort(Comparator.comparing(MenuPlato::getDia)
	                                        .thenComparing(MenuPlato::getOrden));
	    }
	    model.addAttribute("platos", spi.findAll(pageable));
	    model.addAttribute("allPlatos", spi.findAll());
	    model.addAttribute("menus", menusPage);
	    model.addAttribute("titulo", "Menús");	    
	    model.addAttribute("currentUri", request.getRequestURI());
	    return "public/menus";
	}		
	
	@GetMapping("/platos/{id}")
	public String showDetails(@PathVariable("id") int id,
	                          @RequestParam(name = "idCategoria", required = false) Integer idCategoria,
	                          @RequestParam(name = "page", required = false) Integer page,
	                          @RequestParam(name = "isFavoritesView", required = false) Boolean isFavoritesView,
	                          Model model) {
	    Plato p = spi.findById(id).orElse(null);
	    if (p != null) {
	        model.addAttribute("plato", p);
	        model.addAttribute("idCategoria", idCategoria); 
	        model.addAttribute("page", page); 	       
	        model.addAttribute("isFavoritesView", isFavoritesView != null && isFavoritesView);
	        return "public/detail";
	    }
	    return "redirect:/";
	}
	
	@GetMapping("/usuario/favs")
	public String misFavoritos(@RequestParam(name = "idCategoria", required = false) Integer idCategoria,
	                           Model model, Pageable pageable) {	   
	    Page<Plato> platosPage = spi.findAll(pageable);
	    Set<Integer> favoritosIds = favoritos();  	   
	    List<Plato> favoritePlatos = favoritosIds.isEmpty() ? new ArrayList<>() : spi.findByIds(favoritosIds);	   
	    Page<Plato> favoritePlatosPage = new PageImpl<>(favoritePlatos, pageable, favoritePlatos.size());	
	    if (idCategoria != null && idCategoria > 0) {
	        favoritePlatos = favoritePlatos.stream()
	            .filter(plato -> plato.getCategoria().getId().equals(idCategoria))
	            .collect(Collectors.toList());
	    }	  
	    int start = (int) pageable.getOffset();
	    int end = Math.min((start + pageable.getPageSize()), favoritePlatos.size());
	    Page<Plato> favoritosPage = new PageImpl<>(favoritePlatos.subList(start, end), pageable, favoritePlatos.size());
   
	    model.addAttribute("noFavorites", favoritosIds.isEmpty());
	    model.addAttribute("platos", favoritePlatos.isEmpty() ? new ArrayList<>() : favoritePlatos);
	    model.addAttribute("platosPage", platosPage); 
	    model.addAttribute("favoritePlatosPage", favoritosPage);  
	    model.addAttribute("isFavoritesView", true); 	  
	    model.addAttribute("isIndexView", true);
	    return "public/platos"; 
	}						
	
	@Transactional
	@PostMapping("/platos/{id}/addFav")
	public ResponseEntity<String> addPlatoFavorito(@PathVariable("id") Integer id) {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    Usuario usuario = usuarioRepository.findFirstByUsername(username);
	    Plato p = spi.findById(id).get();
	    if (p != null && usuario != null) {
	        usuario.addPlatoFavorito(p);
	        usuarioRepository.save(usuario); 
	        return ResponseEntity.noContent().build();
	    } else {
	        return ResponseEntity.badRequest().build();
	    }
	}

	@Transactional
	@PostMapping("/platos/{id}/removeFav")
	public ResponseEntity<String> removeProductoFavorito(@PathVariable("id") Integer id) {
	    String username = SecurityContextHolder.getContext().getAuthentication().getName();
	    Usuario usuario = usuarioRepository.findFirstByUsername(username);
	    Plato p = spi.findById(id).get();
	    if (p != null && usuario != null) {
	        usuario.removePlatoFavorito(p);
	        usuarioRepository.save(usuario);
	        return ResponseEntity.noContent().build();
	    } else {
	        return ResponseEntity.badRequest().build();
	    }
	}	

    @PostMapping("/platos/search")
    public String searchProduct1(
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
                .sorted(Comparator.comparing(Plato::getId).reversed())
                .collect(Collectors.toList());
        if (filteredPlatos.isEmpty()) {
            model.addAttribute("platos", Collections.emptyList());
            model.addAttribute("platosPage", new PageImpl<>(Collections.emptyList(), PageRequest.of(page, size), 0));
            model.addAttribute("nombre", nombre);
            model.addAttribute("size", size);
            model.addAttribute("idCategoria", idCategoria);
            model.addAttribute("menuId", menuId);
            model.addAttribute("platoId", platoId);
            model.addAttribute("returnUrl", returnUrl);
            model.addAttribute("currentPage", currentPage);
            model.addAttribute("sourceView", sourceView);
            return "redirect:/platos/search?nombre=" + uss.encodeURIComponent(nombre) + "&page=" + page + "&size=" + size + "&sourceView=" + uss.encodeURIComponent(sourceView);
        }       
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset(); 
        if (start >= filteredPlatos.size()) {
            start = filteredPlatos.size(); 
        }        
        int end = Math.min((start + pageable.getPageSize()), filteredPlatos.size());
        Page<Plato> platosPage = new PageImpl<>(filteredPlatos.subList(start, end), pageable, filteredPlatos.size());
        uss.addUserDetailsToModel(model, userDetails);
        if (userDetails != null) {
            Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
            if (usuario != null) {
                Integer numeroItemsCarrito = (Integer) model.getAttribute("numeroItemsCarrito");
                model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
                HttpSession session = request.getSession();
                session.setAttribute("numeroItemsCarrito", numeroItemsCarrito);
            }
        } else {
            model.addAttribute("numeroItemsCarrito", 0); 
        }
        model.addAttribute("platos", platosPage.getContent());
        model.addAttribute("platosPage", platosPage);
        model.addAttribute("nombre", nombre);
        model.addAttribute("size", size);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("menuId", menuId);
        model.addAttribute("platoId", platoId);
        model.addAttribute("returnUrl", returnUrl);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("sourceView", sourceView);        
        String redirectUrl = String.format("/platos/search?nombre=%s&page=0&size=%d&sourceView=%s",
                uss.encodeURIComponent(nombre), size, uss.encodeURIComponent(sourceView));        
        if ("cambiar_plato".equals(sourceView)) {        	
        	return "admin/menus/cambiar_plato";
        } else {
            return "redirect:" + redirectUrl;
        }
    }
														    
    @GetMapping("/platos/search")
    public String searchProductGet(
            @RequestParam(name = "page", required = false, defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String nombre,
            @RequestParam(required = false) Integer idCategoria,
            @RequestParam(name = "sourceView", defaultValue = "platos") String sourceView,
            @RequestParam(value = "menuId", required = false) Integer menuId,
            @RequestParam(value = "platoId", required = false) Integer platoId,
            @RequestParam(value = "returnUrl", required = false) String returnUrl,
            Model model,
            HttpServletRequest request) {       
        HttpSession session = request.getSession();
        Integer numeroItemsCarrito = (Integer) session.getAttribute("numeroItemsCarrito");
        model.addAttribute("numeroItemsCarrito", numeroItemsCarrito != null ? numeroItemsCarrito : 0);       
        List<Plato> filteredPlatos = spi.findAll().stream()
                .filter(p -> p.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .filter(p -> idCategoria == null || p.getCategoria().getId() == idCategoria)
                .sorted(Comparator.comparing(Plato::getId).reversed())
                .collect(Collectors.toList());       
        List<Menu> filteredMenus = smi.searchMenusByName(nombre);       
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filteredPlatos.size());
        if (start >= filteredPlatos.size()) {           
            page = (int) Math.floor((double) filteredPlatos.size() / size);
            pageable = PageRequest.of(page, size); 
            start = (int) pageable.getOffset();
            end = Math.min(start + pageable.getPageSize(), filteredPlatos.size());
        }
        Page<Plato> platosPage = new PageImpl<>(filteredPlatos.subList(start, end), pageable, filteredPlatos.size());
        model.addAttribute("platos", platosPage.getContent());
        model.addAttribute("platosPage", platosPage);
        model.addAttribute("filteredMenus", filteredMenus);
        model.addAttribute("nombre", nombre);
        model.addAttribute("size", size);
        model.addAttribute("idCategoria", idCategoria);
        model.addAttribute("sourceView", sourceView);
        model.addAttribute("menuId", menuId);
        model.addAttribute("platoId", platoId);
        model.addAttribute("returnUrl", returnUrl);       
        if ("cambiar_plato".equals(sourceView)) {        	
        	return "admin/menus/cambiar_plato";
        } else if ("listado-platos".equals(sourceView)) {
            return "admin/platos/listado-platos";
        } else {
            return "public/platos"; 
        }
    }
			
	@GetMapping("/platos/list")
	@ResponseBody
	public List<Plato> listPlatos() {
	    return spi.findAll();
	}		
	
	@GetMapping({"/delete/{id}"})
	public String borrarPlato(@PathVariable int id) {
		System.err.println(spi.findById(id));
		spi.deleteById(id);		
		return "redirect:/platos";		 
	}	

	// añadir platos o menú sueltos
	@PostMapping("/platos/cart")
	public String addCart(@RequestParam Integer id, 
	                      @RequestParam String type,
	                      @RequestParam(value = "cantidad", defaultValue = "1") Integer cantidad, 
	                      Model model, HttpSession session, RedirectAttributes redirectAttributes, 
	                      @AuthenticationPrincipal UserDetails userDetails) {
	    double precioEnvio = (double) model.getAttribute("precioEnvio");
	    PlatoMenuPedido platoMenuPedido = new PlatoMenuPedido();
	    Plato plato = null;
	    Menu menu = null;
	    if ("plato".equalsIgnoreCase(type)) {
	        Optional<Plato> optPlato = spi.findById(id);
	        if (optPlato.isPresent()) {
	            plato = optPlato.get();
	            platoMenuPedido.setPlato(plato);
	            if (plato.getPrecio_of() > 0) {
	                platoMenuPedido.setPrecio_plato(plato.getPrecio_of());
	                platoMenuPedido.setTotalLinea(plato.getPrecio_of() * cantidad);
	            } else {
	                platoMenuPedido.setPrecio_plato(plato.getPrecio());
	                platoMenuPedido.setTotalLinea(plato.getPrecio() * cantidad);
	            }
	        }
	    } else if ("menu".equalsIgnoreCase(type)) {
	        Optional<Menu> optMenu = smi.findById(id);
	        if (optMenu.isPresent()) {
	            menu = optMenu.get();
	            platoMenuPedido.setMenu(menu);
	            platoMenuPedido.setPrecio_menu(menu.getPrecio());
	            platoMenuPedido.setTotalLinea(menu.getPrecio() * cantidad);
	        }
	    }
	    if (plato == null && menu == null) {
	        model.addAttribute("carritoMsg", "El plato o el menú no están disponibles.");
	        return "public/carrito/carrito"; 
	    }

	    Usuario u = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    if (u == null) {
	        model.addAttribute("carritoMsg", "Usuario no encontrado.");
	        return "public/carrito/carrito"; 
	    }
	    Cart cart = csi.findByUsuario(u).stream().findFirst().orElse(null);
	    if (cart == null) {
	        cart = new Cart();
	        cart.setUsuario(u);
	        cart = cr.save(cart);
	    }
	    Integer numeroItemsCarrito = cir.numeroItemsCart(u.getId());
	    List<CartItem> cartItems = cir.findByCart(cart);
	    if (cartItems == null) {
	        cartItems = new ArrayList<>();
	    }	
	    if (numeroItemsCarrito != null && numeroItemsCarrito > 1 && cartItems.get(0).isSemanal()) {
	    	redirectAttributes.addFlashAttribute("carritoMsg", "Tienes elegidos menús para envíos semanales planificados para varias semanas. <br>"
	    			+ "A este tipo de pedido no se pueden añadir otros platos o menús fuera de los ya elegidos.");
  	    	return "redirect:/platos/getCart";					  
	    }	    
	    Integer itemId = (plato != null) ? plato.getId() : (menu != null ? menu.getId() : null);
	    boolean itemExists = false;
	    for (CartItem ci : cartItems) {
	        if (itemId != null) {
	            if (plato != null && ci.getPlato() != null && ci.getPlato().getId().equals(itemId)) {
	                itemExists = true;
	                break;
	            } else if (menu != null && ci.getMenu() != null && ci.getMenu().getId().equals(itemId)) {
	                itemExists = true;
	                break;
	            }
	        }
	    }
	    if (itemExists) {
	        model.addAttribute("carritoMsg", "El artículo ya está en el carrito.");
	    }	
	    CartItem cartItem = null;
	    if (plato != null) {
	        cartItem = cir.findByCartAndPlato(cart, plato).orElse(new CartItem());
	        cartItem.setPlato(plato);
	        Menu menuNull = smi.findById(0).orElse(new Menu()); 
	        cartItem.setMenu(menuNull);
	    } else if (menu != null) {
	        cartItem = cir.findByCartAndMenu(cart, menu).orElse(new CartItem());
	        cartItem.setMenu(menu);
	        Plato platoNull = spi.findById(0).orElse(new Plato()); 
	        cartItem.setPlato(platoNull);
	    }	   
	    if (!itemExists) {
	    	cartItem.setCart(cart);
	    	cartItem.setCantidadCart(cantidad);
	    }	    	  
	    cir.save(cartItem);	  
	    if (itemExists) {
	    	numeroItemsCarrito = numeroItemsCarrito;
	    } else {
	    	numeroItemsCarrito = numeroItemsCarrito + cantidad;
	    }	 	    
	    double sumaTotal = cir.findByCart(cart).stream()
	    	    .mapToDouble(this::calculateItemPrice)  
	    	    .sum();			    
	    Pedido pedido = (Pedido) session.getAttribute("pedido");
	    if (pedido == null) {
	        pedido = new Pedido();
	    }
	    pedido.setTotal(sumaTotal);
	    session.setAttribute("pedido", pedido); 	    
	    boolean isParcial = csi.isCartParcial(cart); 
        model.addAttribute("isParcial", isParcial);  	    
	    model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
	    model.addAttribute("cart", cir.findByCart(cart));
	    model.addAttribute("pedidoTotal", sumaTotal);
	    model.addAttribute("total", sumaTotal + precioEnvio);
	    return "public/carrito/carrito";
	}	
	
	public double calculateItemPrice(CartItem ci) {
	    if (ci.getPlato() != null && ci.getPlato().getId() != 0) {	       
	        return (ci.getPlato().getPrecio_of() > 0 ? ci.getPlato().getPrecio_of() : ci.getPlato().getPrecio()) * ci.getCantidadCart();
	    } else if (ci.getMenu() != null && ci.getMenu().getId() != 0) {
	        return ci.getMenu().getPrecio() * ci.getCantidadCart();
	    }
	    return 0;  
	}
	
	// añadir >=2 menús para envíos semanales
	@PostMapping("/platos/cart/multiple")
	public String addCartMultiple(@RequestParam List<Integer> menuIds, 
	                               @RequestParam(value = "cantidad", defaultValue = "1") Integer cantidad, 
	                               @RequestParam("fecha") String fechaStr,	                               
	                               Model model,
	                               HttpSession session, 
	                               @AuthenticationPrincipal UserDetails userDetails) {	    
	    session.removeAttribute("detalles"); 
	    List<PlatoMenuPedido> detalles = new ArrayList<>();	
	    double precioEnvio = (double) model.getAttribute("precioEnvio");
	    
	    for (Integer id : menuIds) {
	        Optional<Menu> optMenu = smi.findById(id);
	        if (optMenu.isPresent()) {
	            Menu menu = optMenu.get();	            
	            PlatoMenuPedido platoMenuPedido = new PlatoMenuPedido();
	            platoMenuPedido.setMenu(menu);
	            platoMenuPedido.setPrecio_menu(menu.getPrecio());
	            platoMenuPedido.setTotalLinea(menu.getPrecio() * cantidad);
	            platoMenuPedido.setCantidad(cantidad);
	            detalles.add(platoMenuPedido); 
	        } else {	          
	            System.err.println("Menu no encontrado con ID: " + id);
	        }
	    }	   
	    Usuario u = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    if (u == null) {
	        model.addAttribute("carritoMsg", "Usuario no encontrado.");
	        return "public/carrito/carrito"; 
	    } 
 	    Cart cart = csi.findByUsuario(u).stream().findFirst().orElse(null);	    
	    if (cart == null) {
	        cart = new Cart();
	        cart.setUsuario(u);	 
	        try {	     
	        	 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");	            
	             fechaStr = fechaStr.trim();
	             LocalDate fecha = LocalDate.parse(fechaStr, formatter);	      
		         cart.setFechaPrimerEnvio(fecha);
		    } catch (DateTimeParseException e) {	      
		        e.printStackTrace();	       
		    }
	        cart = cr.save(cart);
	    }
	    for (PlatoMenuPedido pedido : detalles) {
	        CartItem cartItem = cir.findByCartAndMenu(cart, pedido.getMenu()).orElse(new CartItem());
	        cartItem.setMenu(pedido.getMenu());
	        Plato platoNull = spi.findById(0).get(); 
	        cartItem.setPlato(platoNull); 
	        cartItem.setCantidadCart(pedido.getCantidad());
	        cartItem.setCart(cart);
	        cartItem.setSemanal(true);
	        cir.save(cartItem);
	    }	   
	    double sumaTotal = cir.findByCart(cart).stream()
	            .mapToDouble(ci -> ci.getMenu().getPrecio() * ci.getCantidadCart())
	            .sum();	  
	    Integer numeroItemsCarrito = cir.numeroItemsCart(u.getId());
	    Pedido pedido = (Pedido) session.getAttribute("pedido");
	    if (pedido == null) {
	        pedido = new Pedido();
	    }
	    pedido.setTotal(sumaTotal);
	    boolean semanal=true;
	    session.setAttribute("semanal", semanal); 	   
	    session.setAttribute("detalles", detalles); 
	    session.setAttribute("pedido", pedido);		    
	    boolean isParcial = csi.isCartParcial(cart); 
        model.addAttribute("isParcial", isParcial);
	    model.addAttribute("cart", cir.findByCart(cart)); 
	    model.addAttribute("pedidoTotal", sumaTotal); 
	    model.addAttribute("numeroItemsCarrito", numeroItemsCarrito); 
	    model.addAttribute("total", sumaTotal + precioEnvio);	   
	    return "public/carrito/carrito";
	}
	
	@GetMapping("/platos/getCart")
	public String getCart(Model model, HttpSession session,
			@AuthenticationPrincipal UserDetails userDetails) {	
		double precioEnvio = (double) model.getAttribute("precioEnvio");
		Usuario u = sui.findByUsername(userDetails.getUsername()).get();
		Integer numeroItemsCarrito = cir.numeroItemsCart(u.getId());
		Cart cart = csi.findByUsuario(u).stream().findFirst().orElse(null);		
		List<CartItem> cartItems = cir.findByCart(cart);		
		double sumaTotal = cir.findByCart(cart).stream()
			    .mapToDouble(this::calculateItemPrice)  
			    .sum();		
		boolean isParcial = csi.isCartParcial(cart); 
        model.addAttribute("isParcial", isParcial);          
	    model.addAttribute("cart", cartItems); 
	    model.addAttribute("pedidoTotal", sumaTotal); 
	    model.addAttribute("usuario", u); 
	    model.addAttribute("titulo", "Carrito");
	    model.addAttribute("sesion", session.getAttribute("idusuario"));
	    model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
	    model.addAttribute("total", sumaTotal + precioEnvio);
		return "public/carrito/carrito";
	}
	
	@GetMapping("/platos/order")
	public String order(Model model, HttpSession session, 
			RedirectAttributes redirectAttributes,
			@AuthenticationPrincipal UserDetails userDetails) {	 
		double precioEnvio = model.getAttribute("precioEnvio") != null 
			    ? (double) model.getAttribute("precioEnvio") 
			    : 0.0;
	    Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    if (usuario == null) {
	    	redirectAttributes.addFlashAttribute("errorMessage", "Usuario no encontrado.");
	    }	   
	    Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);
	    List<CartItem> cartItems = cart != null ? cir.findByCart(cart) : Collections.emptyList();  	    
	    double sumaTotal = cir.findByCart(cart).stream()
	    	    .mapToDouble(this::calculateItemPrice)  
	    	    .sum(); 
    	Integer numeroItemsCarrito = model.getAttribute("numeroItemsCarrito") != null 
    		    ? (Integer) model.getAttribute("numeroItemsCarrito") 
    		    : 0;
	    boolean allItemsPlatos = cartItems.stream().allMatch(item -> item.getPlato().getId() != 0 && item.getMenu().getId() == 0);
	    boolean atLeastOneMenu = cartItems.stream().anyMatch(item -> item.getMenu().getId() != 0 && item.getPlato().getId() == 0);
 	    boolean isParcial = csi.isCartParcial(cart); 
        model.addAttribute("isParcial", isParcial);          
	    model.addAttribute("allItemsPlatos", allItemsPlatos);
	    model.addAttribute("atLeastOneMenu", atLeastOneMenu);	  
	    model.addAttribute("cart", cartItems);
	    model.addAttribute("pedido", new Pedido(sumaTotal)); 
	    model.addAttribute("usuario", usuario);
	    model.addAttribute("cartEmpty", false); 
	    model.addAttribute("fecha", LocalDate.now());
	    model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
	    model.addAttribute("currentStep", "contenido_carrito");  
	    model.addAttribute("total", sumaTotal + precioEnvio);
	    return "public/carrito/resumenorden";
	}		
	
	@GetMapping("/cart/direccion")
	public String direccion(Model model, HttpSession session, @AuthenticationPrincipal UserDetails userDetails) {
	    Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);	    
	    List<Pedido> pedidos = spdi.buscarUltimaDireccion(usuario.getId());
	    Optional<Pedido> ultimaDireccion = pedidos.stream().findFirst();	    
	    if (ultimaDireccion.isPresent()) {
	        Pedido p = ultimaDireccion.get();
	        if (p.isPordefecto()) {
	        	model.addAttribute("cliente", p.getUsuario().getNombre());
	            model.addAttribute("calle", p.getDireccion());
	            model.addAttribute("numero", p.getNum());
	            model.addAttribute("piso", p.getPiso());
	            model.addAttribute("cp", p.getCp());
	            model.addAttribute("localidad", p.getLocalidad());
	            model.addAttribute("pordefecto", true);
	        } else {
	            model.addAttribute("pordefecto", false);
	        }
	    } else {
	        model.addAttribute("pordefecto", false);
	    }
	    prepareCartDetails(model, userDetails);	    
	    model.addAttribute("currentStep", "entrega");
	    model.addAttribute("usuario", usuario);
	    return "public/carrito/formulario_direccion";
	}
	
	@PostMapping("/cart/pago")
	public String payment(
			@RequestParam(required = false) String cliente,
	    @RequestParam(required = false) String calle,
	    @RequestParam(required = false) String numero,
	    @RequestParam(required = false) String piso,
	    @RequestParam(required = false) String cp,
	    @RequestParam(required = false) String localidad,
	    @RequestParam(value = "pordefecto", defaultValue = "0") boolean pordefecto,
	    Model model, HttpSession session,
	    @AuthenticationPrincipal UserDetails userDetails) {  	    
	    Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);	    
	    if (cart != null) {
	        model.addAttribute("fechaPrimerEnvio", cart.getFechaPrimerEnvio());	       
	    } else {
	        model.addAttribute("fechaPrimerEnvio", null); 
	    }	 
	    if (calle.equals("") || numero.equals("") || cp.equals("") || localidad.equals("")) {
	        List<Pedido> pedidos = spdi.buscarUltimaDireccion(usuario.getId());
	        model.addAttribute("cliente", usuario.getNombre());
	        if (!pedidos.isEmpty()) {
	            Pedido p = pedidos.get(0);
	            if (p.isPordefecto()) {
	                calle = p.getDireccion();
	                numero = p.getNum();
	                piso = p.getPiso();
	                cp = p.getCp();
	                localidad = p.getLocalidad();
	                pordefecto = true;
	            }
	        }
	    }	  
	    session.setAttribute("cliente", cliente);
	    session.setAttribute("calle", calle);
	    session.setAttribute("numero", numero);
	    session.setAttribute("piso", piso);
	    session.setAttribute("cp", cp);
	    session.setAttribute("localidad", localidad);
	    session.setAttribute("pordefecto", pordefecto);	    
    	double sumaTotal = cir.findByCart(cart).stream()
            .mapToDouble(this::calculateItemPrice)  
            .sum();	       
    	Pedido p = new Pedido(sumaTotal);
    	model.addAttribute("pedidoTotal", p.getTotal());		        
	    prepareCartDetails(model, userDetails);
	    model.addAttribute("fecha_entrega", LocalDate.now().plusDays(2));
	    model.addAttribute("currentStep", "pago");
	    model.addAttribute("cliente", session.getAttribute("cliente"));
	    return "public/carrito/formulario_pago";
	}												
						
	@PostMapping("/cart/contrareembolso")
	@Transactional
	public String pagarContraReembolso(
	        @RequestParam("contra_r") String method,
	        @RequestParam("total") Double total,
	        @RequestParam(value = "fecha_entrega", required = false) String fechaEntregaString,  
	        HttpSession session,
	        @AuthenticationPrincipal UserDetails userDetails,
	        Model model) {	  
	    Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    if (usuario == null) {	        
	        return "redirect:/"; 
	    }	
	    List<PlatoMenuPedido> detalles = (List<PlatoMenuPedido>) session.getAttribute("detalles");	   
	    Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);
	    if (cart != null) {
	        List<CartItem> cartItems = cir.findByCart(cart);
	        if (cartItems != null && !cartItems.isEmpty()) {
	            detalles = new ArrayList<>();
	            for (CartItem ci : cartItems) {
	                PlatoMenuPedido pp = new PlatoMenuPedido();
	                if (ci.getPlato() != null && ci.getPlato().getId() != 0) {
	                    pp.setPlato(ci.getPlato());
	                    pp.setPrecio_plato(ci.getPlato().getPrecio_of() > 0 ? ci.getPlato().getPrecio_of() : ci.getPlato().getPrecio());

	                    Menu menuNull = smi.findById(0).get();
	                    pp.setMenu(menuNull); 
	                } else if (ci.getMenu() != null && ci.getMenu().getId() != 0) {
	                    pp.setMenu(ci.getMenu());
	                    pp.setPrecio_menu(ci.getMenu().getPrecio());
	                    Plato platoNull = spi.findById(0).get();
	                    pp.setPlato(platoNull); 
	                }
	                pp.setCantidad(ci.getCantidadCart());
	                pp.setTotalLinea(
	                	    (ci.getPlato() != null && ci.getPlato().getId() != 0 
	                	        ? (ci.getPlato().getPrecio_of() > 0 ? ci.getPlato().getPrecio_of() : ci.getPlato().getPrecio()) 
	                	        : ci.getMenu().getPrecio()) 
	                	    * ci.getCantidadCart()
	                	);
	                detalles.add(pp);
	            }
	        }
	    }	  
	    if (detalles == null || detalles.isEmpty()) {	      
	        return "redirect:/"; 
	    }
	    LocalDate fechaEntrega = null;
	    if (fechaEntregaString != null && !fechaEntregaString.isEmpty()) {
	        try {
	            fechaEntrega = LocalDate.parse(fechaEntregaString, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
	        } catch (DateTimeParseException e) {	           
	            return "redirect:/"; 
	        }
	    }
	    Pedido pedido = new Pedido();
	    pedido.setTotal(total);
	    pedido.setMethod(method); 
	    pedido.setUsuario(usuario);
	    pedido.setFechaCreacion(LocalDate.now());
	    pedido.setNumero(spdi.generarNumeroOrden());					
	    pedido.setFechaEntrega(fechaEntrega);  	
	    String calle = (String) session.getAttribute("calle");
	    String numero = (String) session.getAttribute("numero");
	    String piso = (String) session.getAttribute("piso");
	    String cp = (String) session.getAttribute("cp");
	    String localidad = (String) session.getAttribute("localidad");
	    boolean pordefecto = (boolean) session.getAttribute("pordefecto");
	    pedido.setDireccion(calle);
	    pedido.setNum(numero);
	    pedido.setPiso(piso); 
	    pedido.setCp(cp);
	    pedido.setLocalidad(localidad);
	    pedido.setPordefecto(pordefecto);	  
	    boolean semanal = (boolean) (csi.findByUsuario(usuario).get(0).getFechaPrimerEnvio() != null);
	    pedido.setEstadoEnum(semanal ? EstadoPedidoEnum.PARCIAL : EstadoPedidoEnum.PENDIENTE);
	    LocalDate fechaEnvio = csi.findByUsuario(usuario).get(0).getFechaPrimerEnvio();
	    pedido.setFechaPrimerEnvio(fechaEnvio);	  
	    spdi.save(pedido);	 
	    for (PlatoMenuPedido pp : detalles) {
	        pp.setPedido(pedido);
	        pp.setEstadoSemanal(semanal); 
	        sppi.save(pp);
	    }	   
	    if (cart != null) {
	        cir.deleteByCart(cart); 
	        cr.delete(cart); 
	    }	  
	    session.removeAttribute("detalles");
	    session.removeAttribute("calle");
	    session.removeAttribute("numero");
	    session.removeAttribute("piso");
	    session.removeAttribute("cp");
	    session.removeAttribute("localidad");
	    session.removeAttribute("semanal");	  
	    String subject = "Confirmación de Pedido - Contrarreembolso Pedido #" + pedido.getNumero();
	    String message = "Estimado/a " + usuario.getNombre() + ",\n\n" +
	                     "Tu pedido de " + pedido.getTotal() + " EUR ha sido registrado correctamente.\n" +
	                     "Número de pedido: " + pedido.getNumero() + "\n" +
	                     "Nos pondremos en contacto contigo al momento de la entrega para organizar el pago.\n\n" +
	                     "Saludos,\nAsturCat";
	    emailService.sendSimpleEmail(usuario.getEmail(), subject, message);
	    System.out.println("Confirmation email sent to: " + usuario.getEmail());
	    model.addAttribute("currentStep", "index");
	    model.addAttribute("numeroItemsCarrito", 0);
	    return "public/carrito/success";
	}					
						
	@GetMapping("/platos/delete/cart/{id}")
	public String deleteProductoCart(@PathVariable Integer id, 
	                                 @RequestParam("type") String type, 
	                                 Model model, 
	                                 @AuthenticationPrincipal UserDetails userDetails) {	   
	    Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
	    if (usuario == null) {
	        model.addAttribute("carritoMsg", "Usuario no encontrado.");
	        return "public/carrito/carrito"; 
	    }    
	    Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);
	    if (cart == null) {
	        model.addAttribute("carritoMsg", "Carrito no encontrado.");
	        return "public/carrito/carrito"; 
	    }   
	    CartItem cartItem = null;
	    if ("plato".equalsIgnoreCase(type)) {	      
	        Plato plato = spi.findById(id).orElse(null);
	        if (plato != null) {
	            cartItem = cir.findByCartAndPlato(cart, plato).orElse(null);
	        }
	    } else if ("menu".equalsIgnoreCase(type)) {	      
	        Menu menu = smi.findById(id).orElse(null);
	        if (menu != null) {
	            cartItem = cir.findByCartAndMenu(cart, menu).orElse(null);
	        }
	    }	   
	    if (cartItem != null) {
	        cir.delete(cartItem);
	    }
	    boolean hasItems = cir.findByCart(cart).size() > 0;
	    if (!hasItems) {	      
	        cr.delete(cart);
	    }	  
	    double sumaTotal = cir.findByCart(cart).stream()
	        .mapToDouble(ci -> {	          
	            if (ci.getPlato() != null && ci.getPlato().getId() != 0) {
	                return ci.getPlato().getPrecio() * ci.getCantidadCart();
	            } else if (ci.getMenu() != null && ci.getMenu().getId() != 0) {
	                return ci.getMenu().getPrecio() * ci.getCantidadCart();
	            } else {	               
	                return 0;
	            }
	        })
	        .sum();	   
	    model.addAttribute("cart", cir.findByCart(cart)); 
	    model.addAttribute("pedidoTotal", sumaTotal); 
	    return "redirect:/platos/getCart";
	}
					
	// pedidos de un cliente
	@GetMapping("/usuario/compras")
	public String obtenerCompras(Model model, HttpSession session, 
	                             @RequestParam(defaultValue = "0") int page, 
	                             @RequestParam(defaultValue = "10") int size) {
	    model.addAttribute("sesion", session.getAttribute("idusuario"));
	    Usuario usuario = sui.findById(Integer.parseInt(session.getAttribute("idusuario").toString())).orElse(null);
	    if (usuario == null) {	      
	        model.addAttribute("error", "Usuario no encontrado.");
	        return "public/usuario/compras"; 
	    }
	    Pageable pageable = PageRequest.of(page, size);
	    Page<Pedido> pedidosPage = spdi.findByUsuario(usuario, pageable);  
	    model.addAttribute("pedidos", pedidosPage.getContent());
	    model.addAttribute("currentPage", pedidosPage.getNumber());
	    model.addAttribute("totalPages", pedidosPage.getTotalPages());
	    model.addAttribute("totalItems", pedidosPage.getTotalElements());	 
	    model.addAttribute("hasItems", pedidosPage.hasContent());
	    return "public/usuario/compras";
	}	
		
	// detalles del pedido
	@GetMapping("/usuario/detalle/{id}")
	public String detalleCompra(@PathVariable Integer id, @RequestParam("page") int page, 
								HttpSession session, RedirectAttributes redirectAttributes, 
								Model model) {
		double precioEnvio = (double) model.getAttribute("precioEnvio");
	    Optional<Pedido> pedido = spdi.findById(id);
	    double detallePrecio = 0;
	    if (pedido.isPresent()) {	     
	        for (PlatoMenuPedido detalle : pedido.get().getLineas_pedido()) {
	            System.err.println("Detalle: " + detalle);
	            if (detalle.getMenu() != null && detalle.getMenu().getId() != 0) {
	                System.err.println("Menu: " + detalle.getMenu().getNombre() + ", Price: " + detalle.getPrecio_menu());
	                detallePrecio += detalle.getPrecio_menu() * detalle.getCantidad();
	            }
	            if (detalle.getPlato() != null && detalle.getPlato().getId() != 0) {
	                System.err.println("Plato: " + detalle.getPlato().getNombre() + ", Price: " + detalle.getPrecio_plato());
	                detallePrecio += detalle.getPrecio_plato() * detalle.getCantidad();
	            }
	        }	       
	        Integer idUsuario = (Integer) session.getAttribute("idusuario");
	        Usuario usuario = null;
	        if (idUsuario != null) {
	            usuario = sui.findById(idUsuario).orElse(null);
	        }	       
	        model.addAttribute("usuario", usuario);
	        model.addAttribute("pedido", pedido.get());
	        model.addAttribute("detalles", pedido.get().getLineas_pedido());
	        model.addAttribute("detallePrecio", detallePrecio);	  
	        model.addAttribute("precioEnvio", precioEnvio);
	        model.addAttribute("sesion", idUsuario);	     
	        LocalDate fechaCreacion = pedido.get().getFechaCreacion();
	        LocalDate fechaEntrega = pedido.get().getFechaEntrega();
	        LocalDate fechaEnvio = pedido.get().getFechaEnvio();
	        LocalDate fechaPrimerEnvio = pedido.get().getFechaPrimerEnvio();
	        LocalDate fechaEnvioPrevista = null;	       
	        if (pedido.get().getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {	           
	            model.addAttribute("fechaEnvioDesde", fechaPrimerEnvio);	          
	            List<PlatoMenuPedido> lineasPedido = pedido.get().getLineas_pedido();
	            int numberOfWeeks = lineasPedido.size(); 	          
	            List<LocalDate> dispatchDates = new ArrayList<>();
	            for (int i = 0; i < numberOfWeeks; i++) {
	                dispatchDates.add(fechaPrimerEnvio.plusWeeks(i)); 
	            }	          
	            model.addAttribute("dispatchDates", dispatchDates);
	        } else {	          
	            if (fechaEntrega != null) {
	                fechaEnvioPrevista = fechaEntrega;
	            } else {	               
	                if (fechaCreacion.getDayOfWeek() == DayOfWeek.FRIDAY) {
	                    fechaEnvioPrevista = fechaCreacion.plusDays(3); 
	                } else {
	                    fechaEnvioPrevista = fechaCreacion.plusDays(2); 
	                }
	            }	           
	            model.addAttribute("fechaEnvioPrevista", fechaEnvioPrevista);
	            model.addAttribute("fechaEnvio", fechaEnvio);
	        }
	    } else {
	    	redirectAttributes.addFlashAttribute("errorMessage", "Pedido no encontrado.");
	        return "redirect:/usuario/compras";
	    }	    
	    model.addAttribute("page", page);
	    return "public/usuario/detallecompra";
	}					
		
	// cancelar pedido 
	@PostMapping("/usuario/cancelar/{id}")
	public String cancelarPedido(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Pedido pedidoACancelar = spdi.findById(id).orElse(null);
		if (pedidoACancelar != null) {
			pedidoACancelar.setEstadoEnum(EstadoPedidoEnum.CANCELADO);
			spdi.save(pedidoACancelar);
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Pedido no encontrado.");
	    }
		return "redirect:/usuario/compras";
	}
	
	// generar factura de un pedido finalizado
	@GetMapping("/usuario/generar-pdf/{id}")
	public void generarPDF(@PathVariable Integer id, HttpServletResponse response) throws IOException {
	    response.setContentType("application/pdf");	   
	    DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	    String currentDateTime = dateFormatter.format(new Date());
	    String headerKey = "Content-Disposition";
	    String headerValue = "attachment; filename=factura_" + currentDateTime + ".pdf";
	    response.setHeader(headerKey, headerValue);	   
	    Optional<Pedido> pedido = spdi.findById(id); 
	    if (pedido.isPresent()) {			        
	        List<PlatoMenuPedido> detalles = pedido.get().getLineas_pedido();
	        double detallePrecio = 0;
	        for (PlatoMenuPedido detalle : detalles) {
	            if (detalle.getMenu() != null && detalle.getMenu().getId() != 0) {
	                detallePrecio += detalle.getPrecio_menu() * detalle.getCantidad();
	            }
	            if (detalle.getPlato() != null && detalle.getPlato().getId() != 0) {
	                detallePrecio += detalle.getPrecio_plato() * detalle.getCantidad();
	            }
	        }
	        String nombreCliente = pedido.get().getUsuario().getNombre();
	        String localidad = pedido.get().getLocalidad();
	        String direccion = pedido.get().getDireccion();
	        String num = pedido.get().getNum();
	        String piso = pedido.get().getPiso();
	        String cp = pedido.get().getCp();
	        String clientInfo = nombreCliente+"\n"+direccion+" "+num+" "+piso+"\n"+cp+" "+localidad+"\n Asturias, España\n\n"; 
	        String numeroFactura = pedido.get().getNumeroFactura();
	        if (numeroFactura == null) {
	        	numeroFactura = generarNumeroFactura();
	        	pedido.get().setNumeroFactura(numeroFactura);
	        	spdi.save(pedido.get());
	        }	       
	        LocalDate issueDate = LocalDate.now(); 
	        LocalDate dueDate = issueDate.plusWeeks(2); 	       
	        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	        String formattedIssueDate = issueDate.format(formatter);
	        String formattedDueDate = dueDate.format(formatter);
	        double precioEnvio = ar.findById(1).orElseThrow(() -> new IllegalArgumentException("Ajuste ID no válido.")).getPrecioEnvioZona1();
	        pdfGeneratorService.generatePdf(
	            response, 
	            detalles, 
	            clientInfo, 
	            numeroFactura, 
	            formattedIssueDate, 
	            formattedDueDate, 
	            detallePrecio,
	            precioEnvio
	        );
	    } else {	      
	        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Pedido no encontrado");
	    }
	}	
	
	public String generarNumeroFactura() {
	    LocalDateTime now = LocalDateTime.now();
	    String timestamp = now.format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")); 
	    return "INV-" + timestamp;
	}

	@GetMapping("/cliente/perfil")
	public String clientePerfil(Model model, HttpSession session,
	        @AuthenticationPrincipal UserDetails userDetails) {	   
	    Integer userId = (Integer) session.getAttribute("idusuario");
	    Usuario usuario = sui.findById(userId).orElse(null);
	    if (usuario == null) {	     
	        return "redirect:/login"; 
	    }	  
	    model.addAttribute("id", usuario.getId());
	    model.addAttribute("username", usuario.getUsername());
	    model.addAttribute("nombre", usuario.getNombre());
	    model.addAttribute("email", usuario.getEmail());
	    model.addAttribute("localidad", usuario.getLocalidad());
	    return "public/usuario/cliente_perfil";
	}
					
	@GetMapping("/modificar/{id}")
	public String mostrarModificarPerfil(@PathVariable Integer id, 
			RedirectAttributes redirectAttributes, Model model) {
	    Usuario usuario = new Usuario(); 
	    try {
	        usuario = sui.findById(id)
	                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
	    } catch (EntityNotFoundException ex) {
	    	redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());	       
	        return "redirect:/"; 
	    }
	    model.addAttribute("modificado", usuario); 
	    return "public/usuario/modificar_perfil"; 
	}	
	
	@PostMapping("/modificar/submit")
	public String modificarPerfil(
	        @ModelAttribute("modificado") Usuario usuario,
	        RedirectAttributes redirectAttributes,
	        @RequestParam(value = "newPassword", required = false) String newPassword,
	        @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
	        Model model, HttpSession session) {	   
	    if (newPassword != null && !newPassword.isEmpty()) {
	        if (!newPassword.equals(confirmPassword)) {
	            model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
	            return "public/modificar_perfil"; 
	        }
	        String encryptedPassword = passwordEncoder.encode(newPassword);
	        usuario.setPassword(encryptedPassword);
	    }	  
	    Usuario existingUser = sui.findById(usuario.getId())
	            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
	    boolean usernameChanged = !existingUser.getUsername().equals(usuario.getUsername());
	    boolean emailChanged = !existingUser.getEmail().equals(usuario.getEmail());	  
	    if (usernameChanged && sui.existsByUsername(usuario.getUsername())) {
	        model.addAttribute("errorMessage", "El nombre de usuario ya existe. Por favor, elija otro.");
	        return "public/usuario/modificar_perfil"; 
	    }	  
	    if (emailChanged && sui.existsByEmail(usuario.getEmail())) {
	        model.addAttribute("errorMessage", "El correo electrónico ya existe. Por favor, elija otro.");
	        return "public/usuario/modificar_perfil"; 
	    }	  
	    existingUser.setNombre(usuario.getNombre());
	    existingUser.setLocalidad(usuario.getLocalidad());
	    if (usernameChanged) {
	        existingUser.setUsername(usuario.getUsername());
	    }
	    if (emailChanged) {
	        existingUser.setEmail(usuario.getEmail());
	    }
	    if (newPassword != null && !newPassword.isEmpty()) {
	        existingUser.setPassword(usuario.getPassword());
	    }	  
	    sui.save(existingUser);
	    if (usernameChanged) {
	        session.invalidate();
	        redirectAttributes.addFlashAttribute("message", "Nombre de usuario cambiado exitosamente. Por favor, inicie sesión con su nuevo nombre de usuario.");
	        return "redirect:/login";
	    }
	    return "redirect:/cliente/perfil";
	}
	
	@GetMapping("/cerrar")
	public String cerrarSesion(HttpSession session, Principal principal) {	   
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null && authentication.isAuthenticated()) {	     
	        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
	        boolean isAdmin = authorities.stream()
	                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
	        boolean isGestor = authorities.stream()
	                .anyMatch(auth -> auth.getAuthority().equals("ROLE_GESTOR"));
	        boolean isEditor = authorities.stream()
	                .anyMatch(auth -> auth.getAuthority().equals("ROLE_EDITOR"));
	        boolean isUser = authorities.stream()
	                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER"));	       
	        if (session != null) {	           
	            session.invalidate();	           
	        } else {
	            System.err.println("No existe sesión.");
	        }
	        if (isAdmin || isGestor || isEditor) {
	            return "redirect:/login"; 
	        } else if (isUser) {
	            return "redirect:/"; 
	        }
	    }	    
	    if (session != null) {
	        session.invalidate();
	    }
	    return "redirect:/login"; 
	}
      
	// método para recuperar usuario y carrito y calcular el precio total
    private void prepareCartDetails(Model model, UserDetails userDetails) {
        Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
        Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);     
        double sumaTotal = cir.findByCart(cart).stream()
            .mapToDouble(ci -> {
                if (ci.getPlato() != null && ci.getPlato().getId() != 0) {
                    return ci.getPlato().getPrecio() * ci.getCantidadCart();
                } else if (ci.getMenu() != null && ci.getMenu().getId() != 0) {
                    return ci.getMenu().getPrecio() * ci.getCantidadCart();
                } else {
                    return 0;
                }
            }).sum(); 
        Integer numeroItemsCarrito = (Integer) model.getAttribute("numeroItemsCarrito");  
        model.addAttribute("numeroItemsCarrito", numeroItemsCarrito);
        model.addAttribute("pedido", new Pedido(sumaTotal));
        model.addAttribute("cartEmpty", false);
    }
			

}
