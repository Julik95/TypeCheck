package typecheck.file;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.bcel.classfile.Field;

public class CFile {
	
	
	private String absName,code;
	private RandomAccessFile file;
	Pattern regex = Pattern.compile("JNIEXPORT\\s(\\w+)\\sJNICALL\\s(\\w+)\\s*\\(([\\w*,\\s*]*)\\)"),
			regexArgPattern = Pattern.compile("([A-Za-z0-9\\*]+)\\s+([A-Za-z0-9\\*\\_]+)"),
			regexFieldID = Pattern.compile("jfieldID\\s([A-Za-z0-9\\_]+)\\s*=\\s*\\(.*\\)->GetFieldID\\(\\s*[A-Za-z0-9\\_]+\\s*,\\s*[A-Za-z0-9\\_]+\\s*,\\s*\"([A-Za-z0-9\\_]+)\"\\s*,\\s*\"[A-Za-z0-9\\_]+\"\\)");
	
	Map<String, ArrayList<Pair>> nativeFunc;
	
	public CFile(String name){
		this.absName = name;
		nativeFunc = new HashMap<>();
		parse();
		
		
	}
	
	public Map<String, ArrayList<Pair>> getNativeFuncs(){
		return nativeFunc;
	}
	
	public String getName() {
		if(null == absName)
			return "UKNOWN";
		
		return absName.substring(absName.lastIndexOf(File.separator)+1);
	}
	
	public String containsNaitiveFunc(String func) {
		if(null == nativeFunc)
			return null;
		for (Map.Entry<String, ArrayList<Pair>> entry : nativeFunc.entrySet()) {
		    if(entry.getKey().toLowerCase().contains(func))
		    	return entry.getKey();
		}
		
		return null;
	}
	
	private void parse() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(absName));
			StringBuilder sb = new StringBuilder();
		    String line = br.readLine();

		    while (line != null) {
		    	
		        sb.append(line);
		        sb.append(System.lineSeparator());
		        line = br.readLine();
		    }
		    code = sb.toString();
		    Matcher m = regex.matcher(code),m2;
		    while(m.find()){
	    		String[] args = m.group(3).split(",");
	    		ArrayList<Pair> list = new ArrayList<Pair>();
	    		
	    		for(int i=0;i<args.length;i++) {
	    			m2 = regexArgPattern.matcher(args[i]);
	    			if(m2.find()) {
	    				list.add(new Pair(m2.group(1), m2.group(2)));
	    			}
	    		}
	    		nativeFunc.put(m.group(2), list);
	    		
	    	}
		    
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public void constantCheck(ArrayList<String> fields, String funcName){
		int start = code.indexOf(funcName)+funcName.length();

		String toDoString = code.substring(start),line=null, funcCode;
		toDoString = toDoString.substring(toDoString.indexOf("{")+1);
		BufferedReader bufReader = new BufferedReader(new StringReader(toDoString));
		StringBuilder sb = new StringBuilder();
		int cnt = 1;
		try {
			while((line=bufReader.readLine()) != null && cnt>0){
				if(line.contains("{"))
					cnt = cnt+1;
				if(line.contains("}"))
					cnt = cnt-1;
				
				sb.append(line);
			}
			funcCode = sb.toString();
			funcCode = funcCode.substring(0,funcCode.lastIndexOf("}"));
			

			Matcher m = regexFieldID.matcher(funcCode),m2;
			while(m.find()){
				if(fields.contains(m.group(2))) {
					String variableString = m.group(2),fid = m.group(1);
					m2 = Pattern.compile("const\\s+[A-Za-z]+\\s+[A-Za-z0-9\\_]+\\s*=\\s*\\(\\*[A-Za-z0-9\\_]+\\)->Get[A-Za-z]+Field\\(\\s*[A-Za-z0-9\\_]+\\s*,\\s*[A-Za-z0-9\\_]+\\s*,\\s*"+fid+"\\s*\\)").matcher(funcCode);
					if(!m2.find()) {
						m2 = Pattern.compile("[A-Za-z]+\\s+const\\s+[A-Za-z0-9\\_]+\\s*=\\s*\\(\\*[A-Za-z0-9\\_]+\\)->Get[A-Za-z]+Field\\(\\s*[A-Za-z0-9\\_]+\\s*,\\s*[A-Za-z0-9\\_]+\\s*,\\s*"+fid+"\\s*\\)").matcher(funcCode);
						if(!m2.find()) {
							System.out.println("\t\t WARNING!!! Java field - ["+variableString+"] loosed constant property within native function!!!");
						}
					}
					
				}
			}	
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Native function implementation has been corrupted!");
			return;
		}
		
		
		System.out.println();
	}
	
	
	

}
