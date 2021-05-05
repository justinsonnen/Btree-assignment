****************
* Team Programming Project: Bioinformatics
* CS321
* 05/05/2018
* Bethany Weaver, Tyler French, Justin Sonnen
****************

OVERVIEW:

 One problem found in the field of Bioinformatics is managing a large set of data,
 such as a genome sequence.
 The purpose of this project is to parse and organize a large genome sequence and
 determine the frequency of different length sub-sequence to see if they are random 
 or if some sequences are more often found.  
 The chosen data structure for this assignment is a BTree.  
 BTree’s organize data in a sequentially ordered tree making it is possible to 
 iterate to a specific node location.  Also, the insert and look-up times are O(log n).  
 Most advantageous is that a BTree can be stored in a file so that it does not have to 
 use up a lot of RAM while managing a large amount of data.


INCLUDED FILES:

 GeneBankCreateTree.java - source file
 GeneBankSearch.java - source file
 BTree.java - source file
 BTreeNode.java - source file
 TreeObject.java - source file
 InputParser.java - source file
 Cache.java - source file
 README.md - this file


BUILDING AND RUNNING:

 From the directory containing the source files, compile the driver
 class to create a Gene-bank BTree with the command:

 $ javac GeneBankCreateTree.java

 This class has required command line arguments; in order, they are:

 0/1(no/with Cache): create BTree with or without utilizing a cache.
 Degree: the degree of BTree.  Choose 0 for an optimal degree.
 GBK File: the name of file containing DNA sequence.
 Sequence Length: the length of the sub-sequences to be stored,
                  must be between 1 and 31 (inclusive).
 Cache size:  the size of the cache being utilized.                 
 Debug Level: (Optional, will default to 0)
	       0 = Any diagnostic, help, or status messages will
	           be printed to the standard output stream.
	       1 = A dump file, containing the keys and frequency
		    will be created.

 Run the compiled class using the following command and your preferred arguments:

 $ java GeneBankCreateTree 1 2 sample.gbk 7 100 1



 Once the BTree is created the driver class to search the BTree can be
 compiled from the directory containing the source files with the command:

 $ javac GeneBankSearch.java

 This class also has required command line arguments; in order, they are:

 0/1(no/with Cache): search the BTree with or without utilizing a cache.
 BTree File: the file where the BTree is stored.
 Query File: the file containing sequences to search for.
 Cache size:  the size of the cache being utilized. 
 Debug Level: (Optional, will default to 0)
	       0 = Any diagnostic, help, or status messages will
	           be printed to the standard output stream.

 Run the compiled class using the following command and preferred arguments:

 $ java GeneBankSearch 1 sample.gbk.btree.data.2.7 queries.txt 100 0



PROGRAM DESIGN:

 This program has two driver classes, GeneBankCreateBTree.java and
 GeneBankSearch.java.  When a user runs GeneBankCreateBTree.java from the
 command-line they must pass an argument for the degree to establish the
 number of keys and children each node in the BTree can have.  The degree
 will be calculated if “0” is passed.   A function is used to combine the
 degree, as a variable, the children’s offset, the keys, frequency count, and
 overhead (containing additional useful data) into an equation equaling the
 block size.  The block size is assumed to be 4096 bytes.  Solving for the
 degree formulates the optimal degree value.
 The file containing the genome sequence is also passed by name at the
 command-line.  This file's sequence is parsed, ignoring all characters
 other than “ACGT”.  This sequence is divided into sub-sequences of
 length “k”, where “k” is passed in as a command-line argument, and is a
 length between 1 and 31, inclusive.  These sub-sequences will move ahead one
 step at a time and create a sequence of length “k”.  For example, if the
 sequence is “AGCTGACT” and k equals 3, the first four sub-sequences would be
 “AGC”, “GCT”, “CTG”, and “TGA”.  This functionality is found in the
 InputParser.java class.  Next, the sequence gets converted from a string to
 a long using a base four numeric convention that results in a binary
 sequence that is condensed using bitwise “shift” and “or” operations.
 Meanwhile, a BTree is initialized.  A file is created to store the BTree to
 disk using the RandomAccessFile Java class.  As the data is parsed it is
 organized into a BTree.  The BTree is stored as a binary data file to disk.
 The layout of the BTree file on disk is ordered as followed:  the offset 
 within the BTree file, the number of keys stored in the node, the offset 
 of the nodes children, and the keys that are stored in the node.  An additional
 metadata file is created, which stores the offset of the root node and
 the degree of the BTree.
 
 If a cache is utilized a linked-list of BTreeNodes is created with it's
 size first being passed as a command-line argument and then as a parameter
 of the linked-list.  When a call to insert a key is made, and the cache is
 is not empty, the cache is searched to find the key.  If it is found the 
 node containing the key is returned and the key's frequency is incremented.
 The node is then written to the file and moved to the front of the cache.

 Without the cache, running GeneBankCreateBTree with a test gbk file,
 degree 2, and sequence length 20 took 64.11 seconds.
 With a cache of size 100, the runtime with the same gbk file, sequence length,
 and degree took 66.311 seconds.  With cache size 500, it took 61.974 seconds.
 This shows that a sufficiently large cache will improve runtime, but a small
 cache may slow it down slightly.
 
 The debug level is the final command line argument, either “0” or ”1”, can be
 passed.  It is optional and defaults to “0”.  The default level
 results in any diagnostic messages, help, and status messages being printed
 on the standard error stream.
 When “1” is passed as the debug level, a text file named “dump” is written.
 The dump file contains the frequency and DNA string for its respective key,
 as it corresponds to the BTree as it would be read in an in-order traversal.

 The GeneBankSearch.java class can now be run.  This driver class is used to
 search the BTree file to identify matches between it's sub-sequences and the query
 sequences. There are three command line arguments.  The first is the BTree 
 file, in which the BTree is stored.  The second is the Query File, containing 
 the substrings being searched for.  The last argument is an optional debug 
 level that defaults to “0”.  The default prints the output of the queries to 
 the standard output stream, along with any diagnostic messages, help and 
 status messages.  It would be possible to include additional debug 
 functionality; however, it was not expected in this project.
 GeneBankSearch reads the data from the BTree using the RandomAccessFile.
 The query file is scanned, line by line, using the scanner class. The length
 of the sub-sequences in the query file are compared to the BTree sub-sequence
 lengths.  If they are not a match an error message is printed.
 If a cache is being used, the program first searches through each node in the
 cache for the desired key, before searching the BTree.
 The frequency of each key in the query file is printed to standard output.

 Running GeneBankSearch on the BTree created before without using a cache,
 using the provided query file query20, took 44.45 seconds.
 With a cache of size 100, the same search took 46.349 seconds.  When the
 cache size was increased to 500, this search took 55.424 seconds.
 In this case, we can see that the cache actually slowed down execution.
 This is likely due to the fact that the cache is searched linearly.

TESTING:

 A unit test suite was not written for this project, however functionality
 tests were performed after every update to ensure the program was still
 compiling.  The expected operation of individual methods was often tested by 
 printing information to the console for visual verification.
 
 In addition to visual tests such as print statements, the debugging mode was used 
 very thoroughly. Being able to observe the values inside nodes and how they were
 changing was very useful in solving problems of functionality. 



DISCUSSION:
 
 This was a challenging assignment. It took a great deal of understanding to be able
 to correctly implement all of the moving parts. However, through careful discussion 
 with one another, we reached an understanding and were able to complete the project.
 
 Overall, it was a good learning experience and helped us understand BTrees much better
 through a more applied and hands-on approach. Working as a team helped us not only
 learn how to use git, but also helped us practice working with others collaboratively.
