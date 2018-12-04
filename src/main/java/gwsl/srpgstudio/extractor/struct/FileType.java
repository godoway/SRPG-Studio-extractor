package gwsl.srpgstudio.extractor.struct;

import lombok.Data;

@Data
public class FileType {

    private String suffix;
    private byte[] header;

    FileType(String suffix, byte[] header) {
        this.suffix = suffix;
        this.header = header;
    }

}
