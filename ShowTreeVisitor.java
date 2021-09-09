

import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level, boolean isAddr, int frameOffset ) {
    while( expList != null && expList.head != null) {
      expList.head.accept( this, level, false,0 );
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level, boolean isAddr, int frameOffset ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level ,false,0);
    if(exp.rhs != null){
      exp.rhs.accept( this, level,false,0 );
    }
  }

  public void visit( IfExp exp, int level, boolean isAddr, int frameOffset ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level,false,0 );
    exp.thenpart.accept( this, level,false,0 );
    
    if (exp.elsepart != null ){
      //indent(level);
       exp.elsepart.accept( this, level,false ,0);
    }

  }

  public void visit( IntExp exp, int level , boolean isAddr, int frameOffset) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }

  public void visit( OpExp exp, int level , boolean isAddr, int frameOffset) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.OVER:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " == " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    exp.left.accept( this, level, false ,0);
    exp.right.accept( this, level, false,0 );
  }

  public void visit( ReadExp exp, int level , boolean isAddr, int frameOffset) {
    indent( level );
    System.out.println( "ReadExp:" );
    exp.input.accept( this, ++level, false ,0);
  }

  public void visit( RepeatExp exp, int level, boolean isAddr , int frameOffset) {
    indent( level );
    System.out.println( "RepeatExp:" );
    level++;
    exp.exps.accept( this, level ,false,0);
    exp.test.accept( this, level ,false,0); 
  }

  public void visit( VarExp exp, int level, boolean isAddr , int frameOffset) {
    indent( level );
    //System.out.println( "VarExp: " + exp.name );
    System.out.println( "VarExp: " );
    level++;
    if(exp.var != null){
      exp.var.accept(this, level, false,0);
    }
  }

  public void visit( WriteExp exp, int level , boolean isAddr, int frameOffset) {
    indent( level );
    System.out.println( "WriteExp:" );
    exp.output.accept( this, ++level , false,0);
  }
  
  public void visit( WhileExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("WhileExp:");
    level++;
    exp.test.accept(this, level, false,0);
    exp.body.accept(this, level,false,0);
  }

  public void visit( CallExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("CallExp: " + exp.function_name);
    level++;
    if(exp.args != null){
      exp.args.accept(this, level,false,0);
    }
  }

  public void visit( CompoundExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("CompoundExp:");
    level++;
    //add a check to null later
    if(exp.var_list != null){
      exp.var_list.accept(this, level,false,0);
    }
    if(exp.exp_list != null){
      exp.exp_list.accept(this, level,false,0);
    }
  }

  public void visit( ReturnExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("ReturnExp: ");
    level++;
    if(exp.exp != null){
      exp.exp.accept(this, level,false,0);
    }
  }

  public void visit( SimpleDec exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    
    if(exp.type != null){
      String str = "SimpleDec: ";
      str = str+exp.dec_name;
      
      if(exp.type.type == 0){
        str = str+", type VOID";
      }
      else if(exp.type.type == 1){
        str = str+", type INT";
      }
      System.out.println(str);
    }
  }

  public void visit( ArrayDec exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    if(exp.size == null){
      if(exp.type.type == 0){
        System.out.println("ArrayDec: "+exp.array_name+" size of 0"+" , of type VOID");
      }
      else if(exp.type.type == 1){
        System.out.println("ArrayDec: "+exp.array_name+" size of 0"+" , of type INT");
      }
    }
    else{
      if(exp.type.type == 0){
        System.out.println("ArrayDec: "+exp.array_name+" size of "+exp.size.value+" , of type VOID");
      }
      else if(exp.type.type == 1){
        System.out.println("ArrayDec: "+exp.array_name+" size of "+exp.size.value+" , of type INT");
      }
    }
  }

  public void visit( SimpleVar exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("SimpleVar: " + exp.variable_name);
  }

  public void visit( IndexVar exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("IndexVar: "+exp.array_name);
    level++;
    exp.index.accept(this, level,false,0);
  }

  public void visit( NilExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
  }

  public void visit( DecList exp, int level, boolean isAddr, int frameOffset){
    while(exp != null && exp.head != null){
      exp.head.accept(this,level,false,0);
      exp = exp.tail;
    }
  }

  public void visit( ErrorExp exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("ErrorExp");
  }

  public void visit( VarDecList exp, int level, boolean isAddr, int frameOffset){
    while(exp != null && exp.head != null){
      exp.head.accept(this, level,false,0);
      exp = exp.tail;
    }
  }

  public void visit( FunctionDec exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    System.out.println("FunctionDec: "+exp.func_name +level);
    level++;
    if(exp.type != null){
      exp.type.accept(this, level,false,0);
    }
    if(exp.params != null){
      exp.params.accept(this, level,false,0);
    }
    if(exp.body != null){
      exp.body.accept(this, level,false,0);
    }
  }

  public void visit( NameTy exp, int level, boolean isAddr, int frameOffset){
    indent( level );
    if(exp.type == 1){
      System.out.println("NameTy: type INT "+level);
    }
    else{
      System.out.println("NameTy: type VOID "+level);
    }
  }

}
