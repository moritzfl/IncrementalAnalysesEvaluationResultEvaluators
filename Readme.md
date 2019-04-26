# IncrementalAnalysesHelpers

This repository contains tools related to [IncrementalAnalysesInfrastructure](https://github.com/KernelHaven/IncrementalAnalysesInfrastructure) and [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation)

The tools included are:
- DiffGenerator
- QualityEvaluator
- PerformanceEvaluator


## DiffGenerator

This tool can be used to generate diff files representing every commit in a given range to a repository that you have checked out locally.

To launch it use:
```
java -jar DiffGenerator.jar
```

## ResultEvaluators

The evaluation tools (QualityEvaluator, PerformanceEvaluator) assume identical folder structuring to what the configuration and bash-scripts in [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation) define. They also assume a complete set of output-files within the result, time and log directory for both incremental and reference execution. 

### QualityEvaluator

Executing QualityEvaluator checks consistency of the results of the incremental analysis execution compared to the reference execution. There are three implemented modes for result evaluation. Note that for our publication we do not solely rely on this automated consistency checks but also perform additional manual checks.

#### Change-mode:
- The result for a diff file is marked as SAME, if both result files only include identical lines
- The result for a diff file is marked as EQUIVALENT, if the incremental result contains all lines that were modified compared to the results for the previous diff file in the reference execution. However the incremental result must not contain any lines that are not present in the reference result for the same diff file.
- If the result is neither SAME nor EQUIVALENT, it is marked as DIFFERENT

*Rationale: A configuration that considers changes of any kind to be relevant for the analysis results in the processing of all of those changes. Therefore we can expect the result to contain every new entry. The result may however contain more than only the new entries. This is because a change to a file does not necessarily modify the code blocks within the file. Furthermore a change to build or variability files results in a complete analysis that identifies dead code blocks within the entire code model.*

#### Block-Change-mode:
- The result for a diff file is marked as SAME, if both result files only include identical lines
- The result for a diff file is marked as EQUIVALENT, if the incremental result contains all lines that were modified compared to the results for the previous result in the reference execution. However the incremental result must not contain any lines that are not present in the reference result for the same diff file. Entries where only the line number was changed are ignored.
- If the result is neither SAME nor EQUIVALENT, it is marked as DIFFERENT

*Rationale: A configuration that considers changes to block conditions will find every new dead code block. However, it will not output blocks where only the line number has changed as this could be due to the insertion of a comment or line of code within a block. In our perspective changes to lines of code within a code block are irrelevant as we only look at the change of block conditions.*

#### Variability-mode:
- The result for a diff file is marked as SAME, if both result files only include identical lines
- The result for a diff file is marked as EQUIVALENT, if the incremental result contains all *variabillity-related* entries that were modified compared to the results for the previous diff file in the reference execution. However the incremental result must not contain any lines that are not present in the reference result for the same diff file. Entries where only the line number was changed are ignored.
- If the result is neither SAME nor EQUIVALENT, it is marked as DIFFERENT

*Rationale: A configuration that only considers changes to blocks that depend on variables of the variability model will find every new dead code block that is related to variability. In our evaluation on the Linux-Kernel this means that we only consider blocks with a CONFIG_ variable to be relevant results. Similar to the Block-Change-mode, it will not output blocks where only the line number has changed.*


#### Execution of QualityEvaluator
The execution of the QualityEvaluator can be achieved through a command line call:

Change-mode (default)

```
java -jar QualityEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -c "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -change "/path/to/rootfolder_of_kernelhaven_execution"
```

Block-Change-mode

```
java -jar QualityEvaluator.jar -bc "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -blockchange "/path/to/rootfolder_of_kernelhaven_execution"
```

Variability-mode

```
java -jar QualityEvaluator.jar -v "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -variability "/path/to/rootfolder_of_kernelhaven_execution"
```

In order to write the evaluation result to your filesystem, use ``> quality.log`` on Unix systems:

```
java -jar QualityEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution" > quality.log
```

### PerformanceEvaluator

Executing PerformanceEvaluator compares the performance of the one analysis execution with a reference execution. As the performance analysis is performed by extracting information from log files which get rather large, the evaluation process takes a couple of minutes.


The execution of the PerformanceEvaluator can be achieved through a command line call:

```
java -jar PerformanceEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution"
```

In order to write the evaluation result to your filesystem, use ``> performance.log`` on Unix systems:

```
java -jar PerformanceEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution" > performance.log
```
