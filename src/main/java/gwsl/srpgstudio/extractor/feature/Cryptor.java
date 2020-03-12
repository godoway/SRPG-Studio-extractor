package gwsl.srpgstudio.extractor.feature;

import gwsl.srpgstudio.extractor.struct.DataStruct;
import gwsl.srpgstudio.extractor.struct.Resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.stream.Collectors;

public class Cryptor {

    private DataStruct struct = DataStruct.getInstance();
    private String cryptMode = struct.isEncrypt() ? "decrypt" : "encrypt";
    private File key;
    private File target;
    private List<File> generateKeyList;
//    private ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

    public Cryptor(File key, List<File> generateKeyList) {
        this.key = key;
        this.generateKeyList = generateKeyList;
    }

    public Cryptor(File key, File target) {
        this.key = key;
        this.target = target;
    }

    public void generateKey() throws IOException {
        int buffSize = 1024 * 128;
        if (generateKeyList == null || generateKeyList.size() != 2) {
            System.out.println("file can not found");
            return;
        }
        System.out.println(String.format("generate key from: [%s, %s]", generateKeyList.get(0).getPath(), generateKeyList.get(1).getPath()));
        RandomAccessFile encryptFile = new RandomAccessFile(generateKeyList.get(0), "r");
        RandomAccessFile decryptFile = new RandomAccessFile(generateKeyList.get(1), "r");
        RandomAccessFile keyFile = new RandomAccessFile(key, "rw");

        FileChannel encryptChannel = encryptFile.getChannel();
        FileChannel decryptChannel = decryptFile.getChannel();
        FileChannel keyChannel = keyFile.getChannel();
        ByteBuffer encryptBuff = ByteBuffer.allocate(buffSize);
        ByteBuffer decryptBuff = ByteBuffer.allocate(buffSize);
        ByteBuffer keyBuff = ByteBuffer.allocate(buffSize);

        ByteBuffer intBuff = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        encryptChannel.position(20L);
        encryptChannel.read(intBuff);
        intBuff.flip();

        long begin = intBuff.getInt() + 168;
        long end = encryptFile.length();
        long readSize = end - begin;
        intBuff.clear();
        System.out.println("begin: " + begin);
        System.out.println("readSize: " + readSize);

        encryptChannel.position(begin);
        decryptChannel.position(begin);

        int encryptRead = encryptChannel.read(encryptBuff);
        decryptChannel.read(decryptBuff);

        while (encryptRead != -1) {
            encryptBuff.flip();
            decryptBuff.flip();
            byte[] encryptBytes = encryptBuff.array();
            byte[] decryptBytes = decryptBuff.array();

            for (int i = 0; i < encryptRead; i++) {
                encryptBytes[i] ^= decryptBytes[i];
            }
            if (encryptRead < buffSize) {
                keyBuff.put(encryptBytes, 0, encryptRead);
            } else {
                keyBuff.put(encryptBytes);
            }
            keyBuff.flip();
            keyChannel.write(keyBuff);
            encryptBuff.clear();
            decryptBuff.clear();
            keyBuff.clear();

            encryptRead = encryptChannel.read(encryptBuff);
            decryptChannel.read(decryptBuff);
        }
        encryptChannel.close();
        decryptChannel.close();
        keyChannel.close();
        System.out.println("generateKey success.");
    }

    public void crypt() throws IOException {
        System.out.println(String.format("start to %s resource...", cryptMode));
        List<Resource> resources = struct.getFragments().stream()
                .filter(fragment -> fragment.getResourceGroups() != null)
                .flatMap(fragment -> fragment.getResourceGroups().stream())
                .flatMap(resourceGroup -> resourceGroup.getResources().stream())
                .collect(Collectors.toList());
        resources.forEach(resource -> {
            DecryptTask task = new DecryptTask(resource);
            task.run();
        });

        System.out.println(String.format("start to %s project...", cryptMode));
        Resource projectResource = struct.getProjectResource();
        DecryptTask projectTask = new DecryptTask(projectResource);
        projectTask.run();
        System.out.println(String.format("%s project end.", cryptMode));


        RandomAccessFile sourceFile = new RandomAccessFile(target, "rw");
        FileChannel sourceChannel = sourceFile.getChannel();
        ByteBuffer intBuff = ByteBuffer.allocate(Integer.BYTES).order(ByteOrder.LITTLE_ENDIAN);
        intBuff.putInt(struct.isEncrypt() ? 0 : 1);
        intBuff.flip();
        sourceChannel.position(4);
        sourceChannel.write(intBuff);
        intBuff.clear();
        sourceChannel.close();
        sourceFile.close();
        System.out.println(String.format("now is %s.", cryptMode));
    }


    private class DecryptTask implements Runnable {

        private Resource resource;
        private long realSize;
        private int buffSize = 1024 * 32;

        DecryptTask(Resource resource) {
            this.resource = resource;
            this.realSize = resource.getSize();
        }

        @Override
        public void run() {
            System.out.println(String.format("start to %s [%s] begin : [%d]...", cryptMode, resource.savePath(), resource.getBegin()));
            try (RandomAccessFile sourceFile = new RandomAccessFile(target, "rw");
                 RandomAccessFile keyFile = new RandomAccessFile(key, "r");
                 FileChannel sourceChannel = sourceFile.getChannel();
                 FileChannel keyChannel = keyFile.getChannel()) {

                sourceChannel.position(resource.getBegin());
                ByteBuffer sourceBuff = ByteBuffer.allocate(buffSize);
                ByteBuffer keyBuff = ByteBuffer.allocate(buffSize);
                long hadRead = 0L;
                long hadWrite = 0L;
                int srcRead = sourceChannel.read(sourceBuff);
                keyChannel.read(keyBuff);

                while (srcRead != -1) {
                    hadRead += srcRead;

                    sourceBuff.flip();
                    keyBuff.flip();
                    byte[] srcBytes = sourceBuff.array();
                    byte[] keyBytes = keyBuff.array();
                    sourceBuff.clear();
                    keyBuff.clear();

                    for (int i = 0; i < srcRead; i++) {
                        srcBytes[i] ^= keyBytes[i];
                    }
                    int needWrite = srcRead;
                    if (hadRead > realSize) {
                        needWrite = (int) (realSize - (hadRead - buffSize));
                    }

                    sourceBuff.put(srcBytes, 0, needWrite);
                    sourceBuff.flip();
                    long writePosition = sourceChannel.position() - srcRead;
                    sourceChannel.position(writePosition);
                    long write = sourceChannel.write(sourceBuff);
                    sourceBuff.clear();
                    hadWrite += write;
                    if (hadWrite == realSize) {
                        return;
                    }
                    srcRead = sourceChannel.read(sourceBuff);
                    keyChannel.read(keyBuff);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(String.format("%s [%s] end.", cryptMode, resource.savePath()));
        }

        public Resource getResource() {
            return resource;
        }

        public void setResource(Resource resource) {
            this.resource = resource;
        }

        public long getRealSize() {
            return realSize;
        }

        public void setRealSize(long realSize) {
            this.realSize = realSize;
        }

        public int getBuffSize() {
            return buffSize;
        }

        public void setBuffSize(int buffSize) {
            this.buffSize = buffSize;
        }
    }

}
