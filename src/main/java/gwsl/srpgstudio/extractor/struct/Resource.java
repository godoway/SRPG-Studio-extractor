package gwsl.srpgstudio.extractor.struct;

import lombok.Data;

@Data
public class Resource {

    private int num;
    private String path;
    private String name;
    private String suffix = "i_don_known";
    private long begin;
    private long end;
    private long size;

    public String savePath() {
        return String.format("%s/%s.%s", path, name, suffix);
    }

}
