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
package com.ibm.wala.ipa.callgraph.propagation;

import com.ibm.wala.classLoader.IClass;
import com.ibm.wala.classLoader.IMethod;
import com.ibm.wala.classLoader.NewSiteReference;
import com.ibm.wala.classLoader.ProgramCounter;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.CGNode;
import com.ibm.wala.ipa.callgraph.propagation.cfa.ContainerContextSelector;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.warnings.ResolutionFailure;
import com.ibm.wala.util.warnings.WarningSet;

/**
 * 
 * This class provides instance keys where for a given type T in a CGNode
 * N, there is one "abstract allocation site" instance for all T allocations
 * in node N.
 * 
 * @author sfink
 */
public class SmushedAllocationSiteInstanceKeys implements InstanceKeyFactory {

  /**
   * Governing call graph contruction options
   */
  private final AnalysisOptions options;

  /**
   * An object to track analysis warnings
   */
  private final WarningSet warnings;

  /**
   * Governing class hierarchy
   */
  private final ClassHierarchy cha;

  private final ClassBasedInstanceKeys classBased;

  /**
   * @param options
   *          Governing call graph contruction options
   * @param warnings
   *          An object to track analysis warnings
   */
  public SmushedAllocationSiteInstanceKeys(AnalysisOptions options, ClassHierarchy cha, WarningSet warnings) {
    this.options = options;
    this.cha = cha;
    this.warnings = warnings;
    this.classBased = new ClassBasedInstanceKeys(options, cha, warnings);
  }

  public InstanceKey getInstanceKeyForAllocation(CGNode node, NewSiteReference allocation) {
    IClass type = options.getClassTargetSelector().getAllocatedTarget(node, allocation);
    if (type == null) {
      warnings.add(ResolutionFailure.create(node,allocation));
      return null;
    }

    // disallow recursion in contexts.
    if (node.getContext() instanceof ReceiverInstanceContext) {
      IMethod m = node.getMethod();
      CGNode n = ContainerContextSelector.findNodeRecursiveMatchingContext(m, node.getContext());
      if (n != null) {
        return new SmushedAllocationSiteKey(n, type);
      }
    }

    InstanceKey key = new SmushedAllocationSiteKey(node, type);

    return key;
  }

  public InstanceKey getInstanceKeyForMultiNewArray(CGNode node, NewSiteReference allocation, int dim) {
    IClass type = options.getClassTargetSelector().getAllocatedTarget(node, allocation);
    if (type == null) {
      warnings.add(ResolutionFailure.create(node,allocation));
      return null;
    }
    InstanceKey key = new MultiNewArrayAllocationSiteKey(node, allocation, type, dim);

    return key;
  }

  public InstanceKey getInstanceKeyForConstant(Object S) {
    if (options.getUseConstantSpecificKeys())
      return new ConstantKey(S, cha.lookupClass(options.getConstantType(S)));
    else
      return new ConcreteTypeKey(cha.lookupClass(options.getConstantType(S)));
  }

  public String getStringConstantForInstanceKey(InstanceKey I) {
    if (I instanceof StringConstantKey) {
      return ((StringConstantKey) I).getString();
    } else {
      return null;
    }
  }

  public InstanceKey getInstanceKeyForPEI(CGNode node, ProgramCounter pei, TypeReference type) {
    return classBased.getInstanceKeyForPEI(node, pei, type);
  }

  public InstanceKey getInstanceKeyForClassObject(TypeReference type) {
    return classBased.getInstanceKeyForClassObject(type);
  }

}
