import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class GeneBankSearch {

    /**
     * Print program's usage to stderr
     */
    public static void printUsage(){
        System.err.println("Usage: ");
        System.err.println("GeneBankSearch <0/1 (no/with Cache)> <btree file> <query file> <cache size> [<debug level>]");
        System.err.println("Cache: whether or not to use a cache");
        System.err.println("BTree file: file BTree is stored in");
        System.err.println("Query file: file containing sequences to search for");
        System.err.println("Cache Size: size of the cache");
        System.err.println("Debug level (optional): defaults to 0");
        System.err.println("\t0 = Query results output to stdout, errors and status messages to stderr");
    }

    public static void main(String args[]){

        //check for wrong number of arguments
        if (args.length < 3 || args.length > 5){
            printUsage();
            return;
        }

        String btreefile; //filename that BTree data is stored in
        String queryfile; //filename that queries are stored in
        int debug;  //only supported option: 0
        int cacheOption;    //0 or 1
        int cacheSize = 0;  //size of cache, if used
        Cache cache = null;

        //input validity checking
        try {
            cacheOption = Integer.parseInt(args[0]);
            btreefile = args[1];
            queryfile = args[2];
            if (cacheOption == 1){
                cacheSize = Integer.parseInt(args[3]);
                if (args.length == 5){
                    debug = Integer.parseInt(args[4]);
                    if (debug != 0){
                        printUsage();
                        return;
                    }
                }
            }
            else if (args.length == 4){
                debug = Integer.parseInt(args[3]);
                if (debug != 0){
                    printUsage();
                    return;
                }
            }
        } catch (IndexOutOfBoundsException e){
            printUsage();
            return;
        } catch (NumberFormatException e){
            printUsage();
            return;
        }

        //Read the Btree
        RandomAccessFile meta;
        long rootOffset;
        int degree;
        try {
            meta = new RandomAccessFile(btreefile + ".metadata", "r");
            rootOffset = meta.readLong();
            degree = meta.readInt();
        } catch (FileNotFoundException e){
            System.err.println("Metadata for given BTree could not be found");
            printUsage();
            return;
        } catch (IOException e){
            System.err.println("Metadata for given BTree could not be read");
            return;
        }
        RandomAccessFile btree;
        try {
            btree = new RandomAccessFile(btreefile, "r");
        } catch (FileNotFoundException e){
            System.err.println("BTree file not found");
            printUsage();
            return;
        }
        BTreeNode root = new BTreeNode(degree, rootOffset, btree, cache);
        try {
            root.readNode(rootOffset);
        } catch (IOException e){
            System.err.println("Could not read root node");
            return;
        }

        //initialize cache, if used
        if (cacheOption == 1) {
            cache = new Cache<BTreeNode>(cacheSize);
        }

        BTree tree = new BTree(degree, btree ,root, cache);

        //open a scanner on query file
        Scanner scan;
        try {
            scan = new Scanner(new File(queryfile));
        } catch (FileNotFoundException e){
            System.err.println("Invalid query file");
            printUsage();
            return;
        }

        //get subsequence length from first line of query file
        String line = scan.nextLine();
        int k = line.length();

        //check that subsequence length equals BTree subsequence length
        int endbtreek = btreefile.lastIndexOf('.');
        int beginbtreek = endbtreek - 2;
        if (btreefile.charAt(beginbtreek) ==  '.'){
            beginbtreek++;
        }
        String btreek = btreefile.substring(beginbtreek, endbtreek);
        int btreeK = Integer.parseInt(btreek);
        if (k != btreeK){
            System.err.println("Query file subsequence length doesn't match given BTree");
            printUsage();
            return;
        }

        //Create an InputParser based on given length
        InputParser parser = new InputParser(k);

        //parse and search for first sequence
        long frequency = tree.search(parser.stringToBinary(line), root);
        System.out.println(frequency + "\t" + line);

        //parse and search for each other sequence
        while(scan.hasNextLine()){
            line = scan.nextLine();
            if (line.length() != k){
                System.err.println("Invalid query file: all lines must be same length");
                return;
            }
            frequency = tree.search(parser.stringToBinary(line), root);
            System.out.println(frequency + "\t" + line);
        }

    }

}
