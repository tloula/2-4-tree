package termproject;

import java.util.ArrayList;
import java.util.Random;

/**
 * Title: Term Project 2-4 Trees Description: Copyright: Copyright (c) 2019
 * Company:
 *
 * @author Trevor Loula & Yayira Dz
 * @version 1.0
 */
public class TwoFourTree
        implements Dictionary {

    private Comparator treeComp;
    private int size = 0;
    private TFNode treeRoot = null;

    public TwoFourTree(Comparator comp) {
        treeComp = comp;
    }

    private TFNode root() {
        return treeRoot;
    }

    private void setRoot(TFNode root) {
        treeRoot = root;
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return (size == 0);
    }

    /**
     * Searches dictionary to determine if key is present
     *
     * @param key to be searched for
     * @return object corresponding to key; null if not found
     */
    @Override
    public Object findElement(Object key) {

        // Search for node possibly containing key
        TFNode node = search(root(), key);
        
        // Note the index of where the key should be
        int index = FFGTE(node, key);

        // If the index is greater than the number of items in the node, the key is not in the node
        if (index < node.getNumItems()) {
            // Look for key in node
            if (treeComp.isEqual(node.getItem(index).key(), key)) {
                return node.getItem(index);
            }
        }

        // The search hit a leaf without finding the key
        return null;
    }

    /**
     * Searches tree to return node containing key
     * Just a modified version of findElement that returns the entire node
     *
     * @param key to be searched for
     * @return node containing the key; null if not found
     */
    public TFNode findNode(Object key) {

        // Search for node possibly containing key
        TFNode node = search(root(), key);

        // Note the index of where the key should be
        int index = FFGTE(node, key);

        // If the index is greater than the number of items in the node, the key is not in the node
        if (index < node.getNumItems()) {
            // Look for key in node
            if (treeComp.isEqual(node.getItem(index).key(), key)) {
                return node;
            }
        }

        // The search hit a leaf without finding the key
        return null;
    }

    /**
     * Inserts provided element into the Dictionary
     *
     * @param key of object to be inserted
     * @param element to be inserted
     * @exception InvalidIntegerException if Integer is not comparable
     */
    @Override
    public void insertElement(Object key, Object element) throws InvalidIntegerException {

        // Ensure object is comparable
        if (!treeComp.isComparable(key)) {
            throw new InvalidIntegerException("Invalid key");
        }

        // Prepare Item
        Item item = new Item(key, element);

        // Account for empty tree
        if (root() == null) {
            TFNode root = new TFNode();
            root.insertItem(0, item);
            setRoot(root);
        } else {
            // Find where to put item using search()
            // Keep calling search until it hits a leaf
            TFNode node = search(root(), key);
            int index = FFGTE(node, key);
            
            // Search down to the leafs
            while(node.getChild(0) != null){
                node = search(node.getChild(index), key);
                index = FFGTE(node, key);
            }

            // Insert item at the leaf
            node.insertItem(index, item);

            // Check overflow
            overflow(node);
        }
        // Increment Size
        ++this.size;
    }

    /**
     * Searches the tree starting at a given TFNode down to an item
     * Recursively calls itself to traverse down to a child TFNode
     * Will stop when node contains a key or when it hits a leaf
     *
     * @param node the node to start searching from
     * @param item item being searched for
     * @return TFNode where the item should be inserted
     */
    private TFNode search(TFNode node, Object key) throws TFNodeException {

        // Check for empty node
        if (node.getNumItems() == 0) {
            throw new TFNodeException("Search discovered an empty node");
        }

        int index = FFGTE(node, key);
        TFNode child = node.getChild(index);

        // If the node contains they key, return node
        if (index < node.getNumItems()) {
            if(treeComp.isEqual(node.getItem(index).key(), key)){
                return node;
            }
        }
        
        // If the node is a leaf, return node
        if (child == null) {
            return node;
        // If neither of the above, keep searching
        } else {
            return search(child, key);
        }
        
    }

    /**
     * Checks for overflow on a node.
     * If node is overflowed, calls split()
     *
     * @param node the TFNode to check
     */
    private void overflow(TFNode node) {
        if (node.getNumItems() > node.getMaxItems()) {
            split(node);
        }
    }

    /**
     * Split operation for inserts
     * Split at third item
     *
     * This is somewhat complicated because it has to hookup a ton of parent and
     * child pointers
     *
     * @param node the node to split
     */
    private void split(TFNode node) {
        // Move item at index 2 up to the parent
        TFNode parent = node.getParent();
        int childIndex;

        // Make new right node
        TFNode newNode = new TFNode();
        Item rightItem = node.getItem(3);
        newNode.addItem(0, rightItem);

        // If node is root, create new parent (new root)
        if (node.getParent() == null) {
            parent = new TFNode();
            setRoot(parent);
            parent.setChild(0, node);
            childIndex = 0;
            node.setParent(parent);
        } else {
            childIndex = WCIT(node);
        }

        // Move up item at index 2
        Item moveUp = node.getItem(2);
        parent.insertItem(childIndex, moveUp);

        // Hookup new right node's pointers
        newNode.setParent(parent);
        parent.setChild(childIndex + 1, newNode);

        // Save new right node's children
        TFNode child3 = node.getChild(3);
        TFNode child4 = node.getChild(4);

        // Hookup new right node's children pointers
        if (child3 != null) {
            child3.setParent(newNode);
        }
        if (child4 != null) {
            child4.setParent(newNode);
        }
        newNode.setChild(0, child3);
        newNode.setChild(1, child4);
        node.setChild(3, null);
        node.setChild(4, null);

        // Remove items from original node
        node.deleteItem(3);
        node.deleteItem(2);

        // Fix any more overflow
        overflow(parent);
    }

    /**
     * Find First Greater Than or Equal
     *
     * @param node the node we are inspecting
     * @param item the item we are looking for
     * @return the index of the key that is greater than or equal to the given key
     * @exception TwoFourTreeException if node is empty
     */
    private int FFGTE(TFNode node, Object key) throws TwoFourTreeException {
        int numItems = node.getNumItems();

        // Check for an empty node
        if (numItems == 0) {
            throw new TwoFourTreeException("Node is empty");
        }

        // Iterate through each item in the TFNode
        for (int i = 0; i < numItems; i++) {
            if (treeComp.isGreaterThanOrEqualTo(node.getItem(i).key(), key)) {
                return i;
            }
        }
        return numItems;
    }

    /**
     * What Child is This
     *
     * @param child node that is being identified
     * @return integer denoting child's index
     * @throws TFNodeException
     * @exception TFNodeException if child's parent is null
     */
    private int WCIT(TFNode child) throws TFNodeException {
        if (child.getParent() == null) {
            throw new TFNodeException("Node has no parent");
        }

        TFNode parent = child.getParent();
        for (int i = 0; i <= parent.getNumItems(); i++) {
            if (child == parent.getChild(i)) {
                return i;
            }
        }
        throw new TFNodeException("Node is not a child of it's parent");
    }

    /**
     * Searches dictionary to determine if key is present, then removes and
     * returns corresponding object
     *
     * @param key of data to be removed
     * @return object corresponding to key
     * @exception ElementNotFoundException if the key is not in dictionary
     */
    @Override
    public Object removeElement(Object key) throws ElementNotFoundException {
        Item item; // Item removed from tree

        // Find the node containing the key
        TFNode foundNode = findNode(key);
        
        // Check node
        if (foundNode == null) {
            throw new ElementNotFoundException("Key not found in tree");
        }

        int index = FFGTE(foundNode, key);
        TFNode removeNode = foundNode;

        // If it is not a leaf
        if (foundNode.getChild(0) != null) {
            removeNode = findIoS(foundNode, key);
            // Replace the item to be removed with the IoS
            item = foundNode.replaceItem(index, removeNode.removeItem(0));
        } else {
            // Node is at a leaf, just remove the item
            item = foundNode.removeItem(index);
        }

        // Check and fix any underflow on removed node
        fixUnderflow(removeNode);

        // Decrement size
        --size;

        return item;
    }

    /**
     * Find In Order Successor
     *
     * @param node to find in order successor of
     * @return node containing the in order successor
     */
    private TFNode findIoS(TFNode node, Object key) {
        if (node.getChild(0) != null) {
            // Go right once
            node = node.getChild(FFGTE(node, key) + 1);

            // Then go left all the way to the bottom
            while (node.getChild(0) != null) {
                node = node.getChild(0);
            }
        }
        return node;
    }

    private void fixUnderflow(TFNode node) {
        if (node.getNumItems() == 0) {
            // If the empty node is the root, remove it and set its one child to be the new root
            if (node.getParent() == null) {
                treeRoot = node.getChild(0);
                if (treeRoot != null) {
                    treeRoot.setParent(null);
                }
            } else {
                // Determine fixup method
                if (canDoLeftTransfer(node)) {
                    leftTransfer(node);
                } else if (canDoRightTransfer(node)) {
                    rightTransfer(node);
                } else if (canDoLeftFusion(node)) {
                    leftFusion(node);
                } else {
                    rightFusion(node);
                }
                // Continue calling function; will stop when node contains items
                fixUnderflow(node.getParent());
            }
        }
    }

    /**
     * Checks if node can have a left transfer done
     *
     * @param node TFNode being checked
     * @return true of can do a left transfer; false otherwise
     * @throws TFNodeException if node has no parent
     */
    private boolean canDoLeftTransfer(TFNode node) throws TFNodeException {
        TFNode parent = node.getParent();
        if (parent == null) {
            throw new TFNodeException("Node has no parent and therefore no sibling");
        }

        // Must have a left sibling
        if (WCIT(node) > 0) {
            // That sibling must have two or more items in it
            TFNode leftSibling = parent.getChild(WCIT(node) - 1);
            if (leftSibling.getNumItems() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if node can have a right transfer done
     *
     * @param node TFNode being checked
     * @return true of can do a right transfer; false otherwise
     * @throws TFNodeException if node has no parent
     */
    private boolean canDoRightTransfer(TFNode node) throws TFNodeException {
        TFNode parent = node.getParent();
        if (parent == null) {
            throw new TFNodeException("Node has no parent and therefore no sibling");
        }

        // Must have a right sibling
        if (WCIT(node) < 2 && parent.getChild(WCIT(node) + 1) != null) {
            // That sibling must have two or more items in it
            TFNode rightSibling = parent.getChild(WCIT(node) + 1);
            if (rightSibling.getNumItems() > 1) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if node can have a left fusion done
     *
     * @param node TFNode being checked
     * @return true of can do a left fusion; false otherwise
     * @throws TFNodeException if node has no parent
     */
    private boolean canDoLeftFusion(TFNode node) throws TFNodeException {
        TFNode parent = node.getParent();
        if (parent == null) {
            throw new TFNodeException("Node has no parent and therefore no sibling");
        }

        // Must have a left sibling
        if (WCIT(node) > 0) {
            return true;
        }
        return false;
    }

    /**
     * Left Transfer Does a left circular shift to rebuild tree
     *
     * @param node empty node to be fixed
     */
    private void leftTransfer(TFNode node) {
        int WCIT = WCIT(node);
        TFNode parent = node.getParent();
        TFNode leftSibling = parent.getChild(WCIT - 1);

        // 1. Copy item in parent to empty node
        node.insertItem(0, parent.getItem(WCIT - 1));

        // 2. Move the siblings child (at numItems() slot) over to node
        TFNode child = leftSibling.getChild(leftSibling.getNumItems());
        node.setChild(0, child);
        if (child != null) {
            child.setParent(node);
            // Copy the child to the left of the item to be removed over to the right of said item
            // because removing the item will delete the pointer to the left
            leftSibling.setChild(leftSibling.getNumItems(), leftSibling.getChild(leftSibling.getNumItems() - 1));
        }

        // 3. Replace in parent item at (WCIT - 1) with sibling's rightmost item
        parent.replaceItem(WCIT - 1, leftSibling.removeItem(leftSibling.getNumItems() - 1));
    }

    /**
     * Right Transfer Does a right circular shift to rebuild tree
     *
     * @param node empty node to be fixed
     */
    private void rightTransfer(TFNode node) {
        int WCIT = WCIT(node);
        TFNode parent = node.getParent();
        TFNode rightSibling = parent.getChild(WCIT + 1);

        // 1. Copy item in parent to empty node
        node.insertItem(0, parent.getItem(WCIT));

        // 2. Move the siblings child (at numItems() slot) over to node
        TFNode child = rightSibling.getChild(0);
        node.setChild(1, child);
        if (child != null) {
            child.setParent(node);
        }

        // 3. Replace in parent item at (WCIT) with sibling's leftmost item
        parent.replaceItem(WCIT, rightSibling.removeItem(0));
    }

    /**
     * Left Fusion
     *
     * @param node empty node to be fixed
     */
    private void leftFusion(TFNode node) {
        int WCIT = WCIT(node);
        TFNode parent = node.getParent();
        TFNode leftSibling = parent.getChild(WCIT - 1);

        // 1. Copy the item from the parent into the left sibling
        leftSibling.insertItem(leftSibling.getNumItems(), parent.getItem(WCIT - 1));

        // 2. Move child 0 of empty node to child 2 of the sibling
        TFNode child = node.getChild(0);
        leftSibling.setChild(2, child);
        if (child != null) {
            child.setParent(leftSibling);
        }

        // 3. Be very careful and remove item from the parent
        parent.removeItem(WCIT - 1);
        parent.setChild(WCIT - 1, leftSibling);
    }

    /**
     * Right Fusion
     *
     * @param node empty node to be fixed
     */
    private void rightFusion(TFNode node) {
        int WCIT = WCIT(node);
        TFNode parent = node.getParent();
        TFNode rightSibling = parent.getChild(WCIT + 1);

        // 1. Copy the item from the parent into the right sibling
        rightSibling.insertItem(0, parent.getItem(WCIT));

        // 2. Move child 0 of empty node to child 0 of the sibling
        TFNode child = node.getChild(0);
        rightSibling.setChild(0, child);
        if (child != null) {
            child.setParent(rightSibling);
        }

        // 3. Be very careful and remove item from the parent
        parent.removeItem(WCIT);
        parent.setChild(WCIT, rightSibling);
    }

    public static void main(String[] args) {
        
        // Initialize variables for tests
        Comparator myComp = new IntegerComparator();
        TwoFourTree myTree = new TwoFourTree(myComp);
        int TEST_SIZE;
        
        // Test 1: 10,000 Increasing Integers
        myTree = new TwoFourTree(myComp);
        TEST_SIZE = 10000;
        System.out.println("\u001B[34m" + "***** TESTING " + TEST_SIZE + " INCREASING INTEGERS *****");
        System.out.println("Inserting Into Tree");
        for (int i = 0; i < TEST_SIZE; i++) {
            myTree.insertElement(new Integer(i), new Integer(i));
        }
        
        myTree.checkTree();
        System.out.println("Tree Passed Check");
        
        System.out.println("Removing");
        for (int i = 0; i < TEST_SIZE; i++) {
            int out = (Integer) (((Item) myTree.removeElement(new Integer(i))).key());
            if (out != i) {
                throw new TwoFourTreeException("main: wrong element removed");
            }
        }
        System.out.println("\u001B[32m" + "Done");

        // Test 2: Random Integers
        myTree = new TwoFourTree(myComp);
        ArrayList<Integer> myAL = new ArrayList<Integer>();
        Random r = new Random();
        int rand = 0;
        
        /***** Set test size & whether or not the print steps *****/
        TEST_SIZE = 10000;
        boolean printSteps = false;
        
        System.out.println("\u001B[34m" + "***** TESTING " + TEST_SIZE + " RANDOM INTEGERS *****");

        // Build tree with random integers; insert into ArrayList
        System.out.println("Inserting Into Tree And ArrayList");
        for (int i = 0; i < TEST_SIZE; i++) {
            rand = r.nextInt(TEST_SIZE/10);
            myAL.add(rand);
            myTree.insertElement(new Integer(rand), new Integer(rand));
            myTree.checkTree();
        }
        
        // Print Out Array
        if(printSteps){
            System.out.println("ArrayList: ");
            myAL.forEach((num) -> {
                System.out.println(num);
            });
            System.out.println("Initial Tree: ");
            myTree.printAllElements();
            System.out.println("--------------------------------------------------");
        }
        
        myTree.checkTree();
        System.out.println("Tree Passed Check");
        
        System.out.println("Removing");
        try {
            int count  = 0;
            for (Integer num : myAL) {
                count++;
                if(printSteps){
                    System.out.println("Removing: " + num);
                }
                if (count > TEST_SIZE-25){
                    System.out.println("Removing: " + num);
                }
                int out = (Integer)(((Item) myTree.removeElement(new Integer(num))).key());
                if (out != num) {
                    throw new TwoFourTreeException("main: wrong element removed");
                }
                if (count > TEST_SIZE-25){
                    myTree.printAllElements();
                }
                if(printSteps){
                    System.out.println("Removed: " + num);
                    System.out.println("Size: " + myTree.size());
                    myTree.printAllElements();
                    myTree.checkTree();
                    System.out.println("Tree Passed Check");
                    System.out.println("--------------------------------------------------");
                }
                myTree.checkTree();
            }
        } catch (Exception e) {
            System.out.println("\u001B[31m" + "Exception Caught: " + e.getMessage());
        }
        System.out.println("\u001B[32m" + "Done");
    }

    public void printAllElements() {
        int indent = 0;
        if (root() == null) {
            System.out.println("The tree is empty");
        } else {
            printTree(root(), indent);
        }
    }

    public void printTree(TFNode start, int indent) {
        if (start == null) {
            return;
        }
        for (int i = 0; i < indent; i++) {
            System.out.print(" ");
        }
        printTFNode(start);
        indent += 4;
        int numChildren = start.getNumItems() + 1;
        for (int i = 0; i < numChildren; i++) {
            printTree(start.getChild(i), indent);
        }
    }

    public void printTFNode(TFNode node) {
        int numItems = node.getNumItems();
        for (int i = 0; i < numItems; i++) {
            System.out.print(((Item) node.getItem(i)).element() + " ");
        }
        System.out.println();
    }

    // checks if tree is properly hooked up, i.e., children point to parents
    public void checkTree() {
        checkTreeFromNode(treeRoot);
    }

    private void checkTreeFromNode(TFNode start) {
        
        if (start == null) {
            return;
        }

        if (start.getParent() != null) {
            TFNode parent = start.getParent();
            int childIndex = 0;
            for (childIndex = 0; childIndex <= parent.getNumItems(); childIndex++) {
                if (parent.getChild(childIndex) == start) {
                    break;
                }
            }
            // if child wasn't found, print problem
            if (childIndex > parent.getNumItems()) {
                System.out.println("\u001B[31m" + "Child to parent confusion" + "\\u001B[0m");
                printTFNode(start);
            }
        }

        if (start.getChild(0) != null) {
            for (int childIndex = 0; childIndex <= start.getNumItems(); childIndex++) {
                if (start.getChild(childIndex) == null) {
                    System.out.println("\u001B[31m" + "Mixed null and non-null children" + "\\u001B[0m");
                    printTFNode(start);
                } else {
                    if (start.getChild(childIndex).getParent() != start) {
                        System.out.println("\u001B[31m" + "Parent to child confusion" + "\\u001B[0m");
                        printTFNode(start);
                    }
                    for (int i = childIndex - 1; i >= 0; i--) {
                        if (start.getChild(i) == start.getChild(childIndex)) {
                            System.out.println("\u001B[31m" + "Duplicate children of node" + "\\u001B[0m");
                            printTFNode(start);
                        }
                    }
                }

            }
        }
        
        int numChildren = start.getNumItems() + 1;
        for (int childIndex = 0; childIndex < numChildren; childIndex++) {
            checkTreeFromNode(start.getChild(childIndex));
        }

    }
}
