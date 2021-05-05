import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;

public class GeneBankCreateBTree {

    /**
     * Print usage of program
     */
    public static void printUsage(){
        System.err.println("Usage:");
        System.err.println("GeneBankCreateBTree <0/1 (no/with Cache)> <degree> <gbk file> <sequence length> <cache size> [<debug level>]");
        System.err.println("Cache: whether or not to use a cache");
        System.err.println("Degree: degree of BTree.  Choose 0 for optimum degree");
        System.err.println("GBK file: name of file containing DNA sequence");
        System.err.println("Sequence length: length of subsequences to store, between 1 and 31 (inclusive)");
        System.err.println("Cache Size: size of the cache");
        System.err.println("Debug level (optional, defaults to 0): ");
        System.err.println("\t0 = help/status messages printed to stderr");
        System.err.println("\t1 = dump file containing keys and their frequency created");
    }

    public static void main(String args[]){

        int degree; //degree of BTree
        String gbk; //Name of gbk file to parse sequence from
        int k; //length of sequences
        int debug = 0; //0 or 1
        int cacheOption; //0 or 1
        int cacheSize = 0; //size of cache, if being used
        Cache cache = null;

        //check for wrong number of arguments
        if (args.length < 4 || args.length > 6){
            printUsage();
            return;
        }

        try{    //read in arguments
            cacheOption = Integer.parseInt(args[0]);
            degree = Integer.parseInt(args[1]);
            gbk = args[2];
            k = Integer.parseInt(args[3]);
            if (cacheOption == 1){
                cacheSize = Integer.parseInt(args[4]);
                if (args.length == 6){
                    debug = Integer.parseInt(args[5]);
                }
            }
            else if (args.length == 5){
                debug = Integer.parseInt(args[4]);
            }
            if (debug != 0 && debug != 1){
                printUsage();
                return;
            }
        } catch(NumberFormatException e){
            printUsage();
            return;
        } catch(IndexOutOfBoundsException e) {
            printUsage();
            return;
        }

        if (degree == 0){
            //set to optimal degree for disk with block size 4096
            //this will need to be calculated after we've decided exactly what variables to store

            //long offset
            //long numKeys
            //2t longs for children offsets
            //2t-1 longs for keys, ints for frequencies

            //long = 8 bytes, int = 4 bytes
            //Give some overhead -- say 84
            // 8 + 8 + 16t + 16t - 8 + 8t - 4 + 84 = 4096
            //40t + 88 = 4096
            //t = 100.2, round down to 100

            degree = 100;
        }

        if (k < 1 || k > 31){
            printUsage();
            return;
        }

        //build filename to store BTree in
        String filename = gbk + ".btree.data." + k + "." + degree;
        //open a RandomAccessFile
        RandomAccessFile file;
        try {
            file = new RandomAccessFile(filename, "rw");
        } catch (FileNotFoundException e){
            System.err.println("Couldn't create BTree data file " + filename);
            return;
        }

        //initialize cache, if used
        if (cacheOption == 1) {
            cache = new Cache<BTreeNode>(cacheSize);
        }

        //initialize BTree with null root
        BTree tree = new BTree(degree, file, null, cache);

        //initialize input parser
        InputParser parser = new InputParser(k);

        System.out.println("Inserting sequences...");
        //parse files and add them to the BTree
        try {
            parser.parseFile(gbk, tree);
        } catch (FileNotFoundException e){
            System.err.println("gbk file could not be found");
            printUsage();
            return;
        } catch (Exception e){
            System.err.println("No DNA sequence present in file");
        }

        //BTree has been written at this point
        //Record metadata
        System.out.println("Recording metadata...");
        RandomAccessFile meta;
        try {
            meta = new RandomAccessFile(filename + ".metadata", "rw");
            meta.writeLong(tree.getRoot().getOffset());
            meta.writeInt(degree);
        } catch (FileNotFoundException e){
            System.err.println("Could not create BTree metadata file");
            return;
        } catch (IOException e){
            System.err.println("Problems writing to BTree metadata file");
        }

        if (debug == 1) {
            //create dump file
            try {
                //RandomAccessFile dump = new RandomAccessFile("dump", "rw");
                PrintWriter dump = new PrintWriter("dump");
                tree.traverse(dump, parser, tree.getRoot(), tree.getRoot().getOffset());
                dump.flush();
                dump.close();
            } catch (FileNotFoundException e) {
                System.err.println("Problem creating dump file");
                return;
            } catch (IOException e) {
                System.err.println("Problem writing to dump file");
            }

        }

    }

}
