package com.dawes.repositorio;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.dawes.modelo.Usuario;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;

@Repository
@Transactional
public class UserRepository {
	@Autowired
	private EntityManager entityManager;  
	
	public Usuario findUserAccount(String username) {
		try {
			Query query = entityManager.createQuery("select u from Usuario u where u.username=:nombre", Usuario.class);
			 query.setParameter("nombre", username);
			 return (Usuario) query.getSingleResult();
		} catch (NoResultException e) {
			return null;
		}	 
	}

}
