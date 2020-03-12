package gwsl.srpgstudio.extractor.struct;

import java.util.List;

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

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getResourceGroupCount() {
        return resourceGroupCount;
    }

    public void setResourceGroupCount(int resourceGroupCount) {
        this.resourceGroupCount = resourceGroupCount;
    }

    public List<ResourceGroup> getResourceGroups() {
        return resourceGroups;
    }

    public void setResourceGroups(List<ResourceGroup> resourceGroups) {
        this.resourceGroups = resourceGroups;
    }
}
