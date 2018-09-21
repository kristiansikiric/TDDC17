package tddc17;


import aima.core.environment.liuvacuum.*;
import aima.core.agent.Action;
import aima.core.agent.AgentProgram;
import aima.core.agent.Percept;
import aima.core.agent.impl.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

class MyAgentState
{
	public int[][] world = new int[30][30];
	public int initialized = 0;
	final int UNKNOWN 	= 0;
	final int WALL 		= 1;
	final int CLEAR 	= 2;
	final int DIRT		= 3;
	final int HOME		= 4;
	final int DEADEND	= 5; //Own variable
	final int ACTION_NONE 			= 0;
	final int ACTION_MOVE_FORWARD 	= 1;
	final int ACTION_TURN_RIGHT 	= 2;
	final int ACTION_TURN_LEFT 		= 3;
	final int ACTION_SUCK	 		= 4;
	
	public int agent_x_position = 1;
	public int agent_y_position = 1;
	public int agent_last_action = ACTION_NONE;
	public int two_actions_ago = ACTION_NONE;
	
	public static final int NORTH = 0;
	public static final int EAST = 1;
	public static final int SOUTH = 2;
	public static final int WEST = 3;
	public int agent_direction = EAST;
	
	MyAgentState()
	{
		for (int i=0; i < world.length; i++)
			for (int j=0; j < world[i].length ; j++)
				world[i][j] = UNKNOWN;
		world[1][1] = HOME;
		agent_last_action = ACTION_NONE;
	}
	// Based on the last action and the received percept updates the x & y agent position
	public void updatePosition(DynamicPercept p)
	{
		Boolean bump = (Boolean)p.getAttribute("bump");

		if (agent_last_action==ACTION_MOVE_FORWARD && !bump)
	    {
			switch (agent_direction) {
			case MyAgentState.NORTH:
				agent_y_position--;
				break;
			case MyAgentState.EAST:
				agent_x_position++;
				break;
			case MyAgentState.SOUTH:
				agent_y_position++;
				break;
			case MyAgentState.WEST:
				agent_x_position--;
				break;
			}
	    }
		
	}
	
	public void updateWorld(int x_position, int y_position, int info)
	{
		world[x_position][y_position] = info;
	}
	
	public void printWorldDebug()
	{
		for (int i=0; i < world.length; i++)
		{
			for (int j=0; j < world[i].length ; j++)
			{
				if (world[j][i]==UNKNOWN)
					System.out.print(" ? ");
				if (world[j][i]==WALL)
					System.out.print(" # ");
				if (world[j][i]==CLEAR)
					System.out.print(" . ");
				if (world[j][i]==DIRT)
					System.out.print(" D ");
				if (world[j][i]==HOME)
					System.out.print(" H ");
					
			}
			System.out.println("");
		}
	}
}

class Node{
	private int x;
	private int y;
	private int dir;
	private Action action;
	private Node parent;
	
	
	/*
	 * Node class containing the nodes coordinates, which action is needed to reach it and its parent.
	 * Also contains the direction of the cleaner.
	 */
	public Node(int x, int y, int dir, Action action, Node parent){
		this.x = x;
		this.y = y;
		this.action = action;
		this.dir = dir;
		this.parent = parent;
	}
	public int getX(){
		return x;
	}
	public int getY(){
		return y;
	}
	public int getDir(){
		return dir;
	}
	public Action getAction(){
		return action;
	}
	public Node getParent(){
		return parent;
	}
}

class MyAgentProgram implements AgentProgram {

	private int initnialRandomActions = 10;
	private Random random_generator = new Random();
	
	// Here you can define your variables!
	public int iterationCounter = 1000;
	public MyAgentState state = new MyAgentState();

	boolean search = true; //When true, we search for target
	int target = state.UNKNOWN; //Specifies what to search for, first set to unknown then to home when all unknowns are found.
	LinkedList<Node> nodesToVisit = new LinkedList<Node>(); //A linked list containing all nodes we want to visit 
	List<Node> visitedNodes = new ArrayList<Node>(); //A linked list containing all nodes we have visited
	Stack<Action> action_list = new Stack<Action>(); //A stack of all the actions to take to reach a specific target.
	
	// moves the Agent to a random start position
	// uses percepts to update the Agent position - only the position, other percepts are ignored
	// returns a random action
	private Action moveToRandomStartPosition(DynamicPercept percept) {
		int action = random_generator.nextInt(6);
		initnialRandomActions--;
		state.updatePosition(percept);
		if(action==0) {
		    state.agent_direction = ((state.agent_direction-1) % 4);
		    if (state.agent_direction<0) 
		    	state.agent_direction +=4;
		    state.agent_last_action = state.ACTION_TURN_LEFT;
			return LIUVacuumEnvironment.ACTION_TURN_LEFT;
		} else if (action==1) {
			state.agent_direction = ((state.agent_direction+1) % 4);
		    state.agent_last_action = state.ACTION_TURN_RIGHT;
		    return LIUVacuumEnvironment.ACTION_TURN_RIGHT;
		} 
		state.agent_last_action=state.ACTION_MOVE_FORWARD;
		return LIUVacuumEnvironment.ACTION_MOVE_FORWARD;
	}
	
	
	@Override
	public Action execute(Percept percept) {
		
		// DO NOT REMOVE this if condition!!!
    	if (initnialRandomActions>0) {
    		return moveToRandomStartPosition((DynamicPercept) percept);
    	} else if (initnialRandomActions==0) {
    		// process percept for the last step of the initial random actions
    		initnialRandomActions--;
    		state.updatePosition((DynamicPercept) percept);
			System.out.println("Processing percepts after the last execution of moveToRandomStartPosition()");
			state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
    	}
		
    	// This example agent program will update the internal agent state while only moving forward.
    	// START HERE - code below should be modified!
    	System.out.println("---------------------------------------------");
    	System.out.println("x=" + state.agent_x_position);
    	System.out.println("y=" + state.agent_y_position);
    	System.out.println("dir=" + state.agent_direction);

	    iterationCounter--;
	    
	    if (iterationCounter==0)
	    	return NoOpAction.NO_OP;

	    DynamicPercept p = (DynamicPercept) percept;
	    Boolean bump = (Boolean)p.getAttribute("bump");
	    Boolean dirt = (Boolean)p.getAttribute("dirt");
	    Boolean home = (Boolean)p.getAttribute("home");
	    System.out.println("percept: " + p);
	    
	    // State update based on the percept value and the last action
	    state.updatePosition((DynamicPercept)percept);
	    if (bump) {
			switch (state.agent_direction) {
			case MyAgentState.NORTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position-1,state.WALL);
				break;
			case MyAgentState.EAST:
				state.updateWorld(state.agent_x_position+1,state.agent_y_position,state.WALL);
				break;
			case MyAgentState.SOUTH:
				state.updateWorld(state.agent_x_position,state.agent_y_position+1,state.WALL);
				break;
			case MyAgentState.WEST:
				state.updateWorld(state.agent_x_position-1,state.agent_y_position,state.WALL);
				break;
			}
	    }
	    if (dirt)
	    	state.updateWorld(state.agent_x_position,state.agent_y_position,state.DIRT);
	    else
	    {
	    	if(!home)
	    		state.updateWorld(state.agent_x_position,state.agent_y_position,state.CLEAR); //Don't write over home with clear, it makes it easier to find home.
	    }
	    
	    state.printWorldDebug();
	    
	    
	    /*
	     * If the cleaner is home, and the target is set to home, we shut down.
	     */
	    if(home && target == state.HOME){
	    	return NoOpAction.NO_OP;
	    }
	    /*
	     * If the cleaner goes over dirt, it sucks it up.
	     */
	    if (dirt)
	    {
	    	System.out.println("DIRT -> choosing SUCK action!");
	    	state.agent_last_action=state.ACTION_SUCK;
	    	return LIUVacuumEnvironment.ACTION_SUCK;
	    } 
	    else
	    {
	    	if(search){
	    		findUnknown();
	    		search = false;
	    	}
	    	/*
	    	 * Goes through the stack of actions to take and updating direction and last action.
	    	 */
	    	if(action_list.size() > 0){
	    		Action k = action_list.pop();
	    		if (k == LIUVacuumEnvironment.ACTION_MOVE_FORWARD){
	    			state.agent_last_action = state.ACTION_MOVE_FORWARD;
	    		}
	    		else if (k == LIUVacuumEnvironment.ACTION_TURN_LEFT){
	    			state.agent_last_action = state.ACTION_TURN_LEFT;
	    			state.agent_direction = ((state.agent_direction-1) % 4);
	    			if (state.agent_direction<0) 
	    		    	state.agent_direction +=4;
	    		}
	    		else if (k == LIUVacuumEnvironment.ACTION_TURN_RIGHT){
	    			state.agent_last_action = state.ACTION_TURN_RIGHT;
	    			state.agent_direction = ((state.agent_direction+1) % 4);
	    			
	    		}
	    		return k;
	    	}
	    	else{
	    		search = true;
	    		state.agent_last_action=state.ACTION_SUCK;
		    	return LIUVacuumEnvironment.ACTION_SUCK;
	    	}
	    	
	    }
	}
	/*
	 * The functions that searches and finds all unknowns, and home when no more unknowns can be found. Basically implements a breadth-first search.
	 * When target is found, the function create_action_list is called to create the stack of actions needed to reach the target.
	 * Note that we are not interested in nodes that are walls, these are not added in the nodesToVisit list and therefore we are not needed to check for bumps when moving.
	 * We are also only interested in nodes forward, to the left and to the right since those are the only directions the cleaner can go.
	 * We also never add nodes to the nodesToVisit that are already in the list or have already been visited, i.e are in the visitedNodes list.
	 */
	private void findUnknown(){
		nodesToVisit.clear();
		visitedNodes.clear();
		Node root = new Node(state.agent_x_position, state.agent_y_position, state.agent_direction, LIUVacuumEnvironment.ACTION_SUCK, null);
		nodesToVisit.add(root);
		boolean dontAddL = false;
		boolean dontAddR = false;
		boolean dontAddF = false;
		Node leftNode = null;
		Node rightNode = null;
		Node frontNode = null;
		while(nodesToVisit.size() > 0){
			dontAddL = false;
			dontAddR = false;
			dontAddF = false;
			root = nodesToVisit.pop();
			//System.out.println(root.getX()+ "," + root.getY()); //Good for debugging.
			if(state.world[root.getX()][root.getY()] == target){
				create_action_list(root);
				return;
			}
			
			if (root.getDir() == MyAgentState.NORTH){
    			leftNode = new Node(root.getX(), root.getY(), MyAgentState.WEST, LIUVacuumEnvironment.ACTION_TURN_LEFT, root);
    			rightNode = new Node(root.getX(), root.getY(), MyAgentState.EAST, LIUVacuumEnvironment.ACTION_TURN_RIGHT, root);
				frontNode = new Node(root.getX(), root.getY()-1, MyAgentState.NORTH, LIUVacuumEnvironment.ACTION_MOVE_FORWARD, root);
    		}
			else if (root.getDir() == MyAgentState.SOUTH){
				leftNode = new Node(root.getX(), root.getY(), MyAgentState.EAST, LIUVacuumEnvironment.ACTION_TURN_LEFT, root);
    			rightNode = new Node(root.getX(), root.getY(), MyAgentState.WEST, LIUVacuumEnvironment.ACTION_TURN_RIGHT, root);
				frontNode = new Node(root.getX(), root.getY()+1, MyAgentState.SOUTH, LIUVacuumEnvironment.ACTION_MOVE_FORWARD, root);
    		}
			else if (root.getDir() == MyAgentState.EAST){
				leftNode = new Node(root.getX(), root.getY(), MyAgentState.NORTH, LIUVacuumEnvironment.ACTION_TURN_LEFT, root);
    			rightNode = new Node(root.getX(), root.getY(), MyAgentState.SOUTH, LIUVacuumEnvironment.ACTION_TURN_RIGHT, root);
				frontNode = new Node(root.getX()+1, root.getY(), MyAgentState.EAST, LIUVacuumEnvironment.ACTION_MOVE_FORWARD, root);
			}
			else if (root.getDir() == MyAgentState.WEST){
				leftNode = new Node(root.getX(), root.getY(), MyAgentState.SOUTH, LIUVacuumEnvironment.ACTION_TURN_LEFT, root);
    			rightNode = new Node(root.getX(), root.getY(), MyAgentState.NORTH, LIUVacuumEnvironment.ACTION_TURN_RIGHT, root);
				frontNode = new Node(root.getX()-1, root.getY(), MyAgentState.WEST, LIUVacuumEnvironment.ACTION_MOVE_FORWARD, root);
			}
			for (Node elems: visitedNodes){
				if(frontNode != null && frontNode.getX() == elems.getX() && frontNode.getY() == elems.getY()){
					dontAddF = true;
				}
				if(leftNode != null && leftNode.getX() == elems.getX() && leftNode.getY() == elems.getY() && leftNode.getDir() == elems.getDir()){
					dontAddL = true;
				}
				if(rightNode != null && rightNode.getX() == elems.getX() && rightNode.getY() == elems.getY() && rightNode.getDir() == elems.getDir()){
					dontAddR = true;
				}
			}
			for (Node elems: nodesToVisit){
				if(frontNode != null && frontNode.getX() == elems.getX() && frontNode.getY() == elems.getY()){
					dontAddF = true;
				}
				if(leftNode != null && leftNode.getX() == elems.getX() && leftNode.getY() == elems.getY() && leftNode.getDir() == elems.getDir()){
					dontAddL = true;
				}
				if(rightNode != null && rightNode.getX() == elems.getX() && rightNode.getY() == elems.getY() && rightNode.getDir() == elems.getDir()){
					dontAddR = true;
				}
			}
			if (!dontAddF && state.world[frontNode.getX()][frontNode.getY()] != state.WALL){
    			nodesToVisit.add(frontNode);
			}
			if (!dontAddR){
    			nodesToVisit.add(rightNode);
			}
			if (!dontAddL){
    			nodesToVisit.add(leftNode);
			}
			visitedNodes.add(root);
		}
		target = state.HOME; //When this part is reached we have failed to found any unknowns and it is time to go home and shutdown.
	}
	/*
	 * This function creates the stack containing all actions to take to reach the node. It uses the action of the parents and adds it to the stack.
	 * Since a stack is a LIFO-queue the action of the first parent will be retrieved first, then the second and so on until we reach the target node.
	 */
	void create_action_list(Node n){
		while(n.getParent() != null){
			action_list.add(n.getAction());
			n = n.getParent();
		}
	}
}

public class MyVacuumAgent extends AbstractAgent {
    public MyVacuumAgent() {
    	super(new MyAgentProgram());
    }
}