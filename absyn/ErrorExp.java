package absyn;

public class ErrorExp extends Exp
{

    public ErrorExp(int row, int col)
    {
        this.row = row;
        this.col = col;
    }

    public void accept( AbsynVisitor visitor, int level , boolean isAddr, int frameOffset) {
        visitor.visit( this, level, isAddr,frameOffset);
    }
}
