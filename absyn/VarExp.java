package absyn;


public class VarExp extends Exp {
  public Var var;

  public VarExp( int row, int col, Var var ) {
    this.row = row;
    this.col = col;
    this.var = var;
  }

  public void accept( AbsynVisitor visitor, int level , boolean isAddr, int frameOffset ) {
    visitor.visit( this, level, isAddr ,frameOffset);
  }
}
