package com.dawes.serviciosImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery.FetchableFluentQuery;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.dawes.exception.UsuarioNotFoundException;
import com.dawes.modelo.Rol;
import com.dawes.modelo.Usuario;
import com.dawes.modelo.UsuarioRol;
import com.dawes.repositorio.RolRepository;
import com.dawes.repositorio.UsuarioRepository;
import com.dawes.servicios.ServicioUsuario;

@Service
public class ServicioUsuarioImpl implements ServicioUsuario {
	@Autowired
	private UsuarioRepository ur;
	@Autowired
	private RolRepository rr;	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@Override
	public Optional<Usuario> findByEmail(String email) {
		return ur.findByEmail(email);
	}
	
	@Override
//	@Transactional
	public Usuario registrarUsuario(Usuario usuario) {
		return ur.save(usuario);
	}
	
	@Override
	public Optional<Usuario> findByUsernameAndPassword(String username, String password) {	   
	    Optional<Usuario> userOpt = ur.findByUsername(username);	   
	    if (userOpt.isPresent()) {
	        Usuario user = userOpt.get();
	        if (passwordEncoder.matches(password, user.getPassword())) {	          
	            return Optional.of(user);
	        }
	    }	 
	    return Optional.empty();
	}	

	@Override
	public Optional<Usuario> findByUsername(String username) {		
		return ur.findByUsername(username);
	}

	@Override
	public Usuario findFirstByUsername(String username) {		
		return ur.findFirstByUsername(username);
	}	
	
	// recuperar usuarios con rol USER (clientes)
    public List<Usuario> findUsersWithRoleUser() {
        return ur.findByRoles_Rol_RoleName("ROLE_USER");
    }

    // recuperar usuarios con roles ADMIN, GESTOR y EDITOR (uausrios)
    public List<Usuario> findUsersWithOtherRoles() {
        return ur.findByRoles_Rol_RoleNameNot("ROLE_USER");
    }    
 
    public List<Usuario> findUsersWithOnlyRoleUser() {
        return ur.findByRoles_Rol_RoleNameAndRolesSize("ROLE_USER", 1);
    }

	@Override
	public List<Usuario> findByRoles_Rol_RoleNameAndRolesSize(String roleName, int size) {
		 return ur.findByRoles_Rol_RoleNameAndRolesSize("ROLE_USER", 1);
	}
	
	public void updateResetPasswordToken(String token, String email) 
		throws UsuarioNotFoundException 
	 {		 
	 	Optional<Usuario> optionalUsuario = ur.findByEmail(email);  	   
	    if (optionalUsuario.isPresent()) {
	        Usuario u = optionalUsuario.get();  
	        u.setResetPasswordToken(token);      
	        ur.save(u);                         
	    } else {	       
	        throw new UsuarioNotFoundException("No se ha encontrado ningún cliente con email " + email);
	    }	 
	}
	     
    public Usuario getByResetPasswordToken(String token) {
        return ur.findByResetPasswordToken(token);
    }
     
    public void updatePassword(Usuario u, String newPassword) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode(newPassword);
        u.setPassword(encodedPassword);         
        u.setResetPasswordToken(null);
        ur.save(u);
    }  	    
	  
    public Optional<Usuario> authenticate(String username, String password) {	      
        Usuario usuario = ur.findByUsername(username).get();  	      
        if (usuario != null && password.equals(usuario.getPassword())) {
            return Optional.of(usuario); 
        }
        return Optional.empty(); 
    }	    
    
    public boolean existsByUsernameAndPassword(String username, String password) {
        Optional<Usuario> user = ur.findByUsername(username);
        return user.isPresent() && passwordEncoder.matches(password, user.get().getPassword());
    }
    
    public boolean existsByUsername(String username) {
        return ur.findByUsername(username).isPresent();
    }

    public boolean existsByEmail(String email) {
        return ur.findByEmail(email).isPresent();
    }

	@Override
	public <S extends Usuario> S save(S entity) {
		return ur.save(entity);
	}

	@Override
	public <S extends Usuario> List<S> saveAll(Iterable<S> entities) {
		return ur.saveAll(entities);
	}

	@Override
	public <S extends Usuario> Optional<S> findOne(Example<S> example) {
		return ur.findOne(example);
	}

	@Override
	public List<Usuario> findAll(Sort sort) {
		return ur.findAll(sort);
	}

	@Override
	public void flush() {
		ur.flush();
	}

	@Override
	public Page<Usuario> findAll(Pageable pageable) {
		return ur.findAll(pageable);
	}

	@Override
	public <S extends Usuario> S saveAndFlush(S entity) {
		return ur.saveAndFlush(entity);
	}

	@Override
	public <S extends Usuario> List<S> saveAllAndFlush(Iterable<S> entities) {
		return ur.saveAllAndFlush(entities);
	}

	@Override
	public List<Usuario> findAll() {
		return ur.findAll();
	}

	@Override
	public List<Usuario> findAllById(Iterable<Integer> ids) {
		return ur.findAllById(ids);
	}

	@Override
	public void deleteInBatch(Iterable<Usuario> entities) {
		ur.deleteInBatch(entities);
	}

	@Override
	public <S extends Usuario> Page<S> findAll(Example<S> example, Pageable pageable) {
		return ur.findAll(example, pageable);
	}

	@Override
	public Optional<Usuario> findById(Integer id) {
		return ur.findById(id);
	}

	@Override
	public void deleteAllInBatch(Iterable<Usuario> entities) {
		ur.deleteAllInBatch(entities);
	}

	@Override
	public boolean existsById(Integer id) {
		return ur.existsById(id);
	}

	@Override
	public <S extends Usuario> long count(Example<S> example) {
		return ur.count(example);
	}

	@Override
	public void deleteAllByIdInBatch(Iterable<Integer> ids) {
		ur.deleteAllByIdInBatch(ids);
	}

	@Override
	public <S extends Usuario> boolean exists(Example<S> example) {
		return ur.exists(example);
	}

	@Override
	public void deleteAllInBatch() {
		ur.deleteAllInBatch();
	}

	@Override
	public Usuario getOne(Integer id) {
		return ur.getOne(id);
	}

	@Override
	public <S extends Usuario, R> R findBy(Example<S> example, Function<FetchableFluentQuery<S>, R> queryFunction) {
		return ur.findBy(example, queryFunction);
	}

	@Override
	public long count() {
		return ur.count();
	}	
	
	@Override
	@Transactional
	public void deleteById(Integer id) {	   
	    Usuario usuario = ur.findById(id).orElse(null);	    
	    if (usuario != null) {
	        // quitar cada asociación UsuarioRol
	        List<UsuarioRol> roles = new ArrayList<>(usuario.getRoles());
	        for (UsuarioRol usuarioRol : roles) {
	            // quitar la asociación de parte de Rol
	            Rol rol = usuarioRol.getRol();
	            rol.getUsuarios().remove(usuarioRol);
	            // quitar la asociación de parte de Usuario
	            usuario.getRoles().remove(usuarioRol);	            
	            rr.save(rol);
	        }	     
	        ur.deleteById(id);
	    }
	}

	@Override
	public Usuario getById(Integer id) {
		return ur.getById(id);
	}

	@Override
	public void delete(Usuario entity) {
		ur.delete(entity);
	}

	@Override
	public void deleteAllById(Iterable<? extends Integer> ids) {
		ur.deleteAllById(ids);
	}

	@Override
	public Usuario getReferenceById(Integer id) {
		return ur.getReferenceById(id);
	}

	@Override
	public void deleteAll(Iterable<? extends Usuario> entities) {
		ur.deleteAll(entities);
	}

	@Override
	public <S extends Usuario> List<S> findAll(Example<S> example) {
		return ur.findAll(example);
	}

	@Override
	public <S extends Usuario> List<S> findAll(Example<S> example, Sort sort) {
		return ur.findAll(example, sort);
	}

	@Override
	public void deleteAll() {
		ur.deleteAll();
	}

	
}
