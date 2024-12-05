package com.dawes.serviciosImpl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadFileService {
	
	// despliegue
	private String folder = "/opt/images/";
	private final Path root = Paths.get("images");
	
	public UploadFileService() {
		if (!Files.exists(root)) {
			try {
				Files.createDirectory(root);
			} catch (IOException e) {
				System.out.println("No se pudo crear el directorio.");
				e.printStackTrace();
			}
		}
	}
	
	public String saveImage(MultipartFile file) throws IOException  {
		if (!file.isEmpty()) {			
			byte [] bytes = file.getBytes();
			Path path = Paths.get(folder+file.getOriginalFilename());
			Files.write(path, bytes);			 
			return file.getOriginalFilename();
		} else {
			File fileSource = new File("/opt/default.jpg");
			File fileDest = new File("/default.jpg");
			InputStream in = new FileInputStream(fileSource);
			Path path = Paths.get(folder+fileDest);
			Files.write(path, in.readAllBytes());
		}
		return "default.jpg";
	}
	
	public void deleteImage(String nombre) {
		String ruta = "/opt/images/";
		File file = new File(ruta+nombre);
		file.delete();
	      
	}

	
	
	// local
////	private static final Logger logger = LoggerFactory.getLogger(UploadFileService.class);
//	private String folder = "images/";
//	
//	public String saveImage(MultipartFile file) throws IOException  {
//		if (!file.isEmpty()) {			
//			byte [] bytes = file.getBytes();
//			Path path = Paths.get(folder+file.getOriginalFilename());
//			Files.write(path, bytes);
////			 logger.info("Saved image: " + path.toString());
//			return file.getOriginalFilename();
//		}
//		return "default.jpg";
//	}
//	
//	public void deleteImage(String nombre) {
//		String ruta = "images//";
//		File file = new File(ruta+nombre);
////		file.delete();
//		boolean deleted = file.delete();
////	        if (deleted) {
////	            logger.info("Deleted image: " + ruta + nombre);
////	        } else {
////	            logger.warn("Failed to delete image: " + ruta + nombre);
////	        }
//	}

}
