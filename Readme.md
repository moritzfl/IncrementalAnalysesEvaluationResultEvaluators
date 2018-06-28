# IncrementalAnalysesHelpers

This repository contains tools related to [IncrementalAnalysesInfrastructure](https://github.com/KernelHaven/IncrementalAnalysesInfrastructure) and [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation)

The tools included are:
- DiffGenerator
- QualityEvaluator
- PerformanceEvaluator


## DiffGenerator

This tool can be used to generate diff files representing every commit in a given range to a repository that you have checked out on your locally.

To launch it use:
```
java -jar DiffGenerator.jar
```

## ResultEvaluators

The Evaluation-tools (QualityEvaluator, PerformanceEvaluator) assume identical folder structuring to what the configuration and bash-scripts in [IncrementalAnalysesEvaluation](https://github.com/moritzfl/IncrementalAnalysesEvaluation) define. They also assumes a complete set of output-files within the result, time and log directory for both incremental and reference execution. 

### QualityEvaluator

Executing QualityEvaluator checks consistency of the results of the incremental analysis execution compared to the reference execution. There are two implemented modes for result evaluation.

Change-mode:
- The result for a diff file is marked as SAME, if both result files only include identical lines
- The result for a diff file is marked as EQUIVALENT, if the incremental result contains all lines that were modified compared to results for the previous diff file in the reference execution. However the incremental result must not contain any lines that are not present in the reference result for the same diff file.
- If the result is neither SAME nor EQUIVALENT, it is marked as DIFFERENT



Variability-mode:
- The result for a diff file is marked as SAME, if both result files only include identical lines *after* disregarding line-number information
- The result for a diff file is marked as EQUIVALENT, if the incremental result contains all variabillity-related lines that were modified compared to results for the previous diff file in the reference execution. However the incremental result must not contain any lines that are not present in the reference result for the same diff file.
- If the result is neither SAME nor EQUIVALENT, it is marked as DIFFERENT

The execution of the QualityEvaluator can be achieved through a command line call:

Change-mode (default)

```
java -jar QualityEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -c "/path/to/rootfolder_of_kernelhaven_execution"
java -jar QualityEvaluator.jar -change "/path/to/rootfolder_of_kernelhaven_execution"
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

Executing PerformanceEvaluator compares the performance of the one analysis execution with a reference execution. 


The execution of the PerformanceEvaluator can be achieved through a command line call:

```
java -jar PerformanceEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution"
```

In order to write the evaluation result to your filesystem, use ``> performance.log`` on Unix systems:

```
java -jar PerformanceEvaluator.jar "/path/to/rootfolder_of_kernelhaven_execution" > performance.log
```
