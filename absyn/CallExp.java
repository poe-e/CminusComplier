package absyn;

public class CallExp extends Exp
{
    public String function_name;
    public ExpList args;

    public CallExp( int row, int col, String function_name, ExpList args) 
    {
        this.row = row;
        this.col = col;
        this.function_name = function_name;
        this.args = args;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr , int frameOffset) {
        visitor.visit( this, level, isAddr, frameOffset );
    }
}
