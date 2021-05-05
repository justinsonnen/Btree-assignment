import java.io.IOException;
import java.io.RandomAccessFile;

public class BTreeNode {

    public TreeObject keys[]; //array of keys inside this node
    public long children[]; //array of this node's children's file offsets
    private int t; //degree of BTree
    private long offset; //offset within BTree file
    private long numKeys; //number of keys currently stored in this node
    private RandomAccessFile file; //file that BTree is stored in
    private Cache cache; //cache containing BTreeNodes

    /**
     * Constructor
     *
     * @param t --degree of BTree (int)
     * @param offset --offset to be stored at within BTree file (long)
     * @param file --file that BTree is stored in
     */
    public BTreeNode(int t, long offset, RandomAccessFile file, Cache cache){
        this.t = t;
        this.file = file;
        //maximum # of values = 2t - 1
        keys = new TreeObject[2 * (this.t) - 1];
        //initialize each to null
        for (int i = 0; i < keys.length; i++){
            keys[i] = null;
        }
        //maximum # of children: 2t
        children = new long[2 * (this.t)];
        //initialize each to -1 (invalid offset)
        for (int i = 0; i < children.length; i++){
            children[i] = -1;
        }
        this.offset = offset;
        numKeys = 0;
        this.cache = cache;
    }

    public long getNumKeys(){
        return numKeys;
    }

    public long getOffset(){
        return offset;
    }

    /**
     * Decrement the number of keys by one
     */
    public void decNumKeys() { numKeys--; }

    /**
     * Write this node to the end of the file
     * @throws IOException
     */
    public void writeNode() throws IOException {
        // seek to end of file
        file.seek(offset);
        // write offset data
        file.writeLong(offset);
        // write numKeys data
        file.writeLong(numKeys);
        // write children offset
        for (int i = 0; i < children.length; i++){
            file.writeLong(children[i]);
        }
        // write key data
        for (int i = 0; i < keys.length; i++){
            if (keys[i] == null){
                file.writeLong(-1);
                file.writeInt(0);
            }
            else{
                file.writeLong(keys[i].getKey());
                file.writeInt(keys[i].getFrequency());
            }
        }

        //The node has been updated -- move it to the front of the cache
        if (cache != null){
            cache.addObject(this);
        }


    }

    /**
     * Reads a node from the file
     * @param location --offset to begin reading node at
     * @throws IOException
     */
    public void readNode(long location) throws IOException {
        //locate node to read
        file.seek(location);
        //read key
        this.offset = file.readLong();
        //read numKeys
        this.numKeys = file.readLong();
        //read children from node
        for (int i = 0; i < children.length; i++) {
            children[i] = file.readLong();
        }
        /// read key data
        long key = -1;
        int freq = -1;

        for (int i = 0; i < keys.length; i++){
            key = file.readLong();
            freq = file.readInt();
            if (key == -1){
                keys[i] = null;
            }
            else {
                if (keys[i] == null) {
                    keys[i] = new TreeObject(key);
                }
                keys[i].frequency = freq;
                keys[i].key = key;
            }
        }

    }

    /**
     * @return true if Node is a leaf, false otherwise
     */
    public boolean isLeaf(){
        return (children[0] == -1);
    }

    /**
     * Add a new key to the node
     * @param key --long to be added
     * @return true if value was successfully added, false if node is full.
     */
    public int addKey(long key){
        int i = 0;
        while (i < numKeys && keys[i].getKey() < key){
            i++;
        }
        if (i == keys.length){
            //node is full
            return -1;
        }
        if (i == numKeys){
            //at end of list of keys, there is space for new one
            TreeObject newKey = new TreeObject(key);
            keys[i] = newKey;
            numKeys++;
            try {
                writeNode();
            } catch(IOException e){
                System.err.println(e.getMessage());
            }
            return i;
        }
        if (keys[i].getKey() == key){
            //found a key that already exists
            keys[i].incrementFrequency();
            try {
                writeNode();
            } catch(IOException e){
                System.err.println(e.getMessage());
            }
            return i;    //don't increment numKeys, we didn't add a new value
        }
        //keys[i].getKey() > than key
        //need to shift other elements backward to make space for new key
        for (int j = keys.length - 1; j > i; j--){
            keys[j] = keys[j-1];
        }
        TreeObject newKey = new TreeObject(key);
        keys[i] = newKey;
        numKeys++;
        try {
            writeNode();
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
        return i;
    }

    public boolean transferKey(BTreeNode from, int index){
        TreeObject key = from.keys[index];
        int i = addKey(key.getKey());
        if (i == -1){
            return false;
        }
        keys[i].frequency = key.frequency;
        //remove key from other BTreeNode
        for (int j = index; j < keys.length - 1; j++){
            from.keys[j] = from.keys[j + 1];
        }
        from.decNumKeys();
        from.keys[keys.length - 1] = null;
        try {
            from.writeNode();
            writeNode();
        } catch(IOException e){
            System.err.println(e.getMessage());
        }
        return true;
    }

    /**
     * Check if node contains given key
     * @param key (long)
     * @return true if key is stored in this node
     */
    public boolean contains(long key){
        for (int i = 0; i < numKeys; i++){
            if (keys[i].key == key){
                return true;
            }
        }
        return false;
    }

}
