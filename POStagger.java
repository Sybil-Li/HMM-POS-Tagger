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
		// cross validation
		// should be a number between 0-9 inclusive
		// 0 means x00-x09 will be testset
		// if using whole set then set tset = 10
		int tset = 2;

		// Read file content into String
		for (int i = 2; i < 10; i++){
			for (int j = i*100; j < i*100+10*tset; j++) {
				Path file_path = Paths.get("processed/WSJ_0" + j + ".POS");
				trainModel(file_path);
			}	

			for (int j = i*100+10*tset+10; j < i*100+100; j++) {
				Path file_path = Paths.get("processed/WSJ_0" + j + ".POS");
				trainModel(file_path);
			}	
		}
		for (int i = 10; i < 13; i++){
			for (int j = i*100; j < i*100+10*tset; j++) {
				Path file_path = Paths.get("processed/WSJ_" + j + ".POS");
				trainModel(file_path);
			}	

			for (int j = i*100+10*tset+10; j < i*100+100; j++) {
				Path file_path = Paths.get("processed/WSJ_" + j + ".POS");
				trainModel(file_path);
			}
		}

		// testing code
		// trainModel(Paths.get("processed/WSJ_0200.POS"));
		System.out.println("catTotal: " + catTotal.size());
		System.out.println("wordTotal: " + wordTotal.size());

		// after training update set of tags
		Object[] temp = catTotal.keySet().toArray();
		allTags = new String[temp.length];
		for (int i = 0; i < temp.length; i++){
			allTags[i] = (String) temp[i];
		}

		// for (Map.Entry<String,HashMap<String,Integer>> entry : cat2cat.entrySet()) {
		// 	String key = entry.getKey();
		// 	HashMap<String,Integer> value = entry.getValue();
		// 	System.out.println("category: "  + key);
		// 	// for (Map.Entry<String,Integer> occurences : value.entrySet()) {
		// 	// 	String k = occurences.getKey();
		// 	// 	Integer v = occurences.getValue();
		// 	// 	System.out.println(k + " = " + v);
		// 	// }
		// }

		int totalpredicted = 0;
		int totalcorrect = 0;

		for (int i = 2; i < 10; i++){
			for (int j = i*100+10*tset; j < i*100+10*tset+10; j++) {
				Path file_path = Paths.get("processed/WSJ_0" + j + ".POS");
				int[] accuracy = testModel(file_path);
				System.out.println("tested processed/WSJ_0" + j + ".POS");
				totalpredicted += accuracy[0];
				totalcorrect += accuracy[1];
			}	
		}
		for (int i = 10; i < 13; i++){
			for (int j = i*100+10*tset; j < i*100+10*tset+10; j++) {
				Path file_path = Paths.get("processed/WSJ_" + j + ".POS");
				int[] accuracy = testModel(file_path);
				System.out.println("tested processed/WSJ_" + j + ".POS");
				totalpredicted += accuracy[0];
				totalcorrect += accuracy[1];
			}	
		}

		System.out.println("total number of words: " + totalpredicted);
		System.out.println("correctly predicted: " + totalcorrect);
		System.out.println("percentage: " + totalcorrect/1.0/totalpredicted);


		// testing code
		// int[] accuracy = testModel(Paths.get("processed/WSJ_0200.POS"));
		// System.out.println("total number of words: " + accuracy[0]);
		// System.out.println("correctly predicted: " + accuracy[1]);

	}

	public static void trainModel(Path file_path) {
		try {
			BufferedReader br = Files.newBufferedReader(file_path, StandardCharsets.UTF_8);
			// get each word-tag pair from file
			String prevCat = "**start**";
			String line = null;
			while ((line = br.readLine()) != null) {
				String[] pair = line.split("\\/");
				String[] tags = pair[pair.length-1].split("\\|");
				// iterate through each pair of words and tags
				for (int j = 0; j < tags.length; j++) {
					String tag = tags[j];
					for (int i = 0; i < pair.length-1; i++) {
						String word = pair[i].substring(0, pair[i].length()-1);
						updateCount(word, tag, prevCat);
					}
					prevCat = updateCount(pair[pair.length-2], tag, prevCat);
				}
			}
		} catch (IOException e) {
	    	System.out.println(e);
	    }
	}

	public static String updateCount(String word, String tag, String prevCat){
		// update word-category count
		if (!wordTotal.containsKey(word)){
			// add word to collection of words
			wordTotal.put(word, 1);
			// add word to word2cat map
			word2cat.put(word, new HashMap<String, Integer>());
			// add pair to word2cat count
			word2cat.get(word).put(tag, 1);
		}
		else{
			// increment word occurrence
			wordTotal.put(word, wordTotal.get(word)+1);
			// if word-category pair not yet exists
			if (!word2cat.get(word).containsKey(tag)){
				word2cat.get(word).put(tag, 1);
			}
			// if exists
			else {
				int count = word2cat.get(word).get(tag);
				word2cat.get(word).put(tag, count+1);
			}
		}

		// update category-category count
		if (!catTotal.containsKey(tag)){
			// add cat tag to collection of tags
			catTotal.put(tag, 1);
			// add cat to cat2cat map
			cat2cat.put(tag, new HashMap<String, Integer>());
			// add pair to word2cat count
			cat2cat.get(tag).put(prevCat, 1);
		}
		else {
			// increment category occurrence
			catTotal.put(tag, catTotal.get(tag)+1);
			// if category-category pair not yet exists
			if (!cat2cat.get(tag).containsKey(prevCat)){
				cat2cat.get(tag).put(prevCat, 1);
			}
			// if exists
			else {
				int count = cat2cat.get(tag).get(prevCat);
				cat2cat.get(tag).put(prevCat, count+1);
			}
		}
		// update prevCat
		if (word.equals(".")){
			prevCat = "**start**";
		}
		else {
			prevCat = tag;
		}

		return prevCat;
	}

	public static int[] testModel(Path file_path) {
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
	    
	    // debugging code for word-tag split
	    // for (int i = 0; i < tokens.length; i++){
		// 	System.out.println(words_tags[i][0] + " " + words_tags[i][1]);
		// }
	    String[] results = Viterbi(words_tags);

	    // to be returned
	    // accuracy[0] stores total number of words tagged
	    // accuracy[1] stores number of correctly tagged words
	    int[] accuracy = new int[2];
	    accuracy[0] = results.length;


	    //TODO: modify test for correct prediction
		for (int i = 0; i < results.length; i++){
			//System.out.println(results[i]);
			if (words_tags[i][1].contains(results[i])){
				accuracy[1]++;
			}
		}

		return accuracy;
	}

	public static String[] Viterbi(String[][] words_tags) {
		int K = allTags.length;
		int N = words_tags.length;
		double[][] score = new double[K][N];
		int[][] backpointer = new int[K][N];

		String w = getWord(words_tags[0][0]);
		String t = null;
		String prevt = "**start**";

		// initialization
		for (int i = 0; i < K; i++) {
			t = allTags[i];
			double pWordInCat = 0;
			if (word2cat.containsKey(w)){
				if (word2cat.get(w).containsKey(t)) {
					// calculate smoothed probability
					pWordInCat = -(Math.log(word2cat.get(w).get(t)+1)-Math.log(wordTotal.get(w)+K));
				}
				else {
					// calculate default probability
					pWordInCat = -(Math.log(1)-Math.log(wordTotal.get(w)+K));
				}
			}
			else {
				pWordInCat = 0;
			}
			
			//System.out.println(pWordInCat);

			double pCatAfterCat = 0; 
			if (cat2cat.get(t).containsKey(prevt)) {
				// calculate smoothed probability
				pCatAfterCat = -(Math.log(cat2cat.get(t).get(prevt)+1)-Math.log(catTotal.get(t)+K));
			}
			else {
				// calculate default probability
				pCatAfterCat = -(Math.log(1)-Math.log(catTotal.get(t)+K));
			}
			//System.out.println(pCatAfterCat);

			// calculate score (NLL)
			score[i][0] = pWordInCat+pCatAfterCat;
			//System.out.println(score[i][0]);
		}

		// induction
		for (int j = 1; j < N; j++) {
			w = getWord(words_tags[j][0]);
			//System.out.println(w + " " +word2cat.containsKey(w));
			for (int i = 0; i < K; i++) {
				t = allTags[i];
				for (int k = 0; k < K; k++) {
					prevt = allTags[k];
					double pWordInCat = 0;
					if (word2cat.containsKey(w)) {
						if (word2cat.get(w).containsKey(t)) {
							// calculate smoothed probability
							pWordInCat = -(Math.log(word2cat.get(w).get(t)+1)-Math.log(wordTotal.get(w)+K));
						}
						else {
							// calculate default probability
							pWordInCat = -(Math.log(1)-Math.log(wordTotal.get(w)+K));
						}
					}
					else {
						pWordInCat = 0;
					}	

					double pCatAfterCat = 0; 
					if (cat2cat.containsKey(t) && cat2cat.get(t).containsKey(prevt)) {
						// calculate smoothed probability
						pCatAfterCat = -(Math.log(cat2cat.get(t).get(prevt)+1)-Math.log(catTotal.get(t)+K));
					}
					else {
						// calculate default probability
						pCatAfterCat = -(Math.log(1)-Math.log(catTotal.get(t)+K));
					}

					// update score and backpointer
					if (pWordInCat+pCatAfterCat+score[k][j-1] < score[i][j] || score[i][j] == 0) {
						score[i][j] = pWordInCat+pCatAfterCat+score[k][j-1];
						backpointer[i][j] = k;
					}
				}
			}
		}

		// for (int i = 0; i < K; i++) {
		// 	for (int j = 1; j < N; j++) {
		// 		System.out.print(backpointer[i][j] + " ");		
		// 	}
		// 	System.out.println();
		// }
		// for (int i = 0; i < K; i++) {
		// 	for (int j = 1; j < N; j++) {
		// 		System.out.print(score[i][j]);
		// 	}
		// 	System.out.println();
		// }

		// backtracing
		int maxt = 0;
		for (int i = 1; i < K; i++){
			if (score[i][N-1] < score[maxt][N-1]){
				maxt = i;
			}
		}
		String [] results = new String[N];
		results[N-1] = allTags[backpointer[maxt][N-1]];
		for (int i = N-2; i >= 0; i--){
			maxt = backpointer[maxt][i+1];
			results[i] = allTags[maxt];
		}
		return results;
	}

	/** 
	 * Get the first word from the string in form of "word1\/word2\/..."
	 * All words in string have the same tag
	 * @param input string
	 * @return first word without \
	 */
	public static String getWord(String s){
		String[] words = s.split("\\/");
		if (words[0].charAt(words[0].length()-1) != '\\') {
			return words[0];
		}
		return words[0].substring(0, words[0].length()-1);
	}

}
		
