package absyn;

public class FunctionDec extends Dec
{
    public String func_name;
    public VarDecList params;
    public Exp body;
    public NameTy type;

    public FunctionDec(int row, int col, NameTy type, String func_name, VarDecList params, Exp body)
    {
        this.row = row;
        this.col = col;
        this.func_name = func_name;
        this.type = type;
        this.params = params;
        this.body = body;
    }

    public void accept(AbsynVisitor visitor, int level , boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level, isAddr,frameOffset);
    }
}