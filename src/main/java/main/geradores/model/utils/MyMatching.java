package main.geradores.model.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class MyMatching {
	private String line;
	private List<String> matchers;
	
	public MyMatching(String line, Matcher match) {
		this.line = line;
		matchers = new ArrayList<>();
		
		doMatchers(match);
	}
	
	private void doMatchers(Matcher match) {
		match.reset();
		while( match.find() ) {
			if(match.group().trim().equals("") == false)
				this.matchers.add(match.group());
		}
	}
	
	public String getLine() {
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
	public String get(int index) {
		return this.matchers.get(index);
	}
	public int size() {
		return this.matchers.size();
	}
	@Override
	public String toString() {
		String out = "linha -> '" + this.line + "'\r\n";
		int i = 0;
		for(String match : matchers) {
			out += "# " + i++ + " -> '" + match + "'\r\n";
		}
		
		return out;
	}
}
