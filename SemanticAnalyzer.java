
import java.util.*;
import absyn.*;


//LECTURE 10C for code Implementation


//LINE 152, add params list for the functions so it prints with them
//cup, func -> param_list -> which is a VarDecList -> param -> SimpleDec or ArrayDec
//add it in function dec, grab the params, only need declaration

//also add check for hashmap

//ADD
//ArrayDeclarations, have to check x = a[low], check a and low
//For arrays, dont allow arrays of different sizes, only a defined array int x[10] calling a function sort(x), void sort(a[])
//Have to check return types on functions 
//for if statements, look at the exp, (if <exp> then <stmt>)
//PostOrder, look at left right first(exp.left, exp.right lec 8b at the end)

//type-checking for VarExp, IntExp, OpExp, AssignExp, IfExp, WhileExp, CallExp, 
//and ReturnExp in this given order incrementally.

//add name-def pairs for Dec nodes
//do type-checking for Exp nodes


//if checks are broken
public class SemanticAnalyzer implements AbsynVisitor {

  HashMap<String, ArrayList<NodeType>> table;
  int scopeLevel;
  String functionReturnType;

  public SemanticAnalyzer(){
    table = new HashMap<String, ArrayList<NodeType>>();
    scopeLevel = 0;
    functionReturnType = "VOID";
    ArrayList<String> placeholder = new ArrayList<>(); //needed to be declared
    ArrayList<String> placeholderOutput = new ArrayList<>();
    placeholderOutput.add("INT");
    NodeType output = new NodeType("output", "VOID", scopeLevel+1, 0, placeholderOutput, 0,null, 0, 0, 0);
    NodeType input = new NodeType("input", "INT", scopeLevel+1, 0, placeholder, 0,null, 0, 0, 0);
    insertNode(output, "output");
    insertNode(input, "input");
  }

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }
  private void ErrorIndent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.err.print( " " );
  }

  //helper print function
  public void printBlock(int curLevel){
    for(Map.Entry<String, ArrayList<NodeType>> set: table.entrySet()){
      ArrayList<NodeType> tempArr = set.getValue();
        for(NodeType tempNode: tempArr){
          if(tempNode.level == scopeLevel){
            indent(scopeLevel);
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

  public void visit( DecList exp, int level, boolean isAddr, int frameOffset){
    indent(level);
    System.out.print("Entering the global scope:\n");
    scopeLevel++;

    while(exp != null && exp.head != null){
      exp.head.accept(this,level,false, 0);
      exp = exp.tail;
    }


    printBlock(scopeLevel);
    System.out.println("Leaving the global scope");
    if(table.get("main") == null){
      System.err.println("ERROR: 'main' function not found");
      CM.errors = true;
    }

    deleteBlock(scopeLevel);
    scopeLevel--;
  }

  public void visit( SimpleDec exp, int level, boolean isAddr, int frameOffset){
  //indent( level );
  //Check if SimpleDec already exists in hashmap
  //else
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
  //create node with all variables
  NodeType newNode = new NodeType(exp.dec_name, type, scopeLevel, 1, null, 1, null, 0, 0, 0);
  insertNode(newNode, exp.dec_name);
  //create arraylist and add node to it
  //ArrayList<NodeType> tableEntry = new ArrayList<>();
  //tableEntry.add(newNode);
  //add the arraylist to the hashmap
  //table.put(exp.dec_name, tableEntry);

  /*for(ArrayList<NodeType> i: table.values()){
    //Would have to loop through i here too
    System.out.println(i.get(0).name);
    System.out.println(i.get(0).declar);
    System.out.println(i.get(0).level);
  }
  */
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
    NodeType newNode = new NodeType(exp.array_name, type, scopeLevel, 1, null, 0, arrSize, 0, 0, 0);
    insertNode(newNode, exp.array_name);
    //create arraylist and add node to it
    //ArrayList<NodeType> tableEntry = new ArrayList<>();
    //tableEntry.add(newNode);
    //add the arraylist to the hashmap
    //table.put(exp.array_name, tableEntry);
}

public void visit( FunctionDec exp, int level, boolean isAddr, int frameOffset){  
  //add function to hashmap

  //before adding to the hashmap, you have to check the key to enter, 
  //if it exists then you add the new node to the existing arraylist otherwise add the new arraylist
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
  //HAVE TO CHECK IF THE KEY ALREADY EXISTS
  if(checkExistence(exp.func_name)){
    //ErrorIndent(scopeLevel);
    System.err.println("ERROR: function: '"+exp.func_name+"' is already defined: "+exp.row+" column "+exp.col);
    CM.errors = true;
    return;
  }

  NodeType newNode = new NodeType(exp.func_name, funcType, scopeLevel, 0, paramArr, arrOrint,null, 0, 0, 0);
  insertNode(newNode, exp.func_name);

  indent( scopeLevel );
  System.out.println("Entering the scope for function "+exp.func_name);
  scopeLevel++;
  //enter info in the hashmap

  //System.out.println("FunctionDec: "+exp.func_name+"\n");
  level++;
  if(exp.params != null){ // this calls the params that i need to add 
    exp.params.accept(this, level,false,0 );
  }
  if(exp.type != null){
    exp.type.accept(this, level,false,0 );
  }
  if(exp.body != null){//this goes to body which has var dec and while loop
    exp.body.accept(this, level,false,0 );
  }

  printBlock(scopeLevel);
  deleteBlock(scopeLevel);

  
  scopeLevel--;
  indent(scopeLevel);
  System.out.println("Leaving the function");
}

public void visit( WhileExp exp, int level, boolean isAddr, int frameOffset){
  indent( scopeLevel );
  System.out.println("Entering a new block: while");
  scopeLevel++;
  level++;
  if(exp.test instanceof CallExp){
    CallExp callExp = (CallExp)exp.test;
    if(getDecType(callExp.function_name) == "VOID"){
      //ErrorIndent(scopeLevel);
      System.err.println("ERROR: VOID function used in WHILE LOOP expression on line "+callExp.row+" column "+callExp.col);
      CM.errors = true;
    }
  }
  exp.test.accept(this, level,false,0 );
  exp.body.accept(this, level,false,0 );
  printBlock(scopeLevel);
  deleteBlock(scopeLevel);
  scopeLevel--;
  indent(scopeLevel);
  System.out.println("Leaving the block");
}

public void visit( IfExp exp, int level, boolean isAddr, int frameOffset ) {//this will print the q variable cause 
  //check if the ifexp is valid and same types
  indent(level);
  System.out.println("Entering a new block: if");
  scopeLevel++;
  level++;
  if(exp.test instanceof CallExp){
    CallExp callExp = (CallExp)exp.test;
    if(getDecType(callExp.function_name) == "VOID"){
      System.err.println("ERROR: VOID function '"+callExp.function_name+"' used in if statement on line "+callExp.row+" column "+callExp.col);
      CM.errors = true;
    }
  }
  exp.test.accept( this, level , false,0 );
  exp.thenpart.accept( this, level ,false,0 );
  if (exp.elsepart != null ){
    //indent(level);
     exp.elsepart.accept( this, level ,false,0 );
  }
  printBlock(scopeLevel);
  deleteBlock(scopeLevel);

  scopeLevel--;
  indent(scopeLevel);
  System.out.println("Leaving the block if");
}

public void visit( CallExp exp, int level, boolean isAddr, int frameOffset){//check function calls
  //indent( level );
  //System.out.println("CallExp: " + exp.function_name);
  int numArgs = 0;
  int arrCounter = 0;
  ArrayList<String> argArrList = new ArrayList<>();
  if(!checkExistence(exp.function_name)){//check if the function exists
    //ErrorIndent(scopeLevel);
    System.err.println("ERROR: function: '"+exp.function_name+"' does not exist on line "+(exp.row+1)+" column "+exp.col);
    CM.errors = true;
    return;
  }
  numArgs = getArgNum(exp.function_name);
  argArrList = getArgList(exp.function_name);
  ExpList argList = exp.args;
    while(argList != null){//check if the number of params is equal to function and if they're the same dec type
      if(argList.head instanceof VarExp){
        VarExp argDec;
        argDec = (VarExp)argList.head;
        if(argDec.var instanceof SimpleVar){
          SimpleVar var = (SimpleVar)argDec.var;
          String decType = getDecType(var.variable_name);
          int arrOrInt = getArrOrInt(var.variable_name);// 0 array, 1 int var
          if(arrOrInt == 0){
            decType = decType+"[]";
          }
          if(!(decType.isEmpty())){//not empty
            if(argArrList.size() > arrCounter){
              if(!(decType.equals(argArrList.get(arrCounter)))){
                //ErrorIndent(scopeLevel);
                System.err.println("ERROR: INVALID ARGUMENT, function expects "+argArrList.get(arrCounter)+" but got "+decType+" on line "+(var.row+1)+" column "+var.col);
                CM.errors = true;
              }
            }
          }//else the var exists and is of valid declaration type
        }
      }
      arrCounter++;
      argList = argList.tail;
    }
    if((argArrList.size()) != arrCounter){
      //ErrorIndent(scopeLevel);
      System.err.println("ERROR: function call expects "+numArgs+" arguments. Got "+arrCounter+" on line "+(exp.row+1)+" column "+exp.col);
      CM.errors = true;
    }
  level++;
  if(exp.args != null){
    exp.args.accept(this, level,false,0 );
  }
}

public void visit( AssignExp exp, int level, boolean isAddr, int frameOffset ) {//check if assigns are valid
  //indent( level );
  //System.out.println( "AssignExp:" );
  level++;

  //check if rhs is callexp or variable and then check if its possible
  if(exp.rhs instanceof CallExp){//checks if void function is = to variable
    CallExp callExp = (CallExp)exp.rhs;
    if(exp.lhs instanceof Var){
      Var var = (Var)exp.lhs;
      if(var instanceof SimpleVar){
        SimpleVar simpVar = (SimpleVar)var;
        if(getDecType(callExp.function_name) == "VOID"){
          //(scopeLevel);
          System.err.println("ERROR incompatible types: assigning VOID function: '"+callExp.function_name+"' to INT variable: '"+simpVar.variable_name+"' on line "+simpVar.row+" column "+simpVar.col);
          CM.errors = true;
          if(getArrOrInt(simpVar.variable_name) == 0 ){
            //ErrorIndent(scopeLevel);
            System.err.println("ERROR incompatible types: assigning VOID function: '"+callExp.function_name+"' to INT array: '"+simpVar.variable_name+"' on line "+simpVar.row+" column "+simpVar.col);
            CM.errors = true;
          }
        }
        else if(getDecType(callExp.function_name) == "INT"){
          if(getArrOrInt(simpVar.variable_name) == 0 ){
            //ErrorIndent(scopeLevel);
            System.err.println("ERROR incompatible types: assigning INT function: '"+callExp.function_name+"' to INT array: '"+simpVar.variable_name+"' on line "+simpVar.row+" column "+simpVar.col);
            CM.errors = true;
          }
        }
      }
    }
  }
  //check if left side is variable and if its int or void
  if(exp.lhs instanceof Var){
    Var var = (Var)exp.lhs;
    if(var instanceof SimpleVar){
      SimpleVar simpVar = (SimpleVar)var;
      if(getDecType(simpVar.variable_name) == "VOID"){
        //ErrorIndent(scopeLevel);
        System.err.println("ERROR incompatible types: assigning to VOID variable: '"+simpVar.variable_name+"' on line "+simpVar.row+" column "+simpVar.col);
        CM.errors = true;
      }
      if(getDecType(simpVar.variable_name) == "INT"){
        if(exp.rhs instanceof VarExp){
          VarExp varEx = (VarExp)exp.rhs;
          if(varEx.var instanceof SimpleVar){
            SimpleVar simpVarRight = (SimpleVar)varEx.var;
            if(getDecType(simpVarRight.variable_name) == "VOID"){
              //ErrorIndent(scopeLevel);
              System.err.println("ERROR incompatible types: assigning VOID variable '"+simpVar.variable_name+"' to INT variable '"+simpVarRight.variable_name+"' on line "+simpVar.row+" column "+simpVar.col);
              CM.errors = true;
            }
          }
        }
      }
    }
  }
  exp.lhs.accept( this, level,false,0  );
  if(exp.rhs != null){
    exp.rhs.accept( this, level ,false,0 );
  }
}

  public void visit( ExpList expList, int level, boolean isAddr , int frameOffset) {//dont need to change
    while( expList != null && expList.head != null) {
      expList.head.accept( this, level,false ,0 );
      expList = expList.tail;
    } 
  }

  public void visit( OpExp exp, int level , boolean isAddr, int frameOffset) {//make sure ops are valid
    //indent( level );
    level++;


    if(exp.left instanceof CallExp){
      CallExp callExp = (CallExp)exp.left;
      if(getDecType(callExp.function_name) == "VOID"){
        //ErrorIndent(scopeLevel);
        System.err.println("ERROR invalid expression: using VOID function: '"+callExp.function_name+"' in expression: on line "+(callExp.row+1)+" column "+callExp.col);
        CM.errors = true;
      }
    }
    if(exp.right instanceof CallExp){
      CallExp callExp = (CallExp)exp.right;
      if(getDecType(callExp.function_name) == "VOID"){
        //ErrorIndent(scopeLevel);
        System.err.println("ERROR invalid expression: using VOID function: '"+callExp.function_name+"' in expression: on line "+(callExp.row+1)+" column "+callExp.col);
        CM.errors = true;
      }
    }
    if(exp.left instanceof VarExp){
      VarExp varExp = (VarExp)exp.left;
      if(varExp.var instanceof SimpleVar){
        SimpleVar var = (SimpleVar)varExp.var;
          if(getDecType(var.variable_name) == "VOID"){
            //ErrorIndent(scopeLevel);
            System.err.println("ERROR invalid expression: VOID variable '"+var.variable_name+"' used in expression on line "+var.row+" column "+var.col);
            CM.errors = true;
          }
      }
    }
    if(exp.right instanceof VarExp){
      VarExp varExp = (VarExp)exp.right;
      if(varExp.var instanceof SimpleVar){
        SimpleVar var = (SimpleVar)varExp.var;
          if(getDecType(var.variable_name) == "VOID"){
            //ErrorIndent(scopeLevel);
            System.err.println("ERROR invalid expression: VOID variable '"+var.variable_name+"' used in expression on line "+var.row+" column "+var.col);
            CM.errors = true;
          }
      }
    }

    exp.left.accept( this, level,false ,0 );
    exp.right.accept( this, level ,false,0 );
  }

  public void visit( ReturnExp exp, int level, boolean isAddr, int frameOffset){//check for returns
    //indent( level );
    //System.out.println("ReturnExp: ");
    level++;

    if(exp.exp == null && functionReturnType == "INT"){
      //ErrorIndent(scopeLevel);
      System.err.println("ERROR function expects return type of INT. Got NULL on line "+(exp.row+1)+" column "+exp.col);
      CM.errors = true;
    }

    if(exp.exp instanceof VarExp){
      VarExp varExp = (VarExp)exp.exp;
      if(varExp.var instanceof SimpleVar){
        SimpleVar simpVar = (SimpleVar)varExp.var;
        if(functionReturnType == "INT" && getDecType(simpVar.variable_name) == "VOID"){
          //ErrorIndent(scopeLevel);
          System.err.println("ERROR: INT return expected. Got VOID on line "+(simpVar.row+1)+" column "+simpVar.col);
          CM.errors = true;
        }
        if(getDecType(simpVar.variable_name) == "VOID"){
          //ErrorIndent(scopeLevel);
          System.err.println("ERROR: Returning a VOID variable: '"+simpVar.variable_name+"' on line "+(simpVar.row+1)+" column "+simpVar.col);
          CM.errors = true;
        }
        if(functionReturnType == "VOID" /*&& (getDecType(simpVar.variable_name) == "VOID" || getDecType(simpVar.variable_name) == "INT")*/){
          //ErrorIndent(scopeLevel);
          System.err.println("ERROR: VOID function expects no return. Line "+(simpVar.row+1)+" column "+simpVar.col);
          CM.errors = true;
        }
      }
    }
    if(exp.exp != null){
      exp.exp.accept(this, level,false,0 );
    }
  }

  public void visit( SimpleVar exp, int level, boolean isAddr, int frameOffset){//check if the variable exists, stems from VarExp

    if(checkExistence(exp.variable_name) == false){
      //ErrorIndent(scopeLevel);
      System.err.println("ERROR: variable: '"+exp.variable_name+"' is undefined on line "+(exp.row+1)+" column "+exp.row);
      //return;
      CM.errors = true;
    }
    //System.out.println("SimpleVar: " + exp.variable_name);
  }

  public void visit( IndexVar exp, int level, boolean isAddr, int frameOffset){//check if array is valid, stems from VarExp
    //indent( level );
    //System.out.println("IndexVar: "+exp.array_name);
    if(checkExistence(exp.array_name) == false){
      //ErrorIndent(scopeLevel);
      System.err.println("ERROR: array '"+exp.array_name+"' is undefined on line "+(exp.row+1)+" column "+exp.row);
      //return;
      CM.errors = true;
    }
    if(exp.index instanceof Exp){
      Exp indexVar = (Exp)exp.index;
      if(indexVar instanceof VarExp){
        VarExp varExp = (VarExp)indexVar;
        if(varExp.var instanceof SimpleVar){
          SimpleVar simpVar = (SimpleVar)varExp.var;
          if(getDecType(simpVar.variable_name) == "VOID"){
            //ErrorIndent(scopeLevel);
            System.err.println("ERROR: index variable '"+simpVar.variable_name+"' is of incompatible type VOID on line "+simpVar.row+" column "+simpVar.col);
            CM.errors = true;
          }
        }
      }
    }
    if(exp.index instanceof CallExp){
      CallExp callExp = (CallExp)exp.index;
      if(getDecType(callExp.function_name) == "VOID"){
        //ErrorIndent(scopeLevel);
        System.err.println("ERROR: using VOID function: '"+callExp.function_name+"' for array index: on line "+(callExp.row+1)+" column "+callExp.col);
        CM.errors = true;
      }
    }
    level++;
    exp.index.accept(this, level,false,0 );
  }




















  public void visit( IntExp exp, int level , boolean isAddr, int frameOffset) {//dont need
    //indent( level );
    //System.out.println( "IntExp: " + exp.value ); 
  }



  public void visit( ReadExp exp, int level , boolean isAddr, int frameOffset) {//dont need
    //indent( level );
    //System.out.println( "ReadExp:" );
    exp.input.accept( this, ++level,false ,0 );
  }

  public void visit( RepeatExp exp, int level, boolean isAddr, int frameOffset ) {//dont need
    //indent( level );
    //System.out.println( "RepeatExp:" );
    level++;
    exp.exps.accept( this, level ,false,0 );
    exp.test.accept( this, level ,false,0 ); 
  }

  public void visit( VarExp exp, int level, boolean isAddr , int frameOffset) {//dont need to change
    //indent( level );
    //System.out.println( "VarExp: " + exp.name );
    //System.out.println( "VarExp: " );
    level++;
    if(exp.var != null){
      exp.var.accept(this, level,false,0 );
    }
  }

  public void visit( WriteExp exp, int level, boolean isAddr , int frameOffset) {//dont need
    //indent( level );
    //System.out.println( "WriteExp:" );
    exp.output.accept( this, ++level,false,0  );
  }
  
  public void visit( CompoundExp exp, int level, boolean isAddr, int frameOffset){//dont need to change
    //indent( level );
    //System.out.println("CompoundExp:");
    //add a check to null later
    level++;
    if(exp.var_list != null){
      exp.var_list.accept(this, level,false,0 );
    }
    if(exp.exp_list != null){//post order
      exp.exp_list.accept(this, level,false,0 );
    }
  }




  public void visit( NilExp exp, int level, boolean isAddr, int frameOffset){ //like having something equal to null but java doesnt like null here
    //indent( level );
  }

  public void visit( ErrorExp exp, int level, boolean isAddr, int frameOffset){
    //indent( level );
    //System.out.println("ErrorExp");
  }

  public void visit( VarDecList exp, int level, boolean isAddr, int frameOffset){//dont need to change
    while(exp != null && exp.head != null){
      exp.head.accept(this, level,false,0 );
      exp = exp.tail;
    }
  }

  public void visit( NameTy exp, int level, boolean isAddr, int frameOffset){//dont need
    //indent( level );
    //if(exp.type == 1){
      //System.out.println("NameTy: type INT");
    //}
    //else{
      //System.out.println("NameTy: type VOID");
    //}
  }

}
