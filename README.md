Spark Hydra	
	This program is an application for performing proteomic search on a spark cluster.  The program contains multiple search algorithms but most work has been done on a version of comet. The code depends on another GitHub Project  - Spark Accumulators primarily to provide profiling support. The current version of the code depends on a Spark 1.5 cluster. 
	The command to run depends on two files - one describing the cluster and a second describing the proteomic search independent of the cluster. The command line looks line the following:
spark-submit --class com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer SteveSpark.jar ~/SparkClusterEupaG.properties input_searchGUI_scan10000.xml
Where
spark-submit - standard spark command
--class com.lordjoe.distributed.hydra.comet_spark.SparkCometScanScorer  define the main class
HydraSpark.jar  define the jar file
~/SparkClusterEupaG.properties file decsribing the cluster
input_searchGUI_scan10000.xml file describing the search job

Cluster descriptive file
The location and access to the cluster are described in the default config cluster. This file 
sets a series of spark properties. Properties starting with com.lordjoe are hydra specific 
‘A couple of properties of special interest are 
com.lordjoe.distributed.PathPrepend=hdfs://daas/steve/eg3/
PathPrepend is a string prepended to file names to reference the data in hdfs. Because the files for a problem are frequently placed in a directory which is mapped or copied to hdfs this text allows the file describing various files such as spectra and database files to be described without reference to the hdfs implementation. Thus a file such as Tandem.xml would be accessed by spark as hdfs://daas/steve/eg3/Tandem.xml .
 

#
# These are properties to be set on the spark cluster
#
#
# prepend to path 
com.lordjoe.distributed.PathPrepend=hdfs://daas/steve/eg3/

spark.mesos.coarse=true
spark.mesos.executor.memoryOverhead=3128


com.lordjoe.distributed.hydra.BypassScoring=true
com.lordjoe.distributed.hydra.KeepBinStatistics=true
com.lordjoe.distributed.hydra.doGCAfterBin=false

# give executors more memory
spark.executor.memory=12g

# Spark shuffle properties
spark.shuffle.spill=false
spark.shuffle.memoryFraction=0.4
spark.shuffle.consolidateFiles=true
spark.shuffle.file.buffer.kb=1024
spark.reducer.maxMbInFlight=128

spark.storage.memoryFraction=0.3
spark.shuffle.manager=sort
spark.default.parallelism=360
spark.hadoop.validateOutputSpecs=false

#spark.rdd.compress=true
#spark.shuffle.compress=true
spark.shuffle.spill.compress=true
spark.io.compression.codec=lz4
spark.shuffle.sort.bypassMergeThreshold=100

# try to divide the problem into this many partitions
com.lordjoe.distributed.number_partitions=360


Search descriptive file
The location and access to the cluster are described in the default config cluster. This file
follows the format of input for XTandem. 
list path, default parameters is the reference for a file holding common parameters not overtidden in the default file - note the path is absolute do the prepend string described above will not be added
list path, taxonomy information is a file describing the database as specified by XTandem 
spectrum, path is a file containing spectra
output, path is a file the resultant pep.xml
Scoring, algorithm is either comet or xtandem




<?xml version="1.0"?>
<bioml>
	<note type="input" label="list path, default parameters">hdfs://daas/promec/eupa2015/parameters_searchGUI.xml</note>
	<note type="input" label="list path, taxonomy information">taxonomy_searchGUI.xml</note>
	<!-- <note type="input" label="protein, taxon">all</note> -->
	<note type="input" label="protein, taxon">uniprot_sprot_concatenated_target_decoy.fasta</note>
	<note type="input" label="spectrum, path">scan1000000.mzXML</note>
	<note type="input" label="output, path">scan1000000.out.xml</note>
	  <note type="input" label="scoring, algorithm">comet</note>
</bioml>



