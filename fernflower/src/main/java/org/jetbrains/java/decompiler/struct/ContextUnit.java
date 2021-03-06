/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jetbrains.java.decompiler.struct;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader.Link;
import org.jetbrains.java.decompiler.util.DataInputFullStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public class ContextUnit {

  public static final int TYPE_FOLDER = 0;
  public static final int TYPE_JAR = 1;
  public static final int TYPE_ZIP = 2;

  private final int type;
  private final boolean own;

  private final String archivePath;  // relative path to jar/zip
  private final String filename;     // folder: relative path, archive: file name
  private final IResultSaver resultSaver;
  private final IDecompiledData decompiledData;

  private final List<String> classEntries = new ArrayList<>();  // class file or jar/zip entry
  private final List<String> dirEntries = new ArrayList<>();
  private final List<String[]> otherEntries = new ArrayList<>();

  private List<StructClass> classes = new ArrayList<>();
  private Manifest manifest;

  public ContextUnit(int type, String archivePath, String filename, boolean own, IResultSaver resultSaver, IDecompiledData decompiledData) {
    this.type = type;
    this.own = own;
    this.archivePath = archivePath;
    this.filename = filename;
    this.resultSaver = resultSaver;
    this.decompiledData = decompiledData;
  }

  public void addClass(StructClass cl, String entryName) {
    classes.add(cl);
    classEntries.add(entryName);
  }

  public void addDirEntry(String entry) {
    dirEntries.add(entry);
  }

  public void addOtherEntry(String fullPath, String entry) {
    otherEntries.add(new String[]{fullPath, entry});
  }

  public void reload(LazyLoader loader) throws IOException {
    List<StructClass> lstClasses = new ArrayList<>();

    for (StructClass cl : classes) {
      String oldName = cl.qualifiedName;

      StructClass newCl;
      
      LazyLoader.Link link=  loader.getClassLink(oldName);
      DataInputFullStream in =link!=null? loader.getClassStream(link):null;
     
      newCl = new StructClass(in, cl.isOwn(), loader);
      in.close();

      lstClasses.add(newCl);

      Link lnk = loader.getClassLink(oldName);
      loader.removeClassLink(oldName);
      loader.addClassLink(newCl.qualifiedName, lnk);
    }

    classes = lstClasses;
  }

  public void save() {
    

        // classes
        for (int i = 0; i < classes.size(); i++) {
          StructClass cl = classes.get(i);
         
          String clsTag=classEntries.get(i);
          
          if (clsTag != null) {
            String content = decompiledData.getClassContent(cl);
            if (content != null) {
              int[] mapping = null;
              if (DecompilerContext.getOption(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING)) {
                mapping = DecompilerContext.getBytecodeSourceMapper().getOriginalLinesMapping();
              }
              resultSaver.saveResult(clsTag, content);
            }
          }
        }

  }

  public void setManifest(Manifest manifest) {
    this.manifest = manifest;
  }

  public boolean isOwn() {
    return own;
  }

  public List<StructClass> getClasses() {
    return classes;
  }
}
