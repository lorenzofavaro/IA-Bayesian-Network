package utils;

import aima.core.probability.RandomVariable;
import aima.core.probability.bayes.BayesianNetwork;
import aima.core.probability.bayes.Node;
import bayes.Inferences;
import org.graphstream.graph.implementations.AbstractGraph;
import org.graphstream.graph.implementations.AbstractNode;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.graph.implementations.SingleNode;

import java.util.*;

public class MoralGraph extends SingleGraph {

    private final BayesianNetwork bayesianNetwork;
    private final List<RandomVariable> variables;
    private Inferences.Heuristics heuristicType;
    private PriorityQueue<MoralNode> variablesQueue =
            new PriorityQueue<>(Comparator.comparingInt(o -> o.calculateHeuristics(heuristicType)));


    public MoralGraph(BayesianNetwork bayesianNetwork, List<RandomVariable> variables, Inferences.Heuristics heuristicsType) {
        super("MG", true, false);
        this.bayesianNetwork = bayesianNetwork;
        this.variables = variables;
        this.heuristicType = heuristicsType;

        addAttribute("ui.stylesheet", "node { size: 10px, 15px; fill-color: blue; stroke-mode: plain; text-alignment: above; text-size: 25px;}");

        setupGraph();
        setupQueue();
    }

    private void setupGraph(){
        for (RandomVariable var : variables) {

            // Aggiunta nodi
            MoralNode moralNode = addNode(var.getName());
            moralNode.setRandomVariable(var);
            moralNode.addAttribute("ui.label", moralNode.getId());

            // Aggiunta archi "parent-parent"
            HashSet<Node> parents = new HashSet<>(bayesianNetwork.getNode(var).getParents());
            bindParents(parents);

            // Aggiunta archi "padre-figlio"
            for (Node n : parents) {
                if (!getNode(var).hasEdgeBetween(getNode(n.getRandomVariable()))) {
                    addEdge(var.getName() + "--" + n.getRandomVariable().getName(), getNode(var), getNode(n.getRandomVariable()), false);
                }
            }
        }
    }

    private void setupQueue(){
        variablesQueue.addAll(getNodeSet());
    }

    private void bindParents(HashSet<Node> parents) {
        for (int i = 0; i < parents.size(); i++) {
            for (int j = 0; j < parents.size(); j++) {

                RandomVariable var1 = variables.get(i);
                RandomVariable var2 = variables.get(j);

                MoralNode node1 = getNode(var1);
                MoralNode node2 = getNode(var2);

                if (i != j && !node1.hasEdgeBetween(node2)) {
                    addEdge(var1.getName() + "--" + var2.getName(), node1, node2, false);
                }
            }
        }
    }

    @Override
    protected <T extends org.graphstream.graph.Node> T addNode_(String sourceId, long timeId, String nodeId) {
        AbstractNode node = getNode(nodeId);
        if (node != null) {
            return (T) node;
        }
        node = new MoralNode(this, nodeId);
        addNodeCallback(node);
        return (T) node;
    }

    private MoralNode getNode(RandomVariable randomVariable) {
        return getNode(randomVariable.getName());
    }

    public List<RandomVariable> getVariables(Boolean showMoralGraph, int delay) {
        ArrayList<RandomVariable> variables = new ArrayList<>();
        if (showMoralGraph) {
            display();
        }

    }

    public static class MoralNode extends SingleNode {
        private int heuristicsValue = -1;
        private RandomVariable randomVariable;

        protected MoralNode(AbstractGraph graph, String id) {
            super(graph, id);
        }

        public int calculateHeuristics(Inferences.Heuristics heuristicsType) {
            if (heuristicsValue == -1) {
                heuristicsValue = Inferences.calculateHeuristics(heuristicsType);
            }
            return heuristicsValue;
        }

        public final RandomVariable getRandomVariable() {
            return this.randomVariable;
        }

        public final void setRandomVariable(RandomVariable var) {
            this.randomVariable = var;
        }

        Boolean hasEdgeBetween(MoralNode node) {
            return hasEdgeBetween(node.getId());
        }
    }
}
