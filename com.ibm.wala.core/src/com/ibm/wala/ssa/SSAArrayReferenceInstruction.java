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
package com.ibm.wala.ssa;

import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author sfink
 *  
 */
public abstract class SSAArrayReferenceInstruction extends SSAInstruction {

  private final int arrayref;

  private final int index;

  private final TypeReference declaredType;

  SSAArrayReferenceInstruction(int arrayref, int index, TypeReference declaredType) {
    this.arrayref = arrayref;
    this.index = index;
    this.declaredType = declaredType;
  }

  /*
   * @see com.ibm.wala.ssa.SSAInstruction#getNumberOfUses()
   */
  @Override
  public int getNumberOfUses() {
    return 2;
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#getUse(int)
   */
  @Override
  public int getUse(int j) {
    if (Assertions.verifyAssertions)
      Assertions._assert(j <= 1);
    return (j == 0) ? arrayref : index;
  }

  /**
   * Return the value number of the array reference.
   */
  public int getArrayRef() {
    return arrayref;
  }

  /**
   * Return the value number of the index of the array reference.
   */
  public int getIndex() {
    return index;
  }

  public TypeReference getDeclaredType() {
    return declaredType;
  }

  /**
   * @return true iff this represents an aload of a primitive type element
   */
  public boolean typeIsPrimitive() {
    return declaredType.isPrimitiveType();
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#isPEI()
   */
  @Override
  public boolean isPEI() {
    return true;
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#isFallThrough()
   */
  @Override
  public boolean isFallThrough() {
    return true;
  }

}
