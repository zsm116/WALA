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
package com.ibm.wala.util.collections;

import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.intset.IntIterator;

/**
 * 
 * An Iterator which provides a concatenation of two IntIterators.
 * 
 * @author sfink
 */
public class CompoundIntIterator implements IntIterator {

  IntIterator A;

  IntIterator B;


  /**
   * @param A the first iterator in the concatenated result
   * @param B the second iterator in the concatenated result
   */
  public CompoundIntIterator(IntIterator A, IntIterator B) {
    this.A = A;
    this.B = B;
  }


  public boolean hasNext() {
    return A.hasNext() || B.hasNext();
  }


  public int next() {
    if (A.hasNext()) {
      return A.next();
    } else {
      return B.next();
    }
  }

  @Override
  public int hashCode() {
    Assertions.UNREACHABLE("define a custom hash code to avoid non-determinism");
    return 0;
  }
}