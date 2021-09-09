package absyn;

public class CompoundExp extends Exp
{
    public ExpList exp_list;
    public VarDecList var_list;

    public CompoundExp(int row, int col, VarDecList var_list, ExpList exp_list)
    {
        this.row = row;
        this.col = col;
        this.var_list = var_list;
        this.exp_list = exp_list;
    }

    public void accept(AbsynVisitor visitor, int level , boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level, isAddr,frameOffset);
    }
}