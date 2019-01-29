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
import org.jetbrains.java.decompiler.main.extern.IResultSaver;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.jetbrains.java.decompiler.util.InterpreterUtil;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;

public class StructContext {

  private final IResultSaver saver;
  private final IDecompiledData decompiledData;
  private final LazyLoader loader;
  private final Map<String, ContextUnit> units = new HashMap<>();
  private final Map<String, StructClass> classes = new HashMap<>();

  public StructContext(IResultSaver saver, IDecompiledData decompiledData, LazyLoader loader) {
    this.saver = saver;
    this.decompiledData = decompiledData;
    this.loader = loader;

    ContextUnit defaultUnit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, "", true, saver, decompiledData);
    units.put("", defaultUnit);
  }

  public StructClass getClass(String name) {
    return classes.get(name);
  }

  public void reloadContext() throws IOException {
    for (ContextUnit unit : units.values()) {
      for (StructClass cl : unit.getClasses()) {
        classes.remove(cl.qualifiedName);
      }

      unit.reload(loader);

      // adjust global class collection
      for (StructClass cl : unit.getClasses()) {
        classes.put(cl.qualifiedName, cl);
      }
    }
  }

  public void saveContext() {
    for (ContextUnit unit : units.values()) {
      if (unit.isOwn()) {
        unit.save();
      }
    }
  }

  public boolean addSpace(String tag,boolean isOwn){
      ContextUnit unit = units.get(tag);
      
      if (unit != null) {
          String message = "Error! Multi-Add Same Tag: " + tag;
          DecompilerContext.getLogger().writeMessage(message,IFernflowerLogger.Severity.ERROR);        
          return false;        
      }
      
      unit = new ContextUnit(ContextUnit.TYPE_FOLDER, null, tag, isOwn, saver, decompiledData);
      units.put(tag, unit);
      
      try {
          DataInputFullStream in = loader.getClassStream(tag);
          StructClass cl = new StructClass(in, isOwn, loader);
          classes.put(cl.qualifiedName, cl);
          unit.addClass(cl,tag);
          loader.addClassLink(cl.qualifiedName, new LazyLoader.Link(LazyLoader.Link.CLASS, tag));
          in.close();
      }
      catch (IOException ex) {
          String message = "Corrupted class ! Tag: " + tag;
          DecompilerContext.getLogger().writeMessage(message, ex);
          return false;
      }
      return true;
  }
  
  public Map<String, StructClass> getClasses() {
    return classes;
  }
}
