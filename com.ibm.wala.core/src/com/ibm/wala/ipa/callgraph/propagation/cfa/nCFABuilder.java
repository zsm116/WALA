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

package com.ibm.wala.ipa.callgraph.propagation.cfa;

import com.ibm.wala.analysis.reflection.FactoryBypassInterpreter;
import com.ibm.wala.ipa.callgraph.AnalysisOptions;
import com.ibm.wala.ipa.callgraph.ContextSelector;
import com.ibm.wala.ipa.callgraph.ReflectionSpecification;
import com.ibm.wala.ipa.callgraph.impl.DefaultContextSelector;
import com.ibm.wala.ipa.callgraph.impl.DelegatingContextSelector;
import com.ibm.wala.ipa.callgraph.propagation.ClassBasedInstanceKeys;
import com.ibm.wala.ipa.callgraph.propagation.SSAContextInterpreter;
import com.ibm.wala.ipa.cha.ClassHierarchy;
import com.ibm.wala.util.warnings.WarningSet;

/**
 * 
 * nCFA Call graph builder
 * 
 * @author Julian Dolby (dolby@us.ibm.com)
 */
public class nCFABuilder extends CFABuilder {

  public nCFABuilder(int n, ClassHierarchy cha, WarningSet warnings, AnalysisOptions options, ContextSelector appContextSelector,
      SSAContextInterpreter appContextInterpreter, ReflectionSpecification reflect) {

    super(cha, warnings, options);
    if (options == null) {
      throw new IllegalArgumentException("options is null");
    }

    setInstanceKeys(new ClassBasedInstanceKeys(options, cha, warnings));

    ContextSelector def = new DefaultContextSelector(cha, options.getMethodTargetSelector());
    ContextSelector contextSelector = new DelegatingContextSelector(appContextSelector, def);
    contextSelector = new nCFAContextSelector(n, contextSelector);
    setContextSelector(contextSelector);

    SSAContextInterpreter defI = new DefaultSSAInterpreter(options, cha, warnings);
    defI = new DelegatingSSAContextInterpreter(new FactoryBypassInterpreter(options, cha, reflect, warnings), defI);
    SSAContextInterpreter contextInterpreter = new DelegatingSSAContextInterpreter(appContextInterpreter, defI);
    setContextInterpreter(contextInterpreter);
  }

  /*
   * @see com.ibm.wala.ipa.callgraph.propagation.PropagationCallGraphBuilder#getDefaultDispatchBoundHeuristic()
   */
  @Override
  protected byte getDefaultDispatchBoundHeuristic() {
    return AnalysisOptions.CHA_DISPATCH_BOUND;
  }
}
