import java.util.ArrayList;

public class NodeType {
    public String name;
    public String declar; 
    public int level;
    public int type;//0 function, 1 variable
    public int array;//0 array, 1 variable
    public String arraySize;
    public ArrayList<String> paramArr;
    public int funaddr;
    public int offset;
    public int nestLevel;

    public NodeType(String name, String declar, int level, int type, ArrayList<String> paramArr, int array, String arraySize, int funaddr, int offset, int nestLevel){
        this.name = name;
        this.declar = declar;
        this.level = level;
        this.type = type;
        this.paramArr = paramArr;
        this.array = array;
        this.arraySize = arraySize;
        this.funaddr = funaddr;
        this.offset = offset;
        this.nestLevel = nestLevel;
    }
}
