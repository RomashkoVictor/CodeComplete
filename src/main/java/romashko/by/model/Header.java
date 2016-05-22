package romashko.by.model;

public class Header {
    private long length;
    private int startIndex;
    private int endIndex;

    public Header(long length, int startIndex, int endIndex) {
        this.length = length;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

    public long getLength() {
        return length;
    }

    public void setLength(long length) {
        this.length = length;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }
}
