package com.dejavu.extarctor;

import static org.bytedeco.javacpp.lept.pixDestroy;
import static org.bytedeco.javacpp.lept.pixRead;
import static org.bytedeco.javacpp.lept.pixReadMem;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.tika.Tika;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.lept.PIX;
import org.bytedeco.javacpp.tesseract.TessBaseAPI;
import org.springframework.stereotype.Service;

@Service
public class FileConverterService {

	public String getTextFromFile(File file) {

		String docType;
		try {
			docType = this.getFileType(file);
			if ("application/pdf".equals(docType)) {
				return this.getTextFromPDF(file);
			} else {
				return this.getTextFromImage(file, null);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println(e.getMessage());
		}
		return null;
	}
	
	
	public String getTextFromPDF(File file) throws IOException {
	    
		PDDocument document = PDDocument.load(file);

		// Instantiate PDFTextStripper class
		PDFTextStripper pdfStripper = new PDFTextStripper();

		// Retrieving text from PDF document
		String fullText = pdfStripper.getText(document);
		boolean isEmpty = fullText.replaceAll("(\\r|\\n|\\t)", "").isEmpty();

		// Closing the document
		document.close();
		if(isEmpty == true ) {
			byte[]  imgArray= this.convertPdfToImage(file.getPath());
			fullText = this.getTextFromImage(file , imgArray);
		}
		file.delete();
		return fullText;

	}

	@SuppressWarnings("unused")
	private String getTextFromImage(File file, byte[] imgArray) {
		BytePointer outText;
		String filePath = file.getPath();
		TessBaseAPI api = new TessBaseAPI();
		// Initialize tesseract-ocr with English, without specifying tessdata path
		if (api.Init(".", "ENG") != 0) {
			System.err.println("Could not initialize tesseract.");
			System.exit(1);
		}
		
		// Open input image with leptonica library
		PIX image = pixRead(filePath);
		if(imgArray != null) {
			image = pixReadMem(imgArray,imgArray.length);
		}
		else {
			image = pixRead(filePath);
		}
		image = pixRead(filePath);
		api.SetImage(image);
		// Get OCR result
		outText = api.GetUTF8Text();
		String string = outText.getString();
		// System.out.println("OCR output:\n" + string);

		// Destroy used object and release memory
		api.End();
		outText.deallocate();
		pixDestroy(image);
		file.delete();
		return string;
	}

	private byte[] convertPdfToImage(String doc) throws IOException {
		//Loading an existing PDF document
	      File file = new File(doc);
	      PDDocument document = PDDocument.load(file);
	       
	      //Instantiating the PDFRenderer class
	      PDFRenderer renderer = new PDFRenderer(document);

	      //Rendering an image from the PDF document
	      BufferedImage image = renderer.renderImage(0);
	      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	      //Writing the image to a file
	      ImageIO.write(image, "JPEG", outputStream);
	      ImageIO.write(image, "PNG", new File("C:\\Users\\pipa\\Desktop\\ocrTestData\\exm.png")); 
	      System.out.println("Image created");
	       
	      //Closing the document
	      document.close();
	      return outputStream.toByteArray();

	}
	private String getFileType(File file) throws IOException {

		// Instantiating tika facade class
		Tika tika = new Tika();

		// detecting the file type using detect method
		String filetype = tika.detect(file);
		System.out.println(filetype);
		return filetype;
	}
}
