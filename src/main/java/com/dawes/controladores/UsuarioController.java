package com.dawes.controladores;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.exception.ResourceNotFoundException;
import com.dawes.modelo.Ingrediente;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.Rol;
import com.dawes.modelo.Usuario;
import com.dawes.modelo.UsuarioDTO;
import com.dawes.modelo.UsuarioRol;
import com.dawes.repositorio.UsuarioRolRepository;
import com.dawes.serviciosImpl.ServicioPedidoImpl;
import com.dawes.serviciosImpl.ServicioRolImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/usuario")
public class UsuarioController {
	@Autowired
	private ServicioUsuarioImpl sui;
	@Autowired
	private ServicioPedidoImpl spdi;
	@Autowired
	private ServicioRolImpl sri;
	@Autowired
	private UsuarioRolRepository urr;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@ModelAttribute
    public void addTituloAttribute(HttpServletRequest request, Model model) {
        String path = request.getRequestURI();
        if (path.contains("/not-role-user")) {
            model.addAttribute("titulo", "Usuarios");
        } else if (path.contains("/role-user")) {
            model.addAttribute("titulo", "Clientes");
        }
	}	
	
	@GetMapping("")
    public String listUsuarios(Model model, Pageable pageable) {
        Page<Usuario> usuariosPage = sui.findAll(pageable);
        model.addAttribute("usuariosPage", usuariosPage);
        model.addAttribute("currentPage", "usuarios");
        return "admin/usuarios/listado-usuarios";
    }
	
	// listado de clientes
	@GetMapping("/role-user")
	public String listUsuariosRoleUser(@RequestParam(name = "pageUsuarios", defaultValue = "1") int pageUsuarios, 
            Model model) {		
	    List<Usuario> usuariosWithOnlyRoleUser = sui.findUsersWithOnlyRoleUser(); 	    
	    int pageSize = 8; 
	    int currentPage = pageUsuarios - 1; // base 0
	    int startItem = currentPage * pageSize;
	    List<Usuario> usuariosPageList;
	    if (usuariosWithOnlyRoleUser.size() < startItem) {
	        usuariosPageList = Collections.emptyList();
	    } else {
	        int toIndex = Math.min(startItem + pageSize, usuariosWithOnlyRoleUser.size());
	        usuariosPageList = usuariosWithOnlyRoleUser.subList(startItem, toIndex);
	    }
	    Page<Usuario> usuariosPage = new PageImpl<>(usuariosPageList, PageRequest.of(currentPage, pageSize), usuariosWithOnlyRoleUser.size());
	    model.addAttribute("usuariosPage", usuariosPage);
	    model.addAttribute("currentPage", "role-user");
	    model.addAttribute("pageUsuarios", pageUsuarios);
	    System.err.println("pageUsuarios: " + pageUsuarios);
	    model.addAttribute("showRoleColumn", false);
	    model.addAttribute("isClientList", true);	 
	    return "admin/usuarios/listado-usuarios";
	}	
	
	// listado de admins
	@GetMapping("/not-role-user")
	public String listUsuariosNotRoleUser(Model model, Pageable pageable) {
	    List<Usuario> usuariosWithoutRoleUser = sui.findUsersWithOtherRoles();	  
	    int pageSize = pageable.getPageSize();
	    int currentPage = pageable.getPageNumber();
	    int startItem = currentPage * pageSize;
	    List<Usuario> usuariosPageList;
	    if (usuariosWithoutRoleUser.size() < startItem) {
	        usuariosPageList = Collections.emptyList();
	    } else {
	        int toIndex = Math.min(startItem + pageSize, usuariosWithoutRoleUser.size());
	        usuariosPageList = usuariosWithoutRoleUser.subList(startItem, toIndex);
	    }  
	    Page<Usuario> usuariosPage = new PageImpl<>(usuariosPageList, PageRequest.of(currentPage, pageSize), usuariosWithoutRoleUser.size());
	    for (Usuario usuario : usuariosPage.getContent()) {	      
	        List<UsuarioRol> roles = usuario.getRoles();  	       
	        Set<String> roleNames = new HashSet<>();
	        for (UsuarioRol usuarioRol : roles) {
	        	String roleName = usuarioRol.getRol().getRoleName();
	        	if (roleName.startsWith("ROLE_")) {
	                roleName = roleName.substring(5); // quitar el prefijo ("ROLE_")
	            }
	            roleNames.add(roleName); 
	        }  
	    }
	    model.addAttribute("usuariosPage", usuariosPage);
	    model.addAttribute("currentPage", "not-role-user");
	    model.addAttribute("showRoleColumn", true);  
	    return "admin/usuarios/listado-usuarios";
	}
	
	@GetMapping("/nuevo")
	public String altaUsuario(Model model) {
	     UsuarioDTO usuarioDTO = new UsuarioDTO(); 
	     model.addAttribute("usuarioDTO", usuarioDTO); 	    
	     List<Rol> roles = sri.findByRoleNames(List.of("ROLE_ADMIN", "ROLE_GESTOR", "ROLE_EDITOR")); 
	     System.err.println("ROLES: " + roles);
	     model.addAttribute("roles", roles);	     
	     return "admin/usuarios/formulario_alta_usuario";
	}	
    
 	@PostMapping("/nuevoUsuarioSubmit")
    public String submitUsuario(@ModelAttribute("usuarioDTO") UsuarioDTO usuarioDTO, 
    		RedirectAttributes redirectAttributes, Model model) {
        Usuario usuario = new Usuario();
        String encodedPassword = passwordEncoder.encode(usuarioDTO.getPassword());
        boolean existeEmail = sui.findByEmail(usuarioDTO.getEmail()).isPresent();
        boolean existeUsername = sui.findByUsername(usuarioDTO.getUsername()).isPresent();
        if (existeEmail) {  
            redirectAttributes.addFlashAttribute("warningMessage", "El email ya está registrado en la base de datos.");
            return "redirect:/admin/usuario/nuevo"; 
        }	
        if (existeUsername) {  
            redirectAttributes.addFlashAttribute("warningMessage", "El nombre de usuario ya está registrado en la base de datos.");
            return "redirect:/admin/usuario/nuevo"; 
        }	
        usuario.setNombre(usuarioDTO.getNombre());
        usuario.setUsername(usuarioDTO.getUsername());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setLocalidad(usuarioDTO.getDireccion());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(encodedPassword);
        usuario.setEnabled(true);
        usuario.setAccountNoExpired(true);
        usuario.setAccountNoLocked(true);
        usuario.setCredentialsNoExpired(true);       
        List<UsuarioRol> roles = new ArrayList<>();
        for (Integer roleId : usuarioDTO.getSelectedRoleIds()) {
            Rol rol = sri.findById(roleId).orElse(null); 
            if (rol != null) {
                roles.add(new UsuarioRol(usuario, rol)); 
            }
        }
        usuario.setRoles(roles); 
        sui.save(usuario);        
        return "redirect:/admin/usuario/not-role-user"; 
    }	
 	
 	// Pedidos de un cliente 	
 	@GetMapping("/pedidosUsuario/{id}")
 	public String pedidosUsuario(@PathVariable("id") Integer id, Model model,
 	                             @RequestParam(defaultValue = "0") int page, 
 	                             @RequestParam(defaultValue = "10") int size, 
 	                             @RequestParam(defaultValue = "1") int pageUsuarios) {	
 	    Optional<Usuario> optUsuario = sui.findById(id);
 	    if (!optUsuario.isPresent()) {
 	        return "redirect:/admin/usuario/role-user";
 	    }
 	    Usuario u = optUsuario.get();	   
 	    Pageable pageable = PageRequest.of(page, size);	   
 	    Page<Pedido> pedidosPage = spdi.findByUsuario(u, pageable);
 	    model.addAttribute("pedidos", pedidosPage.getContent());
 	    model.addAttribute("currentPage", pedidosPage.getNumber());
 	    model.addAttribute("totalPages", pedidosPage.getTotalPages());
 	    model.addAttribute("totalItems", pedidosPage.getTotalElements());
 	    model.addAttribute("hasItems", pedidosPage.hasContent());
 	    model.addAttribute("page", page + 1); 
 	    model.addAttribute("pageUsuarios", pageUsuarios); 
 	    model.addAttribute("cliente", u.getNombre()); 
 	    return "admin/usuarios/usuario_pedidos";
 	}
 	
	// Editar usuarios con ROLE_USER
    @GetMapping("/edit/cliente/{id}")
    public String editCliente(@PathVariable Integer id, Model model) { 
    	Optional<Usuario> optUsuario = sui.findById(id);
    	if (!optUsuario.isPresent()) {         
            return "redirect:/admin/usuario/role-user";
        }
        Usuario usuario = optUsuario.get();      
        if (!usuario.hasRole("ROLE_USER")) {
            return "redirect:/admin/usuario/role-user"; 
        }        
        model.addAttribute("usuario", usuario);
        return "admin/usuarios/edit-cliente"; 
    }    
    
    @PostMapping("/update/cliente/{id}")
    public String updateCliente(@PathVariable Integer id, 
                                 @RequestParam String nombre, 
                                 @RequestParam String username,                                 
                                 @RequestParam String email, 
                                 @RequestParam(required = false) String direccion, 
                                 Model model) {     
        Usuario usuario = sui.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado"));   
        boolean usernameChanged = !usuario.getUsername().equals(username);
        boolean emailChanged = !usuario.getEmail().equals(email);  
        
        if (usernameChanged && sui.existsByUsername(username)) {
            model.addAttribute("errorMessage", "El nombre de usuario ya existe. Por favor, elija otro.");
            model.addAttribute("usuario", usuario); 
            return "admin/usuarios/edit-cliente"; 
        }
        if (emailChanged && sui.existsByEmail(email)) {
            model.addAttribute("errorMessage", "El correo electrónico ya existe. Por favor, elija otro.");
            model.addAttribute("usuario", usuario); 
            return "admin/usuarios/edit-cliente"; 
        }
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setLocalidad(direccion);        
        // usuario.setEnabled(enabled);       
        sui.save(usuario);       
        return "redirect:/admin/usuario/role-user"; 
    }   
    
    // Editar usuarios con ROLE_ADMIN, GESTOR, EDITOR
    @GetMapping("/edit/usuario/{id}")
    public String editUsuario(@PathVariable Integer id, Model model) {        
        Optional<Usuario> optUsuario = sui.findById(id);
        if (!optUsuario.isPresent()) {           
            return "redirect:/admin/usuario/not-role-user";
        }      
        Usuario usuario = optUsuario.get();
        if (usuario.hasRole("ROLE_USER")) {
            return "redirect:/admin/usuario/not-role-user"; 
        }
        List<UsuarioRol> usuarioRoles = sri.buscarRolesDeUsuario(usuario.getUsername());     
        List<Integer> selectedRoleIds = usuarioRoles.stream()
                .map(UsuarioRol::getRol) 
                .map(Rol::getId) 
                .collect(Collectors.toList());       
        List<Rol> allRoles = sri.findAll();        
        allRoles.removeIf(role -> "ROLE_USER".equals(role.getRoleName()) || "ROLE_ANONYMOUS".equals(role.getRoleName())); 
        for (Rol role : allRoles) {
            role.setRoleName(role.getRoleName().replace("ROLE_", "")); 
        }
        model.addAttribute("usuario", usuario);
        model.addAttribute("roles", allRoles);
        model.addAttribute("selectedRoleIds", selectedRoleIds);
        return "admin/usuarios/edit-usuario"; 
    }
    
    @PostMapping("/update/usuario/{id}")
    public String updateUsuario(@PathVariable Integer id, 
                                @RequestParam String nombre, 
                                @RequestParam String username,                                   
                                @RequestParam String email, 
                                @RequestParam(required = false) String direccion, 
                                @RequestParam List<Integer> roles, 
                                Model model) {        
        Usuario usuario = sui.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        boolean usernameChanged = !usuario.getUsername().equals(username);
        boolean emailChanged = !usuario.getEmail().equals(email);  
        if (usernameChanged && sui.existsByUsername(username)) {
            model.addAttribute("errorMessage", "El nombre de usuario ya existe. Por favor, elija otro.");
            model.addAttribute("usuario", usuario); 
            model.addAttribute("roles", sri.findAll()); 
            model.addAttribute("selectedRoleIds", roles); 
            return "admin/usuarios/edit-usuario"; 
        }
        if (emailChanged && sui.existsByEmail(email)) {
            model.addAttribute("errorMessage", "El correo electrónico ya existe. Por favor, elija otro.");
            model.addAttribute("usuario", usuario); 
            model.addAttribute("roles", sri.findAll()); 
            model.addAttribute("selectedRoleIds", roles); 
            return "admin/usuarios/edit-usuario"; 
        }
        usuario.setNombre(nombre);
        usuario.setUsername(username);
        usuario.setEmail(email);
        usuario.setLocalidad(direccion);
        urr.deleteByUsuarioId(id);  
        for (Integer roleId : roles) {
            Rol rol = sri.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
            UsuarioRol newUsuarioRol = new UsuarioRol(usuario, rol);
            usuario.getRoles().add(newUsuarioRol); 
        }
        sui.save(usuario);
        return "redirect:/admin/usuario/not-role-user"; 
    }  

    //  ROLE_USER
    @GetMapping("/delete/cliente/{id}")
    public String deleteCliente(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> optionalUsuario = sui.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();           
            if (usuario.hasRole("ROLE_USER")) {
                try {
                    sui.deleteById(id); 
                    redirectAttributes.addFlashAttribute("message", "Cliente eliminado exitosamente.");
                } catch (Exception e) {
                	redirectAttributes.addFlashAttribute("error", "Error al eliminar el cliente: este cliente tiene pedidos asicoados.");
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario no es un cliente.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
        }
        return "redirect:/admin/usuario/role-user"; 
    }

    // sin ROLE_USER (admins, gestores, editores)
    @GetMapping("/delete/usuario/{id}")
    public String deleteUsuario(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Optional<Usuario> optionalUsuario = sui.findById(id);
        if (optionalUsuario.isPresent()) {
            Usuario usuario = optionalUsuario.get();           
            if (!usuario.hasRole("ROLE_USER")) {
                try {
                    sui.deleteById(id); 
                    redirectAttributes.addFlashAttribute("message", "Usuario eliminado exitosamente.");
                } catch (Exception e) {
                    redirectAttributes.addFlashAttribute("error", "Error al eliminar el usuario: " + e.getMessage());
                }
            } else {
                redirectAttributes.addFlashAttribute("error", "El usuario es un cliente y no puede ser eliminado aquí.");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Usuario no encontrado.");
        }

        return "redirect:/admin/usuario/not-role-user"; 
    }   
	
	// ROLE_USER
	@GetMapping("/download-csv/role-user")
	public void downloadCsvForRoleUser(HttpServletResponse response) {
	    List<Usuario> usuariosWithOnlyRoleUser = sui.findUsersWithOnlyRoleUser(); 
	    writeUsersCsv(usuariosWithOnlyRoleUser, response, "clientes.csv");
	}

	// sin ROLE_USER
	@GetMapping("/download-csv/not-role-user")
	public void downloadCsvForNotRoleUser(HttpServletResponse response) {
	    List<Usuario> usuariosWithoutRoleUser = sui.findUsersWithOtherRoles(); 	  
	    writeUsersCsv(usuariosWithoutRoleUser, response, "usuarios.csv");
	}
	
	public void writeUsersCsv(List<Usuario> usuarios, HttpServletResponse response, String filename) {
	    response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment; filename=" + filename);
	    try (PrintWriter writer = response.getWriter()) {	       
	        writer.println("Id;Nombre;Dirección;Email;Username");	       
	        for (Usuario usuario : usuarios) {
	            writer.printf("%d;%s;%s;%s;%s%n", 
	                        usuario.getId(), 
	                        usuario.getNombre(), 
	                        usuario.getLocalidad(), 
	                        usuario.getEmail(), 
	                        usuario.getUsername());
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}  

    
}
