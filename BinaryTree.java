package SymboLo;

public class BinaryTree<T> {

    // The BinaryTree is essentially just a wrapper around the linked 
    // structure of BinaryNodes, rooted in root.
    protected BinaryNode<T> root;

    /**
     * Represent a binary subtree.
     * Modified by John Hui
     * to a protected class so that its data can be accesed by
     * the ExpressionTree subclass.
     */
    protected static class BinaryNode<T>{

        public T             data;  // the data 
        public BinaryNode<T> left;  // left subtree
        public BinaryNode<T> right; // right subtree
    
        /**
         * Construct a new binary node. 
         */
        public BinaryNode( T theData, BinaryNode<T> leftChild, 
                                      BinaryNode<T> rightChild ) {
            data    = theData; 
            left    = leftChild;
            right   = rightChild;
        }

        /**
         * Print tree rooted at current node using preorder traversal.
         */
        public void printTree(int indent ) {
            for (int i=0;i<indent;i++)
                System.out.print(" ");

            System.out.println(data);        // Node
            if( left != null )
                left.printTree(indent + 1);  // Left
            if( right != null )
                right.printTree(indent + 1);  // Right
        }

    } // Nested class BinaryNode ends here.
   
 
    /**
     * Construct a new empty BinaryTree
     */
    public BinaryTree() {
        root = null;
    }

    /**
     * Construct a new BinaryTree wrapper around the BinaryNode rootNode.
     */
    public BinaryTree(BinaryNode<T> rootNode) {
        root = rootNode;
    }

    /**
     * Display the tree as an indented in-order notation.
     */
    public void printTree() {
        if (root != null)
            root.printTree(0);
        else
            System.out.println("Empty Tree");
    }

    /** 
     * Factory method that creates a new BinaryTree with two subtrees, that contains theItem
     * as the data object attached to its root.  
     * The two btree methods make it posasible to easily construt binary trees like this: 
     * BinaryTree<Integer> t = btree(1,btree(2,btree(3),btree(4)),btree(5));
     * @return a new BinaryTree with two children.  
     */ 
    public static <T> BinaryTree<T> btree(T theItem, BinaryTree<T> t1, BinaryTree<T> t2) {
        BinaryNode<T> newRoot = new BinaryNode<T>(theItem, t1.root, t2.root);
        return new BinaryTree<T>(newRoot);
    }
    
    /**
     * Factory method that creates a new BinaryTree with no children, which contains 
     * theItem as data object attached to its root.
     * @return a new BinaryTree with no children.
     */
    public static <T> BinaryTree<T> btree(T theItem) {
        return new BinaryTree<T>(new BinaryNode<T>(theItem, null, null ));
    }


    /**
     * Test method: Create and print a BinaryTree. 
     */ 
    public static void main(String[] args) {
        BinaryTree<Integer> t = btree(1,btree(2,btree(3),btree(4)),btree(5));
        t.printTree();
    }

}
