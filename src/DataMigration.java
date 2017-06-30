import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FilenameUtils;

public class DataMigration {

	private static final String RESOURCE_TO_BE_FOUND = "forwarding";
	private static final String XML_EXTENSION = "xml";
	private final static String DIRECTORY_PATH = "C:/Users/MyPath";
	public static int countAIR = 0;
	public static int countSEA = 0;
	private static int modifiedFiles = 0;
	public static Charset charset = StandardCharsets.UTF_8;

	public static void main(String[] args) {

		try {
			migrateData(DIRECTORY_PATH);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.err.println("Count AIR: " + countAIR);
		System.err.println("Count SEA: " + countSEA);
		System.err.println("Modified Files: " + modifiedFiles);
	}

	public static void migrateData(String directory) throws IOException {

		File dir = new File(directory);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				if (child.isDirectory()) {
					migrateData(child.getAbsolutePath());
				} else if (isXMLFile(child)) {
					findRecords(child);
				}
			}
		}
	}

	private static boolean isXMLFile(File child) {
		return child.isFile()
				&& child.getName().toLowerCase().contains(RESOURCE_TO_BE_FOUND.toLowerCase())
				&& FilenameUtils.getExtension(child.getAbsolutePath())
						.equalsIgnoreCase(XML_EXTENSION);
	}

	public static void findRecords(File file) throws IOException {
		BufferedReader r = new BufferedReader(new FileReader(file));
		StringBuilder content = new StringBuilder();
		String s = r.readLine();
		boolean theSame = true;
		boolean contains = false;

		while (s != null) {
			String modifiedLine = s;
			if (s.contains("<CONDITIONS")) {
				modifiedLine = replaceBusinessSolution(s);
				contains = true;
			}
			if (!modifiedLine.replaceAll("\\s+", "").equals(
					s.replaceAll("\\s+", ""))) {
				theSame = false;
			}
			content.append(modifiedLine);
			s = r.readLine();
			if (s != null) {
				content.append("\n");
			}

		}
		if (contains && !theSame) {
			modifiedFiles++;
			writeToFile(file, content);
		}

		r.close();
	}

	private static void writeToFile(File file, StringBuilder content)
			throws FileNotFoundException, UnsupportedEncodingException {
		String output = content.toString().trim();
		PrintWriter writer = new PrintWriter(file.getAbsolutePath());
		writer.print(output);
		writer.close();
	}

	private static String replaceBusinessSolution(String line) {
		String regexAir = ".*DTYPE(?s)=(?s)\"CA\".*";
		String regexSea = ".*DTYPE(?s)=(?s)\"CS\".*";

		
		if (line.contains("BUSINESS_SOLUTION_ID")) {
		
			if (line.matches(regexAir)) {
				line = line.replaceFirst("BUSINESS_SOLUTION_ID",
						"CA_BUSINESS_SOLUTION_ID");
				countAIR++;

			}
			if (line.matches(regexSea)) {
				line = line.replaceFirst("BUSINESS_SOLUTION_ID",
						"CS_BUSINESS_SOLUTION_IM_ID");
				countSEA++;
			}

		}
		return line;

	}

}
