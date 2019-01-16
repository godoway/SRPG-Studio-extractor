package gwsl.srpgstudio.extractor.feature;

import gwsl.srpgstudio.extractor.struct.DataStruct;
import gwsl.srpgstudio.extractor.struct.FileType;
import gwsl.srpgstudio.extractor.struct.Resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.stream.Collectors;

public class Extractor {

    private DataStruct struct = DataStruct.getInstance();
    private File target;
    private File output;
    private boolean cutOnly;
//    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4)

    public Extractor(File target, File output) {
        this.target = target;
        this.output = output;
    }

    public void extract() {
        if (!cutOnly) extractResource();
        extractProject();
    }

    private void extractProject() {
        System.out.println("start to unpack project...");
        Resource projectResource = struct.getProjectResource();
        ExtractTask projectTask = new ExtractTask(projectResource);
        projectTask.run();
        System.out.println("unpack project end.");
    }

    private void extractResource() {
        System.out.println("start to extract...");
        List<Resource> resources = struct.getFragments().stream()
                .filter(fragment -> fragment.getResourceGroups() != null)
                .flatMap(fragment -> fragment.getResourceGroups().stream())
                .flatMap(resourceGroup -> resourceGroup.getResources().stream())
                .collect(Collectors.toList());
        for (int i = resources.size() - 1; i > -1; i--) {
            ExtractTask task = new ExtractTask(resources.get(i));
            task.run();
        }
        System.out.println("extract end.");
    }

    private class ExtractTask implements Runnable {

        private Resource resource;
        private long realSize;
        private int buffSize = 1024 * 32;

        ExtractTask(Resource resource) {
            this.resource = resource;
            this.realSize = resource.getSize();
        }

        @Override
        public void run() {
            try (RandomAccessFile sourceFile = new RandomAccessFile(target, "r");
                 FileChannel sourceChannel = sourceFile.getChannel()) {

                sourceChannel.position(resource.getBegin());
                ByteBuffer sourceBuff = ByteBuffer.allocate(buffSize);
                ByteBuffer outputBuff = ByteBuffer.allocate(buffSize);
                ByteBuffer headerBuff = ByteBuffer.allocate(5);

                sourceChannel.read(headerBuff);
                headerBuff.flip();
                byte[] tmp = headerBuff.array();
                headerBuff.clear();
                for (FileType type : struct.getFileTypeList()) {
                    byte[] header = type.getHeader();
                    boolean same = false;
                    for (int i = 0; i < header.length; i++) {
                        same = header[i] == tmp[i];
                    }
                    if (same) resource.setSuffix(type.getSuffix());
                }
                File save = new File(output, resource.savePath());
                File parent = save.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    throw new IllegalStateException("Couldn't create dir: " + parent);
                }
                System.out.println("extractor: " + save.getPath());
                RandomAccessFile outputFile = new RandomAccessFile(save, "rw");
                FileChannel outputChannel = outputFile.getChannel();
                sourceChannel.position(resource.getBegin());

                long hadRead = 0L;
                long hadWrite = 0L;
                int srcRead = sourceChannel.read(sourceBuff);

                while (srcRead != -1) {
                    hadRead += srcRead;

                    sourceBuff.flip();
                    byte[] srcBytes = sourceBuff.array();
                    sourceBuff.clear();

                    int needWrite = srcRead;
                    if (hadRead > realSize) {
                        needWrite = (int) (realSize - (hadRead - buffSize));
                    }

                    outputBuff.put(srcBytes, 0, needWrite);
                    outputBuff.flip();

                    long write = outputChannel.write(outputBuff);
                    outputBuff.clear();
                    hadWrite += write;
                    if (hadWrite == realSize) {
                        return;
                    }
                    srcRead = sourceChannel.read(sourceBuff);
                }

                outputChannel.close();
                outputFile.close();
                Thread.sleep(10);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void setCutOnly(boolean cutOnly) {
        this.cutOnly = cutOnly;
    }
}
