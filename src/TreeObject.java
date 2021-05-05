/**
 * Object that stores one key and its frequency
 * Multiple TreeObjects can be stored in one BTreeNode
 */
public class TreeObject {

    public long key;   //binary representation of a DNA sequence
    public int frequency; //number of times this key has appeared

    /**
     * Constructor
     * @param key --long
     */
    public TreeObject(long key){
        this.key = key;
        frequency = 1; //If we're creating a TreeObject, this key has appeared exactly once
    }

    /**
     * Add one to frequency count
     */
    public void incrementFrequency(){
        frequency++;
    }

    /**
     * @return frequency (int)
     */
    public int getFrequency(){
        return frequency;
    }

    /**
     * @return key (long)
     */
    public long getKey(){
        return key;
    }

}
