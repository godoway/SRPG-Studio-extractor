package gwsl.srpgstudio.extractor.struct;

public class FileType {

    private String suffix;
    private byte[] header;

    FileType(String suffix, byte[] header) {
        this.suffix = suffix;
        this.header = header;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public byte[] getHeader() {
        return header;
    }

    public void setHeader(byte[] header) {
        this.header = header;
    }
}
