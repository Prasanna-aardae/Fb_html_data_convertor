package fb_html_converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import org.apache.tika.langdetect.OptimaizeLangDetector;
import org.apache.tika.language.detect.LanguageDetector;
import org.apache.tika.language.detect.LanguageResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;

public class FbHtmlConvertor {
	public static void main(String[] args) throws IOException, ParseException, DocumentException {
		String file = "./sample/sample.zip";
		UnZip unZip = new UnZip(file);
		String dataFolder = unZip.fileToFolder();
		System.out.println(dataFolder);
		File htmlFile = getHtmlFile(dataFolder);
		System.out.println(htmlFile);
		htmlReader(htmlFile);
	}
	
	private static File getHtmlFile(String folder) {
		File file = new File(folder);
		File[] listOfFiles = file.listFiles();
		for (File files : listOfFiles) {
			FileSeparator jsonFile = new FileSeparator(files.getName(), '/', '.');
			if (jsonFile.extension().compareTo("posts") == 0) {
				for (File fi : files.listFiles()) {
					String[] tokens = fi.getName().split("_");
					if (Arrays.asList(tokens).contains("posts")) {
						return fi;
					}
				}
			}
		}
		return file;
	}
	
	public static void htmlReader(File htmlFile) throws ParseException, IOException, DocumentException  {
		try (FileReader reader = new FileReader(htmlFile)) {
			BufferedReader htmlDatas = new BufferedReader(reader);
			String content = "";
			String str;
	        while ((str = htmlDatas.readLine()) != null) {
	            content +=str;
	        }
	        separateElementByRole(content);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private static void separateElementByRole(String content) throws ParseException, DocumentException, IOException {
		List<String> divArray = new ArrayList<String>();
        org.jsoup.nodes.Document html = Jsoup.parse(content);
        Elements role = html.body().getElementsByAttribute("role");
		String[] separateByDiv = role.toString().split("</div>");
		for (String div : separateByDiv) {
			divArray.add(div);
		}
	    addValueToHash(divArray);
	}
	
	public static void addValueToHash(List<String> divArray) throws ParseException, DocumentException, IOException {
	    Random random = new Random();
		File myObj = new File("./sample/sample_output_" + random.nextInt(50) + ".pdf");
		myObj.createNewFile();
	    FileOutputStream file = new FileOutputStream(myObj);
	    com.itextpdf.text.Document document = new Document();
	    PdfWriter.getInstance((com.itextpdf.text.Document) document, file);
	    document.open();
	    Map<String, String> mainValues = new HashMap<String, String>();
		List<String> imgArray = new ArrayList<String>();
		String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
		for (int r=0; r<divArray.size(); r++) {
	    	org.jsoup.nodes.Document htmls = Jsoup.parse(divArray.get(r));
	    	if(htmls.body().text().isEmpty() != true) {
		    	Elements imageElement = htmls.body().getElementsByTag("img");
		    	Pattern p = Pattern.compile("src=\"(.*?)\"");
		        Matcher m = p.matcher(imageElement.toString());
	    		String[] sentenceToWord = htmls.body().text().split(" ");
	    		if(sentenceToWord.length == 4 && Arrays.asList(monthNames).contains(sentenceToWord[0])) {
	    			if(isValidDate(dateTimeCorrector(sentenceToWord))) {
		    			mainValues.put("dateTime", "Date & time : " +htmls.body().text());
			    		pushValuesToDoc(mainValues, document);
		    			document.newPage();
	    			}
	    		}else if(imageElement.toString().isEmpty() != true && m.find()) {
			        System.out.println(m.group(1));
					Image img = Image.getInstance("./sample/sample/"+ m.group(1));
	
			float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
					- document.rightMargin() - 2) / img.getWidth()) * 70;
	
			img.scalePercent(scaler);
			document.add(img);
	    		}else{
	    			mainValues.put("post", "Post : " +htmls.body().text());
	    		}

	    	}
	    }
		document.close();
	    file.close();
	}
	
	private static void pushValuesToDoc(Map<String, String> mainValues,Document document) throws ParseException, DocumentException, IOException{
		String[] sentenceToWord = mainValues.get("post").split(" ");
	    Paragraph para = new Paragraph();
		for (String word : sentenceToWord) {
			para.setFont(chooseFont(word));
			para.add(word+" ");
		}
		document.add(para);
		document.add(new Paragraph(mainValues.get("dateTime"), chooseFont("dateTime")));
	}
	
	private static Font chooseFont(String word) throws ParseException, DocumentException, IOException{
		Font font = null; 
		LanguageDetector languageDetector = new OptimaizeLangDetector().loadModels();
		LanguageResult result1 = languageDetector.detect(word);
    	if(result1.getLanguage().compareTo("ta") == 0) {
    		font = new Font(BaseFont.createFont("./font/Latha.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 10, Font.BOLD);
    		return font;
    	}else {
    		font = new Font(BaseFont.createFont("./font/times-ro.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED), 10, Font.BOLD);
    		return font;
    	}
	}
	
	
	private static String dateTimeCorrector(String[] sentenceToWord) throws ParseException{
			java.util.Date date = new SimpleDateFormat("MMM", Locale.ENGLISH).parse(sentenceToWord[0]);
			Calendar cal = Calendar.getInstance();
			int month = cal.get(Calendar.MONTH)+1;
			String time;
			String dateTime;
			if(sentenceToWord[3].contains("am")) {
				time = sentenceToWord[3].split("am")[0];
			}else {
				time = sentenceToWord[3].split("pm")[0];
			}
			if(month < 10) {
				dateTime = "0"+month+"-"+sentenceToWord[1].split(",")[0]+"-"+ sentenceToWord[2]+" "+ time;
			}else {
				dateTime = month+"-"+sentenceToWord[1].split(",")[0]+"-"+ sentenceToWord[2]+" "+ time;
			}
			return dateTime;
	}
	
	public static boolean isValidDate(String inDate) {
		System.out.println(inDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        dateFormat.setLenient(false);
        try {
            dateFormat.parse(inDate.trim());
        } catch (ParseException pe) {
            return false;
        }
        return true;
    }
}
