package com.dawes.controladores;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.dawes.serviciosImpl.EmailService;
import com.dawes.serviciosImpl.ServicioCategoriaImpl;
import com.dawes.serviciosImpl.UsuarioSesionService;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class ContactController {
	   @Autowired
	   private EmailService emailService;
	   @Autowired
	   private ServicioCategoriaImpl sci;
	   private final UsuarioSesionService uss;
	   public ContactController(UsuarioSesionService uss) {
	      this.uss = uss;
	   }

	    @GetMapping("/contact")
	    public String showContactForm(HttpServletRequest request, 
	    		@AuthenticationPrincipal UserDetails userDetails, 
	    		Model model) {
	    	uss.addUserDetailsToModel(model, userDetails);
	    	model.addAttribute("categorias", sci.findAll());
	    	model.addAttribute("currentUri", request.getRequestURI());
	        return "public/contact"; 
	    }
	    
	    @PostMapping("/contact")	            
	            public String processContactForm(@RequestParam String nombre, 
                        @RequestParam String email, 
                        @RequestParam String telefono,
                        @RequestParam String asunto, 
                        @RequestParam String comentarios,
                        RedirectAttributes redirectAttributes,
                        Model model) {
					try {				
					String htmlContent = "<h1>Nuevo mensaje de contacto</h1>"
					      + "<p>Nombre: " + nombre + "</p>"
					      + "<p>Email: " + email + "</p>"
					      + "<p>Teléfono: " + telefono + "</p>"
					      + "<p>Asunto: " + asunto + "</p>"
					      + "<p>Mensaje: " + comentarios + "</p>";	          
	            emailService.sendHtmlEmail("marysgbg@gmail.com", asunto, htmlContent);  	           
	            String userConfirmation = "Gracias por contactarnos, " + nombre + ". Tu mensaje ha sido enviado con éxito. Te responderemos pronto.";	           
	            emailService.sendSimpleEmail(email, "Confirmación de contacto", userConfirmation); 
	            redirectAttributes.addFlashAttribute("message", "Tu mensaje ha sido enviado con éxito. Te responderemos pronto.");	            
	            System.err.println("Flash attribute set: " + redirectAttributes.containsAttribute("message"));
	        } catch (MessagingException e) {
	            redirectAttributes.addFlashAttribute("error", "Hubo un error al enviar tu mensaje. Por favor, inténtalo de nuevo más tarde.");	       
	        }
	        return "redirect:/platos"; 
	    }

}
