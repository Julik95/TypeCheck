package typecheck.main;

import typecheck.visit.Directory;

public class Main {

	public static void main(String[] args) {
		
		Directory directory = new Directory("/Users/Julik/eclipse-workspace/TypeCheck/analyse");
		directory.visit();
		directory.jarExplode();
		
	}

}
