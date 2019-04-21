package com.example.chatbot;

import android.content.res.AssetManager;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import android.content.Context;
import android.util.Log;

class StringMatcher {
	private ArrayList<String>[] dict;
	private HashMap<String, String> qs;
	private Context files;
	private static int treshold = 50;

	public String no_ans = "Maaf, kurang ngerti maksud pertanyaannya...";

	public StringMatcher(Context context) {
		//INISIALISASI ASSETS
		files = context;

		//INISIALISASI SINONIM
		dict = initDict();
		//INISIALISASI DATABASE PERTANYAAN
		qs = initDB();
	}

	public String getQuestion(String ans){
		for (String q : qs.keySet()) {
			if(qs.get(q).equals(ans)){
				return q;
			}
		}
		return "";
	}

	public ArrayList<String> answerQuery(String inp1,int inp2) {
		//MENERIMA INPUT PENGGUNA
		ArrayList<String> answer = new ArrayList<String>();
		String text = removeStopWords(synonim(inp1.substring(0, inp1.length() - 1), dict));
		if (inp2 == 1) {
			float p = -1;
			for (String q : qs.keySet()) {
				if (inp1.length() > removeStopWords(q).length()) {
					p = kmpMatch(text, removeStopWords(q));
					//System.out.println("p " + p);
					Log.d("1 : ",Float.toString(p));
					if (p >= treshold) { //TENTATIVE
						answer.add(qs.get(q));
					}
				} else {
					p = kmpMatch(removeStopWords(q), text);
					//System.out.println("p " + p);
					Log.d("2 : ",Float.toString(p));
					if (p >= treshold) { //TENTATIVE
						answer.add(qs.get(q));
					}
				}
			}
			if (p == -1) {
				for (String q : qs.keySet()) {
					p = alternativeFunc(text, removeStopWords(q));
					Log.d("3 : ",Float.toString(p));
					//System.out.println("p " + p);
					if (p >= treshold) {
						answer.add(qs.get(q));
					}
				}
			}
		} else if (inp2 == 2) {
			float p = -1;
			for (String q : qs.keySet()) {
				if (inp1.length() > removeStopWords(q).length()) {
					p = bmMatch(text, removeStopWords(q));
					//System.out.println("p " + p);
					if (p >= treshold) { //TENTATIVE
						answer.add(qs.get(q));
					}
				} else {
					p = bmMatch(removeStopWords(q), text);
					//System.out.println("p " + p);
					if (p >= treshold) { //TENTATIVE
						answer.add(qs.get(q));
					}
				}
			}
			if (p == -1) {
				for (String q : qs.keySet()) {
					p = alternativeFunc(text, removeStopWords(q));
					if (p >= treshold) {
						answer.add(qs.get(q));
					}
				}
			}
		} else if (inp2 == 3) {
			for (String q : qs.keySet()) {
				String[] exploded = q.split(" ");
				String pat = "(?i)(.*";
				for (String expl : exploded) {
					pat += expl + ".*";
				}
				pat += ")";
				Pattern p = Pattern.compile(pat);
				Matcher m = p.matcher(inp1);
				boolean valid = m.find();
				valid = valid && compareStrings(q,inp1,1)>=treshold;
				valid = valid && compareStrings(q,inp1,2)>=treshold;
				if (valid) {
					answer.add(qs.get(q));
					break;
				}
			}
		} else {
			System.out.println("Masukan salah");
		}
		if(answer.size()==0){
			answer.add(no_ans);
		}
		return answer;
	}

	public float compareStrings(String text, String pattern, int inp){
		float p = 0;
		if (inp == 1) {
			if (text.length() > pattern.length()) {
				p = kmpMatch(text, pattern);
			} else {
				p = kmpMatch(pattern, text);
			}
			if (p == 0) {
				p = alternativeFunc(text, pattern);
			}
		} else if (inp == 2) {
			if (text.length() > pattern.length()) {
				p = bmMatch(text, pattern);
			} else {
				p = bmMatch(pattern, text);
			}
			if (p == 0) {
				p = alternativeFunc(text, pattern);
			}
		}
		return p;
	}

	public float kmpMatch(String text, String pattern) {
		text = text.toLowerCase();
		pattern = pattern.toLowerCase();
		//System.out.println("text " + text);
		//System.out.println("pattern " + pattern);
		int n = text.length() - 1;
		int m = pattern.length() - 1;
		int fail[] = preProcessKMP(pattern);
		int i = 0;
		int j = 0;
		while (i < n) {
			if (pattern.charAt(j) == text.charAt(i)) {
				if (j == m - 1) {
					return (float) m / (float) n * 100;
				}
				i++;
				j++;
			} else if (j > 0) {
				j = fail[j - 1];
			} else {
				i++;
			}
		}
		return -1;
	}

	public int[] preProcessKMP(String pattern) {
		int fail[] = new int[pattern.length() - 1];
		fail[0] = 0;
		int m = pattern.length() - 1;
		int j = 0;
		for (int i = 1; i < m; i++) {
			if (pattern.charAt(i) == pattern.charAt(j)) {
				j++;
				fail[i] = j;
			} else {
				if (j != 0) {
					j = fail[j - 1];
					i--;
				} else {
					fail[i] = 0;
				}
			}
		}
		return fail;
	}

	public String removeStopWords(String text) {
		String result = "";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(files.getAssets().open("stopwords.txt")))) {
			StringBuilder sb = new StringBuilder();
			Set<String> stopwords = new HashSet<String>();
			String line = br.readLine();
			while (line != null) {
				stopwords.add(line);
				line = br.readLine();
			}
			String[] words = text.split(" ");
			ArrayList<String> resList = new ArrayList<String>();
			for (String word : words) {
				if (!stopwords.contains(word.toLowerCase())) {
					resList.add(word.toLowerCase());
				}
			}
			sb = new StringBuilder();
			for (String s : resList) {
				sb.append(s);
				sb.append(" ");
			}
			result = sb.toString();
		} catch (IOException e) {
			System.err.format("IOException");
		}
		return result;
	}

	public float bmMatch(String text, String pattern) {
		text = text.toLowerCase();
		pattern = pattern.toLowerCase();
		int last[] = preProcessBM(pattern);
		int n = text.length() - 1;
		int m = pattern.length() - 1;
		int i = m - 1;
		if (n != m) {
			return -1;
		}
		int j = m - 1;
		do {
			if (pattern.charAt(j) == text.charAt(i)) {
				if (j == 0) {
					return (float) m / (float) n * 100;
				} else {
					i--;
					j--;
				}
			} else {
				int lo = last[text.charAt(i)];
				i = i + m - Math.min(j, 1 + lo);
				j = m - 1;
			}
		} while (i <= n - 1);
		return -1;
	}

	public int[] preProcessBM(String pattern) {
		int last[] = new int[256];
		for (int i = 0; i < 256; i++) {
			last[i] = -1;
		}
		for (int i = 0; i < pattern.length() - 1; i++) {
			last[(int) pattern.charAt(i)] = i;
		}
		return last;
	}

	public String synonim(String text, ArrayList<String>[] dict) {
		String[] words = (text.toLowerCase()).split(" ");
		int i = 0;
		for (String word : words) {
			for (ArrayList<String> v : dict) {
				if (v.contains(word) && v.get(0) != word) {
					words[i] = v.get(0);
					break;
				}
			}
			i++;
		}
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			sb.append(word);
			sb.append(" ");
		}
		return sb.toString();
	}

	public ArrayList<String>[] initDict() {
		int line_count = 0;
		try (BufferedReader br = new BufferedReader(new InputStreamReader(files.getAssets().open("dictionary.txt")))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				line_count++;
				line = br.readLine();
			}
		} catch (IOException e) {
			System.err.format("IOException");
		}
		@SuppressWarnings({"rawtypes","unchecked"})
		ArrayList<String>[] dict = new ArrayList[line_count];
		for(int i =0;i<line_count;i++){
			dict[i] = new ArrayList<String>();
		}

		try (BufferedReader br = new BufferedReader(new InputStreamReader(files.getAssets().open("dictionary.txt")))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			int l = 0;
			while (line != null) {
				String[] syn = line.split(" ");
				for (String sy : syn) {
					dict[l].add(sy);
				}
				line = br.readLine();
				l++;
			}
		} catch (IOException e) {
			System.err.format("IOException");
		}
		return dict;
	}

	public HashMap<String, String> initDB() {
		HashMap<String, String> qs = new HashMap<String, String>();
		try (BufferedReader br = new BufferedReader(new InputStreamReader(files.getAssets().open("pertanyaan.txt")))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				String[] qa = line.split("\\? ");
				qs.put(qa[0], qa[1]);
				line = br.readLine();
			}
		} catch (IOException e) {
			System.err.format("IOException");
		}
		return qs;
	}

	public float alternativeFunc(String inp, String db) {
		String[] inpTemp = inp.split(" ");
		String[] dbTemp = db.split(" ");
		int i = 0;
		int j = 0;
		int count = 0;
		boolean found = false;
		for (String s1 : inpTemp) {
			if (!found) {
				i = j;
			}
			found = false;
			j = i;
			while (i < dbTemp.length && !found) {
				//System.out.println(i);
				//System.out.println("s1 " + s1);
				//System.out.println("db " + dbTemp[i]);
				if (s1.equals(dbTemp[i])) {
					found = true;
					count++;
				}
				i++;
			}
		}
		if (inp.length() > db.length()) {
			return (float) count / (float) inpTemp.length * 100;
		} else {
			return (float) count / (float) dbTemp.length * 100;
		}
	}
}
