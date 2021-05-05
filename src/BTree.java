import java.io.*;

public class BTree {

    private BTreeNode root; //root node
    private int t; //degree
    RandomAccessFile file; //file to store tree inside
    Cache cache; //cache to store BTreeNodes in

    /**
     * Constructor
     *
     * @param t --degree of BTree (int)
     * @param file -- file that BTree is stored in (RandomAccessFile)
     * @param root -- root of BTree (null if tree has not yet been created)
     */
    public BTree(int t, RandomAccessFile file, BTreeNode root, Cache cache) {
        this.t = t;
        this.root = root;
        this.file = file;
        this.cache = cache;
    }

    /**
     * Create a new (empty) BTreeNode and write it to the file
     * @return new node
     */
    public BTreeNode createBTreeNode(){
        long offset;
        try {
            offset = file.length();
        } catch (IOException e){
            System.out.println(e.getMessage());
            return null;    //if we can't place new node at the correct offset, don't create one.
        }
        BTreeNode node = new BTreeNode(t, offset, file, cache);
        try {
            node.writeNode();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return node;
    }

    /**
     * Search the BTree for a given sequence
     * @param sequence -- to search for (long)
     * @param node -- BTreeNode to begin search at (initial call should be to root)
     * @return frequency of sequence's occurrence.
     */
    public long search(long sequence, BTreeNode node){
        if (cache != null){
            int frequency = cache.getFrequency(sequence);
                //if the sequence is in the cache, getFrequency will move the node to the beginning
            if (frequency !=0 ){
                return frequency;
            }//otherwise, need to search entire BTree
        }
        if (node == null){
            //subtree is empty
            return 0;
        }
        int i = 0;
        while (i < node.getNumKeys() && node.keys[i].getKey() < sequence){
            i++;
        }
        if (i == node.getNumKeys()){
            //key is greater than all that are stored in this node
            if (node.isLeaf()){
                return 0;
            }
        }
        else if (node.keys[i].getKey() == sequence){
            //found
            //if cache is being used, this node needs to be added
            if (cache != null){
                cache.addObject(node);
            }
            return node.keys[i].getFrequency();
        }

        if (node.children[i] == -1){
            return 0;   //no more children to search, this element isn't here.
        }
        BTreeNode child = null;
        try {
            //read node's largest child
            child = new BTreeNode(t, node.children[i], file, cache);
            child.readNode(node.children[i]);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return search(sequence, child);
    }

    /**
     * insert() inserts a key into the BTree. It will find the
     * location in the BTree and insert the key into that location.
     * It will also call splitChild and insertNonFull accordingly when
     * the BTree must split.
     * @param key
     */
    public void insert(long key){

        if (cache != null){
            BTreeNode node = cache.getObject(key);
                //if key is in cache, this call will have incremented frequency
            if (cache.getObject(key) != null){
                try {
                    node.writeNode();       //write updated node and move it to front of cache
                } catch (IOException e){
                    System.err.println(e.getMessage());
                }
                return;
            }
        }

        //If the tree is empty
        if (root == null) {
            //Allocate a new node for the root
            root = createBTreeNode();

            //Add the key to be inserted
            root.addKey(key);
        }
        //The tree is not empty
        else {
            //If the root is full, then the tree grows in height
            if (root.getNumKeys() == ((2*t)-1) && !root.contains(key)) {
                //Allocate memory for a new root
                BTreeNode newNode = createBTreeNode();

                //Make the old root the child of the new root
                newNode.children[0] = root.getOffset();

                //Split the old root into 2, and move middle key upwards
                splitChild(0, root, newNode);

                //Now it's time to actually add the new key into the child
                try {
                    root.readNode(newNode.getOffset());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                insertNonFull(key, newNode);

            }
            else {
                insertNonFull(key, root);

            }
        }
    }

    /**
     * insertNonFull() is a helper method for insert().
     * It takes a key k and inserts it into a BTreeNode n.
     * The assumption for this method is that n is nonfull
     *
     * @param k key to be inserted
     * @param n BTreeNode to be inserted into
     */
    public void insertNonFull(long k, BTreeNode n) {

        boolean duplicate = false;
        for (int i = 0; i < n.getNumKeys(); i++) {
            if (n.keys[i].getKey() == k) {
                duplicate = true;
            }
        }

        if (n.isLeaf() || duplicate) {
            n.addKey(k);

            try {
                n.writeNode();
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        else {
            int i = 0;
            while (i < n.getNumKeys() && n.keys[i].getKey() < k) {
                i++;
            }

            //Overwrite n as the child to be inserted into
            BTreeNode child = null;
            try {
                child = new BTreeNode(t, n.getOffset(), file, cache);
                child.readNode(n.children[i]);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            duplicate = false;
            int num = 0;
            while (num < child.keys.length && child.keys[num] != null) {
                if (child.keys[num].getKey() == k && duplicate == false) {
                    duplicate = true;
                }
                num++;
            }
            if (child.getNumKeys() == ((2*t)-1) && !duplicate) {


                splitChild(i, child, n);
                i=0;
                while (i < n.getNumKeys() && k > n.keys[i].getKey()) {
                    i++;
                }

                try {
                    child.readNode(n.children[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            insertNonFull(k, child);
        }


    }


    /**
     * splitChild() is helper method for insert().
     * It splits the child y located at the index i of
     * the parent p's children
     *
     * @param i index of split child (you will insert the new child at (i+1)
     * @param y child to be split
     * @param p parent node of y (NOTE: y must be full)
     */
    public void splitChild(int i, BTreeNode y, BTreeNode p) {
        //One node is splitting into two, so we need a new node
        //The new node "z" will hold the keys to the right of the middle key
        if (p.getNumKeys() == (2*t -1)) {
            BTreeNode newP = createBTreeNode();
            newP.children[0] = p.getOffset();
            splitChild(0, p, newP);

            root = new BTreeNode(t, newP.getOffset(), file, cache);
            try {
                root.readNode(newP.getOffset());
                root.writeNode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        BTreeNode z = createBTreeNode();

        //Copy the last (t-1) keys of y into z
        for (int n = 0; n < t-1; n++) {
            z.transferKey(y, t);
        }

        //If y (the node being split) has children, we need to give the last half to z
        if (!y.isLeaf()) {
            for (int n = 0; n < t; n++) {
                z.children[n] = y.children[n+t];
                y.children[n+t] = -1;
            }
        }

        //Link the new child z to the parent p
        if (p.children[i+1] != -1) {
            for (int num = p.children.length-1; num > i+1; num--) {
                p.children[num] = p.children[num - 1];
            }
        }
        p.children[i+1] = z.getOffset();

        p.transferKey(y, t-1);

        try {
            z.writeNode();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }



    /**
     * Traverse the tree
     * Write all keys and their frequencies to a file
     * This traversal will print the nodes in order, by key
     * @param dump --RandomAccessFile
     * @param node --BTreeNode to begin traversal at (root of subtree)
     * @param offset --offset starting node is at in BTree file (long)
     * @throws IOException if there is a problem writing to dump
     */
    public void traverse(PrintWriter dump, InputParser parser, BTreeNode node, long offset) throws IOException {
        if (offset < 0){
            return;
        }

        node.readNode(offset);

        for (int i = 0; i < node.getNumKeys(); i++) {

            if (!node.isLeaf())
                traverse(dump, parser, new BTreeNode(t, node.getOffset(), file, cache), node.children[i]);

            dump.println(node.keys[i].getFrequency() + " " + parser.longToSequence(node.keys[i].getKey()));
        }

        if (!node.isLeaf())
            traverse(dump, parser, new BTreeNode(t, node.getOffset(), file, cache), node.children[(int) node.getNumKeys()]);

    }

    /**
     * @return root of BTree
     */
    public BTreeNode getRoot(){
        return root;
    }

}