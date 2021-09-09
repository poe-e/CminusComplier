/*
  Created by: Fei Song
  File Name: Main.java
  To Build: 
  After the scanner, tiny.flex, and the parser, tiny.cup, have been created.
    javac Main.java
  
  To Run: 
    java -classpath /usr/share/java/cup.jar:. Main gcd.tiny

  where gcd.tiny is an test input file for the tiny language.
*/
   
import java.io.*;
import absyn.*;
   
class CM {
  public static boolean errors = false;
  public final static boolean SHOW_TREE = true;
  static public void main(String argv[]) {    
    /* Start the parser */
    try {
      parser p = new parser(new Lexer(new FileReader(argv[0])));
      
      Absyn result = (Absyn)(p.parse().value);    
      if (SHOW_TREE && result != null) {
         //if -a then write to file and don't print, but if no -a then just print to out
         ShowTreeVisitor visitor = new ShowTreeVisitor();
         SemanticAnalyzer semVisitor = new SemanticAnalyzer();
         CodeGenerator codeGen = new CodeGenerator(argv[0]);
         

         if(argv.length > 1){
          if(argv[1].equals("-a")){ // don't print, write to file
            String filename = argv[0].replace("cm","abs");

            PrintStream originalOut = System.out;
            PrintStream fileOut = new PrintStream(filename);
            System.setOut(fileOut);
            result.accept(visitor, 0, false, 0);
            System.setOut(originalOut);  
          }
          else if(argv[1].equals("-s")){
            String filename = argv[0].replace("cm","sym");

            PrintStream originalOut = System.out;
            PrintStream fileOut = new PrintStream(filename);
            System.setOut(fileOut);
            result.accept(semVisitor, 0, false, 0);
            System.setOut(originalOut);
          }
          else if(argv[1].equals("-c")){
            String filename = argv[0].replace("cm","abs");

            PrintStream originalOut = System.out;
            PrintStream fileOut = new PrintStream(filename);
            System.setOut(fileOut);
            result.accept(visitor, 0, false, 0);
            System.setOut(originalOut);  


            String filename2 = argv[0].replace("cm","sym");

            originalOut = System.out;
            fileOut = new PrintStream(filename2);
            System.setOut(fileOut);
            result.accept(semVisitor, 0, false, 0);
            System.setOut(originalOut);
            //codeGen.visit(result);
            if(!errors){
              result.accept(codeGen, 0, false, -2);//call for declist
            }
            else{
              System.out.println("Errors detected. Not generating TM file.");
            }
          }
          else{
            System.out.println("Invalid argument specified. Exiting...");
            System.exit(0);
          }
         }
        else{ //don't write to file, just print
          //System.out.println("The abstract syntax tree is:");
          //result.accept(visitor, 0); 
          //System.out.println("The symbol tree is:");
          result.accept(semVisitor, 0, false, 0); // this is how you call semantic analzyer to print
        }
      }
    } catch (Exception e) {
      /* do cleanup here -- possibly rethrow e */
      e.printStackTrace();
    }
    try{

      if(argv.length > 0){
        
      }
    }catch(Exception e){
      e.printStackTrace();
    }
  }
}


