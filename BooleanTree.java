package SymboLo;
import java.util.LinkedList;
import java.util.Queue;
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
    public static BooleanTree booltree(Symbol.Logic theItem) {
        return new BooleanTree(new BinaryNode<Symbol.Logic>(theItem, null, null));
    }
    
    public static BooleanTree postToTree(Queue<Symbol> exp) { //postfix stack to tree
        Stack<BooleanTree> ops = new Stack<BooleanTree>();
        while(!exp.isEmpty()) {
        	boolean isOp = false;
            for(Symbol.Operator o : Symbol.Operator.values()) {
                if(o.compareTo(exp.peek()) == 0) {
                    if(o.isMonadic()) {
                    	ops.push(BooleanTree.booltree(o.operator(), ops.pop(), null));
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
        String[] x = input.split(" "); //assume every symbol is separated by " "
        Queue<Symbol> q = new LinkedList<Symbol>();
        
        for(int i = 0; i < x.length; i++) {
            if(Symbol.Parentheses.OPEN.compareTo(x[i]) == 0) {
            	s.push(Symbol.Parentheses.OPEN);
            } else if (Symbol.Parentheses.CLOSE.compareTo(x[i]) == 0) {
            	while(s.peek().compareTo(Symbol.Parentheses.OPEN) != 0) {
            		q.add(s.pop());
            	}
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
            		q.add(new Symbol.Variable(x[i]));
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
    	if(operand.data.getPrecedence() <= operatorPrecedence) {
    		return "( " + treeToIn(operand) + " )";
        } else {
            return treeToIn(operand);
        }
    }
}
