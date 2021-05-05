import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.util.Random;

public class BTreeInsertTest {

    public static void main(String[] args) {
        System.out.println("TESTING: BTree Insertion\n------------------------");
        RandomAccessFile testFile = null;
        try {
            testFile = new RandomAccessFile("testFile", "rw");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        /** CREATE THE BTREE **/
        System.out.println("Creating BTree...");
        Random rand = new Random();
        int degree = rand.nextInt(100)+1;
        Cache cache = new Cache<BTreeNode>(100);
        BTree tree = new BTree(degree, testFile, null, cache);
        System.out.println("Random Degree Chosen: " + degree);

        /** INSERT KEYS INTO BTREE **/
        System.out.println("Inserting keys...");
        for (int i = 0; i < 1000; i++) {
            tree.insert(rand.nextInt(40));
        }

        /** WRITE OUT A FILE CONTAINING BTREE **/
       // RandomAccessFile dumpTest = null;
        try {
          //  System.out.println("Writing dump...");
           // dumpTest = new RandomAccessFile("dumpTest", "rw");
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("Done.");
    }

}
