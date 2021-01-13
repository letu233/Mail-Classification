import java.util.ArrayList;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.HashSet;
public class Classification {
	
	public static ArrayList<String> readData(String url){
		ArrayList<String> bag = new ArrayList<>();
		FileInputStream fileInputStream = null;
        BufferedReader bufferedReader = null;
        try {
			fileInputStream = new FileInputStream(url);
			bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
			String line = bufferedReader.readLine();
			while(line!=null) {
				bag.add(line);
				//System.out.println(line);
                line = bufferedReader.readLine();
			}
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			try {
                bufferedReader.close();
                fileInputStream.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
		}
        return bag;
	}
	
	public static HashSet<String> getBagWords(ArrayList<String> list1, ArrayList<String> list2){
		HashSet<String> s = new HashSet<>();
		for(int i=0;i<list1.size();i++) {
			String sen = list1.get(i);
			String[] words = sen.split("\\s+");
			for(String word: words) {
				if(!s.contains(word)) s.add(word);
			}
		}
		for(int i=0;i<list2.size();i++) {
			String sen = list2.get(i);
			String[] words = sen.split("\\s+");
			for(String word: words) {
				if(!s.contains(word)) s.add(word);
			}
		}
		return s;
	}
	
	//count frequency of each word
	public static HashMap<String,Integer> countAppearance(ArrayList<String> bag){
		HashMap<String,Integer> map = new HashMap<>();
		for(int i=0;i<bag.size();i++) {
			String sentence = bag.get(i);
			String[] words = sentence.split("\\s+");
			HashSet<String> set = new HashSet<>();
			for(String word:words) {
				if(!set.contains(word)) {
					set.add(word);
				}
			}
			for(String s:set) {
				String temp=s;
				if(!map.containsKey(temp)) {
					map.put(temp, 1);
				}else {
					map.put(temp,map.get(temp)+1);
				}
			}
		}
		return map;
	}
	
	//calculate probability of 1 word
	public static double calculate(int tu,int mau) {
		return (double)(tu+1)/(mau+1);
	}
	public static int predict(ArrayList<String> allword_list, int nonspambagSize, int spambagSize, 
			int[] vector_test, double nonspam_pro, double spam_pro,
			HashMap<String,Integer> nonspam_map, HashMap<String,Integer> spam_map) {
		
		int label = 0;
		// calculate p(xi|Cj=nonspam)
		for(int i=0;i<allword_list.size();i++) {
			if(vector_test[i]==0) {
				if(nonspam_map.containsKey(allword_list.get(i))){
					int nonspam_appear = nonspambagSize - nonspam_map.get(allword_list.get(i));
					double appear_pro = calculate(nonspam_appear, nonspambagSize);
					nonspam_pro += Math.log(appear_pro);
				}
			}else {
				if(nonspam_map.containsKey(allword_list.get(i))) {
					double nonspam_appear = calculate(nonspam_map.get(allword_list.get(i)), nonspambagSize);
					nonspam_pro += Math.log(nonspam_appear);
				}else {
					nonspam_pro += Math.log(calculate(0, nonspambagSize));
				}
			}
		}
		// calculate p(xi|Cj=spam)
		for(int i=0;i<allword_list.size();i++) {
			if(vector_test[i]==0) {
				if(spam_map.containsKey(allword_list.get(i))) {
					int spam_appear = spambagSize - spam_map.get(allword_list.get(i));
					double appear_pro = calculate(spam_appear, spambagSize);
					spam_pro += Math.log(appear_pro);
				}
			}else {
				if(spam_map.containsKey(allword_list.get(i))) {
					double spam_appear = calculate(spam_map.get(allword_list.get(i)), spambagSize);
					spam_pro += Math.log(spam_appear);
				}else {
					spam_pro += Math.log(calculate(0, spambagSize));
				}
			}
		}
		if(spam_pro>nonspam_pro) label=1;
		else label=0;
		return label;
	}
	
	//create vector that have value 0 or 1
	//0 means that word doesn't appear on the list
	//1 means that word does appears on the list
	public static int[] createVector(ArrayList<String> allword_list, String sentence) {
		int[] vectorWord = new int[allword_list.size()];
		String[] words = sentence.split("\\s+");
		for(int i=0;i<words.length;i++) {
			for(int j=0;j<allword_list.size();j++) {
				if(allword_list.get(j).equalsIgnoreCase(words[i])) {
					vectorWord[j]=1;
				}
			}
		}
		return vectorWord;
	}
	
	public static ArrayList<String> getLabelTest(ArrayList<String> list){
		ArrayList<String> res = new ArrayList<>();
		for(int i=0;i<list.size();i++) {
			String sentence = list.get(i);
			String[] words = sentence.split("\\s+");
			res.add(words[0]);
		}
		return res;
	}
	public static ArrayList<String> getDataTest(ArrayList<String> list){
		ArrayList<String> res = new ArrayList<>();
		for(int i=0;i<list.size();i++) {
			String sentence = list.get(i);
			String[] words = sentence.split("\\s+");
			if(words[0].equalsIgnoreCase("spam")) {
				sentence = list.get(i).substring(5);
			}else {
				sentence = list.get(i).substring(8);
			}
			res.add(sentence);
		}
		return res;
	}
	
	public static void main(String[] args) {
		String nonspam_data="src/non-spam.txt";
		String spam_data="src/spam.txt";
		ArrayList<String> nonspam_bag = new ArrayList<String>(readData(nonspam_data));
		ArrayList<String> spam_bag = new ArrayList<String>(readData(spam_data));
		HashSet<String> allword_set = new HashSet<>(getBagWords(nonspam_bag, spam_bag));
		//Sentences in each bag
		int nonspam_bag_length = nonspam_bag.size();
		int spam_bag_length = spam_bag.size();
		
		HashMap<String,Integer> nonspam_map = new HashMap<>(countAppearance(nonspam_bag));
		HashMap<String,Integer> spam_map = new HashMap<>(countAppearance(spam_bag));
		ArrayList<String> allword_list = new ArrayList<String>(allword_set);

		// probability of Cj
		double nonspam_pro = Math.log(calculate(nonspam_bag.size(), (nonspam_bag.size()+spam_bag.size())));
		double spam_pro = Math.log(calculate(spam_bag.size(), (nonspam_bag.size()+spam_bag.size())));
		
		//load test data
		String fulltest_data="src/fulltest.txt";
		ArrayList<String> raw  = new ArrayList<>(readData(fulltest_data));
		ArrayList<String> label = new ArrayList<>(getLabelTest(raw));
		ArrayList<String> data = new ArrayList<>(getDataTest(raw));
		int count=0;
		for(int i=0;i<data.size();i++) {
			String sentence = data.get(i);
			int[] vector_test = createVector(allword_list, sentence);
			//predict new sentence
			int res = predict(allword_list, nonspam_bag_length, spam_bag_length, vector_test, nonspam_pro, spam_pro, nonspam_map, spam_map);
			if(res==1 && label.get(i).equalsIgnoreCase("spam")) {
				count++;
			}
			if(res==0 && label.get(i).equalsIgnoreCase("nonspam")) {
				count++;
			}
		}
		
		System.out.println("Accuracy: "+(double) count/label.size());
		

		
	}
}
