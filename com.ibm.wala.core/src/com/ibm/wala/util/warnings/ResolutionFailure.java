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
package com.ibm.wala.util.warnings;

import com.ibm.wala.ipa.callgraph.CGNode;

/**
 * 
 * A failure to resolve some entity while processing a particular
 * node
 * 
 * @author sfink
 */
public class ResolutionFailure extends MethodWarning {

  final Object ref;
  final String message;

  /**
   * @param node
   * @param ref
   * @throws NullPointerException  if node is null
   */
  public ResolutionFailure(CGNode node, Object ref, String message) throws NullPointerException {
    super(Warning.SEVERE, node.getMethod().getReference());
    this.message = message;
    this.ref = ref;
  }

  private ResolutionFailure(CGNode node, Object ref) {
    this(node, ref, null);
  }

  /*
   * @see com.ibm.wala.util.warnings.Warning#getMsg()
   */
  @Override
  public String getMsg() {
    if (message == null) {
      return getClass() + " " + getMethod() + " " + ref;
    } else {
      return getClass() + " " + getMethod() + ": " + message + " for " + ref;
    }
  }

  public static ResolutionFailure create(CGNode node, Object ref) throws IllegalArgumentException {
    if (node == null) {
      throw new IllegalArgumentException("node cannot be null");
    }
    return make(node, ref);
  }

  public static ResolutionFailure create(CGNode node, Object ref, String msg) throws IllegalArgumentException {
    if (node == null) {
      throw new IllegalArgumentException("node cannot be null");
    }
    return new ResolutionFailure(node, ref, msg);
  }

  public static ResolutionFailure make(CGNode node, Object ref) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    return new ResolutionFailure(node, ref);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (getClass().equals(obj.getClass())) {
      ResolutionFailure other = (ResolutionFailure)obj;
      return (getMethod().equals(other.getMethod()) && getLevel()==other.getLevel() && ref.equals(other.ref));
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return getMethod().hashCode() * 8999 + ref.hashCode() * 8461 + getLevel();
  }
  
  

}
