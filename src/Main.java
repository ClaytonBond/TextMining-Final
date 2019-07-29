import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class Main {

	public static ArrayList<String> filePaths = new ArrayList<>();
	public static HashSet<String> skills = new HashSet<String>();
	public static HashSet<String> states = new HashSet<String>();
	
	public static void main(String[] args) throws IOException {
		
		FileWriter writer = new FileWriter("output.txt");
		findFile(".*html", new File(args[0]));
		buildHashSets();
		int count = 0;
		int sizeRestriction = (20 * 1024 * 1024); 	//20 represents the max size in MB the file can be, we consider anything larger to be unusable
		
		for(int i = 0; i < filePaths.size(); i++) {
			
			
			File file = new File(filePaths.get(i));
			
			if(file.length() < sizeRestriction) {
				
				ArrayList<String> words = HTMLToken.cleanRecords(filePaths.get(i));
				
				boolean state = false;
				boolean skill = false;
				
				for(String word : words) {
					if (states.contains(word)) {
						state = true;
					}
					if(skills.contains(word)) {
						skill = true;
					}
				}
				
				if(state && skill) {
					System.out.println(filePaths.get(i) + " \t " + ((file.length() / 1024) ) + "KB");
					writer.write(filePaths.get(i) + "\n");
					count++;
				}
				
			}
			
			else {
				System.out.println(filePaths.get(i) + " \t was too long at: " + ((file.length() / 1024) / 1024 ) + "MB");
			}
			
			
			
		}
		
		System.out.println("totalDocs = " + filePaths.size());
		System.out.println("totalKept = " + count);
		System.out.println("Docs eliminated = " + (filePaths.size() - count));
		
		writer.close();
		
	}
	
	public static void buildHashSets() throws IOException {
		
		BufferedReader stateReader = new BufferedReader(new FileReader("states.txt"));
		BufferedReader skillReader = new BufferedReader(new FileReader("skills.txt"));
		String line;
		
		while((line = stateReader.readLine()) != null) {
			states.add(line.toLowerCase());
		}
		
		while((line = skillReader.readLine()) != null) {
			skills.add(line.toLowerCase());
		}
		
		stateReader.close();
		skillReader.close();
		
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

}
