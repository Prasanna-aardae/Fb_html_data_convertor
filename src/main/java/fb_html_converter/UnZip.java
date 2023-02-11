package fb_html_converter;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class UnZip {
	private String fileName;

	public UnZip(String name) {
		fileName = name;
	}

	public String fileToFolder() throws IOException {
		int buffer = 4096;
		File file = new File(fileName);
		FileSeparator filePath = new FileSeparator(fileName, '/', '.');
		String extractFolder = filePath.path() + "/" + filePath.filename();
		try (ZipFile zip = new ZipFile(file)) {
			String newPath = extractFolder;
			new File(newPath).mkdir();
			@SuppressWarnings("rawtypes")
			Enumeration zipFileEntries = zip.entries();
			while (zipFileEntries.hasMoreElements()) {
				ZipEntry entry = (ZipEntry) zipFileEntries.nextElement();
				String currentEntry = entry.getName();
				currentEntry = currentEntry.replace('\\', '/');
				File destFile = new File(newPath, currentEntry);
				File destinationParent = destFile.getParentFile();
				destinationParent.mkdirs();
				if (!entry.isDirectory()) {
					BufferedInputStream is = new BufferedInputStream(zip.getInputStream(entry));
					int currentByte;
					byte[] data = new byte[buffer];
					FileOutputStream fos = new FileOutputStream(destFile);
					BufferedOutputStream dest = new BufferedOutputStream(fos, buffer);
					while ((currentByte = is.read(data, 0, buffer)) != -1) {
						dest.write(data, 0, currentByte);
					}
					dest.flush();
					dest.close();
					is.close();
				}
			}
		}
		return extractFolder;
	}
}
