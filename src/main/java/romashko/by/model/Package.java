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

    public int getLength() {
        return 4 + data.length * 1;
    }

    public byte[] getData() {
        return data;
    }

    public static byte[] getByteArrayFromInt(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static int getIntFromByteArray(byte[] array){
        return (((int)array[0])<<24) | ((((int)array[1])<<16)&0x00FF0000) |
                ((((int)array[2])<<8)&0x0000FF00) | ((((int)array[3]))&0x000000FF);
    }

    @Override
    public int compareTo(Object o) {
        return this.num - ((Package)o).num;
    }
}
