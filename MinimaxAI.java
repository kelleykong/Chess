package chai;

import java.util.HashMap;
import java.util.Random;

import chesspresso.move.IllegalMoveException;
import chesspresso.position.Position;

public class MinimaxAI implements ChessAI {
	
	private int depth_limit = 3;
//	private int depth;
	private int myTurn;
	private int numofPos;
	private short[] bestMoves = new short[1000000];
	
	private HashMap<Position, Integer> transpositionTable = new HashMap<Position, Integer>();

	@Override
	public short getMove(Position position) {
		Position pos = new Position(position);
		myTurn = pos.getToPlay();
		numofPos = 0;

//		long t1=System.currentTimeMillis();
//		short minimax = Minimax(pos);
//		int n1 = numofPos;
//		numofPos = 0;
		long t2=System.currentTimeMillis();
		short abp = iterative(pos);
		int n1 = numofPos;
		numofPos = 0;
		long t3=System.currentTimeMillis();
		short abp_trans = iterative_reorder(pos);
		long t4 = System.currentTimeMillis();
//		System.out.println("Minimax " + minimax + " visited states: " + n1 + " time " + (t2-t1) + " ABP " + abp + " visited states: " + numofPos + " time " + (t3-t2));
//		System.out.println("Minimax depth:3 visited states: " + n1 + " time: " + (t2-t1));
//		System.out.println("Minimax depth:5 visited states: " + numofPos + " time: " + (t3-t2));
		System.out.println("iterative " + abp + " visited states: " + n1 + " time: " + (t3-t2) + " iterative reorder " + abp_trans + " visited states: " + numofPos + " time: " + (t4-t3));
//		return minimax;
		return abp;
	}
	
	private short iterative(Position position) {
		short[] bestmove = new short[depth_limit];
		int d  = depth_limit;
		for (depth_limit = 1; depth_limit < d; depth_limit++) {
			bestmove[depth_limit] = AlphaBetaPruning_Trans(position);
		}
		//if the time is out...
		depth_limit = d;
		return bestmove[depth_limit-1];
	}
	
	private short iterative_reorder(Position position) {
		int d  = depth_limit;
		for (depth_limit = 1; depth_limit < d; depth_limit++) {
			bestMoves[1] = iterative_recursive(position);
		}

		depth_limit = d;
		return bestMoves[1];
	}
	
	private short iterative_recursive(Position position) {
		updateExplored();			// record visited states
		
		int v = Integer.MIN_VALUE;	// keep max value
		short action = 0;			//keep the action leading to max value
		
		int a = Integer.MIN_VALUE;
		int b = Integer.MAX_VALUE;
		if (bestMoves[1] != 0) {
			short move = bestMoves[1];
			doMove(position, move);
			int tmp = MIN_Value_ABP_Trans(position, a, b, 1);
			if (tmp > v) {
				v = tmp;
				action = move;
			}
			undoMove(position);
		}
			
		short [] moves = position.getAllMoves();
		for (short move: moves) {
		    if (move == bestMoves[1])
		        continue;
			doMove(position, move);
			int tmp = MIN_Value_ABP_Trans_reorder(position, a, b, 1);
			if (tmp > v) {
				v = tmp;
				action = move;
			}
			// randomly choose one from equal value, to alleviate repeating
			else if (tmp == v) {
				int r = new Random().nextInt(100);
				if (r < 50) {
					v = tmp;
					action = move;
				}
			}
				
			undoMove(position);
		}
		if (!transpositionTable.containsKey(position))
			transpositionTable.put(position, v);
		System.out.println("Alpha-Beta Pruning Max value: " + v);
		return action;		
	}
	
	private int MAX_Value_ABP_Trans_reorder(Position position, int a, int b, int depth) {   
		if (transpositionTable.containsKey(position))
			return transpositionTable.get(position);
		    
		updateExplored();     // record visited states
		  
		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		 
		int v = Integer.MIN_VALUE;   
		if (bestMoves[depth+1] != 0) {
			short move = bestMoves[depth+1];
			doMove(position, move);
			v = Math.max(v, MIN_Value_ABP_Trans_reorder(position, a, b, depth+1));
			undoMove(position);
			if (v >= b)
				return v;
			a = Math.max(a, v);     
		}
		for (short move: position.getAllMoves()) {
		    if (move == bestMoves[depth+1])
		        continue;
		    doMove(position, move);
		    v = Math.max(v, MIN_Value_ABP_Trans_reorder(position, a, b, depth+1));
		    undoMove(position);
		    if (v >= b)
		        return v;
		    a = Math.max(a, v);
		}
		    
		transpositionTable.put(position, v);
		return v;
	}

	private int MIN_Value_ABP_Trans_reorder(Position position, int a, int b, int depth) {
		if (transpositionTable.containsKey(position))
		      return transpositionTable.get(position);
		    
		updateExplored();     // record visited states
		        
		if (Cutoff_Test(position, depth))
		      return Evaluation(position);
		    
		int v = Integer.MAX_VALUE;
		if (bestMoves[depth+1] != 0) {
      
			short move = bestMoves[depth+1];
			doMove(position, move);
		    v = Math.min(v, MAX_Value_ABP_Trans_reorder(position, a, b, depth+1));
		    undoMove(position);
		    if (v <= a)
		        return v;
		    b = Math.min(b, v);
		}      
		for (short move: position.getAllMoves()) {
			if (move == bestMoves[depth+1])
				continue;
			doMove(position, move);
		    v = Math.min(v, MAX_Value_ABP_Trans_reorder(position, a, b, depth+1));
		    undoMove(position);
		    if (v <= a)
		        return v;
		    b = Math.min(b, v);
		}
		    
		transpositionTable.put(position, v);
		return v;
	}	
	
	private short Minimax(Position position) {
		int v = Integer.MIN_VALUE;	// keep max value
		short action = 0;			// keep the action leading to max value
		updateExplored();			// record visited states
		
		short [] moves = position.getAllMoves();
		for (short move: moves) {
			doMove(position, move);
			int tmp = MIN_Value(position, 1);
			if (tmp > v) {			// find the max value
				v = tmp;
				action = move;
			}
			undoMove(position);
		}
		System.out.println("Simple Minimax Max value: " + v);
		return action;		
	}
	
	private int MAX_Value(Position position, int depth) {
		updateExplored();			// record visited states

		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MIN_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.max(v, MIN_Value(position, depth+1));
			undoMove(position);
		}
		
		return v;
	}

	private int MIN_Value(Position position, int depth) {
		updateExplored();			// record visited states

		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MAX_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.min(v, MAX_Value(position, depth+1));
			undoMove(position);
		}
		
		return v;
	}
	
	private boolean Cutoff_Test(Position position, int depth) {
		if (position.isTerminal() || depth > depth_limit)
			return true;
		else
			return false;
	}
	
	private int Utility(Position position) {
		// if I win, return MAX_VALUE
		//    I lose, return MIN_VALUE
		//    draw, return 0
		//  else  return random value
		if (position.isTerminal()) {	// terminal
			if (position.isMate()) {	// checkmate
				// this turn play is checkmated
				if (position.getToPlay() == myTurn) {
					System.out.println("Reach lose leaf! value: " + Integer.MIN_VALUE);
					return Integer.MIN_VALUE;
				}
				else {
					System.out.println("Reach win leaf! value: " + Integer.MAX_VALUE);
					return Integer.MAX_VALUE;
				}
			}
			else {
				System.out.println("Reach draw leaf! value: " + 0);
				return 0;
			}
		}
		
		return new Random().nextInt();
	}
	
	private int Evaluation(Position position) {
		
		// if I win, return MAX_VALUE
		//    I lose, return MIN_VALUE
		//    draw, return 0
		//  else  return random value
		int v;
		if (position.isTerminal()) {	// terminal
			if (position.isMate()) {	// checkmate
				// this turn play is checkmated
				if (position.getToPlay() == myTurn)
					v =  Integer.MIN_VALUE;
				else
					v = Integer.MAX_VALUE;
			}
			else
				v = 0;
		}
		else
			v = position.getMaterial();	
		System.out.println("value "+ v);
		transpositionTable.put(position, v);
		return v;
	}
	
	
	private short AlphaBetaPruning(Position position) {
		updateExplored();			// record visited states
		
		int v = Integer.MIN_VALUE;	// keep max value
		short action = 0;			//keep the action leading to max value
		
		short [] moves = position.getAllMoves();
		for (short move: moves) {
			doMove(position, move);
			int tmp = MIN_Value_ABP(position, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
			if (tmp > v) {
				v = tmp;
				action = move;
			}
			// randomly choose one from equal value, to alleviate repeating
			else if (tmp == v) {
				int r = new Random().nextInt(100);
				if (r < 50) {
					v = tmp;
					action = move;
				}
			}
				
			undoMove(position);
		}

//		System.out.println("Alpha-Beta Pruning Max value: " + v);
		return action;		
	}
	
	private int MAX_Value_ABP(Position position, int a, int b, int depth) {
		updateExplored();			// record visited states
			
		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MIN_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.max(v, MIN_Value_ABP(position, a, b, depth+1));
			undoMove(position);
			if (v >= b)
				return v;
			a = Math.max(a, v);
		}
		
		return v;
	}

	private int MIN_Value_ABP(Position position, int a, int b, int depth) {
		updateExplored();			// record visited states
				
		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MAX_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.min(v, MAX_Value_ABP(position, a, b, depth+1));
			undoMove(position);
			if (v <= a)
				return v;
			b = Math.min(b, v);
		}
		
		return v;
	}
	
	private short AlphaBetaPruning_Trans(Position position) {
		updateExplored();			// record visited states
		
		int v = Integer.MIN_VALUE;	// keep max value
		short action = 0;			//keep the action leading to max value
		
		short [] moves = position.getAllMoves();
		for (short move: moves) {
			doMove(position, move);
			int tmp = MIN_Value_ABP_Trans(position, Integer.MIN_VALUE, Integer.MAX_VALUE, 1);
			if (tmp > v) {
				v = tmp;
				action = move;
			}
			// randomly choose one from equal value, to alleviate repeating
			else if (tmp == v) {
				int r = new Random().nextInt(100);
				if (r < 50) {
					v = tmp;
					action = move;
				}
			}
				
			undoMove(position);
		}
		if (!transpositionTable.containsKey(position))
			transpositionTable.put(position, v);
		System.out.println("Alpha-Beta Pruning Max value: " + v);
		return action;		
	}
	
	private int MAX_Value_ABP_Trans(Position position, int a, int b, int depth) {		
		if (transpositionTable.containsKey(position))
			return transpositionTable.get(position);
		
		updateExplored();			// record visited states
	
		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MIN_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.max(v, MIN_Value_ABP_Trans(position, a, b, depth+1));
			undoMove(position);
			if (v >= b)
				return v;
			a = Math.max(a, v);
		}
		
		transpositionTable.put(position, v);
		return v;
	}

	private int MIN_Value_ABP_Trans(Position position, int a, int b, int depth) {
		if (transpositionTable.containsKey(position))
			return transpositionTable.get(position);
		
		updateExplored();			// record visited states
				
		if (Cutoff_Test(position, depth))
			return Evaluation(position);
		
		int v = Integer.MAX_VALUE;
		for (short move: position.getAllMoves()) {
			doMove(position, move);
			v = Math.min(v, MAX_Value_ABP_Trans(position, a, b, depth+1));
			undoMove(position);
			if (v <= a)
				return v;
			b = Math.min(b, v);
		}
		
		transpositionTable.put(position, v);
		return v;
	}
	
	private void doMove(Position position, short move) {
		try {
			System.out.println("AI try to make move " + move);

			position.doMove(move);
			System.out.println(position);
		} catch (IllegalMoveException e) {
			System.out.println("illegal move!");
		}
	}
	
	private void undoMove(Position position) {
		if (!position.undoMove())
			System.out.println("cannot undoMove!!!");
//		System.out.println("");
	}
	
	private void updateExplored() {
		numofPos++;
	}
}
