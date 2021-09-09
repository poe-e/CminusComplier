package absyn;

public class SimpleDec extends VarDec
{
    public String dec_name;
    public NameTy type;

    public SimpleDec(int row, int col, NameTy type, String dec_name)
    {
        this.row = row;
        this.col = col;
        this.type = type;
        this.dec_name = dec_name;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level, isAddr,frameOffset );
    }
}