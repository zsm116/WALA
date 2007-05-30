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

import com.ibm.wala.types.FieldReference;
import com.ibm.wala.types.TypeReference;

/**
 * 
 * Abstract base class for GetInstruction and PutInstruction
 * 
 * @author sfink
 */
public abstract class SSAFieldAccessInstruction extends SSAInstruction {

  private final FieldReference field;

  private final int ref;

  protected SSAFieldAccessInstruction(FieldReference field, int ref) throws IllegalArgumentException {
    super();
    this.field = field;
    this.ref = ref;
    if (field == null) {
      throw new IllegalArgumentException("field cannot be null");
    }
  }

  /**
   * @return TypeReference
   */
  public TypeReference getDeclaredFieldType() {
    return field.getFieldType();
  }

  /**
   * @return TypeReference
   */
  public FieldReference getDeclaredField() {
    return field;
  }

  public int getRef() {
    return ref;
  }

  public boolean isStatic() {
    return ref == -1;
  }

  /*
   * @see com.ibm.wala.ssa.Instruction#isPEI()
   */
  @Override
  public boolean isPEI() {
    return true;
  }

}
