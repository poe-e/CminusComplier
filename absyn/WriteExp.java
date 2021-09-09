package absyn;

public class WriteExp extends Exp {
  public Exp output;

  public WriteExp( int row, int col, Exp output ) {
    this.row = row;
    this.col = col;
    this.output = output;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr, int frameOffset ) {
    visitor.visit( this, level ,isAddr,frameOffset);
  }
}
