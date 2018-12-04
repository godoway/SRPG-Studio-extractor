package gwsl.srpgstudio.extractor.struct;

import lombok.Data;

import java.util.List;

@Data
public class Fragment {
    private String path;

    private long begin;
    private long end;
    private long size;
    private int resourceGroupCount;
    private List<ResourceGroup> resourceGroups;

//    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    Fragment(String path) {
        this.path = path;
    }
}
