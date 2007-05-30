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
package com.ibm.wala.util.graph.impl;

import java.util.Iterator;

import com.ibm.wala.util.collections.EmptyIterator;
import com.ibm.wala.util.debug.Assertions;
import com.ibm.wala.util.graph.INodeWithNumberedEdges;
import com.ibm.wala.util.graph.NumberedEdgeManager;
import com.ibm.wala.util.intset.IntIterator;
import com.ibm.wala.util.intset.IntSet;
import com.ibm.wala.util.intset.SparseIntSet;

/**
 * 
 * An object that delegates edge management to the nodes, INodeWithNumberedEdges
 * 
 * @author sfink
 * 
 */
public class DelegatingNumberedEdgeManager<T extends INodeWithNumberedEdges> implements NumberedEdgeManager<T> {

  private final DelegatingNumberedNodeManager<T> nodeManager;

  /**
   * 
   */
  public DelegatingNumberedEdgeManager(DelegatingNumberedNodeManager<T> nodeManager) {
    this.nodeManager = nodeManager;
  }

  // TODO: optimization is possible
  private class IntSetNodeIterator implements Iterator<T> {

    private final IntIterator delegate;

    IntSetNodeIterator(IntIterator delegate) {
      this.delegate = delegate;
    }

    public boolean hasNext() {
      return delegate.hasNext();
    }

    public T next() {
      return nodeManager.getNode(delegate.next());
    }

    public void remove() {
      // TODO Auto-generated method stub
      Assertions.UNREACHABLE();
    }
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#getPredNodes(com.ibm.wala.util.graph.Node)
   */
  public Iterator<T> getPredNodes(T N) throws IllegalArgumentException {
    if (N == null) {
      throw new IllegalArgumentException("N cannot be null");
    }
    INodeWithNumberedEdges en = N;
    IntSet pred = en.getPredNumbers();
    Iterator<T> empty = EmptyIterator.instance();
    return (pred == null) ? empty : (Iterator<T>) new IntSetNodeIterator(pred.intIterator());
  }

  public IntSet getPredNodeNumbers(T node) {
    if (node == null) {
      throw new IllegalArgumentException("N cannot be null");
    }
    INodeWithNumberedEdges en = node;
    IntSet pred = en.getPredNumbers();
    return (pred == null) ? new SparseIntSet() : pred;
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#getPredNodeCount(com.ibm.wala.util.graph.Node)
   */
  public int getPredNodeCount(T N) throws IllegalArgumentException {
    if (N == null) {
      throw new IllegalArgumentException("N cannot be null");
    }
    INodeWithNumberedEdges en = N;
    return en.getPredNumbers().size();
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodes(com.ibm.wala.util.graph.Node)
   */
  public Iterator<T> getSuccNodes(T N) {
    if (N == null) {
      throw new IllegalArgumentException("N cannot be null");
    }
    INodeWithNumberedEdges en = N;
    IntSet succ = en.getSuccNumbers();
    Iterator<T> empty = EmptyIterator.instance();
    return (succ == null) ? empty : (Iterator<T>) new IntSetNodeIterator(succ.intIterator());
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#getSuccNodeCount(com.ibm.wala.util.graph.Node)
   */
  public int getSuccNodeCount(T N) {
    if (N == null) {
      throw new IllegalArgumentException("N is null");
    }
    INodeWithNumberedEdges en = N;
    return en.getSuccNumbers().size();
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#addEdge(com.ibm.wala.util.graph.Node,
   *      com.ibm.wala.util.graph.Node)
   */
  public void addEdge(T src, T dst) {
    if (dst == null) {
      throw new IllegalArgumentException("dst is null");
    }
    src.addSucc(dst.getGraphNodeId());
  }

  public void removeEdge(T src, T dst) {
    Assertions.UNREACHABLE();
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(com.ibm.wala.util.graph.Node)
   */
  public void removeAllIncidentEdges(T node) {
    if (node == null) {
      throw new IllegalArgumentException("node is null");
    }
    INodeWithNumberedEdges n = node;
    n.removeAllIncidentEdges();
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(com.ibm.wala.util.graph.Node)
   */
  public void removeIncomingEdges(T node) {
    if (node == null) {
      throw new IllegalArgumentException("node cannot be null");
    }
    INodeWithNumberedEdges n = node;
    n.removeIncomingEdges();
  }

  /*
   * @see com.ibm.wala.util.graph.EdgeManager#removeEdges(com.ibm.wala.util.graph.Node)
   */
  public void removeOutgoingEdges(T node) {
    if (node == null) {
      throw new IllegalArgumentException("node cannot be null");
    }
    INodeWithNumberedEdges n = node;
    n.removeOutgoingEdges();
  }

  public boolean hasEdge(T src, T dst) {
    return getSuccNodeNumbers(src).contains(dst.getGraphNodeId());
  }

  public IntSet getSuccNodeNumbers(T node) {
    if (node == null) {
      throw new IllegalArgumentException("node cannot be null");
    }
    INodeWithNumberedEdges en = node;
    IntSet succ = en.getSuccNumbers();
    return (succ == null) ? new SparseIntSet() : succ;
  }

}
