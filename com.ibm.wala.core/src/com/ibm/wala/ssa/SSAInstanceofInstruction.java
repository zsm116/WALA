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

import java.util.Collection;

import com.ibm.wala.types.TypeReference;
import com.ibm.wala.util.debug.Assertions;

/**
 * @author sfink
 * 
 */
public class SSAInstanceofInstruction extends SSAInstruction {
  private final int result;

  private final int ref;

  private final TypeReference checkedType;

  SSAInstanceofInstruction(int result, int ref, TypeReference checkedType) {
    super();
    this.result = result;
    this.ref = ref;
    this.checkedType = checkedType;
  }

  @Override
  public SSAInstruction copyForSSA(int[] defs, int[] uses) {
    return new SSAInstanceofInstruction(defs == null || defs.length == 0 ? result : defs[0], uses == null ? ref : uses[0],
        checkedType);
  }

  @Override
  public String toString(SymbolTable symbolTable, ValueDecorator d) {
    return getValueString(symbolTable, d, result) + " = instanceof " + getValueString(symbolTable, d, ref) + " " + checkedType;
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#visit(IVisitor)
   */
  @Override
  public void visit(IVisitor v) throws NullPointerException {
    v.visitInstanceof(this);
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#getDef()
   */
  @Override
  public boolean hasDef() {
    return true;
  }

  @Override
  public int getDef() {
    return result;
  }

  @Override
  public int getDef(int i) {
    Assertions._assert(i == 0);
    return result;
  }

  public TypeReference getCheckedType() {
    return checkedType;
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#getNumberOfUses()
   */
  @Override
  public int getNumberOfDefs() {
    return 1;
  }

  @Override
  public int getNumberOfUses() {
    return 1;
  }

  /**
   * @see com.ibm.wala.ssa.SSAInstruction#getUse(int)
   */
  @Override
  public int getUse(int j) {
    if (Assertions.verifyAssertions)
      Assertions._assert(j == 0);
    return ref;
  }

  @Override
  public int hashCode() {
    return ref * 677 ^ result * 3803;
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#isFallThrough()
   */
  @Override
  public boolean isFallThrough() {
    return true;
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#getExceptionTypes()
   */
  @Override
  public Collection<TypeReference> getExceptionTypes() {
    return null;
  }

  public int getRef() {
    return ref;
  }
}
