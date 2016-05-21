package romashko.by.model;

public class Package implements Comparable{
    private int num;
    private byte[] data;

    public Package(int num, byte[] data){
        this.num = num;
        this.data = data;
    }

    public int getNum() {
        return num;
    }

    public byte[] getData() {
        return data;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getLength() {
        return 4 + data.length * 1;
    }

    @Override
    public int compareTo(Object o) {
        return this.num - ((Package)o).num;
    }
}
