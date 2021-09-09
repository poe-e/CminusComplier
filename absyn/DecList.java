package absyn;

public class DecList extends Absyn{
    public Dec head;
    public DecList tail;

    public DecList(Dec head, DecList tail){
        this.head = head;
        this.tail = tail;
    }

    public void accept( AbsynVisitor visitor, int level, boolean isAddr, int frameOffset){
        visitor.visit( this , level,isAddr, frameOffset);
    }
}
