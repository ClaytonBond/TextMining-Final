/********************************
Name: Clayton Bond
Username: text06
Problem Set: PS2
Due Date: June 22, 2019
********************************/

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class TermContextMatrix {

	public static ArrayList<String> filePaths = new ArrayList<>();
	public static HashMap<String, Integer> index = new HashMap<>();
	public static HashMap<Integer, String> invIndex = new HashMap<>();
	public static HashMap<String, Integer> frequency;
	public static HashSet<String> stopwords = new HashSet<>();
	public static int[][] V;
	public static int window = 4;
	
	public static void main(String[] args) throws IOException {
		
		findFile(".*txt", new File(args[0]));
		
		if(!args[1].isEmpty()){
			window = Integer.parseInt(args[1]);
		}
		
		buildStopwords();
		buildFreq();
		buildV();

	}
	
	public static void displayMatrix() {
		for(String x : index.keySet()) {
			invIndex.put(index.get(x), x);
		}
		
		for(int i = 0; i < V.length; i++) {
			System.out.printf("%-15s \t | ", invIndex.get(i));
			for(int j = 0; j < V.length; j++) {
				System.out.printf("%4s ", V[i][j]);
			}
			System.out.println();
		}
	}

	public static void findFile(String name, File file) {
        File[] list = file.listFiles();
        
        if(list!=null) {
	        for (File fil : list) {
	            if (fil.isDirectory()) {
	                findFile(name,fil);
	            }
	            else if (fil.getName().matches(name)) {
	            	filePaths.add(fil.getAbsolutePath());
	            }
	        }
        }
    }
	
	public static String[] getContext(String a, int num) {
		Double[] top = new Double[num];
		String[] result = new String[num];
		
		for(int i = 0; i < num; i++) {
			top[i] = 0.0;
			result[i] = "";
		}
		
		for(String x : index.keySet()) {
			invIndex.put(index.get(x), x);
		}
		
		for(int i = 0; i < V.length; i++) {
			double temp = calcSimilarity(a, invIndex.get(i));
			
			String comp = invIndex.get(i);
			for(int j = 0; j < num; j++) {
				if(temp > top[j]) {
					double hold = top[j];
					top[j] = temp;
					temp = hold;
					
					String holdStr = result[j];
					result[j] = comp;
					comp = holdStr;
				}
			}
		}
		
		for(String x : result) {
			System.out.println(x);
		}
		
		return result;
	}
	
	public static double calcSimilarity(String a, String b) {
		double numerator = 0;
		double sumA = 0, sumB = 0;
		
		for(int i = 0; i < V.length; i++) {
			numerator += V[index.get(a)][i] * V[index.get(b)][i];
		}
		
		for(int i = 0; i < V.length; i++) {
			sumA += Math.pow(V[index.get(a)][i], 2);
			sumB += Math.pow(V[index.get(b)][i], 2);
		}
		
		sumA = Math.pow(sumA, 1/2);
		sumB = Math.pow(sumB, 1/2);
		
		return numerator / (sumA * sumB);
		
	}
	
	public static double calcPPMI(String a, String b) {
		double total = 0;
		double numerator = 0;
		double sumA = 0, sumB = 0;
		
		for(int i = 0; i < V.length; i++) {
			for(int j = 0; j < V.length; j++) {
				total += V[i][j];
			}
		}
		
		numerator = V[index.get(a)][index.get(b)] / total;
		
		for(int i = 0; i < V.length; i++) 
			sumA += V[index.get(a)][i];
		for(int i = 0; i < V.length; i++) 
			sumB += V[i][index.get(b)];
		
		double result = numerator / (  (sumA / total) * (Math.pow(sumB, 0.75) / Math.pow(total, 0.75))  );
	
		if(result > 0) {
			return result;
		}
		
		return 0;
		
	}
	
	public static void buildV() throws IOException {
		int size = frequency.size();
		V = new int[size][size];
		
		//Hashes all words to an index location in the array V -----------------------------------
		int count = 0;
		for(String w : frequency.keySet()) {
			index.put(w, count);
			count++;
		}
		
		String a[];
		int counter = 0;
		
		for(int i = 0; i < filePaths.size(); i++) {
			BufferedReader reader = new BufferedReader(new FileReader(filePaths.get(i)));
			String line;
			
			// - Build the term context matrix ------------------------------------------------
			while((line = reader.readLine()) != null) { 
				a = line.toLowerCase().replaceAll("[^a-z]", " ").split(" ");
				a = removeSW(a);
			
				
				for(int j = 0; j < a.length; j++) {
					//System.out.println(index.get(a[j]));
					
					try {
						for(int k = j; k < a.length ; k++) { //Scan forwards
							V[index.get(a[j])][index.get(a[k + 1])] += 1;
							//System.out.println("Bumped " + a[j] + " : " + a[k+1] + " by 2 ");
							counter++;
							if(counter == window) {
								//System.out.println("Broke out of  " + a[j]);
								k = a.length;
							}
						}
					}catch(Exception e) {
						//System.out.println("Broke out");
					}
					
					counter = 0;
					
					for(int k = j; k > 0; k--) { //Scan backwards
						V[index.get(a[j])][index.get(a[k - 1])] += 1;
						//System.out.println("Bumped " + a[j] + " : " + a[k-1] + " by 1");
						counter++;
						if(counter == window) {
							k = 0;
						}
					}
					
					counter = 0;
				}
			}
			
			reader.close();
			
		}

	}
	
	public static void buildFreq() throws IOException {
		int totalWords = 0;
		// - Get a quick word count -------------------------------------------------------------
		for(int i = 0; i < filePaths.size(); i++) {
			BufferedReader reader = new BufferedReader(new FileReader(filePaths.get(i)));
			String line;
			
			while((line = reader.readLine()) != null) {
				totalWords += line.split(" ").length;
			}
			
			reader.close();
		}
		
		/*
		 * --------------------------------------------------------------------------------------
		 * We're iterating over the files twice here to avoid having to store additional Strings 
		 * in memory
		 * --------------------------------------------------------------------------------------
		 */
		
		frequency = new HashMap<>(totalWords); //Prevents rehashing
		
		//Run for each file ---------------------------------------------------------------------
		for(int i = 0; i < filePaths.size(); i++) {
			BufferedReader reader = new BufferedReader(new FileReader(filePaths.get(i)));
			String line;
			
			//Read through and get word count to prevent rehashing -------------------------------
			while((line = reader.readLine()) != null) {
				line = line.toLowerCase().replaceAll("[^a-z]", " ");
				String[] tokens = line.split(" ");
				
				for(String w : tokens) {
					if(!stopwords.contains(w)) {
						if(!frequency.containsKey(w))
							frequency.put(w, 1);
						else 
							frequency.put(w, frequency.get(w) + 1);
					}
				}
			}
			
			reader.close();
		}
	}
	
	public static String[] removeSW(String[] a) {
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(a));
		
		//Remove stop words from array
		for(int j = 0; j < list.size(); j++) {
			if(stopwords.contains(list.get(j))) {
				list.remove(list.get(j));
				j--;
			}
		}
		
		a = list.toArray(new String[0]);
		return a;
	}
	
	public static void buildStopwords() {
		String[] builder = {"", " ", "i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", 
				"yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", 
				"herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", 
				"which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", 
				"be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", 
				"an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", 
				"for", "with", "about", "against", "between", "into", "through", "during", "before", "after", 
				"above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", 
				"again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", 
				"any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", 
				"only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", 
				"should", "now"};
		
		for(String x : builder) {
			stopwords.add(x);
		}
	}

}
