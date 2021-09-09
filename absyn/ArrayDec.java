package absyn;

public class ArrayDec extends VarDec
{
    public String array_name;
    public NameTy type;
    public IntExp size;

    public ArrayDec(int row, int col, NameTy type, String array_name, IntExp size)
    {
        this.row = row;
        this.col = col;
        this.type = type;
        this.array_name = array_name;
        this.size = size;
    }

    public void accept(AbsynVisitor visitor, int level, boolean isAddr, int frameOffset)
    {
        visitor.visit(this, level, isAddr , frameOffset);
    }
}