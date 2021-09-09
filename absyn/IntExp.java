package absyn;

public class IntExp extends Exp {
  public String value;

  public IntExp( int row, int col, String value ) {
    this.row = row;
    this.col = col;
    this.value = value;
  }

  public void accept( AbsynVisitor visitor, int level, boolean isAddr , int frameOffset) {
    visitor.visit( this, level ,isAddr,frameOffset);
  }
}
