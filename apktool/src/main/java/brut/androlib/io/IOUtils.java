//by       : aenu
//license  : MIT
package brut.androlib.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class IOUtils
{
    public static void copy(InputStream in,OutputStream out) throws IOException{
        int n;
        byte[] buf=new byte[4096];
        while ((n = in.read(buf))!=-1) {
            out.write(buf, 0, n);
        }
    }
}
