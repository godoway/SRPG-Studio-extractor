package gwsl.srpgstudio.extractor.work;

import gwsl.srpgstudio.extractor.struct.DataStruct;
import gwsl.srpgstudio.extractor.struct.Fragment;
import gwsl.srpgstudio.extractor.struct.Resource;
import gwsl.srpgstudio.extractor.struct.ResourceGroup;
import gwsl.srpgstudio.extractor.util.ByteBuffToStringUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class Analyzer {

    private DataStruct struct = DataStruct.getInstance();
    private File target;
    private RandomAccessFile source;
    private FileChannel sourceChannel;

    public Analyzer(File target) throws FileNotFoundException {
        this.target = target;
        source = new RandomAccessFile(target, "r");
    }

    public DataStruct analysis() throws IOException {
        System.out.println("analysis start...");
        analysisHeader();
        analysisFragments();
        System.out.println("analysis end.");
        sourceChannel.close();
        source.close();
        return struct;
    }

    private void analysisHeader() throws IOException {
        System.out.println("analysis header...");
        sourceChannel = source.getChannel();
        sourceChannel.position(0);
        ByteBuffer buffer = ByteBuffer.allocate(256).order(ByteOrder.LITTLE_ENDIAN);
        sourceChannel.read(buffer);
        buffer.flip();

        byte[] tmp4 = new byte[4];
        buffer.get(tmp4);
        String fileType = new String(tmp4);
        if (!fileType.equals("SDTS")) {
            throw new RuntimeException("it is not a srpg studio game");
        }
        struct.setEncrypt(buffer.getInt() == 1);

        buffer.position(20);
        struct.setProjectBegin(buffer.getInt() + 168);
        struct.setProjectSize(source.length() - struct.getProjectBegin() + 1);

        // position of resource,
        System.out.println("get fragment position...");
        long[] split = new long[36];
        for (int i = 0; i < 36; i++) {
            long splitPosition = buffer.getInt();
            split[i] = splitPosition;
        }
        List<Fragment> fragments = struct.getFragments();
        for (int i = 0; i < fragments.size(); i++) {
            Fragment fragment = fragments.get(i);
            fragment.setBegin(split[i] + 168);
            fragment.setEnd(split[i + 1] + 168 - 1);
            fragment.setSize(split[i + 1] - split[i]);
        }
        System.out.println("analysis header end.");
        buffer.clear();
    }


    private void analysisFragments() throws IOException {
        System.out.println("analysis fragments...");
        sourceChannel = source.getChannel();
        List<Fragment> fragments = struct.getFragments();
        for (Fragment fragment : fragments) {
            analysisFragment(fragment, sourceChannel);
        }
        System.out.println("analysis fragments end.");
    }

    private void analysisFragment(Fragment fragment, FileChannel sourceChannel) throws IOException {
        System.out.println(String.format("analysis fragment[%s]...", fragment.getPath()));
        ByteBuffer intBuff = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
        sourceChannel.position(fragment.getBegin());

        sourceChannel.read(intBuff);
        intBuff.flip();
        int count = intBuff.getInt();
        intBuff.clear();
        if (count == 0 || fragment.getSize() == 0) {
            System.out.println(String.format("end of analysis fragment[%s], because it is empty.", fragment.getPath()));
            return;
        }
        fragment.setResourceGroupCount(count);

        int infoLength = count * 4 + 4;
        List<ResourceGroup> resourceGroups = new ArrayList<>(count);
        List<Long> positions = new ArrayList<>(count + 1);
        int read = sourceChannel.read(buffer);
        long hasRead = 0L;
        while (hasRead < infoLength) {
            hasRead += read;
            buffer.flip();
            for (int i = 0; i < read; i = i + 4) {
                long position = buffer.getInt() + fragment.getBegin();
                positions.add(position);
                if (positions.size() == count) break;
            }
            buffer.clear();
            read = sourceChannel.read(buffer);
        }
        positions.add(fragment.getEnd() + 1);
        for (int i = 0; i < count; i++) {
            ResourceGroup info = new ResourceGroup();
            info.setPath(fragment.getPath());
            info.setBegin(positions.get(i));
            info.setEnd(positions.get(i + 1) - 1);
            resourceGroups.add(info);
        }
        fragment.setResourceGroups(resourceGroups);
        System.out.println(String.format("analysis fragment[%s] end.", fragment.getPath()));

        System.out.println("start to analysis resourceGroups...");
        for (ResourceGroup group : resourceGroups) {
            analysisResourceGroup(group);
        }
    }

    private void analysisResourceGroup(ResourceGroup group) throws IOException {
        sourceChannel = source.getChannel();
        sourceChannel.position(group.getBegin());
        ByteBuffer intBuff = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);

        sourceChannel.read(intBuff);
        intBuff.flip();
        int nameLength = intBuff.getInt();
        intBuff.clear();

        ByteBuffer strBuff = ByteBuffer.allocate(nameLength).order(ByteOrder.LITTLE_ENDIAN);
        sourceChannel.read(strBuff);
        strBuff.flip();
        String name = ByteBuffToStringUtil.converToString(strBuff, nameLength / Character.BYTES);
        strBuff.clear();
        group.setName(name);

        sourceChannel.position(sourceChannel.position() + 8);
        sourceChannel.read(intBuff);
        intBuff.flip();
        int resourceCount = intBuff.getInt();
        intBuff.clear();
        List<Resource> resources = new ArrayList<>(resourceCount);
        for (int i = 0; i < resourceCount; i++) {
            Resource resource = new Resource();
            sourceChannel.read(intBuff);
            intBuff.flip();
            resource.setNum(i);
            if (i > 0) {
                char a = (char) ('a' + (i - 1));
                resource.setName(String.format("%s-%s", group.getName(), a));
            } else {
                resource.setName(group.getName());
            }
            resource.setPath(group.getPath());
            resource.setSize(intBuff.getInt());
            intBuff.clear();
            resources.add(resource);
        }
        long position = sourceChannel.position();
        for (int i = 0; i < resourceCount; i++) {
            Resource res = resources.get(i);
            res.setBegin(position);
            position += res.getSize();
            res.setEnd(res.getSize() + res.getBegin() - 1);
        }
        group.setResources(resources);
    }

}
