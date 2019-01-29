/**
 *  Copyright 2014 Ryszard Wiśniewski <brut.alll@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package brut.androlib.res;

import brut.androlib.AndrolibException;
import brut.androlib.err.CantFindFrameworkResException; 
import brut.androlib.res.data.*;
import brut.androlib.res.decoder.*;
import brut.androlib.res.decoder.ARSCDecoder.ARSCData;
import brut.androlib.res.decoder.ARSCDecoder.FlagsOffset;
import brut.androlib.res.xml.ResValuesXmlSerializable;
import brut.androlib.res.xml.ResXmlPatcher;
import brut.util.Duo;
import org.xmlpull.v1.XmlSerializer;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import brut.androlib.err.DirectoryException;

/**
 * @author Ryszard Wiśniewski <brut.alll@gmail.com>
 */
final public class AndrolibResources {
    
    private String frameworkApkPath;
    
    public AndrolibResources(String frameworkApkPath){
        this.frameworkApkPath=frameworkApkPath;
    }
    
    public ZipFile getFrameworkApk(int id)
    throws AndrolibException {
        try{
            return new ZipFile(frameworkApkPath);
        }catch(IOException e){
            throw new AndrolibException(e);              
        }
    }
    
    public ResTable getResTable(ZipFile apkFile, boolean loadMainPkg)
    throws AndrolibException {
        ResTable resTable = new ResTable(this);
        if (loadMainPkg) {
            loadMainPkg(resTable, apkFile);
        }
        return resTable;
    }

    public ResPackage loadMainPkg(ResTable resTable, ZipFile apkFile)
    throws AndrolibException {
        LOGGER.info("Loading resource table...");
        ResPackage[] pkgs = getResPackagesFromApk(apkFile, resTable, sKeepBroken);
        ResPackage pkg = null;

        switch (pkgs.length) {
            case 1:
                pkg = pkgs[0];
                break;
            case 2:
                if (pkgs[0].getName().equals("android")) {
                    LOGGER.warning("Skipping \"android\" package group");
                    pkg = pkgs[1];
                    break;
                } else if (pkgs[0].getName().equals("com.htc")) {
                    LOGGER.warning("Skipping \"htc\" package group");
                    pkg = pkgs[1];
                    break;
                }

            default:
                pkg = selectPkgWithMostResSpecs(pkgs);
                break;
        }

        if (pkg == null) {
            throw new AndrolibException("arsc files with zero packages or no arsc file found.");
        }

        resTable.addPackage(pkg, true);
        return pkg;
    }
    
    public ResPackage selectPkgWithMostResSpecs(ResPackage[] pkgs)
        throws AndrolibException {
        int id = 0;
        int value = 0;

        for (ResPackage resPackage : pkgs) {
            if (resPackage.getResSpecCount() > value && ! resPackage.getName().equalsIgnoreCase("android")) {
                value = resPackage.getResSpecCount();
                id = resPackage.getId();
            }
        }

        // if id is still 0, we only have one pkgId which is "android" -> 1
        return (id == 0) ? pkgs[0] : pkgs[1];
    }

    public ResPackage loadFrameworkPkg(ResTable resTable, int id)
            throws AndrolibException {
        ZipFile apk = getFrameworkApk(id);

        LOGGER.info("Loading resource table from file: " + apk);
        ResPackage[] pkgs = getResPackagesFromApk(apk, resTable, true);

        try{
            apk.close();
        }catch(IOException e){}

        ResPackage pkg;
        if (pkgs.length > 1) {
            pkg = selectPkgWithMostResSpecs(pkgs);
        } else if (pkgs.length == 0) {
            throw new AndrolibException("Arsc files with zero or multiple packages");
        } else {
            pkg = pkgs[0];
        }

        if (pkg.getId() != id) {
            throw new AndrolibException("Expected pkg of id: " + String.valueOf(id) + ", got: " + pkg.getId());
        }

        resTable.addPackage(pkg, false);
        return pkg;
    }

    private ResPackage[] getResPackagesFromApk(ZipFile apkFile,ResTable resTable, boolean keepBroken)
            throws AndrolibException {
        try {
            ZipEntry arsc=apkFile.getEntry("resources.arsc");
            if(arsc==null) throw new DirectoryException();
            BufferedInputStream bfi = new BufferedInputStream(apkFile.getInputStream(arsc));
            return ARSCDecoder.decode(bfi, false, keepBroken, resTable).getPackages();
        } catch (DirectoryException ex) {
            throw new AndrolibException("Could not load resources.arsc from file: " + apkFile, ex);
        } catch (IOException ex) {
            throw new AndrolibException("Could not load resources.arsc from file: " + apkFile, ex);
        }
    }

    // TODO: dirty static hack. I have to refactor decoding mechanisms.
    public static boolean sKeepBroken = false;

    private final static Logger LOGGER = Logger.getLogger(AndrolibResources.class.getName());

    //private File mFrameworkDirectory = null;

    //private String mMinSdkVersion = null;
    //private String mMaxSdkVersion = null;
    //private String mTargetSdkVersion = null;
    //private String mVersionCode = null;
    //private String mVersionName = null;
    //private String mPackageRenamed = null;
    //private String mPackageId = null;

    //private boolean mSharedLibrary = false;

    //private final static String[] IGNORED_PACKAGES = new String[] {
    //        "android", "com.htc", "miui", "com.lge", "com.lge.internal", "yi", "com.miui.core", "flyme",
    //        "air.com.adobe.appentry" };

    //private final static String[] ALLOWED_PACKAGES = new String[] {
    //        "com.miui" };
}
