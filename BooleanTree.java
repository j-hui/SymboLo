package SymboLo;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.Stack;

public class BooleanTree extends BinaryTree<Symbol.Logic>{
	
	public BooleanTree() {
        root = null;
    }
    public BooleanTree(BinaryNode<Symbol.Logic> rootNode) {
        root = rootNode;
    }
    public static BooleanTree booltree(Symbol.Logic theItem, 
    		BooleanTree t1, BooleanTree t2) {
        BinaryNode<Symbol.Logic> newRoot = 
                new BinaryNode<Symbol.Logic>(theItem, t1.root, t2.root);
        return new BooleanTree(newRoot);
    }
    public static BooleanTree booltree(Symbol.Logic theItem, BooleanTree t1) {
        BinaryNode<Symbol.Logic> newRoot = 
                new BinaryNode<Symbol.Logic>(theItem, t1.root, null);
        return new BooleanTree(newRoot);
    }
    public static BooleanTree booltree(Symbol.Logic theItem) {
        return new BooleanTree(new BinaryNode<Symbol.Logic>(theItem, null, null));
    }
    
    public static BooleanTree postToTree(Queue<Symbol> exp) { //postfix stack to tree
    	Stack<BooleanTree> ops = new Stack<BooleanTree>();
        System.out.print("Queue is ");
    	for(Symbol sym : exp) { 
    		System.out.print(sym.getSymbol() + ".");
    	}
    	System.out.println();
    	while(!exp.isEmpty()) {
        	boolean isOp = false;
            for(Symbol.Operator o : Symbol.Operator.values()) {
            	if(o.compareTo(exp.peek()) == 0) {
                	if(o.isMonadic()) {
                    	ops.push(BooleanTree.booltree(o.operator(), ops.pop()));
                    } else {
                    	ops.push(BooleanTree.booltree(o.operator(), ops.pop(), ops.pop()));
                    }
                    isOp = true;
                    break;
                }
            }
            if(!isOp) {
            	ops.push(BooleanTree.booltree(new Symbol.Logic.Variable(exp.peek().getSymbol())));
            }
            exp.remove();
        }
        return ops.pop();
    }
    
    public static Queue<Symbol> inToPost(String input) {
    	Stack<Symbol> s = new Stack<Symbol>();
        //String w = input.replaceAll(" ", "");
        String[] x = input.replaceAll(" ", "").split("");
        Queue<Symbol> q = new LinkedList<Symbol>();
        for(int i = 1; i < x.length; i++) {
        	if(Symbol.Parentheses.OPEN.compareTo(x[i]) == 0) {
            	s.push(Symbol.Parentheses.OPEN);
            } else if (Symbol.Parentheses.CLOSE.compareTo(x[i]) == 0) {
            	while(s.peek().compareTo(Symbol.Parentheses.OPEN) != 0) {
            		q.add(s.pop());
            	}
            	s.pop();
            } else {
            	boolean isOp = false;
            	for(Symbol.Operator o : Symbol.Operator.values()) {
            		if(o.compareTo(x[i]) == 0) {
            			while(!s.empty() && (o.getPrecedence() < s.peek().getPrecedence())) {
            				q.add(s.pop());
            			}
            			s.push(o);
            			isOp = true;
            			break;
            		}
            	}
            	if(!isOp) {
            		Symbol newVar = new Symbol.Variable(x[i]);
            		q.add(newVar);
            	}
            } 
        }
        while(!s.empty()) {
            q.add(s.pop());
        }
        return q;
    }
    
    public String treeToIn() {
        return treeToIn(root);
    }
    private String treeToIn(BinaryNode<Symbol.Logic> n) {
        if(n.left != null) { // is operator
        	if(n.right != null) { //is boolean
        		return treeToIn(n.data.getPrecedence(), n.right) + " "
            			+ n.data.getSymbol() + " "
            			+ treeToIn(n.data.getPrecedence(), n.left);
        	} else { // is monadic
        		return n.data.getSymbol() + " "
        				+ treeToIn(n.data.getPrecedence(), n.left);
        	}	
        } else {
        	return n.data.getSymbol();
        }
    }
    private String treeToIn(int operatorPrecedence, BinaryNode<Symbol.Logic> operand) {
    	if(operand.data.getPrecedence() < operatorPrecedence) {
    		return "( " + treeToIn(operand) + " )";
        } else {
            return treeToIn(operand);
        }
    }
    
    public void eval() {
    	Symbol.Logic.Variable.countAndEnumTVals();
    	eval(root);
    }
    
	@SuppressWarnings("unchecked")
	private ArrayList<Boolean> eval(BinaryNode<Symbol.Logic> operand) {
    	//ArrayList<ArrayList<Boolean>> ops = new ArrayList<ArrayList<Boolean>>();
    	@SuppressWarnings("rawtypes")
		ArrayList[] ops = new ArrayList[2];
    	if(operand.left != null) {
    		ops[0] = eval(operand.left);
    	}
    	if(operand.right != null) {
    		ops[1] = eval(operand.right);
    	}
    	return operand.data.eval(ops);
    }
    
    public static void main(String[] args) {
        try {
            BooleanTree exp = BooleanTree.postToTree
                    (BooleanTree.inToPost(BooleanTree.prompt()));
            //System.out.println("Prefix: " + exp.toPrefix());
            //System.out.println("Tree grown");
            System.out.print(exp.treeToIn() + " ");
            exp.eval();
            //System.out.println("eval()'ed");
            System.out.println(exp.root.data.getTruthValues());
            //System.out.println(exp.eval());
            //System.out.println("Result: " + exp.evaluate());
        } catch(NumberFormatException e) {System.out.println(e);}
    }
    
    private static String prompt() {
        Scanner s = new Scanner(System.in);
        String userIn;
        System.out.println("Please enter your "
                + "boolean expression and press enter: ");
        userIn = s.nextLine();
        s.close();
        return userIn;
    }
}
