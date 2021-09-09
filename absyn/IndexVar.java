package absyn;

public class IndexVar extends Var
{
    public String array_name;
    public Exp index;

    public IndexVar(int row, int col, String array_name, Exp index) {
        this.row = row;
        this.col = col;
        this.array_name = array_name;
        this.index = index;
    }

    public void accept(AbsynVisitor visitor, int level , boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level,isAddr,frameOffset);
    }
}