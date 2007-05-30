/*******************************************************************************
 * Copyright (c) 2002 - 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package com.ibm.wala.ipa.callgraph.propagation.rta;

import java.util.Iterator;

import com.ibm.wala.classLoader.CallSiteReference;
import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.impl.FakeRootMethod;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContextInsensitiveSSAInterpreter;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.debug.Trace;
import com.ibm.wala.util.warnings.WarningSet;

/**
 *
 * Basic analysis; context-insensitive
 * 
 * @author sfink
 */
public class DefaultRTAInterpreter implements RTAContextInterpreter {

  private static final boolean DEBUG = false;
  /**
   * Object to track analysis warnings
   */
  private WarningSet warnings;

  private final ContextInsensitiveRTAInterpreter defaultInterpreter;

  /**
   * @param options governing analysis options
   * @param cha governing class hierarchy
   * @param warnings an object to track analysis warnings
   */
  public DefaultRTAInterpreter(AnalysisOptions options, ClassHierarchy cha, WarningSet warnings) {
    this.warnings = warnings;
    defaultInterpreter = new ContextInsensitiveSSAInterpreter(options, cha);
  }

  private RTAContextInterpreter getNodeInterpreter(CGNode node) {
    
    if (node.getMethod() instanceof FakeRootMethod) {
      FakeRootMethod f = (FakeRootMethod)node.getMethod();
      return f.getInterpreter();
    } else {
      if (DEBUG) {
        Trace.println("providing context insensitive interpreter for node " + node);
      }
      return defaultInterpreter;
    } 
  }

  public Iterator<NewSiteReference> iterateNewSites(CGNode node, WarningSet warnings) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    return getNodeInterpreter(node).iterateNewSites(node,warnings);
  }

  public Iterator<CallSiteReference> iterateCallSites(CGNode node, WarningSet warnings) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    return getNodeInterpreter(node).iterateCallSites(node,warnings);
  }

  public Iterator iterateFieldsRead(CGNode node, WarningSet warnings) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    return getNodeInterpreter(node).iterateFieldsRead(node, warnings);
  }

  public Iterator iterateFieldsWritten(CGNode node, WarningSet warnings) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    return getNodeInterpreter(node).iterateFieldsWritten(node, warnings);
  }


  public boolean understands(CGNode node) {
    return true;
  }
  
  public boolean recordFactoryType(CGNode node, IClass klass) {
    // not a factory type
    return false;
  }
  
  public void setWarnings(WarningSet newWarnings) {
    this.warnings = newWarnings;
  }

  public WarningSet getWarnings() {
    return warnings;
  }
}
