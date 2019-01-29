package org.slf4j;

import android.util.*;
import android.widget.TextView;
import android.text.Html;
import android.os.Handler;
import android.os.Message;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class LoggerFactory{

    private LoggerFactory(){
    }

    public static Logger getLogger(Class reSugarCode){
        return new Logger_Android(reSugarCode.getName());
    }
}
