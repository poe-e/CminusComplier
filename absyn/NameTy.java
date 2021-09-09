package absyn;


public class NameTy extends Absyn
{
    public static final int VOID = 0;
    public static final int INT = 1;
    public int type;

    public NameTy(int row, int col, int type)
    {
        this.row = row;
        this.col = col;
        this.type = type;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level,isAddr,frameOffset );
    }
}