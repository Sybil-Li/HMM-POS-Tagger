import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class BulkProcess {
	public static void main (String[] args) {
		String posString = "";

		// Read file content into String
		Path file_path = Paths.get("WSJ-2-12/02", "WSJ_0200.POS");
		try {
	      byte[] posArray = Files.readAllBytes(file_path);

	      posString = new String(posArray);
	      //System.out.println(posString);
	    } catch (IOException e) {
	      System.out.println(e);
	    }

	    // split String to extract useful content
	    String [] tokens = posString.split(" ");
	    // for (int i = 0; i < tokens.length; i++) {
	    // 	if (!tokens[i].contains("[") && !tokens[i].contains("]") && !tokens[i].contains("========")) {
	    // 		System.out.println(tokens[i]);
	    // 	}
	    // }

	    // Write to a different file
	    try {
			File file = new File("WSJ_0200.txt");

			// if file doesnt exists, then create it
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			for (int i = 0; i < tokens.length; i++) {
		    	if (!tokens[i].contains("[") && !tokens[i].contains("]") && !tokens[i].contains("========")) {
		    		bw.write(tokens[i]);
		    		bw.newLine();
		    	}
		    }
			bw.close();

			System.out.println("Done");

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}