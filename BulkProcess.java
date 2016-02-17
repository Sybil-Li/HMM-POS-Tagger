import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class BulkProcess {
	public static void main (String[] args) {
		// Read file content into String
		// try {
		// 	Files.walk(Paths.get("/Users/Sybil/Documents/College/Oxford/Computational Linguistics/Practical/WSJ-2-12")).forEach(file_path -> {
		// 		try {
		// 			Files.walk(Paths.get(file_path.toString())).forEach(filePath -> {
		// 			    processFile(filePath);
		// 			});
		// 		} catch (IOException e) {
		// 	    	System.out.println("1 " + e);
		// 		}
		// 	});
		// } catch (IOException e) {
		// System.out.println("2 " + e);
		// }
		for (int i = 2; i < 10; i++){
			for (int j = i*100; j < i*100+100; j++) {
				Path file_path = Paths.get("WSJ-2-12/0" + i, "WSJ_0" + j + ".POS");
				processFile(file_path);
			}	
		}
		for (int i = 10; i < 13; i++){
			for (int j = i*100; j < i*100+100; j++) {
				Path file_path = Paths.get("WSJ-2-12/" + i, "WSJ_" + j + ".POS");
				processFile(file_path);
			}
		}

	}

	public static void processFile(Path file_path) {
		String posString = "";
		try {
	    	byte[] posArray = Files.readAllBytes(file_path);

	    	posString = new String(posArray);
	    	//System.out.println(posString);
	    } catch (IOException e) {
	    	System.out.println("3 " +e);
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
			File file = new File("processed/"+file_path.getFileName().toString());

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