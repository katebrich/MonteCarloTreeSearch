package MCTS;

import java.util.*;

public class Node<S,A> {
	public S state;
	public Node<S, A> parent;
	public A action;
	public List<Node<S, A>> children;
	public int simulations;
	public int wins;
	public boolean fullyExpanded;
	private List<A> remainingActions;
	
	public Node(S state, A action, Node<S,A> parent, List<A> actions) {
		this.state = state;
		this.action = action;
		this.parent = parent;
		this.children = new ArrayList<Node<S, A>>();
		this.simulations = 0;
		this.wins = 0;
		this.remainingActions = actions;
		this.fullyExpanded = (remainingActions.size() == 0);
	}
	
	public A getUntriedAction() {
		A act = remainingActions.get(0);
		remainingActions.remove(0);
		if (remainingActions.size() == 0)
			fullyExpanded = true;
		return act;
	}
}
