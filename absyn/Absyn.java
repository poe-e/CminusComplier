package absyn;

abstract public class Absyn {
  public int row, col;

  //abstract public void visit(AbsynVisitor codeGen);
  abstract public void accept( AbsynVisitor visitor, int level, boolean idAddr, int frameOffset );

}
