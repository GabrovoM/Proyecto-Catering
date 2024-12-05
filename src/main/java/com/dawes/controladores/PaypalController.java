package com.dawes.controladores;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.dawes.modelo.Cart;
import com.dawes.modelo.CartItem;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Menu;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.Plato;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.CartItemRepository;
import com.dawes.repositorio.CartRepository;
import com.dawes.serviciosImpl.EmailService;
import com.dawes.serviciosImpl.PaypalService;
import com.dawes.serviciosImpl.ServicioCartImpl;
import com.dawes.serviciosImpl.ServicioMenuImpl;
import com.dawes.serviciosImpl.ServicioPedidoImpl;
import com.dawes.serviciosImpl.ServicioPlatoImpl;
import com.dawes.serviciosImpl.ServicioPlatoMenuPedidoImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.servlet.http.HttpSession;

@Controller
public class PaypalController {
	@Autowired
	PaypalService service;
	@Autowired
	private ServicioUsuarioImpl sui;
	@Autowired
	private ServicioPedidoImpl spdi;
	@Autowired
	private ServicioPlatoMenuPedidoImpl sppi;
	@Autowired
	private ServicioPlatoImpl spi;
	@Autowired
	private ServicioMenuImpl smi;
	@Autowired
	private ServicioCartImpl csi;
	@Autowired
	private EmailService emailService;
	@Autowired
	private CartRepository cr;
	@Autowired
	private CartItemRepository cir;

	public static final String SUCCESS_URL = "pay/success";
	public static final String CANCEL_URL = "pay/cancel";

	@PostMapping("/pay")
	public String payment(@RequestParam("method") String method, @RequestParam("total") Double total,
			@RequestParam(value = "fecha_entrega", required = false) String fechaEntregaString,
			HttpSession session, @AuthenticationPrincipal UserDetails userDetails 
	) {		
		List<PlatoMenuPedido> detalles = (List<PlatoMenuPedido>) session.getAttribute("detalles");
		Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
		if (usuario == null) {
			System.err.println("Error: Usuario no encontrado.");
			return "redirect:/"; 
		}
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
			System.err.println("Error: Lista de detalles vacía al iniciar el pago.");
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
		pedido.setMethod("Paypal");
		pedido.setFechaEntrega(fechaEntrega); 
		session.setAttribute("pedido", pedido);
		session.setAttribute("detalles", new ArrayList<>(detalles));
		
		// server
//		try {
//			Payment payment = service.createPayment(total, "EUR", method, "sale", "Venta de menus/platos",
//					"http://192.168.82.101:9012/" + CANCEL_URL, "http://192.168.82.101:9012/" + SUCCESS_URL);
//			for (Links link : payment.getLinks()) {
//				if (link.getRel().equals("approval_url")) {
//					return "redirect:" + link.getHref();
//				}
//			}
//		} catch (PayPalRESTException e) {
//			e.printStackTrace();
//		}		
		
//		try {
//			Payment payment = service.createPayment(total, "EUR", method, "sale", "Venta de menus/platos",
//					"http://138.68.182.123:9012/" + CANCEL_URL, "http://138.68.182.123:9012/" + SUCCESS_URL);
//			for (Links link : payment.getLinks()) {
//				if (link.getRel().equals("approval_url")) {
//					return "redirect:" + link.getHref();
//				}
//			}
//		} catch (PayPalRESTException e) {
//			e.printStackTrace();
//		}
				
		try {
			Payment payment = service.createPayment(total, "EUR", method, "sale", "Venta de menus/platos",
					"https://astur-catering.duckdns.org/" + CANCEL_URL, "https://astur-catering.duckdns.org/" + SUCCESS_URL);
			for (Links link : payment.getLinks()) {
				if (link.getRel().equals("approval_url")) {
					return "redirect:" + link.getHref();
				}
			}
		} catch (PayPalRESTException e) {
			e.printStackTrace();
		}
		
		// local
//		try {
//			Payment payment = service.createPayment(total, "EUR", method, "sale", "Venta de menus/platos",
//					"http://localhost:9012/" + CANCEL_URL, "http://localhost:9012/" + SUCCESS_URL);
//			for (Links link : payment.getLinks()) {
//				if (link.getRel().equals("approval_url")) {
//					return "redirect:" + link.getHref();
//				}
//			}
//		} catch (PayPalRESTException e) {
//			e.printStackTrace();
//		}
		
		return "redirect:/";
	}

	@GetMapping(value = SUCCESS_URL)
	@Transactional
	public String successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId,
			HttpSession session, Model model, @AuthenticationPrincipal UserDetails userDetails) { 																								
		try {
			Payment payment = service.executePayment(paymentId, payerId);
			System.out.println(payment.toJSON());
			if (payment.getState().equals("approved")) {
				Pedido savedOrden = (Pedido) session.getAttribute("pedido");
				List<PlatoMenuPedido> savedDetalles = (List<PlatoMenuPedido>) session.getAttribute("detalles");
				String fechaStr = (String) session.getAttribute("fecha");
				if (savedOrden != null && savedDetalles != null && !savedDetalles.isEmpty()) {
					savedOrden.setFechaCreacion(LocalDate.now());
					savedOrden.setNumero(spdi.generarNumeroOrden());
					Usuario usuario = sui.findByUsername(userDetails.getUsername()).orElse(null);
					if (usuario == null) {
						return "redirect:/";
					}
					savedOrden.setUsuario(usuario);
					if (!savedDetalles.isEmpty()) {
						LocalDate fechaEnvio = csi.findByUsuario(usuario).get(0).getFechaPrimerEnvio();
						savedOrden.setFechaPrimerEnvio(fechaEnvio);
					}
					String calle = (String) session.getAttribute("calle");
					String numero = (String) session.getAttribute("numero");
					String piso = (String) session.getAttribute("piso");
					String cp = (String) session.getAttribute("cp");
					String localidad = (String) session.getAttribute("localidad");
					boolean pordefecto = (boolean) session.getAttribute("pordefecto");
					boolean semanal = (boolean) (csi.findByUsuario(usuario).get(0).getFechaPrimerEnvio() != null);
					savedOrden.setDireccion(calle);
					savedOrden.setNum(numero);
					savedOrden.setPiso(piso); // opcional
					savedOrden.setCp(cp);
					savedOrden.setLocalidad(localidad);
					savedOrden.setPordefecto(pordefecto);
					savedOrden.setEstadoEnum(semanal ? EstadoPedidoEnum.PARCIAL : EstadoPedidoEnum.PENDIENTE);
					spdi.save(savedOrden);
					// persistir detalles
					for (PlatoMenuPedido dt : savedDetalles) {
						dt.setPedido(savedOrden);
						dt.setEstadoSemanal(semanal);
						sppi.save(dt);
					}					
					Cart cart = csi.findByUsuario(usuario).stream().findFirst().orElse(null);
					if (cart != null) {
						cir.deleteByCart(cart); // vaciar carrito
						cr.delete(cart); 
					}
					System.err.println("DETALLES ANTES: " + session.getAttribute("detalles"));					
					session.removeAttribute("orden");
					session.removeAttribute("detalles");
					session.removeAttribute("semanal");
					session.removeAttribute("calle");
					session.removeAttribute("numero");
					session.removeAttribute("piso");
					session.removeAttribute("cp");
					session.removeAttribute("localidad");
					session.removeAttribute("fecha");
					session.removeAttribute("orden");
					session.removeAttribute("detalles");
					System.err.println("DETALLES DESPUES: " + session.getAttribute("detalles"));					
					String subject = "Confirmación de pago - Pedido #" + savedOrden.getNumero();
					String message = "Estimado/a " + usuario.getNombre() + ",\n\n"
							+ "¡Gracias por tu compra! Tu pago de " + savedOrden.getTotal()
							+ " EUR ha sido procesado con éxito.\n" + "Pedido #: " + savedOrden.getNumero()
							+ "\n" + "Fecha: " + savedOrden.getFechaCreacion() + "\n\n" + "Un saludo,\nAsturCat";
					emailService.sendSimpleEmail(usuario.getEmail(), subject, message);
					System.out.println("Email de confirmación enviado a: " + usuario.getEmail());
				}
				model.addAttribute("currentStep", "index");
				return "public/carrito/success";
			}
		} catch (PayPalRESTException e) {
			System.out.println(e.getMessage());
			return "public/carrito/cancel";
		}
		return "redirect:/";
	}
}
