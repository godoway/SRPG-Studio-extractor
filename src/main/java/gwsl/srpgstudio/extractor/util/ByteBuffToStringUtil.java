package gwsl.srpgstudio.extractor.util;

import java.nio.ByteBuffer;

public class ByteBuffToStringUtil {

    public static String converToString(ByteBuffer buffer, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < chars.length; i++) {
            chars[i] = buffer.getChar();
        }
        return String.valueOf(chars, 0, length - 1);

    }
}
