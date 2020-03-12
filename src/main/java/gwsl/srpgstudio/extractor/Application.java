package gwsl.srpgstudio.extractor;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import gwsl.srpgstudio.extractor.struct.DataStruct;
import gwsl.srpgstudio.extractor.util.FileConverter;
import gwsl.srpgstudio.extractor.feature.Analyzer;
import gwsl.srpgstudio.extractor.feature.Cryptor;
import gwsl.srpgstudio.extractor.feature.Extractor;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class Application {

//    public static Gson gson = new Gson();

    public static void main(String[] args) throws IOException {
        Arguments arguments = new Arguments();
        JCommander commander = JCommander.newBuilder()
                .addObject(arguments)
                .build();
        commander.parse(args);

        List<File> listToGenerateKey = arguments.getListToGenerateKey();
        File target = arguments.getTarget();
        File key = arguments.getKey();
        File output = arguments.getOutput();
        boolean unpack = arguments.isUnpack();
        boolean cutOnly = arguments.isCut();

        if (!output.exists() && !output.mkdir()) {
            throw new IllegalStateException("Couldn't create output dir: " + output);
        }

        if (listToGenerateKey != null && listToGenerateKey.size() == 2) {
            generateKey(key, listToGenerateKey);
        } else if (target != null && target.exists()) {
            if (!target.getPath().endsWith(".dts")) {
                System.out.println("it is not a srpg studio game.");
                return;
            }
            DataStruct struct = analysis(target);
//            System.out.println(gson.toJson(struct));
           if (unpack) {
                unpack(key, target, output, cutOnly);
            } else {
                crypt(key, target);
            }
        } else {
            commander.usage();
        }

    }


    private static void generateKey(File key, List<File> listToGenerateKey) throws IOException {
        Cryptor cryptor = new Cryptor(key, listToGenerateKey);
        cryptor.generateKey();
    }


    private static DataStruct analysis(File target) throws IOException {
        Analyzer analyzer = new Analyzer(target);
        return analyzer.analysis();
    }

    private static void unpack(File key, File target, File output) throws IOException {
        unpack(key, target, output, false);
    }

    private static void unpack(File key, File target, File output, boolean cutOnly) throws IOException {
        if (DataStruct.getInstance().isEncrypt()) {
            System.out.println("the data.dts is encrypt.");
            crypt(key, target);
        }
        Extractor extractor = new Extractor(target, output);
        extractor.setCutOnly(cutOnly);
        extractor.extract();

    }

    private static void crypt(File key, File target) throws IOException {
        if (!key.exists()) {
            throw new IllegalStateException("key does not exists.");
        }
        Cryptor cryptor = new Cryptor(key, target);
        cryptor.crypt();
    }



    private static class Arguments {
        @Parameter(names = {"--target", "-T"}, converter = FileConverter.class, description = "set file to decrypt or encrypt.")
        private File target;
        @Parameter(names = {"--key", "-K"}, converter = FileConverter.class, description = "set the key file,default is [./key.bin].")
        private File key = new File("key.bin");
        @Parameter(names = {"--generate", "-G"}, arity = 2, converter = FileConverter.class, description = "gennerate key need two file(decrypt/encrypt) from same project.")
        private List<File> listToGenerateKey;
        @Parameter(names = {"--unpack", "-U"}, description = "to unpack,need set target file and key file ")
        private boolean unpack = false;
        @Parameter(names = {"--output", "-O"}, converter = FileConverter.class, description = "set unpack output path, default is [./tmp].")
        private File output = new File("output");
        @Parameter(names = {"--cut", "-C"}, description = "only cut the project file")
        private boolean cut = false;
        @Parameter(names = {"--thread"}, description = "set multiple thread num.(Not implemented)")
        private int threadNum;
        @Parameter(names = {"--patch"}, description = "set the translate file to patch game.(Not implemented)")
        private File patch;

        public File getTarget() {
            return target;
        }

        public void setTarget(File target) {
            this.target = target;
        }

        public File getKey() {
            return key;
        }

        public void setKey(File key) {
            this.key = key;
        }

        public List<File> getListToGenerateKey() {
            return listToGenerateKey;
        }

        public void setListToGenerateKey(List<File> listToGenerateKey) {
            this.listToGenerateKey = listToGenerateKey;
        }

        public boolean isUnpack() {
            return unpack;
        }

        public void setUnpack(boolean unpack) {
            this.unpack = unpack;
        }

        public File getOutput() {
            return output;
        }

        public void setOutput(File output) {
            this.output = output;
        }

        public boolean isCut() {
            return cut;
        }

        public void setCut(boolean cut) {
            this.cut = cut;
        }

        public int getThreadNum() {
            return threadNum;
        }

        public void setThreadNum(int threadNum) {
            this.threadNum = threadNum;
        }

        public File getPatch() {
            return patch;
        }

        public void setPatch(File patch) {
            this.patch = patch;
        }
    }

}
