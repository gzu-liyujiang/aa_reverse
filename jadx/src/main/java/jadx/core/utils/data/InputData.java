package jadx.core.utils.data;

import jadx.exception.DecodeException;
import jadx.exception.JadxException;
import jadx.exception.JadxRuntimeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.android.dex.Dex;

import com.android.dx.dex.DexOptions;
import com.android.dx.dex.file.DexFile;
import java.io.OutputStreamWriter;
import java.io.ByteArrayOutputStream;
import com.android.dex.DexFormat;

public class InputData {
    
    private final byte[] data;
    private Dex dexBuf;
    
    public InputData(byte[] data) throws IOException, DecodeException {
        if (data==null) {
            throw new IOException("InputData is null");
        }
        this.data = data;     
        this.dexBuf=new Dex(data);
    }

    public Dex getDexBuf(){
        return dexBuf;
    }
    
}
