package com.dawes.controladores;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.modelo.Categoria;
import com.dawes.modelo.Rol;
import com.dawes.modelo.Usuario;
import com.dawes.modelo.UsuarioRol;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.ServicioMenuImpl;
import com.dawes.serviciosImpl.ServicioPedidoImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;
import com.dawes.serviciosImpl.ServicioRolImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class SeguridadController {	
	@Autowired
	private ServicioUsuarioImpl us; 	
	@Autowired
    private ServicioRolImpl rs;		
	@Autowired
	private ServicioPedidoImpl spdi;
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioMenuImpl smi;
	@Autowired
	private ServicioUsuarioImpl sui;
	@Autowired
	private ServicioCategoriaImpl sci;
	@Autowired
	private BCryptPasswordEncoder passwordEncoder; 	
	@ModelAttribute("categorias")
	public List<Categoria> todasCategorias() {
		return sci.findAll();
	}	
	 
	@RequestMapping("/admin")
	public String admin(@AuthenticationPrincipal UserDetails userDetails, Model model) {
	     String username = userDetails.getUsername();
	     List<UsuarioRol> userRoles = rs.buscarRolesDeUsuario(username);	
	     List<String> roles = userRoles.stream()
             .map(ur -> ur.getRol().getRoleName().replace("ROLE_", ""))
             .collect(Collectors.toList());
	     // solo usuarios con "ROLE_USER"
	     long numUsuarios = us.findAll().stream()
             .filter(user -> user.getRoles().stream()
                 .anyMatch(ur -> "ROLE_USER".equals(ur.getRol().getRoleName())))
             .count();
	     model.addAttribute("username", username);
	     model.addAttribute("roles", roles); 
	     model.addAttribute("numPedidos", spdi.findAll().size());
	     model.addAttribute("numUsuarios", numUsuarios);
	     model.addAttribute("numPlatos", spi.findAll().size()-1);
	     model.addAttribute("numMenus", smi.findAll().size()-1);
	     model.addAttribute("titulo", "Gestión de contenidos");
	     return "admin/home";
	 }

	 @RequestMapping("/registrado")
	 public String registrado(@AuthenticationPrincipal UserDetails userDetails, Model model) {		
	     String username = userDetails.getUsername();
	     List<UsuarioRol> userRoles = rs.buscarRolesDeUsuario(username);	  
	     List<String> roles = userRoles.stream()
	                                    .map(ur -> ur.getRol().getRoleName())
	                                    .collect(Collectors.toList());	     
	     model.addAttribute("username", username);
	     model.addAttribute("roles", roles);   
	     return "redirect:/platos";  
	 }
	
	@RequestMapping("/login")
	public String login(Model model) {
		model.addAttribute("usuario", new Usuario());
		return "login";
	}	
	
	@GetMapping("/registrarse")
    public String formularioRegistrarse() {	    	
        return "registrarse";
    }	
	
	@PostMapping("/registrarse")
	public String procesarRegistro(@RequestParam("username") String username, 
	                               @RequestParam("password") String password,
	                               @RequestParam("confirm_password") String confirm_password,
	                               @RequestParam("us_nombre") String nombre,
	                               @RequestParam("email") String email,
	                               @RequestParam("localidad") String localidad,
	                               RedirectAttributes redirectAttributes) {    
	    boolean existeEmail = us.findByEmail(email).isPresent(); 
	    boolean existeUsername = us.findByUsername(username).isPresent(); 	    
	    Usuario newUser = new Usuario(); 
	    Rol rol = rs.findByRoleName("ROLE_USER");
	    String encodedPassword = passwordEncoder.encode(password); 
	    if (!existeUsername) {  
	        if (existeEmail) {  
	            redirectAttributes.addFlashAttribute("warningMessage", "El email ya está registrado. Si no recuerda su nombre de usuario y/o contraseña, haga click en el enlace ¿Olvidó contraseña?");
	            return "redirect:/registrarse"; 
	        } else {  
	            newUser.setUsername(username);
	            newUser.setPassword(encodedPassword);
	            newUser.setNombre(nombre);
	            newUser.setEnabled(true);
	            newUser.setEmail(email);
	            newUser.setLocalidad(localidad);    
	            UsuarioRol urvo = new UsuarioRol(newUser, rol);
	            newUser.getRoles().add(urvo);
	            rol.getUsuarios().add(urvo);  
	            us.registrarUsuario(newUser); 	           
	            redirectAttributes.addFlashAttribute("message", "Registro exitoso. Ya puedes iniciar sesión.");
	            return "redirect:/login";  
	        }
	    } else {  
	        redirectAttributes.addFlashAttribute("errorMessage", "Este nombre de usuario ya existe en la base de datos. Por favor, elija otro.");
	        return "redirect:/registrarse";  
	    }
	}
	
	@RequestMapping("/redirectByRole")
	public String redirectByRole(HttpServletRequest request, HttpSession session) {
	    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
	    if (auth != null && auth.getPrincipal() instanceof UserDetails) {
	        UserDetails userDetails = (UserDetails) auth.getPrincipal();
	        String username = userDetails.getUsername();	       
	        Optional<Usuario> user = us.findByUsername(username); 
	        if (user.isPresent()) {
	            Usuario usuarioDb = user.get();	           
	            session.setAttribute("idusuario", usuarioDb.getId());
	        } else {
	            System.err.println("Usuario no existe");
	            return "redirect:/login"; 
	        }	     
	        for (GrantedAuthority authority : auth.getAuthorities()) {
	            if (authority.getAuthority().equals("ROLE_ADMIN")) {
	                return "redirect:/admin";  
	            } else if (authority.getAuthority().equals("ROLE_USER")) {
	                return "redirect:/registrado";  
	            } else if (authority.getAuthority().equals("ROLE_GESTOR") || authority.getAuthority().equals("ROLE_EDITOR")) {
	                return "redirect:/admin";  
	            }
	        }
	    }	  
	    return "redirect:/";
	}
	
	// modificar detalles desde el perfil de usuarios (admin, gestor, editor)
	@GetMapping("/admin/editarPerfil")
	public String editarPerfil(Model model, HttpSession session,
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
		return "admin/usuarios/usuario_perfil";
	}
	
	@GetMapping("/modificarUsuario/{id}")
	public String modificarUsuarioPerfil(@PathVariable Integer id, Model model) {
	    Usuario usuario;
	    try {
	        usuario = sui.findById(id)
	                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
  	        usuario.setPassword(null); 
	    } catch (EntityNotFoundException ex) {
	        model.addAttribute("errorMessage", ex.getMessage());
	        return "admin/error"; 
	    }
	    model.addAttribute("modificado", usuario); 
	    return "admin/usuarios/modificar_perfil_usuario"; 
	}
	
	@PostMapping("/modificarUsuario/submit")
	public String modificarPerfil(@ModelAttribute("modificado") Usuario usuario,
            RedirectAttributes redirectAttributes,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            Model model, HttpSession session) {		
		if (newPassword != null && !newPassword.isEmpty()) {		  
		    if (!newPassword.equals(confirmPassword)) {
		        model.addAttribute("errorMessage", "Las contraseñas no coinciden.");
		        return "admin/usuarios/modificar_perfil_usuario"; 
		    }
		    String encryptedPassword = passwordEncoder.encode(newPassword);
		    usuario.setPassword(encryptedPassword); 
		}
		  Usuario existingUser = sui.findById(usuario.getId())
		            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));		    
		    boolean usernameChanged = !existingUser.getUsername().equals(usuario.getUsername());
		    boolean emailChanged = !existingUser.getEmail().equals(usuario.getEmail());		   
		    existingUser.setNombre(usuario.getNombre());
		    existingUser.setEmail(usuario.getEmail());
		    existingUser.setLocalidad(usuario.getLocalidad());  
		    if (usernameChanged) {		       
		        if (sui.existsByUsername(usuario.getUsername())) {
		        	model.addAttribute("errorMessage", "El nombre de usuario ya existe. Por favor, elija otro.");
		            return "admin/usuarios/modificar_perfil_usuario"; 
		        }
		        if (sui.existsByUsernameAndPassword(usuario.getUsername(), newPassword)) {
		            model.addAttribute("errorMessage", "El nombre de usuario y la contraseña ya existen."); 
		            return "admin/usuarios/modificar_perfil_usuario"; 
		        }
		        existingUser.setUsername(usuario.getUsername());
		    }		    
		    if (emailChanged) {		       
		        if (sui.existsByEmail(usuario.getEmail())) {
		        	model.addAttribute("errorMessage", "El correo electrónico ya existe. Por favor, elija otro.");
		            return "admin/usuarios/modificar_perfil_usuario"; 
		        }
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
		    return "redirect:/admin/editarPerfil";
	}
	
	@RequestMapping(value = "/403")
	public String accesoDenegado(Model modelo) {
		modelo.addAttribute("message", "No tienes permiso de acceso a esta página");
		return "403Page";
	}	

}
