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
package com.ibm.wala.dataflow.graph;

import java.util.Iterator;
import java.util.Map;

import com.ibm.wala.fixedpoint.impl.DefaultFixedPointSolver;
import com.ibm.wala.fixedpoint.impl.UnaryOperator;
import com.ibm.wala.fixpoint.IVariable;
import com.ibm.wala.util.collections.HashMapFactory;
import com.ibm.wala.util.collections.ObjectArrayMapping;
import com.ibm.wala.util.collections.Pair;
import com.ibm.wala.util.graph.Graph;
import com.ibm.wala.util.intset.IntegerUnionFind;

/**
 * 
 * Iterative solver for a killdall dataflow framework
 * 
 * @author sfink
 */
public abstract class DataflowSolver<T> extends DefaultFixedPointSolver {

  /**
   * the dataflow problem to solve
   */
  private final IKilldallFramework<T> problem;

  /**
   * The "IN" variable for each node.
   */
  private final Map<Object, IVariable> node2In = HashMapFactory.make();

  /**
   * The "OUT" variable for each node, when node transfer requested.
   */
  private final Map<Object, IVariable> node2Out = HashMapFactory.make();

  /**
   * The variable for each edge, when edge transfers requested (indexed by
   * Pair(src, dst))
   */
  private final Map<Object, IVariable> edge2Var = HashMapFactory.make();

  /**
   * @param problem
   */
  public DataflowSolver(IKilldallFramework<T> problem) {
    // tune the implementation for common case of 2 uses for each
    // dataflow def
    super(2);
    this.problem = problem;
  }

  /**
   * @param n
   *          a node
   * @return a fresh variable to represent the lattice value at the IN or OUT of
   *         n
   */
  protected abstract IVariable makeNodeVariable(T n, boolean IN);

  protected abstract IVariable makeEdgeVariable(T src, T dst);

  @Override
  protected void initializeVariables() {
    Graph<T> G = problem.getFlowGraph();
    ITransferFunctionProvider functions = problem.getTransferFunctionProvider();
    // create a variable for each node.
    for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
      T N = it.next();
      IVariable v = makeNodeVariable(N, true);
      node2In.put(N, v);

      if (functions.hasNodeTransferFunctions()) {
        v = makeNodeVariable(N, false);
        node2Out.put(N, v);
      }

      if (functions.hasEdgeTransferFunctions()) {
        for (Iterator<? extends T> it2 = G.getSuccNodes(N); it2.hasNext();) {
          T S = it2.next();
          v = makeEdgeVariable(N, S);
          edge2Var.put(new Pair<T,Object>(N, S), v);
        }
      }
    }
  }

  @Override
  protected void initializeWorkList() {
    buildEquations(true, false);
  }

  public IVariable getOut(Object node) {
    return node2Out.get(node);
  }

  public IVariable getIn(Object node) {
    return node2In.get(node);
  }

  public IVariable getEdge(Object key) {
    return edge2Var.get(key);
  }

  public IVariable getEdge(Object src, Object dst) {
    return getEdge(new Pair<Object,Object>(src, dst));
  }

  private class UnionFind {
    IntegerUnionFind uf;

    ObjectArrayMapping map;

    boolean didSomething = false;

    private Object[] allKeys;

    private int mapIt(int i, Object[] allKeys, Object[] allVars, Map<Object, IVariable> varMap) {
      for (Iterator<Object> it = varMap.keySet().iterator(); it.hasNext();) {
        Object key = it.next();
        allKeys[i] = key;
        allVars[i++] = varMap.get(key);
      }

      return i;
    }

    UnionFind() {
      allKeys = new Object[node2In.size() + node2Out.size() + edge2Var.size()];
      Object allVars[] = new Object[node2In.size() + node2Out.size() + edge2Var.size()];

      int i = mapIt(0, allKeys, allVars, node2In);
      i = mapIt(i, allKeys, allVars, node2Out);
      mapIt(i, allKeys, allVars, edge2Var);

      uf = new IntegerUnionFind(allVars.length);
      map = new ObjectArrayMapping<Object>(allVars);
    }

    /**
     * record that variable (n1, in1) is the same as variable (n2,in2), where
     * (x,true) = IN(X) and (x,false) = OUT(X)
     */
    public void union(Object n1, Object n2) {
      int x = map.getMappedIndex(n1);
      int y = map.getMappedIndex(n2);
      uf.union(x, y);
      didSomething = true;
    }

    public int size() {
      return map.getMappingSize();
    }
    
    public int find(int i) {
      return uf.find(i);
    }

    public boolean isIn(int i) {
      return i < node2In.size();
    }

    public boolean isOut(int i) {
      return !isIn(i) && i < (node2In.size() + node2Out.size());
    }

    public Object getKey(int i) {
      return allKeys[i];
    }
  }

  protected void buildEquations(boolean toWorkList, boolean eager) {
    ITransferFunctionProvider<T> functions = problem.getTransferFunctionProvider();
    Graph<T> G = problem.getFlowGraph();
    AbstractMeetOperator meet = functions.getMeetOperator();
    UnionFind uf = new UnionFind();
    if (meet.isUnaryNoOp()) {
      shortCircuitUnaryMeets(G, functions, uf);
    }
    shortCircuitIdentities(G, functions, uf);
    fixShortCircuits(uf);

    // add meet operations
    int meetThreshold = (meet.isUnaryNoOp() ? 2 : 1);
    for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
      T node = it.next();
      int nPred = G.getPredNodeCount(node);
      if (nPred >= meetThreshold) {
        // todo: optimize further using unary operators when possible?
        IVariable[] rhs = new IVariable[nPred];
        int i = 0;
        for (Iterator it2 = G.getPredNodes(node); it2.hasNext();) {
          rhs[i++] = (functions.hasEdgeTransferFunctions()) ? getEdge(it2.next(), node) : getOut(it2.next());
        }
        newStatement(getIn(node), meet, rhs, toWorkList, eager);
      }
    }

    // add node transfer operations, if requested
    if (functions.hasNodeTransferFunctions()) {
      for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
        T node = it.next();
        UnaryOperator f = functions.getNodeTransferFunction(node);
        if (!f.isIdentity()) {
          newStatement(getOut(node), f, getIn(node), toWorkList, eager);
        }
      }
    }

    // add edge transfer operations, if requested
    if (functions.hasEdgeTransferFunctions()) {
      for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
        T node = it.next();
        for (Iterator<? extends T> it2 = G.getSuccNodes(node); it2.hasNext();) {
          T succ = it2.next();
          UnaryOperator f = functions.getEdgeTransferFunction(node, succ);
          if (!f.isIdentity()) {
            newStatement(getEdge(node, succ), f, (functions.hasNodeTransferFunctions()) ? getOut(node) : getIn(node), toWorkList,
                eager);
          }
        }
      }
    }
  }

  /**
   * Swap variables to account for identity transfer functions.
   */
  private void shortCircuitIdentities(Graph<T> G, ITransferFunctionProvider<T> functions, UnionFind uf) {
    if (functions.hasNodeTransferFunctions()) {
      for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
        T node = it.next();
        UnaryOperator f = functions.getNodeTransferFunction(node);
        if (f.isIdentity()) {
          uf.union(getIn(node), getOut(node));
        }
      }
    }

    if (functions.hasEdgeTransferFunctions()) {
      for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
        T node = it.next();
        for (Iterator<? extends T> it2 = G.getSuccNodes(node); it2.hasNext();) {
          T succ = it2.next();
          UnaryOperator f = functions.getEdgeTransferFunction(node, succ);
          if (f.isIdentity()) {
            uf.union(getEdge(node, succ), (functions.hasNodeTransferFunctions()) ? getOut(node) : getIn(node));
          }
        }
      }
    }
  }

  /**
   * change the variables to account for short circuit optimizations
   */
  private void fixShortCircuits(UnionFind uf) {
    if (uf.didSomething) {
      for (int i = 0; i < uf.size(); i++) {
        int rep = uf.find(i);
        if (i != rep) {
          Object x = uf.getKey(i);
          Object y = uf.getKey(rep);
          if (uf.isIn(i)) {
            if (uf.isIn(rep)) {
              node2In.put(x, getIn(y));
            } else if (uf.isOut(rep)) {
              node2In.put(x, getOut(y));
            } else {
              node2In.put(x, getEdge(y));
            }
          } else if (uf.isOut(i)) {
            if (uf.isIn(rep)) {
              node2Out.put(x, getIn(y));
            } else if (uf.isOut(rep)) {
              node2Out.put(x, getOut(y));
            } else {
              node2Out.put(x, getEdge(y));
            }
          } else {
            if (uf.isIn(rep)) {
              edge2Var.put(x, getIn(y));
            } else if (uf.isOut(rep)) {
              edge2Var.put(x, getOut(y));
            } else {
              edge2Var.put(x, getEdge(y));
            }
          }
        }
      }
    }
  }

  /**
   * @param G
   */
  private void shortCircuitUnaryMeets(Graph<T> G, ITransferFunctionProvider functions, UnionFind uf) {
    for (Iterator<? extends T> it = G.iterator(); it.hasNext();) {
      T node = it.next();
      int nPred = G.getPredNodeCount(node);
      if (nPred == 1) {
        // short circuit by setting IN = OUT_p
        Object p = G.getPredNodes(node).next();
        uf.union(getIn(node), functions.hasEdgeTransferFunctions() ? getEdge(p, node) : getOut(p));
      }
    }
  }

  /**
   * @return Returns the problem.
   */
  public IKilldallFramework getProblem() {
    return problem;
  }
}
