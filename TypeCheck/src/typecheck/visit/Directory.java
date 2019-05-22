package typecheck.visit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.EmptyVisitor;
import org.apache.bcel.classfile.Field;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.Type;

import typecheck.file.CFile;

public class Directory extends EmptyVisitor{
	
	private String path,jarPath;
	private JarFile jarFile;
	private ArrayList<CFile> cFiles;
	
	
	
	
	public Directory(String path){
		this.path = path;
		cFiles = new ArrayList<>();
	}
	
	public int getCFilesCount() {
		return cFiles.size();
	}
	
	
	public void visit() {
		File dir = new File(path);
		if(!dir.exists()) {
			System.out.println("Specified directory does not exists!");
			return;
		}
		if(!dir.isDirectory()) {
			System.out.println("Specified file is not a valid directory!");
			return;
		}
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null){
		    for (File file : directoryListing){
		    	String fileNameString = file.getName();
		    	if(fileNameString.substring(fileNameString.lastIndexOf(".")).equalsIgnoreCase(".jar")) {
		    		try {
						jarFile = new JarFile(file.getAbsolutePath());
						jarPath = file.getAbsolutePath();
						System.out.println("Jar file: "+fileNameString+" has been found.");
					} catch (IOException e) {
						e.printStackTrace();
					}
		    	}
		    	
		    	if(fileNameString.substring(fileNameString.lastIndexOf(".")).equalsIgnoreCase(".c")) {
		    		cFiles.add(new CFile(file.getAbsolutePath()));
		    		
		    	}
		    	
		    }
		}
	}
	
	
	
	public void jarExplode() {
		if(null == jarFile) {
			System.out.println("JAR File has been corrupted!");
			return;
		}
		try {
            Enumeration<JarEntry> entries = jarFile.entries();
            while(entries.hasMoreElements()) {
            	JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")){
                	ClassParser parser = new ClassParser(jarPath, entry.getName());
                	System.out.println("\t-Class: <"+entry.getName()+">");
                	visitClass(parser.parse());
                   
                }
                
            }
            
		}catch (IOException e){
            e.printStackTrace();
        }
        finally{
            if (jarFile != null){
                try{
                	jarFile.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
	}
	
	private void visitClass(JavaClass clazz) {
		if(null == clazz) {
			System.out.println("Unenable to visit class!");
			return;
		}
		
		clazz.getConstantPool().accept(this);
		Method[] methods = clazz.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
	        if(method.isNative()) {
	            System.out.print("\t\t-Native Method: ["+method.getName()+"]");
	            for(CFile cFile : cFiles) {
	            	String cFileImplString = cFile.containsNaitiveFunc(method.getName());
	            	if(null != cFileImplString) {
	            		System.out.println(" -> Implementation in: ["+cFile.getName()+"]");
	            		System.out.println();
	            		Type[] argsTypes = method.getArgumentTypes();
	            		for(int j=0;j<argsTypes.length;j++) {
	            			System.out.print("\t\t Argument: "+argsTypes[j].toString()+" -> "+cFile.getNativeFuncs().get(cFileImplString).get(j+2).getType());
	            			if(("j"+argsTypes[j].toString()).equalsIgnoreCase(cFile.getNativeFuncs().get(cFileImplString).get(j+2).getType())) {
	            				System.out.println(" OK!");
	            			}else {
	            				System.out.println(" ERROR ON TYPES!!!");
	            				
	            			}
	            		}
	            		
	            		Field[] fields = clazz.getFields();
	            		ArrayList<String> finalFields = new ArrayList<>();
	            		for(int j=0;j<fields.length;j++) {
	            			if(fields[j].isFinal())
	            				finalFields.add(fields[j].getName().toString());
	            		}
	            		
	            		if(finalFields.size()>0) {
	            			cFile.constantCheck(finalFields,cFileImplString);
	            		}
	            		
	            	}
	            }
	        }
			
		}
		
		
	}
	

}
