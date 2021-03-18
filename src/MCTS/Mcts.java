package MCTS;

import java.io.Console;
import java.util.List;
import java.util.Random;


//Monte Carlo tree search
public class Mcts<S, A> implements Strategy<S, A> {

	Game<S, A> game;
	Generator<S, A> generator;
	int timeLimit;
	int determinizations;
	int timeLimitPerDeterminization;
	double c = 1 / Math.sqrt(2);
	long startMillis;
	
	public Mcts(Game<S, A> game, Generator<S, A> generator,
           int determinizations, int timeLimit) {
		this.game = game;
		this.generator = generator;
		this.determinizations = determinizations;
		this.timeLimit = timeLimit;
		this.timeLimitPerDeterminization = timeLimit / determinizations;
		
	}

	public A action(S state) {
		Random rand = new Random();
		List<A> actions = generator.actions(state);
		int[] counts = new int[actions.size()];		
		
		for (int i = 0; i < determinizations; i++) {
			//System.out.println("Starting determinization " + i);
			//start measuring time
			startMillis = System.currentTimeMillis();
			rand.setSeed(i*100);
			//run MCTS for this determinization
			A action = getBestAction(state, rand);
			counts[actions.indexOf(action)]++;
		}
		
		int max = 0;
		int maxIndex = 0;
		//find maximal count
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > max) {
				max = counts[i];
				maxIndex = i;
			}
		}
		//return action with the biggest count
		return actions.get(maxIndex);
	}
	
	private A getBestAction(S state, Random rand) {
		Node<S, A> root = new Node<S,A>(game.clone(state), null, null, generator.actions(state));
		
		while (System.currentTimeMillis() - startMillis < timeLimitPerDeterminization) {
			//long difference = System.currentTimeMillis() - startMillis;
			Node<S,A> v1 = treePolicy(root);
			//System.out.println("Tree policy");
			double result = defaultPolicy(v1.state, rand);
			//System.out.println("Default policy");
			backup(v1, result);
			//System.out.println("Backup");
		}
		return bestChild(root, 0).action;
	}
	
	private Node<S, A> treePolicy(Node<S, A> node) {
		Node<S, A> v = node;
		while (!game.isDone(v.state)) {
			if (!v.fullyExpanded) {
				return expand(v);
			}
			else {
				v = bestChild(v, c);
			}
		}
		return v;
	}
	
	private double defaultPolicy(S state, Random rand) {
		S s = game.clone(state);
		while (!game.isDone(s)) {
			List<A> actions = generator.actions(s);
			int randomIndex = rand.nextInt(actions.size());
			A randomAction = actions.get(randomIndex);
			game.apply(s, randomAction); //TODO tohle zkontrolovat, co to vlastne meni...
		}
		return game.outcome(s);
	}
	
	private void backup(Node<S,A> node, double result) {
		Node<S,A> v = node;
		result = 2 * result - 1; //win = 1, loss = -1, draw = 0
		int player = game.player(v.state);
		if (player == 2)
			result = - result;
		while (v != null) {
			v.simulations++;
			/*int player = game.player(v.state);
			if (player == 2) { //minimizing player
				v.wins += result;
			}
			else if (player == 1) { //maximizing player
				v.wins += result * (-1);
			}*/		
			
			v.wins += result;			
			result = (-1)* result;
			
			v = v.parent;
		}
	}
	
	private Node<S, A> expand(Node<S,A> node) {
		A action = node.getUntriedAction();
		S newstate = game.clone(node.state);
		game.apply(newstate, action);
		Node<S, A> newNode = new Node<S,A>(newstate, action, node, generator.actions(newstate));
		node.children.add(newNode);
		return newNode;
	}
	
	private Node<S, A> bestChild(Node<S, A> node, double c) {
		double max = (-1) * Double.MAX_VALUE;
		int maxIndex = 0;
		int i = 0;
		
		for (Node<S,A> child : node.children) {
			double result = -1.0*child.wins / child.simulations 
					+ c * Math.sqrt(
							(2*Math.log(node.simulations)) / child.simulations
							);
			if (result > max) {
				max = result;
				maxIndex = i;
			}
			i++;
		}
		
		return node.children.get(maxIndex);
	}
	
	private double negateResult(double result) {
		if (result == 0.5) return 0.5;
		else if (result == 0) return 1;
		else return 0;
	}
	
	
	
	
	
	
}
