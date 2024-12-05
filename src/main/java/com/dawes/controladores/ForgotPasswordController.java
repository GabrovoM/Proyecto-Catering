package com.dawes.controladores;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.dawes.exception.UsuarioNotFoundException;
import com.dawes.Utility;
import com.dawes.modelo.Usuario;
import com.dawes.serviciosImpl.ServicioUsuarioImpl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpServletRequest;
import net.bytebuddy.utility.RandomString;

@Controller
public class ForgotPasswordController {
	@Autowired
    private JavaMailSender mailSender;
	@Autowired
	private ServicioUsuarioImpl sui;
	
	@GetMapping("/forgot_password")
	public String showForgotPasswordForm() {
	    return "public/forgot_password_form";
	}	
	
	@PostMapping("/forgot_password")
	public String processForgotPassword(HttpServletRequest request, Model model) {
	    String email = request.getParameter("email");
	    String token = RandomString.make(30);
	    System.err.println("Email: " + email);
	    System.err.println("Token: " + token);

	    try {	       
	        Optional<Usuario> usuarioOptional = sui.findByEmail(email);
	        if (usuarioOptional.isEmpty()) {
	            throw new UsuarioNotFoundException("Ningún usuario encontrado con este email.");
	        }
	        Usuario usuario = usuarioOptional.get();
	        String username = usuario.getUsername(); 	       
	        sui.updateResetPasswordToken(token, email);	      
	        String resetPasswordLink = Utility.getSiteURL(request) + "/reset_password?token=" + token;
	        sendEmail(email, resetPasswordLink, username);
	        model.addAttribute("message", "Hemos enviado un enlace para restablecer la contraseña a su correo electrónico. Por favor, verifíquelo.");	         
	    } catch (UsuarioNotFoundException ex) {	      
	        model.addAttribute("error", ex.getMessage());
	        return "public/forgot_password_form"; 
	    } catch (UnsupportedEncodingException | MessagingException e) {
	        model.addAttribute("error", "Error al enviar el correo electrónico.");
	    }
	    return "login"; 
	}
	
	@GetMapping("/reset_password")
	public String showResetPasswordForm(@Param(value = "token") String token, Model model) {
	    Usuario usuario = sui.getByResetPasswordToken(token);
	    model.addAttribute("token", token);	     
	    if (usuario == null) {
	        model.addAttribute("error", "Token inválido.");
	        return "login";
	    }	     
	    return "public/reset_password_form";
	}
	
	@PostMapping("/reset_password")
	public String processResetPassword(HttpServletRequest request, Model model) {
	    String token = request.getParameter("token");
	    String password = request.getParameter("password");	     
	    Usuario usuario = sui.getByResetPasswordToken(token);
	    model.addAttribute("title", "Restablecer su contraseña");	     
	    if (usuario == null) {
	        model.addAttribute("error", "Token inválido.");
	        return "login";
	    } else {           
	        sui.updatePassword(usuario, password);	         
	        model.addAttribute("message", "Ha cambiado su contraseña con éxito.");
	    }	
	    return "login";
	}	

	public void sendEmail(String recipientEmail, String link, String username)
	        throws MessagingException, UnsupportedEncodingException {
	    MimeMessage message = mailSender.createMimeMessage();              
	    MimeMessageHelper helper = new MimeMessageHelper(message);
	    helper.setFrom("contacto@asturcat.com", "Soporte AsturCat");
	    helper.setTo(recipientEmail);
	    String subject = "Aquí está el enlace para restablecer su contraseña.";
	    String content = "<p>Hola,</p>" 
	            + "<p>Ha solicitado restablecer su contraseña.</p>"
	    		+ "<p>Su nombre de usuario es "+username +".</p>"
	            + "<p>Haga clic en el enlace de abajo para cambiar su contraseña:</p>"
	            + "<p><a href=\"" + link + "\">Cambiar mi contraseña</a></p>"
	            + "<br>"
	            + "<p>Ignora este correo electrónico si recuerdas tu contraseña, "
	            + "o si no has realizado la solicitud.</p>";
	    helper.setSubject(subject);
	    helper.setText(content, true);
	    mailSender.send(message);
	}
	
}
