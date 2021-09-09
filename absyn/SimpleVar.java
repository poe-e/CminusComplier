package absyn;

public class SimpleVar extends Var
{
    public String variable_name;

    public SimpleVar(int row, int col, String variable_name)
    {
        this.row = row;
        this.col = col;
        this.variable_name = variable_name;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level ,isAddr,frameOffset);
    }
}