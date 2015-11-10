package SymboLo;


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
}
