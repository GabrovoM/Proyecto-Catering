package com.dawes.controladores;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
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

import com.dawes.modelo.Ajuste;
import com.dawes.modelo.EstadoPedidoEnum;
import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.modelo.Usuario;
import com.dawes.repositorio.AjusteRepository;
import com.dawes.serviciosImpl.PDFGeneratorService;
import com.dawes.serviciosImpl.ServicioPedidoImpl;
import com.dawes.serviciosImpl.ServicioPlatoMenuPedidoImpl;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin/pedidos")
public class PedidoController {
	@Autowired
	private ServicioPedidoImpl spdi;	
	@Autowired
	private ServicioPlatoMenuPedidoImpl spmpi;
	@Autowired
	private AjusteRepository ar;
	DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");	
	@Autowired
	private PDFGeneratorService pdfGeneratorService;	
	
	@ModelAttribute
	public void addTituloAttribute(Model model) {
	   model.addAttribute("titulo", "Pedidos");
	}
	@ModelAttribute
	 public void addCommonAttributes(Model model) {
	     Ajuste ajuste = ar.findById(1).orElseThrow(() -> new IllegalArgumentException("Ajuste ID no válido."));
	     model.addAttribute("precioEnvio", ajuste.getPrecioEnvioZona1());	       
	 }
	
	@GetMapping("")
	public String listPedidos(Model model, 
			Pageable pageable, @RequestParam(defaultValue = "0") int page) {		
	    List<Pedido> pendingOrPartialWithWarnings = spdi.getPendingOrPartialWithWarnings();
	    model.addAttribute("warningPedidos", pendingOrPartialWithWarnings);
	    List<Pedido> pedidosParcialesFechaSemanal = spdi.getPedidosWithLastFechaEnvioSemanal();	
	    model.addAttribute("cPage", page);	    
	    model.addAttribute("pedidos", spdi.findAll(pageable));	    
	    model.addAttribute("currentPage", "pedidos");
	    LocalDate fecha_hoy = LocalDate.now();
	    model.addAttribute("fecha_hoy", fecha_hoy);
	    LocalDate nextDay = fecha_hoy.plusDays(1);
		LocalDate fiveDaysAgo = fecha_hoy.minusDays(5);
	    List<Pedido> warningPedidos = spdi.getPendingOrPartialWithWarnings(); 
	    List<Pedido> partialWithFiveDaysWarning = spdi.findPartialWithFiveDaysWarning(nextDay, fiveDaysAgo); 	    
	    if (fecha_hoy.getDayOfWeek() == DayOfWeek.SATURDAY) {
	        warningPedidos.addAll(partialWithFiveDaysWarning);
	    }	    
	    model.addAttribute("warningPedidos", warningPedidos);	    
	    Map<Integer, LocalDate> nextDispatchDates = getNextDispatchDates();
        model.addAttribute("nextDispatchDates", nextDispatchDates);           
	    Map<Integer, LocalDate> dispatchDates = calculateDispatchDates(pedidosParcialesFechaSemanal);
	    model.addAttribute("dispatchDates", dispatchDates);	
	    EstadoPedidoEnum estadoEnum = EstadoPedidoEnum.PARCIAL;
	    List<Pedido> pedidos = spdi.findByEstadoEnum(estadoEnum);
	    Map<Integer, LocalDate> dispatchDates1 = calculateDispatchDates(pedidos);
	    model.addAttribute("dispatchDates1", dispatchDates1);
	    return "admin/pedidos/listado-pedidos";
	}		
	
	private Map<Integer, LocalDate> calculateDispatchDates(List<Pedido> pedidos) {
	    Map<Integer, LocalDate> dispatchDates = new HashMap<>();
	    for (Pedido pedido : pedidos) {
	        LocalDate dispatchDate = null;
	        if (pedido.getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {
	            Optional<LocalDate> maxFechaEnvioSemanal = pedido.getLineas_pedido().stream()
	                .map(PlatoMenuPedido::getFechaEnvioSemanal)
	                .filter(Objects::nonNull)
	                .max(LocalDate::compareTo);
	            dispatchDate = maxFechaEnvioSemanal
	                .map(fecha -> fecha.plusDays(7))
	                .orElse(pedido.getFechaPrimerEnvio());
	        } else if (pedido.getEstadoEnum() == EstadoPedidoEnum.PENDIENTE) {	            
	            dispatchDate = LocalDate.now().plusDays(1);
	        }
	        if (dispatchDate != null) {
	            dispatchDates.put(pedido.getId(), dispatchDate);
	        }
	    }	   
	    return dispatchDates;
	}
	
	public Map<Integer, LocalDate> getNextDispatchDates() {
	    Map<Integer, LocalDate> nextDispatchDates = new HashMap<>();
	    for (Pedido pedido : spdi.findAll()) {
	        LocalDate nextDispatchDate = spmpi.getNextDispatchDate(pedido.getId());
	        if (nextDispatchDate != null && pedido.getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {
	            nextDispatchDates.put(pedido.getId(), nextDispatchDate);
	        }
	    }
	    return nextDispatchDates;
	}
	
	@GetMapping("/detalle/{id}")
	public String detallePedido(@PathVariable Integer id, 
	                            @RequestParam(defaultValue = "0") int page, 
	                            @RequestParam(value = "estado", required = false) String estado, 
	                            HttpServletRequest request, 
	                            HttpSession session, Model model) {  
	    String referer = request.getHeader("Referer");
	    if (referer != null) {
	        session.setAttribute("refererUrl", referer);  
	        session.setAttribute("estadoFilter", estado);  
	    }
	    Optional<Pedido> pedido = spdi.findById(id);
	    if (!pedido.isPresent()) {	      
	        return "redirect:/admin/pedidos";
	    }   
	    boolean fechasAsignadas = pedido.get().getLineas_pedido().stream()
	            .allMatch(linea -> linea.getFechaEnvioSemanal() != null);
	    double detallePrecio = 0;
        for (PlatoMenuPedido detalle : pedido.get().getLineas_pedido()) {
            if (detalle.getMenu().getId() != 0) {
                detallePrecio += detalle.getPrecio_menu() * detalle.getCantidad();
            }
            if (detalle.getPlato().getId() != 0) {
                detallePrecio += detalle.getPrecio_plato() * detalle.getCantidad();
            }
        }	        
        LocalDate fechaEntrega;
        LocalDate fechaEnvio = null;
        if (pedido.get().getFechaEntrega() != null) {
            fechaEntrega = pedido.get().getFechaEntrega();
        } else {
            LocalDate fechaCreacion = pedido.get().getFechaCreacion();
            fechaEntrega = fechaCreacion.plusDays(2);
            if (fechaCreacion.getDayOfWeek() == DayOfWeek.FRIDAY) {
                if (fechaEntrega.getDayOfWeek() == DayOfWeek.SUNDAY) {
                    fechaEntrega = fechaEntrega.plusDays(1);
                }
            }
        }
        if (pedido.get().getFechaEnvio() != null) {
            fechaEnvio = pedido.get().getFechaEnvio();
        }
        model.addAttribute("fechaEntrega", fechaEntrega);
        model.addAttribute("fechaEnvio", fechaEnvio);  
	    double precioEnvio = (double) model.getAttribute("precioEnvio");
	    model.addAttribute("cPage", page);
	    model.addAttribute("pedido", pedido.get());
	    model.addAttribute("detalles", pedido.get().getLineas_pedido());
	    model.addAttribute("detallePrecio", detallePrecio);
	    model.addAttribute("sesion", session.getAttribute("idusuario"));
	    model.addAttribute("refererUrl", referer);
	    model.addAttribute("fechasAsignadas", fechasAsignadas);
	    model.addAttribute("precioEnvio", precioEnvio);	    
	    return "admin/pedidos/detallepedido";
	}
	
	@GetMapping({"/delete/{id}"})
	public String borrarPedido(@PathVariable int id, RedirectAttributes redirectAttributes)  {
		try {			
			spdi.deleteById(id);	
		} catch (NoSuchElementException e) {
			System.err.println("No existe pedido con id "+id + e.getLocalizedMessage());
			redirectAttributes.addFlashAttribute("errorMessage", "No existe pedido con este id");			
		}					
		return "redirect:/admin/pedidos";	
	}
	
	@PostMapping("/confirmar/{id}")
	public String confirmarEnvioSemanal(@PathVariable("id") Integer id, 
	                                    @RequestParam(name = "marcado", required = false) String marcado,
	                                    @RequestParam(name = "fechaEnvioSemanal", required = false) String fechaEnvioSemanal, 
	                                    HttpSession session, Model model) {
	    PlatoMenuPedido pmp = spmpi.findById(id).orElse(null);
	    if (pmp != null) {	      
	        if (marcado != null && !fechaEnvioSemanal.isEmpty()) {
	            pmp.setFechaEnvioSemanal(LocalDate.parse(fechaEnvioSemanal));
	        } else {	          
	            pmp.setFechaEnvioSemanal(null);
	        }
	        spmpi.save(pmp);     	        
	        Pedido pedido = pmp.getPedido();	        
	        double detallePrecio = 0;
	        for (PlatoMenuPedido detalle : pedido.getLineas_pedido()) {
	            if (detalle.getMenu().getId() != 0) {
	                detallePrecio += detalle.getPrecio_menu() * detalle.getCantidad();
	            }
	            if (detalle.getPlato().getId() != 0) {
	                detallePrecio += detalle.getPrecio_plato() * detalle.getCantidad();
	            }
	        }	        
	        model.addAttribute("detallePrecio", detallePrecio);
	        model.addAttribute("pedido", pedido);
	        model.addAttribute("detalles", pedido.getLineas_pedido());
	        String refererUrl = (String) session.getAttribute("refererUrl");
	        model.addAttribute("refererUrl", refererUrl);
	        return "admin/pedidos/detallepedido";
	    }	    
	    return "admin/error";
	}
		
	@GetMapping("/confirmar/{id}")
	public String handleGetRequest(@PathVariable("id") Integer id, Model model) {
	    model.addAttribute("errorMsg", "Confirmar debe hacerce via POST.");
	    return "admin/error"; 
	}
	 
	 @GetMapping("/finalizar/check/{id}")
	 @ResponseBody
	 public ResponseEntity<String> checkFinalizar(@PathVariable("id") Integer id) {
	     Optional<Pedido> pedido = spdi.findById(id);	    
	     if (pedido.isPresent()) {		       
	         if (pedido.get().getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {		           
	             boolean allDatesAssigned = checkAllDatesAssigned(pedido.get().getLineas_pedido());
	             if (!allDatesAssigned) {
	                 return ResponseEntity.badRequest().body("No se puede finalizar el pedido con ID " + id + " porque tiene líneas sin fecha asignada.");
	             }
	         }		       
	         return ResponseEntity.ok("success");
	     }
	     return ResponseEntity.notFound().build(); 
	 }
	
	 private boolean checkAllDatesAssigned(List<PlatoMenuPedido> platosMenusPedidos) {
	     return platosMenusPedidos.stream().allMatch(plato -> plato.getFechaEnvioSemanal() != null);
	 }	
	 
	@GetMapping("/finalizar/{id}")
	public String finalizarPedido(@PathVariable("id") Integer id,
	                              @RequestParam(value = "fechaInicioStr", required = false) String fechaInicioStr,
	                              @RequestParam(value = "fechaFinStr", required = false) String fechaFinStr,
	                              @RequestParam(value = "estado", required = false) String estado,
	                              HttpSession session) {	 
	    Optional<Pedido> pedido = spdi.findById(id);
	    if (pedido.isPresent()) {
	        pedido.get().setEstadoEnum(EstadoPedidoEnum.FINALIZADO);
	        pedido.get().setFechaEnvio(LocalDate.now());
	        spdi.save(pedido.get());
	    }	  
	    if (fechaInicioStr == null) {
	        fechaInicioStr = ""; 
	    }
	    if (fechaFinStr == null) {
	        fechaFinStr = ""; 
	    }
	    if (estado == null) {
	        estado = "PENDIENTE"; 
	    }
	    String refererUrl = (String) session.getAttribute("refererUrl");
	    if (refererUrl != null) {
	        return "redirect:" + refererUrl; 
	    } else {
	      
	        return "redirect:/admin/pedidos/results?fechaInicioStr=" + fechaInicioStr + "&fechaFinStr=" + fechaFinStr + "&estado=" + estado;
	    }
	}
	
	@PostMapping("/cancelar/{id}")
	public String cancelarPedido(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
		Pedido pedidoACancelar = spdi.findById(id).orElse(null);
		if (pedidoACancelar != null) {
			pedidoACancelar.setEstadoEnum(EstadoPedidoEnum.CANCELADO);
			spdi.save(pedidoACancelar);
		} else {
			redirectAttributes.addFlashAttribute("errorMessage", "Pedido no encontrado.");
	    }
		return "redirect:/admin/pedidos";
	}	
		
	@GetMapping("/fechaEnvio")
	public String getPedidosFechaEnvio(Model model, HttpSession session) {
	    session.setAttribute("refererUrl", "/admin/pedidos/fechaEnvio"); 	   
	    List<PlatoMenuPedido> platoMenuPedidos = spmpi.getPedidosConFechaEnvioSemanalHoyYSieteDiasAntes();
	    Set<Pedido> pedidos = new HashSet<>();	
	    Map<Integer, LocalDate> dispatchDates = new HashMap<>();	    
	    LocalDate today = LocalDate.now();  
	    model.addAttribute("fecha_hoy", today);	    
	    Map<Integer, LocalDate> nextDispatchDates = getNextDispatchDates();  	    	
	    for (PlatoMenuPedido pmp : platoMenuPedidos) {
	        Optional<Pedido> optionalPedido = spdi.findById(pmp.getPedido().getId());
	        if (optionalPedido.isPresent()) {
	            Pedido pedido = optionalPedido.get(); 					           
	            boolean allFechaEnvioSemanalFilled = pedido.getLineas_pedido().stream()
	                    .allMatch(linea -> linea.getFechaEnvioSemanal() != null);	            
	            if (!allFechaEnvioSemanalFilled) {
		            pedidos.add(pedido); 	          
		            if (pedido.getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {	               
		                Optional<LocalDate> maxFechaEnvioSemanal = pedido.getLineas_pedido().stream()
		                        .map(PlatoMenuPedido::getFechaEnvioSemanal)
		                        .filter(Objects::nonNull)  
		                        .max(LocalDate::compareTo);	              
		                LocalDate dispatchDate;
		                if (maxFechaEnvioSemanal.isPresent()) {
		                    dispatchDate = maxFechaEnvioSemanal.get().plusDays(7);
		                } else {	                   
		                    dispatchDate = pedido.getFechaPrimerEnvio();
		                }
		                dispatchDates.put(pedido.getId(), dispatchDate);
		            }
				}	
	        } else {	   
	        	// pedido no encontrado
	            System.out.println("Pedido bo encontrado con ID: " + pmp.getPedido().getId());
	        }
	    }	  
	    pedidos.removeIf(pedido -> {
	        LocalDate dispatchDate = dispatchDates.get(pedido.getId());
	        return dispatchDate != null && dispatchDate.isAfter(today);  
	    });	  	    
	    model.addAttribute("nextDispatchDates", nextDispatchDates);	    
	    model.addAttribute("nextDispatchDates", nextDispatchDates);  
	    model.addAttribute("listaPedidosE", pedidos.isEmpty() ? Collections.emptyList() : pedidos); 
	    model.addAttribute("dispatchDates", dispatchDates);
	    return "admin/pedidos/pedidos_estado";
	}	
	
	@PostMapping("/submit")
	public String combinedSubmit(@RequestParam(value = "fechaInicioStr", required = false) String fechaInicioStr,
	                             @RequestParam(value = "fechaFinStr", required = false) String fechaFinStr,
	                             @RequestParam(value = "estado", required = false) String estado,
	                             RedirectAttributes redirectAttributes, Model model) {		 
	    redirectAttributes.addAttribute("fechaInicioStr", fechaInicioStr);
	    redirectAttributes.addAttribute("fechaFinStr", fechaFinStr);
	    redirectAttributes.addAttribute("estado", estado);    	    
	    return "redirect:/admin/pedidos/results"; 
	}		
	
	@GetMapping("/results")
	public String getFilteredPedidos(
	        @RequestParam(value = "fechaInicioStr", required = false) String fechaInicioStr,
	        @RequestParam(value = "fechaFinStr", required = false) String fechaFinStr,
	        @RequestParam(value = "estado", required = false) String estado,
	        @RequestParam(value = "page", defaultValue = "0") int page,
	        Model model) {			
		String formattedFechaInicio = null;
	    String formattedFechaFin = null;
		if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
	        LocalDate fechaInicio = LocalDate.parse(fechaInicioStr);
	        formattedFechaInicio = fechaInicio.format(dtf);
	    }
	    if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
	        LocalDate fechaFin = LocalDate.parse(fechaFinStr);
	        formattedFechaFin = fechaFin.format(dtf);
	    }	  
	    LocalDate today = LocalDate.now();
	    LocalDate nextDay = today.plusDays(1);
	    LocalDate fiveDaysAgo = today.minusDays(5);
	    List<Pedido> warningPedidos = spdi.getPendingOrPartialWithWarnings(); 	 
	    List<Pedido> partialWithFiveDaysWarning = spdi.findPartialWithFiveDaysWarning(nextDay, fiveDaysAgo); 	    
	    if (today.getDayOfWeek() == DayOfWeek.SATURDAY) {
	        warningPedidos.addAll(partialWithFiveDaysWarning);
	    }	  
	    model.addAttribute("warningPedidos", warningPedidos);		    
		Map<Integer, LocalDate> nextDispatchDates = getNextDispatchDates();
		model.addAttribute("nextDispatchDates", nextDispatchDates);          
	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    List<Pedido> listaPedidosF = new ArrayList<>();
	    Pageable pageable = PageRequest.of(page, 10); 
	    Map<Integer, LocalDate> dispatchDates = new HashMap<>();
	    Map<Integer, LocalDate> dispatchDates1 = new HashMap<>(); 
	    LocalDate fecha_hoy = LocalDate.now();
	    model.addAttribute("fecha_hoy", fecha_hoy);

	    try {
	        LocalDate fechaInicio = null;
	        LocalDate fechaFin = null;
	        EstadoPedidoEnum estadoEnum = null;	     
	        if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
	            fechaInicio = LocalDate.parse(fechaInicioStr, dtf);
	            model.addAttribute("fini", fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	        }
	        if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
	            fechaFin = LocalDate.parse(fechaFinStr, dtf);
	            model.addAttribute("ffin", fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	        }
	        if (estado != null && !estado.isEmpty()) {
	            estadoEnum = EstadoPedidoEnum.valueOf(estado);
	        }
	        Page<Pedido> pedidosPage;
	        if (fechaInicio != null && fechaFin != null && estadoEnum != null) {
	            pedidosPage = spdi.findByEstadoEnumAndFechaCreacionBetween(estadoEnum, fechaInicio, fechaFin, pageable);
	        } else if (fechaInicio != null && fechaFin != null) {
	            pedidosPage = spdi.findByFechaCreacionBetween(fechaInicio, fechaFin, pageable);
	        } else if (estadoEnum != null) {
	            pedidosPage = spdi.findByEstadoEnum(estadoEnum, pageable);
	        } else {
	            model.addAttribute("error", "Por favor, seleccione al menos un filtro.");
	            return "admin/pedidos/pedidos_results";
	        }
	        listaPedidosF = pedidosPage.getContent();
	        model.addAttribute("pedidos", pedidosPage);
	        for (Pedido pedido : listaPedidosF) {
	            if (pedido.getEstadoEnum() == EstadoPedidoEnum.PARCIAL) {
	                Optional<LocalDate> maxFechaEnvioSemanal = pedido.getLineas_pedido().stream()
	                        .map(PlatoMenuPedido::getFechaEnvioSemanal)
	                        .filter(Objects::nonNull)
	                        .max(LocalDate::compareTo);
	                LocalDate dispatchDate = maxFechaEnvioSemanal
	                        .map(fecha -> fecha.plusDays(7))
	                        .orElse(pedido.getFechaPrimerEnvio());
	                dispatchDates.put(pedido.getId(), dispatchDate);
	            }
	        }
	        List<Pedido> pedidosParciales = spdi.findByEstadoEnum(EstadoPedidoEnum.PARCIAL);
	        for (Pedido pedido : pedidosParciales) {
	            Optional<LocalDate> maxFechaEnvioSemanal = pedido.getLineas_pedido().stream()
	                    .map(PlatoMenuPedido::getFechaEnvioSemanal)
	                    .filter(Objects::nonNull)
	                    .max(LocalDate::compareTo);
	            LocalDate dispatchDate1 = maxFechaEnvioSemanal
	                    .map(fecha -> fecha.plusDays(7))
	                    .orElse(pedido.getFechaPrimerEnvio());
	            dispatchDates1.put(pedido.getId(), dispatchDate1);
	        }
	        model.addAttribute("listaPedidosF", listaPedidosF);
	        model.addAttribute("dispatchDates", dispatchDates);
	        model.addAttribute("dispatchDates1", dispatchDates1); 
	        model.addAttribute("estado", estado);
	        model.addAttribute("fechaInicioStr", formattedFechaInicio);
		    model.addAttribute("fechaFinStr", formattedFechaFin);
	    } catch (DateTimeParseException e) {
	        model.addAttribute("error", "Formato de fecha no válido. Por favor usa yyyy-MM-dd.");
	    } catch (IllegalArgumentException e) {
	        model.addAttribute("error", "Estado no válido.");
	    } catch (Exception e) {
	        model.addAttribute("error", "Ocurrió un error inesperado. Por favor, intenta nuevamente.");
	    }
	    return "admin/pedidos/pedidos_results";
	}	

	@GetMapping("/download-pdf")
	public void downloadPdf(
	        @RequestParam(value = "estado", required = false) String estado,
	        @RequestParam(value = "fechaInicioStr", required = false) String fechaInicioStr,
	        @RequestParam(value = "fechaFinStr", required = false) String fechaFinStr,
	        HttpServletResponse response
	) throws IOException {
	    response.setContentType("application/pdf");
	    response.setHeader("Content-Disposition", "attachment; filename=pedidos.pdf");
	    List<Pedido> pedidos;
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    try {
	        LocalDate fechaInicio = (fechaInicioStr != null && !fechaInicioStr.isEmpty()) 
	                ? LocalDate.parse(fechaInicioStr, formatter) : null;
	        LocalDate fechaFin = (fechaFinStr != null && !fechaFinStr.isEmpty()) 
	                ? LocalDate.parse(fechaFinStr, formatter) : null;
	     
	        if (estado != null && !estado.isEmpty()) {
	            EstadoPedidoEnum estadoEnum = EstadoPedidoEnum.valueOf(estado);
	            if (fechaInicio != null && fechaFin != null) {
	                pedidos = spdi.findByEstadoEnumAndFechaCreacionBetween(estadoEnum, fechaInicio, fechaFin).get();
	            } else {
	                pedidos = spdi.findByEstadoEnum(estadoEnum);
	            }
	        } else if (fechaInicio != null && fechaFin != null) {
	            pedidos = spdi.findByFechaCreacionBetween(fechaInicio, fechaFin).get();
	        } else {
	            pedidos = spdi.findAll();
	        }	        
	        String subtituloFechas = null;
	        if (fechaInicio != null && fechaFin != null) {
	        	subtituloFechas = "Entre Fechas: " + fechaInicio.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) 
	                                + " - " + fechaFin.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
	        }
	        pdfGeneratorService.generatePedidosPdf(response, pedidos, subtituloFechas);
	    } catch (IllegalArgumentException | DateTimeParseException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid date or estado parameter: " + e.getMessage());
	    } 	   
	}
	
	@GetMapping("/download-csv")
	public void downloadCsv(
	        @RequestParam(value = "estado", required = false) String estado,
	        @RequestParam(value = "fechaInicioStr", required = false) String fechaInicioStr,
	        @RequestParam(value = "fechaFinStr", required = false) String fechaFinStr,
	        HttpServletResponse response
	) throws IOException {
	    response.setContentType("text/csv");
	    response.setHeader("Content-Disposition", "attachment; filename=pedidos.csv");
	    List<Pedido> pedidos = new ArrayList<>();
	    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	    try {
	        LocalDate fechaInicio = null;
	        LocalDate fechaFin = null;
	        if (fechaInicioStr != null && !fechaInicioStr.isEmpty()) {
	            fechaInicio = LocalDate.parse(fechaInicioStr, formatter);
	        }
	        if (fechaFinStr != null && !fechaFinStr.isEmpty()) {
	            fechaFin = LocalDate.parse(fechaFinStr, formatter);
	        }    	      
	        if (estado != null && !estado.isEmpty()) {
	            EstadoPedidoEnum estadoEnum = EstadoPedidoEnum.valueOf(estado);
	            if (fechaInicio != null && fechaFin != null) {			              
	                pedidos = spdi.findByEstadoEnumAndFechaCreacionBetween(estadoEnum, fechaInicio, fechaFin).get();
	            } else {			               
	                pedidos = spdi.findByEstadoEnum(estadoEnum);
	            }
	        } else if (fechaInicio != null && fechaFin != null) {			          
	            pedidos = spdi.findByFechaCreacionBetween(fechaInicio, fechaFin).get();
	        } else {			           
	            pedidos = spdi.findAll();
	        }		    
	        try (PrintWriter writer = response.getWriter()) {
	            writer.println("Id;Fecha creación;Número;Estado;Total;Cliente");
	            for (Pedido pedido : pedidos) {
	            	writer.printf("%d;%s;%s;%s;%.2f;%s%n",
	                        pedido.getId(),
	                        pedido.getFechaCreacion().format(formatter),
	                        pedido.getNumero(),			                        
	                        pedido.getEstadoEnum().toString(),
	                        pedido.getTotal(),
	                        pedido.getUsuario().getNombre());
	            }
	        }
	    } catch (IllegalArgumentException | DateTimeParseException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Fecha o estado no válido: " + e.getMessage());
	    } catch (IOException e) {
	        e.printStackTrace();
	        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error al crear CSV.");
	    }
	}
			
	
}
