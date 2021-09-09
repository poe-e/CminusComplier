import java.io.*;
import java.io.IOException;
import java.util.*;
import absyn.*;
import java_cup.emit;


//THE ONLY THING I HAVE TO FIX IS ARRAYS IN FUNCTION CALLS
//RIGHT NOW IT load id value WHEN IT SHOULD load id address





public class CodeGenerator implements AbsynVisitor {

  HashMap<String, ArrayList<NodeType>> table;
  int scopeLevel;
  String functionReturnType;
  String filename;
  File tmFile;
  String tmFileString;

  private static int emitLoc = 0;
  private static int highEmitLoc = 0;
  //These are relative to the framepoint(fp)
  private static int ofp = 0; //control link
  private static int retFO = -1; // returnAddress
  private static int initFO = -2; // where you start with parameters
  private static int mainEntry = 0;
  private static int globalOffset = 0;
  private static int offset = -1;
  private static boolean parm = false;

  //frameoffset: as recursive gets called, left and right children, 
  //decrement frameoffset so you know where you are, 
  //then when the recursion returns frameoffset will go back to 0 or top

  /* Special Registers */
  private static final int pc = 7;
  //Global point, points to the global space for global variables
  private static final int gp = 6;
  private static final int fp = 5;
  //arithmitic registers, ac = ac op ac1, load one value in ac, 
  //compute with ac1 and store back into ac
  private static final int ac = 0; 
  private static final int ac1 = 1;

  public CodeGenerator(String filename){
    this.filename = filename;
    this.tmFileString = filename.replace("cm","tm");
    table = new HashMap<String, ArrayList<NodeType>>();
    scopeLevel = 0;
    functionReturnType = "VOID";
    ArrayList<String> placeholder = new ArrayList<>(); //needed to be declared
    ArrayList<String> placeholderOutput = new ArrayList<>();
    placeholderOutput.add("INT");
    NodeType output = new NodeType("output", "VOID", scopeLevel+1, 0, placeholderOutput, 0,null, 7, globalOffset, 1);
    NodeType input = new NodeType("input", "INT", scopeLevel+1, 0, placeholder, 0,null, 4, globalOffset, 1);
    insertNode(output, "output");
    insertNode(input, "input");
  }

  //helper print function
  public void printBlock(int curLevel){
    for(Map.Entry<String, ArrayList<NodeType>> set: table.entrySet()){
      ArrayList<NodeType> tempArr = set.getValue();
        for(NodeType tempNode: tempArr){
          if(tempNode.level == scopeLevel){
            //indent(scopeLevel);
            if(tempNode.type == 0){//print funcs              
              String args = "("+String.join(", ", tempNode.paramArr)+")"; 
              System.out.println(tempNode.name+": "+args+" -> "+tempNode.declar);
            }
            else if(tempNode.type == 1){//print var
              if(tempNode.array == 0){//array dec
                System.out.println(tempNode.name+"["+tempNode.arraySize+"]: "+tempNode.declar);
              }
              else if(tempNode.array == 1){//variable dec
                System.out.println(tempNode.name+": "+tempNode.declar);
              }
            }
          }
        }
    }
  }
  
  public void deleteBlock(int curLevel){
    Iterator mapIter = table.entrySet().iterator();

    while(mapIter.hasNext()){
      Map.Entry mapEle = (Map.Entry)mapIter.next();
      ArrayList<NodeType> tempArr = (ArrayList<NodeType>)mapEle.getValue();
      Iterator arrIt = tempArr.iterator();
      while(arrIt.hasNext()){
        NodeType tempNode = (NodeType)arrIt.next();
        if(tempNode.level == curLevel){
          arrIt.remove();
        }
      }
    }
  }
  public void insertNode(NodeType nodeEntry, String varName){
    //insert func, var...
    if(table.get(varName) != null){
      table.get(varName).add(nodeEntry);
    }
    else{
      ArrayList<NodeType> tableEntry = new ArrayList<>();
      tableEntry.add(nodeEntry);
      table.put(varName, tableEntry);
    }    
  }

  public boolean checkExistence(String varName){

    if(table.get(varName) != null && (table.get(varName).isEmpty() != true)){
      if(table.get(varName).isEmpty() == false){
        return true;
      }
    }
    return false;
  }

  public String getDecType(String varName){

    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
      if(tempArr != null){
      for(NodeType tempNode : tempArr){
        if(tempNode.type == 0){
            return tempNode.declar;
        }
        else if(tempNode.type == 1){
          return tempNode.declar;
        }
      }
    }
    return "";
  }

  public Integer getArrOrInt(String varName){

    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      for(NodeType tempNode : tempArr){
        if(tempNode.array == 0){ //array
            return 0;
        }
        else if(tempNode.array == 1){ //variable
          return 1;
        }
      }
    }
    return 2;
  }

  public Integer getArgNum(String varName){ // don't use, i use getArgList() and then just size it wherever it was called
    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      if(tempArr.isEmpty() == false){
        for(NodeType tempNode : tempArr){
          return tempNode.paramArr.size();
        }
      }
    }
    return 0;
  }

  public ArrayList<String> getArgList(String varName){

  
    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      if(tempArr.isEmpty() == false){
        for(NodeType tempNode: tempArr){
          return tempNode.paramArr;
        }
      }
    }
    return new ArrayList<String>();
  }

public Integer returnOffset(String varName){

  ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
  if(tempArr != null){
    if(tempArr.isEmpty()==false){
      for(int i = tempArr.size(); i-- > 0;){
        return tempArr.get(i).offset;
      }
    }
  }
  return 0;
}

  public Integer returnFunctionAddress(String varName){

    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      if(tempArr.isEmpty()==false){
        for(NodeType tempNode:tempArr){
          return tempNode.funaddr;
        }
      }
    }
    return 0;
  }

  public boolean returnIsGlobal(String varName){
    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      if(tempArr.isEmpty()==false){
        for(int i = tempArr.size(); i-- > 0;){
          if(tempArr.get(i).nestLevel == 1 ){
            return true;
          }
          else{
            return false;
          }
        }
      }
    }
    return false;
  }

  public boolean returnArrSize(String varName){
    ArrayList<NodeType> tempArr = (ArrayList<NodeType>)table.get(varName);
    if(tempArr != null){
      if(tempArr.isEmpty()==false){
        for(int i = tempArr.size(); i-- > 0;){
          if(tempArr.get(i).arraySize == null){
            return false;
          }
          else{
            return true;
          }
        }
      }
    }
    return false;
  }

  public void createTMFile(){
    this.tmFile = new File(filename.replace("cm", "tm"));
    try {
      this.tmFile.createNewFile();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return;
  }

  public void createPrelude(){
    int savedLoc = 0;
    int savedLoc2 = 0;
    try {
      FileWriter myWriter = new FileWriter(tmFileString);
      myWriter.write("* C-Minus Compilation to TM Code\n");
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
    //emitComment("C-minus Compilation to TM Code");
    emitComment("File: "+tmFileString);
    emitComment("Standard prelude:");
    emitRM("LD", 6, 0, 0, "load gp with maxaddress");
    emitRM("LDA", 5, 0, 6, "copy to gp to fp");
    emitRM("ST", 0, 0, 0, "clear location 0");
    emitComment("Jump around i/o routines here");

    savedLoc = emitSkip(1);

    emitComment("code for input routine");
    emitRM("ST", 0, -1, 5, "store return");
    emitRO("IN", 0, 0, 0, "input");
    emitRM("LD", 7, -1, 5, "return to caller");
    emitComment("code for output routine");
    emitRM("ST", 0, -1, 5, "store return");
    emitRM("LD", 0, -2, 5, "load output value");
    emitRO("OUT", 0, 0, 0, "output");
    emitRM("LD", 7, -1, 5, "return to caller");

    savedLoc2 = emitSkip(0); //save the current emitLoc
    emitBackup(savedLoc); //set emitLoc to saved io line, 3
    emitRM_Abs("LDA", pc, savedLoc2, "jump around i/o code");//print instruction as 3
    emitRestore(); //restore back to current line, emitLoc = highEmitLoc, highEmitLoc gets set in emitSkip, 11
    emitComment("End of standard prelude.");
  }
  public void finale(){
    emitRM("ST", fp, globalOffset, fp, "push ofp");
    emitRM("LDA", fp, globalOffset, fp, "push frame");
    emitRM("LDA", ac, 1, pc, "load ac with ret ptr");
    emitRM_Abs("LDA", pc, mainEntry + 1, "jump to main loc");//idk why this works
    emitRM("LD", fp, 0, fp, "pop frame");
    emitComment("End of execution.");
    emitRO("HALT", 0, 0, 0, "");
  }

  public void emitComment(String comment){
    try {
      FileWriter myWriter = new FileWriter(tmFileString, true);
      myWriter.write("* "+comment+"\n");
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void emitRM(String opCode, int r, int d, int s, String comment){
    /* 
      OpCode r, d(s) 
          d is offset, where to move according to register s
      (7) is PC counter, offset(7) moves the pc  
    */
    /* 
      LD = Load Data, reg[r] = dMem[a], a = d+reg[s], LOADS THE DATA AT THE ADDRESS a
      LDA = Load Adress, reg[r] = a, a = d+reg[s], LOADS THE ADDRESS OF THE DATA, unconditional jump
      LDC = Load Constant, ignore s, s will be 0 (reg[r] = d)
      ST = Store Data, dMem[a] = reg[r], a = d + reg[s]

      Condition jumps
        JLT, JLE, JGT, JGE, JEQ, JNE 
    */
    try {
      String str = String.format("%3d:  %5s  %d,%d(%d)         %s\n", emitLoc, opCode, r, d, s, comment);
      FileWriter myWriter = new FileWriter(tmFileString, true);
      myWriter.write(str);
      myWriter.close();
      ++emitLoc;
      if(highEmitLoc < emitLoc){
        highEmitLoc = emitLoc;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public void emitRO(String opCode, int r, int s, int t, String comment){
    /* OpCode r, s, t*/
    /* HALT, IN, OUT, ADD, SUB, MUL, DIV */
    try {
      String str = String.format("%3d:  %5s  %d,%d,%d         %s\n", emitLoc, opCode, r, s, t, comment);
      FileWriter myWriter = new FileWriter(tmFileString, true);
      myWriter.write(str);
      myWriter.close();
      ++emitLoc;
      if(highEmitLoc < emitLoc){
        highEmitLoc = emitLoc;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  public int emitSkip(int distance){//used to jump over functions and stuff
    int i = emitLoc;
    emitLoc += distance;
    if(highEmitLoc < emitLoc){
      highEmitLoc = emitLoc;
    }
    return i;
  }
  public void emitBackup(int loc){
    if(loc > highEmitLoc){
      emitComment("BUG in emitBackup");
    }
    emitLoc = loc;
  }
  public void emitRestore(){
    emitLoc = highEmitLoc;
  }
  public void emitRM_Abs(String op, int r, int a, String c){
    String absString = String.format("%3d:  %5s  %d,%d(%d)         %s\n", emitLoc, op, r, a-(emitLoc+1), pc, c);
    try {
      FileWriter myWriter;
      myWriter = new FileWriter(tmFileString, true);
      myWriter.write(absString);
      myWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    ++emitLoc;
    if(highEmitLoc < emitLoc){
      highEmitLoc = emitLoc;
    }
  }
  public void visit(Absyn dec){//wrapper for post order traversal
    //createTMFile();
    //Create prelude?
    //createPrelude();
    //trees.accept(trees.head, 0);
    //generate prelude
    //generate i/o routines
    //visit(dec, 0 , false, -2);
    //call the visit method for DecList
    //visit(DecList decs, int offset, Boolean isAddress)
    //visit(trees, 0, false);
    //generate finale
    //finale();
    //add Boolean isAddr to all function headers for the visitor pattern
    //ask what isAddr is or why its needed
  }

  public void visit( DecList exp, int level, boolean isAddr, int frameOffset){
    //indent(level);
    createTMFile();
    createPrelude();
    //System.out.print("Entering the global scope:\n");

    scopeLevel++;
    //frameOffset--;

    while(exp != null && exp.head != null){
      //exp.head.accept(this,level, false,frameOffset--);
      exp.head.accept(this,level, false,frameOffset);
      exp = exp.tail;
    }


    //printBlock(scopeLevel);
    //System.out.println("Leaving the global scope");
    if(table.get("main") == null){
      System.err.println("ERROR: 'main' function not found");
    }

    finale();
    deleteBlock(scopeLevel);
    scopeLevel--;
  }

  public void visit( SimpleDec exp, int level, boolean isAddr, int frameOffset){

  String type;

  if(exp.type.type == 0){
    type = "VOID";
  }
  else if(exp.type.type == 1){
    type = "INT";
  }
  else{
    type = "Invalid";
  }
  if(scopeLevel == 1){
    emitComment("allocating global var: "+exp.dec_name);
    emitComment("<- vardecl");
     //don't know if i decrement here or in declist for functions and global variables
    NodeType newNode = new NodeType(exp.dec_name, type, scopeLevel, 1, null, 1, null, emitLoc,globalOffset, scopeLevel);
    insertNode(newNode, exp.dec_name);
    globalOffset--;
    //System.out.println("CHECK IF GLOBAL OFFSET IS RIGHT: "+exp.dec_name + " offset: "+globalOffset);
    //globalOffset will be decremented here or something
  }
  else if(scopeLevel > 1){
    if(parm == false){
      emitComment("processing local var: "+exp.dec_name);
    }
    offset--;
    NodeType newNode = new NodeType(exp.dec_name, type, scopeLevel, 1, null, 1, null, emitLoc, offset, scopeLevel);
    insertNode(newNode, exp.dec_name);
  }
}

public void visit( ArrayDec exp, int level, boolean isAddr, int frameOffset){
  //indent( level );
  String type;
  String arrSize = null;
  if(exp.size == null){
    if(exp.type.type == 0){
      type = "VOID";
      //System.out.println("ArrayDec: "+exp.array_name+" size of 0"+" , of type VOID");
    }
    else if(exp.type.type == 1){
      type = "INT";
      //System.out.println("ArrayDec: "+exp.array_name+" size of 0"+" , of type INT");
    }
    else{
      type = "INVALID";
    }
  }
  else{
    if(exp.type.type == 0){
      type = "VOID";
      arrSize = exp.size.value;
      //System.out.println("ArrayDec: "+exp.array_name+" size of "+exp.size.value+" , of type VOID");
    }
    else if(exp.type.type == 1){
      type = "INT";
      arrSize = exp.size.value;
      //System.out.println("ArrayDec: "+exp.array_name+" size of "+exp.size.value+" , of type INT");
    }
    else{
      type = "INVALID";
      arrSize = exp.size.value;
    }
  }
    //create node with all variables
    if(scopeLevel == 1){
      emitComment("allocating global var: "+exp.array_name);
      emitComment("<- vardecl");
      NodeType newNode = new NodeType(exp.array_name, type, scopeLevel, 1, null, 0, arrSize, emitLoc, globalOffset, scopeLevel);
      //globalOffset--;
      globalOffset = (globalOffset-Integer.parseInt(exp.size.value));
      insertNode(newNode, exp.array_name);
    }
    else if(scopeLevel > 1){
      if(exp.size != null){
        frameOffset = (frameOffset-Integer.parseInt(exp.size.value))-1;
      }
        offset--;
        NodeType newNode = new NodeType(exp.array_name, type, scopeLevel, 1, null, 0, arrSize, emitLoc, offset, scopeLevel);
        //offset = (offset-Integer.parseInt(exp.size.value))-1;
        frameOffset--;

        insertNode(newNode, exp.array_name);
      
    }
}

public void visit( FunctionDec exp, int level, boolean isAddr, int frameOffset){  

  String funcType = "VOID";
  if(exp.type.type == 0){
    funcType = "VOID";
    functionReturnType = "VOID";
  }
  else if(exp.type.type == 1){
    funcType = "INT";
    functionReturnType = "INT";
  }

  int arrOrint = 2;
  ArrayList<String> paramArr = new ArrayList<>();
  VarDecList paramList = exp.params;
    while(paramList != null){
      //decrement offset here

      if(paramList.head instanceof SimpleDec){
        String paramType;
        SimpleDec parmDec;
        arrOrint = 1;
        parmDec = (SimpleDec)paramList.head;
        if(parmDec.type.type == 0){
          paramType = "VOID";
        }
        else if(parmDec.type.type == 1){
          paramType = "INT";
        }
        else{
          paramType = "ERROR INVALID DECLARATION";
        }
        paramArr.add(paramType);
        paramList = paramList.tail;
      }
      else if(paramList.head instanceof ArrayDec){
        String paramType;
        ArrayDec parmDec;
        arrOrint = 0;
        parmDec = (ArrayDec)paramList.head;
        
        if(parmDec.type.type == 0){
          paramType = "VOID[]"; //this might come back and bite me cause typechecking
        }
        else if(parmDec.type.type == 1){
          paramType = "INT[]";
        }
        else{
          paramType = "ERROR INVALID DECLARATION";
        }
        paramArr.add(paramType);
        paramList = paramList.tail;
      }
    }
  if(exp.func_name.equals("main")){
    mainEntry = emitLoc;
  }
  emitComment("processing function: "+exp.func_name);
  emitComment("jump around function body here");
  int savedLoc = emitSkip(1);
  emitRM("ST", 0, -1, fp, "store return");
  //Pass in emitLoc-1 cause the function is one behind
  NodeType newNode = new NodeType(exp.func_name, funcType, scopeLevel, 0, paramArr, arrOrint,null, emitLoc-1, globalOffset, scopeLevel);
  insertNode(newNode, exp.func_name);

  //System.out.println("Entering the scope for function "+exp.func_name);
  scopeLevel++;
  //enter info in the hashmap

  //System.out.println("FunctionDec: "+exp.func_name+"\n");
  level++;
  if(exp.params != null){
    parm = true;
    exp.params.accept(this, level, false,frameOffset);
  }
  parm = false;
  if(exp.type != null){
    exp.type.accept(this, level, false,frameOffset);
  }
  if(exp.body != null){//this goes to body which has var dec and while loop
    exp.body.accept(this, level,false,frameOffset);
  }
  emitRM("LD", pc, -1, fp, "return to caller");
  int savedLoc2 = emitSkip(0);
  emitBackup(savedLoc);
  emitRM_Abs("LDA", pc, savedLoc2, "jump around fn body");
  emitRestore();
  emitComment("<- fundecl");

  offset = -1;
  deleteBlock(scopeLevel);
  scopeLevel--;
}

public void visit( WhileExp exp, int level, boolean isAddr, int frameOffset){
  //System.out.println("Entering a new block: while");
  scopeLevel++;
  level++;
  emitComment("-> while");
  emitComment("while: jump after body comes back here");


  int savedLoc = emitSkip(0);
  exp.test.accept(this, level,false,frameOffset);//frameoffset?
  int savedLoc2 = emitSkip(1);
  emitComment("while: jump to end belongs here");
  exp.body.accept(this, level,false,frameOffset);//frameoffset?
  emitRM_Abs("LDA", pc, savedLoc, "while: absolute jmp to test");
  int savedLoc3 = emitSkip(0);
  emitBackup(savedLoc2);
  emitRM_Abs("JEQ", ac, savedLoc3, "while: jmp to end");
  emitRestore();

  deleteBlock(scopeLevel);
  scopeLevel--;

  emitComment("<- while");
}

public void visit( IfExp exp, int level, boolean isAddr , int frameOffset) {
  //System.out.println("Entering a new block: if");

  emitComment("-> if");

  scopeLevel++;
  level++;

  exp.test.accept( this, level,false,frameOffset);
  int savedLoc = emitSkip(1);
  emitComment("if: jump to else belongs here");
  exp.thenpart.accept( this, level ,false,frameOffset);


  emitComment("if: jump to end belongs here");
  int savedLoc2 = emitSkip(1);

  emitBackup(savedLoc);
  emitRM_Abs("JEQ", ac, highEmitLoc, "if: jmp to else");//or highEmitLoc?
  emitRestore();

  if (exp.elsepart != null ){
     exp.elsepart.accept( this, level,false,frameOffset);
  }
  emitBackup(savedLoc2);
  emitRM_Abs("LDA", pc, highEmitLoc, "jmp to end");
  emitRestore();
  //printBlock(scopeLevel);
  deleteBlock(scopeLevel);

  scopeLevel--;

  emitComment("<- if");
}

public void visit( CallExp exp, int level, boolean isAddr, int frameOffset){
  emitComment("-> call of function: "+exp.function_name);
  int offsetHolder = frameOffset - 2; // need to move two so we can move the control
  level++;

  /*
  while(exp.args != null && exp.args.head != null){
    exp.args.head.accept(this, level,false,frameOffset);
    emitRM("ST", ac, offsetHolder--, fp, "store arg val");
    exp.args = exp.args.tail;
  }
*/
  //frameOffset = offsetHolder;
  
  
  while(exp.args != null && exp.args.head != null){
    if(exp.args.head instanceof VarExp){
      VarExp varExp = (VarExp)exp.args.head;
      if(varExp.var instanceof IndexVar){//indexvar
        loadArrayIndex = true;
        exp.args.head.accept(this, level,true,offsetHolder);//Goes to indexVar
        loadArrayIndex = false;
      }
      else{//simplevar
        //ln 235 of sort.tm, idk how to fix, it says simplevar but its a global array so idk
        SimpleVar var = (SimpleVar)varExp.var;
        //I check if global, but I think I should be checking if its a parameter
        if(getArrOrInt(var.variable_name) == 0 && returnIsGlobal(var.variable_name)){//array
          exp.args.head.accept(this, level, true, frameOffset);
        }
        else{
          exp.args.head.accept(this, level,false,offsetHolder);
        }
        //System.out.println("IN HERE2 "+emitLoc+varExp.var.getClass());
        //varExp.var
        //exp.args.head.accept(this, level,false,offsetHolder);
      }
    } 
    else{
      exp.args.head.accept(this, level,false,offsetHolder);
    }
      emitRM("ST", ac, offsetHolder--, fp, "store arg val");
      exp.args = exp.args.tail;
  }
  /*
  while(exp.args != null && exp.args.head != null){
    if(exp.args.head instanceof VarExp){
      VarExp varExp = (VarExp)exp.args.head;
      if(varExp.var instanceof Var){
        Var var = (Var)varExp.var;
        if(var instanceof IndexVar){
          System.out.println("IN HERERERERER"+emitLoc);
          exp.args.head.accept(this, level, true, offsetHolder);
        }
      }
    }
    else{
      System.out.println("whatwhat");
      exp.args.head.accept(this, level,true,offsetHolder);
    }
    emitRM("ST", ac, offsetHolder--, fp, "store arg val");
    exp.args = exp.args.tail;
  }
  */

  int functionAddy = returnFunctionAddress(exp.function_name);

  emitRM("ST", fp, frameOffset, fp, "push ofp");
  emitRM("LDA", fp, frameOffset, fp, "push frame");
  emitRM("LDA", ac, ac1, pc, "load ac with ret ptr");
  emitRM_Abs("LDA", pc, functionAddy, "jump to fun loc");
  emitRM("LD", fp, ac, fp, "pop frame");
  emitComment("<- call");
  //ST  5,-4(5) 	push ofp
//LDA  5,-4(5) 	push frame
//LDA  0,1(7) 	load ac with ret ptr
//LDA  7,-55(7) 	jump to fun loc
//LD  5,0(5) 	pop frame
  //emitRM("ST", fp, offset, fp, "push ofp");
}


boolean loadArrayIndex = false;
public void visit( AssignExp exp, int level, boolean isAddr, int frameOffset ) {
  //frameOffset--; // do not know if this is in the right place?

  emitComment("-> op");
  level++;

  Var var = (Var)exp.lhs;
  if(var instanceof SimpleVar){ //ex. x = 0, left side of operand, goes to SimpleVar or IndexVar
    //exp.lhs.accept( this, level, true, frameOffset--);
    exp.lhs.accept( this, level, true, frameOffset);
    emitRM("ST", ac, frameOffset--, fp, "op: push left"); //here or in simplevar
  }
  else if(var instanceof IndexVar){
    loadArrayIndex = false;//lhs do not load value at array index
    //exp.lhs.accept(this, level, true, frameOffset);
    //exp.lhs.accept(this, level, false, frameOffset);
    if(exp.rhs instanceof CallExp){
      exp.lhs.accept(this, level, true, frameOffset);
    }
    else{
      exp.lhs.accept(this, level, false, frameOffset);
    }
    emitRM("ST", ac, frameOffset--, fp, "op: push left");
  }

  if(exp.rhs != null){//goes to IntExp, VarExp, OpExp, CallExp
    loadArrayIndex = true;//rhs load value at array index
    exp.rhs.accept( this, level, false, frameOffset); //IDK if i minus frameoffset here
    
    //frameOffset++;
    //exp.rhs.accept( this, level,false, --frameOffset);
    //emitRM("LD", ac, frameOffset+1, fp, "don't even know");
    emitRM("LD",ac1, ++frameOffset,fp,"op: load left");
    emitRM("ST",ac,ac,ac1,"assign: store value");
    emitComment("<- op");

  }
}

  public void visit( ExpList expList, int level, boolean isAddr, int frameOffset ) {
    while( expList != null && expList.head != null) {
      expList.head.accept( this, level,isAddr,offset-1);//idk maybe decrement in assignexp
       expList = expList.tail;
    } 
  }

  public void visit( OpExp exp, int level, boolean isAddr, int frameOffset ) {
    level++;
    //frameOffset--;
    emitComment("-> op");
    
    exp.left.accept( this, level,false , frameOffset);//goes to simplevar
    emitRM("ST", ac, frameOffset--, fp, "op: push left");
    exp.right.accept( this, level,false, frameOffset);//goes to simplevar or intexp

    //emitRM("LD", ac, frameOffset+1, fp, "load");
    emitRM("LD", ac1, frameOffset+1, fp, "op: load left");
    switch(exp.op){
      case OpExp.PLUS:
        emitRO("ADD", ac, ac1, ac, "op +");
        break;
      case OpExp.MINUS:
        emitRO("SUB", ac, ac1, ac, "op -");
        break;
      case OpExp.TIMES:
        emitRO("MUL", ac, ac1, ac, "op *");
        break;
      case OpExp.OVER:
        emitRO("DIV", ac, ac1, ac, "op /");
        break;
      case OpExp.EQ:
        emitRO("SUB", ac, ac1, ac, "op ==");
        emitRM("JEQ", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
        break;
      case OpExp.LT:
        emitRO("SUB", ac, ac1, ac, "op <");
        emitRM("JLT", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
        break;
      case OpExp.GT:
        emitRO("SUB", ac, ac1, ac, "op >");
        emitRM("JGT", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
     // 31:    SUB  0,1,0 	op >
      //32:    JGT  0,2(7) 	br if true
     // 33:    LDC  0,0(0) 	false case
     // 34:    LDA  7,1(7) 	unconditional jmp
     // 35:    LDC  0,1(0) 	true case
        break;
      case OpExp.LTEQ:
        emitRO("SUB", ac, ac1, ac, "op <=");
        emitRM("JLE", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
        break;
      case OpExp.GTEQ:
        emitRO("SUB", ac, ac1, ac, "op <=");
        emitRM("JGE", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
        break;
      case OpExp.NEQ:
        emitRO("SUB", ac, ac1, ac, "op <=");
        emitRM("JNE", ac, 2, pc, "br if true");
        emitRM("LDC", ac,ac,ac,"false case");
        emitRM("LDA", pc, ac1, pc, "unconditional jmp");
        emitRM("LDC", ac, ac1, ac, "true case");
        break;
    }
    //emitRM("ST", ac, frameOffset+2, fp, "result of x+3");
    emitComment("<- op");
  }

  public void visit( ReturnExp exp, int level, boolean isAddr, int frameOffset){
    emitComment("-> return");
    level++;


    if(exp.exp != null){
      exp.exp.accept(this, level,false,frameOffset);
    }
    emitRM("LD", pc, -1, fp, "return to caller");
    emitComment("<- return");
  }

  public void visit( SimpleVar exp, int level, boolean isAddr, int frameOffset){//check if the variable exists, stems from VarExp

    emitComment("-> id");

    emitComment("looking up id: "+exp.variable_name);

    //check if variable is global?

    int retOffset = returnOffset(exp.variable_name);
    if(returnIsGlobal(exp.variable_name) == true){
      if(isAddr){
        emitRM("LDA", ac, retOffset, gp, "load id address");
        //emitRM("ST", ac, frameOffset, fp, "op: push left");
      }
      else{
        emitRM("LD", ac, retOffset, gp, "load id value");
      }
    }
    else{
      if(isAddr){
        emitRM("LDA", ac, retOffset, fp, "load id address");
        //emitRM("ST", ac, frameOffset, fp, "op: push left");
      }
      else{
        emitRM("LD", ac, retOffset, fp, "load id value");
      }
    }
    emitComment("<- id");
  }

  public void visit( IndexVar exp, int level, boolean isAddr, int frameOffset){
    int varOffset = 0;
    varOffset = returnOffset(exp.array_name);
    emitComment("-> subs");
    level++;

    boolean refOrVal = returnArrSize(exp.array_name);


    if(returnIsGlobal(exp.array_name)){
     /* if(isAddr == true){
        emitRM("LDA",ac,varOffset,gp,"load id address");
      }
      else if(isAddr == false){
        emitRM("LD",ac,varOffset,gp,"load id value");
      }*/
      if(refOrVal == true){
        emitRM("LDA",ac,varOffset,gp,"load id address");
      }
      else if(refOrVal == false){
        emitRM("LD",ac,varOffset,gp,"load id value");
      }
      emitRM("ST", ac, frameOffset, fp, "store array addr");
    }
    else{
      /*
      if(isAddr == true){
        emitRM("LDA",ac,varOffset,fp,"load id address");
      }
      else if(isAddr == false){
        emitRM("LD",ac,varOffset,fp,"load id value");
      }*/
      if(refOrVal == true){
        emitRM("LDA",ac,varOffset,fp,"load id address");
      }
      else if(refOrVal == false){
        emitRM("LD",ac,varOffset,fp,"load id value");
      }
      emitRM("ST", ac, frameOffset, fp, "store array addr");
    }


    exp.index.accept(this, level,false,frameOffset);

    emitRM("JLT", ac, ac1, pc, "halt if subscript < 0");
    emitRM("LDA", pc, ac1, pc, "absolute jump if not");
    emitRO("HALT", ac, ac, ac, "halt if subscript < 0");
    emitRM("LD", ac1, frameOffset, fp, "load array base addr");
    emitRO("SUB", ac, ac1, ac, "base is at top of array");
//    212:    JLT  0,1(7) 	halt if subscript < 0
//213:    LDA  7,1(7) 	absolute jump if not
//214:   HALT  0,0,0 	halt if subscript < 0
//215:     LD  1,-3(5) 	load array base addr
//216:    SUB  0,1,0 	base is at top of array
    //if(isAddr == false){
     // emitRM("LD", ac, ac, ac, "load value at array index");
      //LD  0,0(0) 	load value at array index
    //}
    if(loadArrayIndex == true){//load value at array index
      emitRM("LD", ac, ac, ac, "load value at array index");
      //LD  0,0(0) 	load value at array index
    }
  
    emitComment("<- subs");
  }


  public void visit( IntExp exp, int level, boolean isAddr , int frameOffset) {
    emitComment("-> constant");
    emitRM("LDC", ac, Integer.parseInt(exp.value), ac, "load const");
    emitComment("<- constant");
    //emitRM("ST", ac, frameOffset, fp, "store const");
  }



  public void visit( ReadExp exp, int level, boolean isAddr , int frameOffset) {
    exp.input.accept( this, ++level,false ,0);
  }

  public void visit( RepeatExp exp, int level, boolean isAddr, int frameOffset) {
    level++;
    exp.exps.accept( this, level,false ,0);
    exp.test.accept( this, level ,false,0); 
  }

  public void visit( VarExp exp, int level, boolean isAddr , int frameOffset) {
    //System.out.println( "VarExp: " );
    level++;
    if(exp.var != null){
      exp.var.accept(this, level,isAddr,frameOffset);
    }
  }

  public void visit( WriteExp exp, int level, boolean isAddr, int frameOffset ) {
    exp.output.accept( this, ++level,false,0 );
  }
  
  public void visit( CompoundExp exp, int level, boolean isAddr, int frameOffset){
    emitComment("-> compound statement");
    level++;
    if(exp.var_list != null){
      exp.var_list.accept(this, level,false,frameOffset--);
    }
    if(exp.exp_list != null){//post order
      exp.exp_list.accept(this, level,false,frameOffset);
    }
    emitComment("<- compound statement");
  }




  public void visit( NilExp exp, int level, boolean isAddr, int frameOffset){ //like having something equal to null but java doesnt like null here
  }

  public void visit( ErrorExp exp, int level, boolean isAddr, int frameOffset){
  }

  public void visit( VarDecList exp, int level, boolean isAddr, int frameOffset){//dont need to change
    while(exp != null && exp.head != null){
      exp.head.accept(this, level,false,frameOffset); //move offset for each Variable Dec
      exp = exp.tail;
    }
  }

  public void visit( NameTy exp, int level, boolean isAddr, int frameOffset){//dont need

  }


}
