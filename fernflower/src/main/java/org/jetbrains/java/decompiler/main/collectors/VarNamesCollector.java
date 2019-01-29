/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
package org.jetbrains.java.decompiler.main.collectors;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.struct.gen.VarType;

public class VarNamesCollector {

  private final Set<String> usedNames = new HashSet<>();

  public VarNamesCollector() { }

  public VarNamesCollector(Collection<String> setNames) {
    usedNames.addAll(setNames);
  }

  public void addName(String value) {
    usedNames.add(value);
  }
  /*
    public String getFreName(String type,int index) {
        String name=type.substring(type.lastIndexOf('/')+1);
        name=name.toLowerCase();
        while (usedNames.contains(name)) {
            name += index;
        }
        usedNames.add(name);
        return name;
    }
    */
  public String getFreeName(VarType var_type,int index) {
      String type;
      int p;
      if((p=var_type.value.lastIndexOf('$'))!=-1)
          type=var_type.value.substring(p+1);
      else if((p=var_type.value.lastIndexOf('/'))!=-1)
          type=var_type.value.substring(p+1);
      else
          type=var_type.value;//not package or base type
      
      if(type.equals("String"))
          type="str";
      else if(type.equals("Class"))
          type="cls";
      else if(type.equals("Integer"))
          type="i";
      else if(type.equals("Character"))
          type="ch";
      else if(type.equals("Byte"))
          type="b";
      else if(type.equals("Short"))
          type="s";
      else if(type.equals("Object"))
          type="obj";
      else if(type.charAt(0)>='0'&&type.charAt(0)<='9')//anonymous class
          type="impl";
          
          
      {//handlde name
          if(DecompilerContext.getOption(IFernflowerPreferences.LOCAL_VAR_USE_HUMP_STYLE)){
              int ch=type.charAt(0);
              if(ch>='A'&&ch<='Z')
                  ch=ch-'A'+'a';
              type=Character.toString((char)ch)+type.substring(1);
          }
          else{// underline style
              type=type.replaceAll("[A-Z]","_$0").toLowerCase();
              if(type.charAt(0)=='_')
                  type=type.substring(1);
          }
      }
      
      int i=0;
      String name=type;
      while (usedNames.contains(name)) {
          name=type + ++i;
      }
      usedNames.add(name);
      return name;
  }
 
  public String getFreeName(String proposition) {
    while (usedNames.contains(proposition)) {
      proposition += "_";
    }
    usedNames.add(proposition);
    return proposition;
  }
}
