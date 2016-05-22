package romashko.by.model;

public class Package implements Comparable{
    private Header header;
    private byte[] data;

    public Package(Header header) {
        this.header = header;
        data = null;
    }

    public Package(int num, byte[] data){
        header = new Header(data.length, num, num);
        this.data = data;
    }

    public Package(Header header, byte[] data) {
        this.header = header;
        this.data = data;
    }

    public Header getHeader() {
        return header;
    }

    public int getStartIndex() {
        return header.getStartIndex();
    }

    public int getEndIndex() {
        return header.getEndIndex();
    }

    public byte[] getData() {
        return data;
    }

    public void setStartIndex(int startIndex) {
        header.setStartIndex(startIndex);
    }

    public void setEndIndex(int endIndex) {
        header.setEndIndex(endIndex);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public long getLength() {
        return header.getLength();
    }

    @Override
    public int compareTo(Object o) {
        return this.header.getStartIndex() - ((Package)o).header.getStartIndex();
    }
}
