package fb_html_converter;

import java.io.IOException;

import org.apache.tika.language.detect.LanguageResult;

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.pdf.BaseFont;

public class FontChooser {
	private LanguageResult lang;

	public FontChooser(LanguageResult language) {
		lang = language;
	}
	
	public Font selectFont() throws DocumentException, IOException{
		Font font;
		BaseFont baseFont;
		switch(lang.getLanguage()) {
		  case "ta":
			baseFont = BaseFont.createFont("./font/TAM-005.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			font = new Font(baseFont, 10, Font.BOLD);
			return font;
		  case "hi":
			baseFont = BaseFont.createFont("./font/Amiko-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
			font = new Font(baseFont, 10, Font.BOLD);
			System.out.println("Prasanna");
			return font;
		default:
			baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, false);
			font = new Font(baseFont, 10, Font.BOLD);
			return font; 
		}
	}
}
