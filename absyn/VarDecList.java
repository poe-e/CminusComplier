package absyn;

public class VarDecList extends Absyn {
    public VarDec head;
    public VarDecList tail;

    public VarDecList( VarDec head, VarDecList tail ) {
        this.head = head;
        this.tail = tail;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr , int frameOffset) {
        visitor.visit( this, level ,isAddr,frameOffset);
    }
}
