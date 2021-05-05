import java.util.LinkedList;

public class Cache<T> {

    /** INSTANCE VARIABLES **/
    private LinkedList<BTreeNode> cache; //linked list used to implement cache
    private int size;   //maximum number of elements to store in cache

    /**
     * Constructor
     * @param size --maximum number of elements to store in cache
     */
    public Cache(int size) {
        cache = new LinkedList<BTreeNode>();
        this.size = size;
    }

    /**
     * Adds a BTreeNode to the cache
     * If node is already present, it is moved to the front
     * If the cache is full, the last element is removed.
     * @param node --node to be added
     */
    public void addObject(BTreeNode node) {
        if (cache.contains(node)) {
            cache.remove(node);
        }

        cache.addFirst(node);
        checkSize();
    }

    /**
     * Searches cache for a specific key
     * @param key (long)
     * @return frequency of key's occurrence.
     */
    public int getFrequency(long key) {
        int i = 0;
        while (i < cache.size()-1) {
            for (TreeObject k : cache.get(i).keys) {
                if (k != null && k.getKey() == key) {
                    addObject(cache.get(i));    //move this node to the front
                    return k.getFrequency();
                }
            }
            i++;
        }
        return 0;
    }

    /**
     * Searches cache for a specific key
     * @param key (long)
     * @return BTreeNode containing key
     */
    public BTreeNode getObject(long key) {
        int i = 0;
        while (i < cache.size()-1) {
            for (TreeObject k : cache.get(i).keys) {
                if (k != null && k.getKey() == key) {
                    k.incrementFrequency(); //increment frequence of key
                    return cache.get(i);    //return node --needs to be written
                }
            }
            i++;
        }
        return null;
    }

    /**
     * Check the size of the cache
     * If it is full, the last element is removed
     */
    public void checkSize() {
        if (cache.size() == size) {
            cache.removeLast();
        }
    }

}
