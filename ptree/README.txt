PTree: Pattern-based, Stochastic Search for Maximum Parsimony Phylogenies.

REQUIREMENTS

OS:
PTree runs on all major platforms:
Linux 32/64bit (tested on Debian 64bit)
OSX (tested on Lion 10.7.3) 
Windows (tested on Windows XP 32bit and Windows Vista 64bit)

Software:
Java 1.6 (including JNI (Java Native Interface) support which may require corresponding Java SDK)
(Java 1.6 is recommended however there are no known issues with Java 1.7)

Hardware:
~4GB of the main memory (Java heap size) for datasets containing up to  ~4,000 sequences
~8GB of the main memory (Java heap size) for datasets containing up to ~10,000 sequences

INSTALL
Copy the unpacked "release" folder in your favorite program folder.


RUN
Run PTree from the "release" folder using one of the shell/batch scripts depending on your operating
system from the command line.

On Linux 64bit run: 
sh ptreeLinux64.sh input_file_phylip output_file_newick configuration_file

On Linux 32bit run: 
sh ptreeLinux32.sh input_file_phylip output_file_newick configuration_file

On OSX (32/64bit compatible with Lion) run: 
sh ptreeOSX.sh input_file_phylip output_file_newick configuration_file

On Windows 32bit run:
ptreeWin32.bat input_file_phylip output_file_newick configuration_file

On Windows 64bit run:
ptreeWin64.bat input_file_phylip output_file_newick configuration_file


INPUT FILE
The input file is in the PHYLIP format (http://www.phylo.org/tools/phylip.html). 

First line contain two numbers: 
number of the sequences, length of the sequences
The second and the following lines contain:
sequence identifier, sequence

One input file can contain several datasets.

Two sample input files are in directory in the "release" directory in /test/input/
Input file "arb_125.phy" contain one dataset with 125 sequences
and file "all_seq30_40.phylip" contain 30 datasets where each contain 40 sequences.


OUTPUT FILE
The output file contain a tree in the newick format (http://en.wikipedia.org/wiki/Newick_format)
for each dataset on a separate line. The order of the trees in the output file correspond to the
order of the sequences in the input file.

To visualize the resulting tree, you can open the output newick file using, e.g.: 
FigTree which can be downloaded from: http://tree.bio.ed.ac.uk/software/figtree/


CONFIGURATION FILE
All parameters of PTree are specified in one configuration file in the XML format.
One sample configuration file "config.xml" is in the "release" directory (see comments).


JAVA VIRTUAL MACHINE HEAP SIZE
The maximum heap size of the Java VM is set by default to:
4096MB for Linux 64bit and OSX
1024MB for Linux 32bit and Windows 32/64bit

To change this settings, please edit the corresponding shell/batch script in the release directory
and change the "-Xmx" parameter (e.g. parameter "-Xmx4096m " in the "ptreeLinux64.sh" script
means that the Java VM can use up to 4096MB of the memory for its heap if you run this script).


EXAMPLE
To test PTree on Linux 64bit, you can run the following command from the "release" directory:

sh ptreeLinux64.sh ./test/input/arb_125.phy ./test/output/arb_125.newick config.xml

You can then inspect the resulting output file "./test/output/arb_125.newick" using FigTree.


RELEASE DIRECTORY STRUCTURE
LICENSE.txt - license
config.xml - PTree configuration file
ptreeLinux32.sh - shell script to run PTree on Linux 32bit from the command line
ptreeLinux64.sh - shell script to run PTree on Linux 64bit from the command line
ptreeOSX.sh - shell script to run PTree on OSX (Lion compatible) from the command line
ptreeWin32.bat - batch script to run PTree on Windows 32bit from the command line
ptreeWin64.bat - batch script to run PTree on Windows 64bit from the command line
test/input/arb_125.phy - sample phylip input file containing one dataset (125 sequences)
test/input/all_seq30_40.phylip - sample phylip input file containing 30 datasets (40 sequences each)
test/output - empty output folder 
log - contain one log file for each run of PTree
resources - contain all resources (libraries) of PTree
resources/ptree.jar - executable PTree JAR file
resources/lib - compiled libraries that implement the relaxed NJ algorithm for all major platforms