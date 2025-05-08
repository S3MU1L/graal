package com.oracle.svm.hosted.analysis.ai.interpreter;

import com.oracle.svm.hosted.analysis.ai.analyzer.call.InvokeCallBack;
import com.oracle.svm.hosted.analysis.ai.domain.AbstractDomain;
import com.oracle.svm.hosted.analysis.ai.fixpoint.state.AbstractState;
import jdk.graal.compiler.graph.Node;

/**
 * This interface provides functionality for executing operations
 * on nodes and edges within the GraalIR of an analyzed method.
 *
 * @param <Domain> type of the derived {@link AbstractDomain} used in the analysis
 */
public interface AbstractInterpreter<Domain extends AbstractDomain<Domain>> {

    /**
     * Simulate the effect of executing an edge between two nodes {@link AbstractState}.
     * For efficiency, this method should not modify the {@link AbstractState} directly.
     *
     * @param source        the node from which the edge originates
     * @param target        the node to which the edge goes
     * @param abstractState of the analyzed method
     */
    void execEdge(Node source, Node target, AbstractState<Domain> abstractState);

    /**
     * Simulate the effect of executing a node in the GraalIR on the {@link AbstractState}.
     * For efficiency, this method should not modify the {@link AbstractState} directly.
     *
     * @param node           to analyze
     * @param abstractState  of the analyzed method
     * @param invokeCallBack callback that can be used to analyze the summary of invokes
     */
    void execNode(Node node, AbstractState<Domain> abstractState, InvokeCallBack<Domain> invokeCallBack);
}
