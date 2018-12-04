package gwsl.srpgstudio.extractor.struct;

import lombok.Data;

import java.util.List;

@Data
public class ResourceGroup {
    private long begin;
    private long end;
    private String name;
    private String path;

    private List<Resource> resources;
}
