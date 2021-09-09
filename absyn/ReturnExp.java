package absyn;

public class ReturnExp extends Exp
{
    public Exp exp;

    public ReturnExp(int row, int col, Exp exp)
    {
        this.row = row;
        this.col = col;
        this.exp = exp;
    }

    public void accept(AbsynVisitor visitor, int level , boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level, isAddr,frameOffset);
    }
}