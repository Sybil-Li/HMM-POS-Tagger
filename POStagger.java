import java.io.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public class POStagger {
	// stores total number of occurences of each category and each word
	private static HashMap<String, Integer> catTotal = new HashMap<String, Integer>();
	private static HashMap<String, Integer> wordTotal = new HashMap<String, Integer>();
	// stores number of occurences of ci after cj
	private static HashMap<String, HashMap<String, Integer>> cat2cat = new HashMap<String, HashMap<String, Integer>>();
	// stores number of occurences of w tagged as ci
	private static HashMap<String, HashMap<String, Integer>> word2cat = new HashMap<String, HashMap<String, Integer>>();
	private static String[] allTags = null;
	public static void main (String[] args) {
		// Read file content into String
		// for (int i = 2; i < 10; i++){
		// 	for (int j = i*100; j < i*100+100; j++) {
		// 		Path file_path = Paths.get("processed/WSJ_0" + j + ".POS");
		// 		trainModel(file_path);
		// 	}	
		// }
		// for (int i = 10; i < 13; i++){
		// 	for (int j = i*100; j < i*100+100; j++) {
		// 		Path file_path = Paths.get("processed/WSJ_" + j + ".POS");
		// 		trainModel(file_path);
		// 	}
		// }
		trainModel(Paths.get("processed/WSJ_0200.POS"));

		// after training update set of tags
		Object[] temp = catTotal.keySet().toArray();
		allTags = new String[temp.length];
		for (int i = 0; i < temp.length; i++){
			allTags[i] = (String) temp[i];
		}

		testModel(Paths.get("processed/WSJ_0200.POS"));
	}

	public static void trainModel(Path file_path) {
		try {
			BufferedReader br = Files.newBufferedReader(file_path, StandardCharsets.UTF_8);
			// get each word-tag pair from file
			String prevCat = "**start**";
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\/");
				// update word-category count
				if (!wordTotal.containsKey(pair[0])){
					// add word to collection of words
					wordTotal.put(pair[0], 1);
					// add word to word2cat map
					word2cat.put(pair[0], new HashMap<String, Integer>());
					// add pair to word2cat count
					word2cat.get(pair[0]).put(pair[1], 1);
				}
				else{
					// increment word occurrence
					wordTotal.put(pair[0], wordTotal.get(pair[0])+1);
					// if word-category pair not yet exists
					if (!word2cat.get(pair[0]).containsKey(pair[1])){
						word2cat.get(pair[0]).put(pair[1], 1);
					}
					// if exists
					else {
						int count = word2cat.get(pair[0]).get(pair[1]);
						word2cat.get(pair[0]).put(pair[1], count+1);
					}
				}

				// update category-category count
				if (!catTotal.containsKey(pair[1])){
					// add cat tag to collection of tags
					catTotal.put(pair[1], 1);
					// add cat to cat2cat map
					cat2cat.put(pair[1], new HashMap<String, Integer>());
					// add pair to word2cat count
					cat2cat.get(pair[1]).put(pair[1], 1);
				}
				else {
					// increment category occurrence
					catTotal.put(pair[1], catTotal.get(pair[1])+1);
					// if category-category pair not yet exists
					if (!cat2cat.get(pair[1]).containsKey(prevCat)){
						cat2cat.get(pair[1]).put(prevCat, 1);
					}
					// if exists
					else {
						int count = cat2cat.get(pair[1]).get(prevCat);
						cat2cat.get(pair[1]).put(prevCat, count+1);
					}
				}
				// update prevCat
				if (pair[0].equals(".")){
					prevCat = "**start**";
				}
				else {
					prevCat = pair[1];
				}
			}
		} catch (IOException e) {
	    	System.out.println("3 " +e);
	    }
	}

	public static void testModel(Path file_path) {
		String posString = null;
		try {
	    	byte[] posArray = Files.readAllBytes(file_path);

	    	posString = new String(posArray);
	    } catch (IOException e) {
	    	System.out.println(e);
	    }

	    String[] tokens = posString.split("\\n");
	    String[][] words_tags = new String[tokens.length][2];
	    for (int i = 0; i < tokens.length; i++){
	    	words_tags[i] = tokens[i].split("\\/");
	    }
	    for (int i = 0; i < tokens.length; i++){
	    	System.out.println(words_tags[i][0] + " " + words_tags[i][1]);
	    }
	    Viterbi(words_tags);
	}

	public static String[] Viterbi(String[][] words_tags) {
		int K = allTags.length;
		int N = words_tags.length;
		double[][] score = new double[K][N];
		int[][] backpointer = new int[K][N];

		String w = words_tags[0][0];
		String t = null;
		String prevt = "**start**";

		// initialization
		for (int i = 0; i < K; i++) {
			t = allTags[i];
			double pWordInCat = 0;
			if (word2cat.get(w).containsKey(t)) {
				// calculate smoothed probability
				pWordInCat = (word2cat.get(w).get(t)+1)/(wordTotal.get(w)+K);
			}
			else {
				// calculate default probability
				pWordInCat = 1/(wordTotal.get(w)+K);
			}

			double pCatAfterCat = 0; 
			if (cat2cat.get(t).containsKey(prevt)) {
				// calculate smoothed probability
				pCatAfterCat = (cat2cat.get(t).get(prevt)+1)/(catTotal.get(t)+K);
			}
			else {
				// calculate default probability
				pCatAfterCat = 1/(catTotal.get(t)+K);
			}

			// calculate score
			score[i][0] = pWordInCat*pCatAfterCat;
		}

		// induction
		for (int j = 1; j < N; j++) {
			w = words_tags[0][j];
			for (int i = 0; i < K; i++) {
				t = allTags[i];
				for (int k = 0; k < K; k++) {
					prevt = allTags[k];
					double pWordInCat = 0;
					if (word2cat.containsKey(w) && word2cat.get(w).containsKey(t)) {
						// calculate smoothed probability
						pWordInCat = (word2cat.get(w).get(t)+1)/(wordTotal.get(w)+K);
					}
					else {
						// calculate default probability
						pWordInCat = 1/(wordTotal.get(w)+K);
					}

					double pCatAfterCat = 0; 
					if (cat2cat.containsKey(t) && cat2cat.get(t).containsKey(prevt)) {
						// calculate smoothed probability
						pCatAfterCat = (cat2cat.get(t).get(prevt)+1)/(catTotal.get(t)+K);
					}
					else {
						// calculate default probability
						pCatAfterCat = 1/(catTotal.get(t)+K);
					}

					// update score and backpointer
					if (pWordInCat*pCatAfterCat > score[i][j]) {
						score[i][j] = pWordInCat*pCatAfterCat;
						backpointer[i][j] = k;
					}
				}
			}
		}

		// backtracing
		int maxt = 0;
		for (int i = 1; i < N; i++){
			if (score[N-1][i] > score[maxt][N-1]){
				maxt = i;
			}
		}
		String [] results = new String[N];
		results[N-1] = allTags[backpointer[maxt][N-1]];
		for (int i = N-2; i > 0; i--){
			maxt = backpointer[maxt][i+1];
			results[i] = allTags[maxt];
		}
		return results;
	}

}
		
