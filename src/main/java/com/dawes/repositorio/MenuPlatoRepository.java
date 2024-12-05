package com.dawes.repositorio;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.dawes.modelo.Menu;
import com.dawes.modelo.MenuPlato;
import com.dawes.modelo.Plato;

public interface MenuPlatoRepository extends JpaRepository<MenuPlato, Integer> {	
	Optional<MenuPlato> findById(int id);
	@Query("select mp from MenuPlato mp where mp.menu.id=?1 order by mp.dia")
	List<MenuPlato> findByIdMenu(Integer id);
	List<MenuPlato> findByMenuIdAndDia(Integer menuId, Integer dia);
	List<MenuPlato> findByPlato(Plato plato);	
	@Query("SELECT mp FROM MenuPlato mp WHERE mp.dia = ?1 AND mp.menu = ?2 AND mp.plato != ?3")
	MenuPlato buscarOtroPlatoDelDia(Integer dia, Menu menu, Plato plato);
}
