package com.dawes.serviciosImpl;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dawes.modelo.Pedido;
import com.dawes.modelo.PlatoMenuPedido;
import com.dawes.repositorio.AjusteRepository;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class PDFGeneratorService {
	@Autowired
	private AjusteRepository ar;
	
	public void generatePdf(HttpServletResponse response, List<PlatoMenuPedido> detalles, String clientInfo, String invoiceNumber, String issueDate, String dueDate, double totalAmount, double precioEnvio) {
	    try {	       
	        Document document = new Document(PageSize.A4);
	        PdfWriter.getInstance(document, response.getOutputStream());
	        document.open(); 
	        String dir = ar.findById(1).get().getDireccionEmpresa();
	        String num = ar.findById(1).get().getNum();
	        String piso = ar.findById(1).get().getPiso();
	        String cp = ar.findById(1).get().getCp();
	        String localidad = ar.findById(1).get().getLocalidad();
	        String email = ar.findById(1).get().getEmailEmpresa();
	        Paragraph companyInfo = new Paragraph("AsturCat\n"+ dir+ " " + num + " " +piso+ "\n" +cp + " "+ localidad+"\nAsturias,  España\n" +email);
	        companyInfo.setAlignment(Paragraph.ALIGN_RIGHT);
	        document.add(companyInfo);
	        document.add(Chunk.NEWLINE); 	       
	        Paragraph client = new Paragraph("PARA\n" + clientInfo);
	        document.add(client);	       
	        PdfPTable invoiceDetailsTable = new PdfPTable(2);
	        invoiceDetailsTable.setWidthPercentage(100);
	        invoiceDetailsTable.addCell("Nº de factura:");
	        invoiceDetailsTable.addCell(invoiceNumber);
	        invoiceDetailsTable.addCell("Fecha:");
	        invoiceDetailsTable.addCell(issueDate);
	        invoiceDetailsTable.addCell("Vencimiento:");
	        invoiceDetailsTable.addCell(dueDate);
	        document.add(invoiceDetailsTable);
	        document.add(Chunk.NEWLINE); 	      
	        PdfPTable table = new PdfPTable(4);
	        table.setWidthPercentage(100);
	        table.setWidths(new int[]{5, 2, 2, 2});
	        table.addCell("DESCRIPCIÓN");
	        table.addCell("CANTIDAD");
	        table.addCell("PRECIO (€)");
	        table.addCell("IMPORTE (€)");	        
	        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));

	        for (PlatoMenuPedido detalle : detalles) {
	            String description = "";
	            double price = 0.0;	          
	            if (detalle.getPlato() != null && detalle.getPlato().getId() != 0) {
	                description = detalle.getPlato().getNombre();
	                price = detalle.getPrecio_plato();
	            } else if (detalle.getMenu() != null && detalle.getMenu().getId() != 0) {
	                description = detalle.getMenu().getNombre();
	                price = detalle.getPrecio_menu();
	            }
	            table.addCell(description);  
	            table.addCell(String.valueOf(detalle.getCantidad()));  
	            table.addCell(currencyFormat.format(price));  
	            table.addCell(currencyFormat.format(detalle.getTotalLinea()));  
	        } 
	       
	        table.addCell("");
	        table.addCell("");
	        table.addCell("TOTAL (€):");
	        table.addCell(currencyFormat.format(totalAmount));
	        table.addCell("");
	        table.addCell("");
	        table.addCell("PRECIO ENVÍO:");
	        table.addCell(currencyFormat.format(precioEnvio));
	        document.add(table);	       
	        Font fontBold = FontFactory.getFont(FontFactory.HELVETICA_BOLD);
	        Paragraph totalAmountParagraph = new Paragraph("TOTAL A PAGAR (€): " + currencyFormat.format(totalAmount + precioEnvio), fontBold);
	        totalAmountParagraph.setAlignment(Paragraph.ALIGN_RIGHT);
	        document.add(totalAmountParagraph);
	        document.close();
	    } catch (Exception e) {
	        System.err.println("Error generating PDF: " + e.getMessage());
	    }
	}	
	
	 public void generatePedidosPdf(HttpServletResponse response, List<Pedido> pedidos, String subtituloFechas) {
	        try {
	            Document document = new Document(PageSize.A4);
	            PdfWriter.getInstance(document, response.getOutputStream());
	            document.open();		        
	            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
	            Paragraph title = new Paragraph("Lista de Pedidos", titleFont);
	            title.setAlignment(Paragraph.ALIGN_CENTER);
	            document.add(title);
	            document.add(Paragraph.getInstance(" ")); 	            
	            if (subtituloFechas != null) {
	                Font subtitleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	                Paragraph subtitle = new Paragraph(subtituloFechas, subtitleFont);
	                subtitle.setAlignment(Paragraph.ALIGN_CENTER);
	                document.add(subtitle);
	                document.add(Paragraph.getInstance(" ")); 
	            }	            
	            PdfPTable table = new PdfPTable(6);
	            table.setWidthPercentage(100);
	            table.setWidths(new float[]{1, 2, 3, 2, 3, 4}); 	          
	            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
	            table.addCell(new Paragraph("Id", headerFont));
	            table.addCell(new Paragraph("Fecha Creación", headerFont));
	            table.addCell(new Paragraph("Número", headerFont));
	            table.addCell(new Paragraph("Total (EUR)", headerFont));
	            table.addCell(new Paragraph("Estado", headerFont));
	            table.addCell(new Paragraph("Cliente", headerFont));
	            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
	            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("es", "ES"));	         
	            for (Pedido pedido : pedidos) {
	                table.addCell(String.valueOf(pedido.getId()));
	                table.addCell(pedido.getFechaCreacion().format(formatter));
	                table.addCell(pedido.getNumero());
	                table.addCell(currencyFormat.format(pedido.getTotal()));
	                table.addCell(pedido.getEstadoEnum().toString());
	                table.addCell(pedido.getUsuario().getNombre());
	            }	            
	            document.add(table);
	            document.close();
	        } catch (Exception e) {
	            System.err.println("Error generating PDF: " + e.getMessage());
	        }
	    }

}
