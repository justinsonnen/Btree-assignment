import java.io.IOException;
import java.io.RandomAccessFile;

public class InputParser {

    private int k;

    /**
     * Constructor
     * @param length --length of subsequences to parse (int)
     */
    public InputParser(int length){
        if (length <= 0){
            throw new IllegalArgumentException("Length must be greater than 0");
        }
        k = length;
    }

    /**
     * @param filename -- name of file to parse (String)
     * @param tree -- BTree to insert binary sequences into
     * @throws IOException
     */
    public void parseFile(String filename, BTree tree) throws IOException{
        RandomAccessFile file = new RandomAccessFile(filename, "r");
        parse(file, 0, tree);
    }

    /**
     * Helper method for parseFile
     * @param file --RandomAccessFile to parse
     * @param offset --offset to begin parsing at (0 for first call)
     * @param tree --BTree to store keys in
     * @throws IOException
     */
    private void parse(RandomAccessFile file, long offset, BTree tree) throws IOException {
        file.seek(offset);
        while (file.getFilePointer() != file.length()){
            if (file.readLine().equals("ORIGIN")){  //found starting point
                break;
            }
        }
        if (file.getFilePointer() == file.length()){    //reached end of file without finding ORIGIN
            return;
        }
        String sequence = "";
        while (file.getFilePointer() != file.length()){ //Until we reach end of file
            String next = file.readLine(); //read a string
            if (next.equals("//")){
                break;
            }
            next = next.replaceAll("\\d", "");  //remove all numbers
            next = next.replaceAll(" ", "");    //remove all spaces
            sequence += next;
        }
        int numValues = sequence.length() - k + 1;
        if (numValues < 0){
            return;
        }

        for (int i = 0; i < numValues; i++){
            String subsequence = sequence.substring(i, i+k);
            if (subsequence.contains("n")){
                continue;   //skip all subsequences that include an n, space, or digit
            }
            long binary = stringToBinary(subsequence);

            // If we are using the cache, we must first use the cache to do the operation.
            tree.insert(binary);
        }
        //look for another ORIGIN
        parse(file, file.getFilePointer(), tree);
    }

    /**
     * Converts a string of 'a', 'c', 'g', 't' to a binary sequence
     * Any character not in the four above is ignored
     * @param sequence (string)
     * @return binary sequence
     */
    public long stringToBinary(String sequence){
        long binary = 0;
        for (int i = 0; i < sequence.length(); i++){
            int temp;
            switch(sequence.charAt(i)){
                case 'a':
                case 'A': temp = 0; break;
                case 'c':
                case 'C': temp = 1; break;
                case 'g':
                case 'G': temp = 2; break;
                case 't':
                case 'T': temp = 3; break;
                default: continue; //if char isn't a, c, g, or t, just move on
            }
            binary = binary << 2;
            binary = binary | temp;
        }
        return binary;
    }

    /**
     * Checks if a character is a numeric digit
     * @param c -- char to check
     * @return true if digit, false otherwise
     */
    private boolean isNumber(char c){
        String numbers = "0123456789";
        for (int i = 0; i < numbers.length(); i++){
            if (numbers.charAt(i) ==c ){
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a binary sequence to a string
     * @param key --long
     * @return string composed of 'a', 'c', 'g', and 't'
     */
    public String longToSequence(long key){
        String result = "";
        char[] dna = {'a', 'c', 'g', 't'};
        for (int i = 0; i < k; i++){
            long temp = key;
            temp = temp >>(2*i);
            temp = temp & 0b11;
            result = dna[(int)temp] + result;
        }
        return result;
    }

}
