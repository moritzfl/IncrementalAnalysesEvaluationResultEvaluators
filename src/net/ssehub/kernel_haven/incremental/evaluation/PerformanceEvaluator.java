package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.util.Logger;

/**
 * The Class PerformanceEvaluator.
 */
public class PerformanceEvaluator {

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The Constant LOG_INCREMENTAL_DIR. */
	public static final Path LOG_INCREMENTAL_DIR = Paths.get("log/incremental");

	/** The Constant LOG_REFERENCE_DIR. */
	public static final Path LOG_REFERENCE_DIR = Paths.get("log/reference");

	/** The base dir. */
	private Path baseDir;

	/**
	 * Instantiates a new performance evaluator.
	 *
	 * @param baseDir
	 *            the base dir
	 */
	public PerformanceEvaluator(Path baseDir) {
		super();
		this.baseDir = baseDir;
	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 */
	public static void main(String args[]) {
		Path baseDir = null;
		// Parse arguments
		if (args.length == 1) {
			baseDir = Paths.get(args[0]);
		} else if (args.length > 1) {
			LOGGER.logError("too many arguments ");
			System.exit(1);
		} else {
			LOGGER.logError("You need to provide the path to the execution results.");
			System.exit(1);
		}

		PerformanceEvaluator evaluator = new PerformanceEvaluator(baseDir);

		LOGGER.logInfo("Extracting times from incremental");
		Map<String, PerformanceResult> incrementalResults = evaluator.generateResults(LOG_INCREMENTAL_DIR);
		LOGGER.logInfo("Extracting times from reference");
		Map<String, PerformanceResult> referenceResults = evaluator.generateResults(LOG_REFERENCE_DIR);

		LOGGER.logInfo("Calculating times for incremental results");
		logGenericDurations(incrementalResults);

		LOGGER.logInfo("Calculating times for reference results");
		logGenericDurations(referenceResults);


		LOGGER.logInfo("Execution times for every incremental iteration:");
		logExecutionTimes(incrementalResults);

		LOGGER.logInfo("Execution times for every reference iteration:");
		logExecutionTimes(referenceResults);

		LOGGER.logInfo("Logging data for partial analyses in incremental:");
		logPartialExecutionData(incrementalResults);

		LOGGER.logInfo("Logging data for non partial analyses in incremental:");
		logNonPartialExecutionData(incrementalResults);

		
		LOGGER.logInfo("Calculating comparison between incremental and reference");
		logWhichOneWasFaster(incrementalResults, referenceResults);
	}

	private static void logNonPartialExecutionData(Map<String, PerformanceResult> results) {
		List<String> keySet = new ArrayList<String>(results.keySet());
		Collections.sort(keySet);

		long shorterThan60sec = 0;
		long longestNonPartial = 0;
		long accumulatedNonPartial = 0;
		long totalNonPartialCount = 0;
		long shortestNonPartial = -1;

		for (String key : keySet) {
			PerformanceResult performanceResult = results.get(key);
			long currentDuration = ChronoUnit.SECONDS.between(performanceResult.getStartTime(),
					performanceResult.getEndTime());

			if (!performanceResult.isPartialAnalysis()) {
				totalNonPartialCount++;
				accumulatedNonPartial += currentDuration;
				if (currentDuration > longestNonPartial) {
					longestNonPartial = currentDuration;
				}
				if (shortestNonPartial == -1 || currentDuration < shortestNonPartial) {
					shortestNonPartial = currentDuration;
				}

				if (currentDuration < 60) {
					shorterThan60sec++;
				}
			}
		}

		double avgPartialDuration = ((double) accumulatedNonPartial) / ((double) totalNonPartialCount);

		LOGGER.logInfo("NonPartialAccumulatedDuration:" + accumulatedNonPartial + "s NonPartialAnalysisCount: "
				+ totalNonPartialCount + " NonPartialAnalysesShorterThan60s: " + shorterThan60sec
				+ " AverageNonPartialDuration: " + avgPartialDuration + "s LongestNonPartialDuration: "
				+ longestNonPartial + "s ShortestNonPartialDuration:" + shortestNonPartial + "s");

	}

	private static void logPartialExecutionData(Map<String, PerformanceResult> results) {
		List<String> keySet = new ArrayList<String>(results.keySet());
		Collections.sort(keySet);

		long longerThan60sec = 0;
		long longestPartial = 0;
		long accumulatdPartial = 0;
		long totalPartialCount = 0;

		for (String key : keySet) {
			PerformanceResult performanceResult = results.get(key);
			long currentDuration = ChronoUnit.SECONDS.between(performanceResult.getStartTime(),
					performanceResult.getEndTime());

			if (performanceResult.isPartialAnalysis()) {
				totalPartialCount++;
				accumulatdPartial += currentDuration;
				if (currentDuration > longestPartial) {
					longestPartial = currentDuration;
				}

				if (currentDuration > 60) {
					longerThan60sec++;
				}
			}
		}

		double avgPartialDuration = ((double) accumulatdPartial) / ((double) totalPartialCount);

		LOGGER.logInfo("PartialAccumulatedDuration:" + accumulatdPartial + "s PartialAnalysisCount: "
				+ totalPartialCount + " PartialAnalysesLongerThan60s: " + longerThan60sec + " AveragePartialDuration: "
				+ avgPartialDuration + "s LongestPartialDuration: " + longestPartial + "s");

	}

	private static void logExecutionTimes(Map<String, PerformanceResult> results) {
		StringJoiner times = new StringJoiner(";");
		List<String> keySet = new ArrayList<String>(results.keySet());
		Collections.sort(keySet);
		for (String key : keySet) {
			PerformanceResult performanceResult = results.get(key);
			if (performanceResult == null) {
				LOGGER.logError("Performance result for " + key + " was null" );
			}
			long currentDuration = ChronoUnit.SECONDS.between(performanceResult.getStartTime(),
					performanceResult.getEndTime());
			times.add(Long.toString(currentDuration));
		}

		LOGGER.logInfo("Execution times: " + times.toString());
	}

	private static long getTimeInSeconds(LocalDateTime start, LocalDateTime end) {
		long duration = 0;
		if (start != null && end != null && start.isBefore(end)) {
			duration = ChronoUnit.SECONDS.between(start, end);
		}
		return duration;
	}

	/**
	 * Log generic durations.
	 *
	 * @param results
	 *            the results
	 */
	private static void logGenericDurations(Map<String, PerformanceResult> results) {

		long accumulatedDuration = 0;
		List<String> keySet = new ArrayList<String>(results.keySet());
		Collections.sort(keySet);

		long accumulatedSetupDuration = 0;
		long accumulatedExtractionDuration = 0;
		long accumulatedOverlap = 0;
		long accumulatedAnalysisDuration = 0;
		long accumulatedTimeBetweenExtractionAndAnalysis = 0;
		long accumulatedPreparationTime = 0;

		int numberOfValues = keySet.size();
		long maxDuration = 0;
		long minDuration = -1;

		PerformanceResult maxResult = null;
		PerformanceResult minResult = null;

		for (String key : keySet) {
			PerformanceResult incrResult = results.get(key);
			long currentDuration = getTimeInSeconds(incrResult.getStartTime(), incrResult.getEndTime());
			long currentSetupDuration = getTimeInSeconds(incrResult.getStartTime(),
					incrResult.getStartExtractionPhase());
			long currentPreparationDuration = getTimeInSeconds(incrResult.getStartPreparationPhase(),
					incrResult.getEndPreparationPhase());

			long currentExtractionDuration = getTimeInSeconds(incrResult.getStartExtractionPhase(),
					incrResult.getEndExtractionPhase());
			// TODO: only if extraction starts after analysis
			long currentTimeBetweenExtractionAndAnalysis = getTimeInSeconds(incrResult.getEndExtractionPhase(),
					incrResult.getStartAnalysisPhase());

			long currentAnalysisTime = getTimeInSeconds(incrResult.getStartAnalysisPhase(),
					incrResult.getEndAnalysisPhase());

			long currentOverlap = getTimeInSeconds(incrResult.getStartAnalysisPhase(),
					incrResult.getEndExtractionPhase());

			accumulatedSetupDuration += currentSetupDuration;
			accumulatedExtractionDuration += currentExtractionDuration;
			accumulatedOverlap += currentOverlap;
			accumulatedAnalysisDuration += currentAnalysisTime;
			accumulatedTimeBetweenExtractionAndAnalysis += currentTimeBetweenExtractionAndAnalysis;
			accumulatedPreparationTime += currentPreparationDuration;
			accumulatedDuration += currentDuration;
			if (maxDuration < currentDuration) {
				maxDuration = currentDuration;
				maxResult = incrResult;
			}

			if (minDuration == -1 || minDuration > currentDuration) {
				minDuration = currentDuration;
				minResult = incrResult;
			}
		}

		long averageDuration = accumulatedDuration / numberOfValues;
		LOGGER.logInfo(
				"Average duration: " + (int) Math.floor(averageDuration / 60) + "min " + averageDuration % 60 + "s");
		LOGGER.logInfo("Maximum duration: " + (int) Math.floor(maxDuration / 60) + "min " + maxDuration % 60 + "s  for "
				+ maxResult.getDiffFileName());
		LOGGER.logInfo("Minimum duration: " + (int) Math.floor(minDuration / 60) + "min " + minDuration % 60 + "s  for "
				+ minResult.getDiffFileName());
		LOGGER.logInfo("Accumulated duration: " + (int) Math.floor(accumulatedDuration / 60) + "min "
				+ accumulatedDuration % 60 + "s");

		LOGGER.logInfo("Accumulated values: setup=" + accumulatedSetupDuration + " preparation="
				+ accumulatedPreparationTime + " extraction=" + accumulatedExtractionDuration + " analysis="
				+ accumulatedAnalysisDuration + " overlap=" + accumulatedOverlap + " post-extraction="
				+ accumulatedTimeBetweenExtractionAndAnalysis);

		LOGGER.logInfo("Average values: setup=" + ((double) accumulatedSetupDuration) / ((double) numberOfValues)
				+ " preparation=" + ((double) accumulatedPreparationTime) / ((double) numberOfValues) + " extraction="
				+ ((double) accumulatedExtractionDuration) / ((double) numberOfValues) + " analysis="
				+ ((double) accumulatedAnalysisDuration) / ((double) numberOfValues) + " overlap="
				+ ((double) accumulatedOverlap) / ((double) numberOfValues) + " post-extraction="
				+ ((double) accumulatedTimeBetweenExtractionAndAnalysis) / ((double) numberOfValues));

	}

	private static double getDeviationInPercent(long thisValue, long thatValue) {
		double thisValueDouble = thisValue;
		double thatValueDouble = thatValue;
		double percent = 100d - thisValueDouble / thatValueDouble * 100d;
		return percent;
	}

	/**
	 * Log which one was faster.
	 *
	 * @param incrementalResults
	 *            the incremental results
	 * @param referenceResults
	 *            the reference results
	 */
	private static void logWhichOneWasFaster(Map<String, PerformanceResult> incrementalResults,
			Map<String, PerformanceResult> referenceResults) {

		int refFaster = 0;
		int incrFaster = 0;
		StringJoiner fasterReference = new StringJoiner(", ");
		StringJoiner fasterIncr = new StringJoiner(", ");
		int fullAnalysisCount = 0;
		int fullAnalysisRefFasterThanIncr = 0;

		for (String key : referenceResults.keySet()) {
			PerformanceResult incrResult = incrementalResults.get(key);
			PerformanceResult refResult = referenceResults.get(key);
			long refDuration = ChronoUnit.SECONDS.between(refResult.getStartTime(), refResult.getEndTime());
			long incrDuration = ChronoUnit.SECONDS.between(incrResult.getStartTime(), incrResult.getEndTime());

			if (refDuration < incrDuration) {
				fasterReference.add(key + "(difference: " + (incrDuration - refDuration) + ", "
						+ Double.toString(getDeviationInPercent(refDuration, incrDuration)) + "% )");
				refFaster++;
			} else if (incrDuration < refDuration) {
				fasterIncr.add(key + "(difference: " + (refDuration - incrDuration) + ", "
						+ Double.toString(getDeviationInPercent(incrDuration, refDuration)) + "% )");
				incrFaster++;
			}

			if (!refResult.isPartialAnalysis()) {
				fullAnalysisCount++;

				if (incrDuration > refDuration) {
					fullAnalysisRefFasterThanIncr++;
				}
			}

		}

		LOGGER.logInfo("Number of times that reference was faster: " + refFaster);
		LOGGER.logInfo("Diffs where reference was faster: " + fasterReference);
		LOGGER.logInfo("Number of times that incremental was faster: " + incrFaster);
		LOGGER.logInfo("Diffs where incemental was faster: " + fasterIncr);
		LOGGER.logInfo("Number of executions: " + referenceResults.keySet().size());
		LOGGER.logInfo("Number of full analyses done by ref (only makes sense when comparing 2 incremental analysis resuls) where ref was faster than incr" + fullAnalysisRefFasterThanIncr + "/" + fullAnalysisCount);
	}

	/**
	 * Generate results.
	 *
	 * @param relativeLogDir
	 *            the relative log dir
	 * @return the map
	 */
	private Map<String, PerformanceResult> generateResults(Path relativeLogDir) {
		Map<String, PerformanceResult> resultMap = new HashMap<String, PerformanceResult>();
		File[] files = baseDir.resolve(relativeLogDir).toFile().listFiles();
		Arrays.sort(files);
		for (File file : files) {
			PerformanceResult result = new PerformanceResult(getDiffFileName(file));
			try {
				extractTimes(file, result);
			} catch (IOException e) {
				LOGGER.logException("Could not generate result", e);
			}
			resultMap.put(result.getDiffFileName(), result);
		}
		return resultMap;
	}

	/**
	 * Gets the diff file name.
	 *
	 * @param file
	 *            the file
	 * @return the diff file name
	 */
	public String getDiffFileName(File file) {
		return file.getName().substring("log-".length(), file.getName().length() - ".log".length());
	}

	/**
	 * Extract date from log line.
	 *
	 * @param logLine
	 *            the log line
	 * @return the local date time
	 */
	private LocalDateTime extractDateFromLogLine(String logLine) {
		Pattern pattern = Pattern.compile("\\[(\\d{4}-\\d{2}-\\d{2}\\s{1}\\d{2}:\\d{2}:\\d{2})\\]");
		Matcher matcher = pattern.matcher(logLine);

		LocalDateTime time = null;

		if (matcher.find()) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

			String timeString = matcher.group(1);
			time = LocalDateTime.parse(timeString, formatter);
		}

		return time;
	}

	/**
	 * Extract times.
	 *
	 * @param logFile
	 *            the log file
	 * @param result
	 *            the result
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private void extractTimes(File logFile, PerformanceResult result) throws IOException {
		LOGGER.logInfo("Extracting times for " + logFile.getName());
		try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
			String currentLine = br.readLine();
			LocalDateTime startTime = extractDateFromLogLine(currentLine);
			LocalDateTime currentTime = startTime;
			LocalDateTime startPreparationPhase = null;
			LocalDateTime finishPreparationPhase = null;
			LocalDateTime startExtractionPhase = null;
			LocalDateTime endExtractionPhase = null;
			LocalDateTime startAnalysisPhase = null;
			LocalDateTime endAnalysisPhase = null;
			LocalDateTime endTime = null;
			boolean partial = false;

			for (String nextLine; (nextLine = br.readLine()) != null;) {
				// Update the time to always reflect the most recent timestamp
				LocalDateTime timeFromCurrentLine = extractDateFromLogLine(currentLine);
				if (timeFromCurrentLine != null) {
					currentTime = timeFromCurrentLine;
				}

				if (currentLine
						.contains("IncrementalDeadCodeAnalysis will run on the updated parts of the code-model only")) {
					partial = true;
				} else if (currentLine.contains("[Setup] Running preparation")) {
					startPreparationPhase = currentTime;
				} else if (currentLine.contains("IncrementalPreparation duration:")) {
					finishPreparationPhase = currentTime;
				} else if (startExtractionPhase == null && currentLine.contains("ExtractorThread]")) {
					startExtractionPhase = currentTime;
				} else if (currentLine.contains("ExtractorThread] All threads done")) {
					endExtractionPhase = currentTime;
				} else if (startAnalysisPhase == null
						&& (currentLine.contains("[info   ] [OrderPreservingParallelizer-Worker")
								|| currentLine.contains("DeadCodeFinder] Couldn't get models"))) {
					startAnalysisPhase = currentTime;
				} else if (currentLine.contains("[info   ] [Setup] Analysis has finished")) {
					endAnalysisPhase = currentTime;
				}
				if (currentLine.matches(".*Analysis component .* done")) {
					Pattern componentPattern = Pattern.compile(".*Analysis component (.*) done");
					Matcher componentMatcher = componentPattern.matcher(currentLine);
					componentMatcher.find();
					String finishedComponent = componentMatcher.group(1);
					Pattern timePattern = Pattern.compile(".\\s*Execution took (\\d*)");
					Matcher timeMatcher = timePattern.matcher(nextLine);
					timeMatcher.find();
					long componentTime = Long.parseLong(timeMatcher.group(1));
					result.addAnalysisComponentTime(finishedComponent, componentTime);
				}

				currentLine = nextLine;
			}
			endTime = currentTime;
			result.setEndAnalysisPhase(endAnalysisPhase);
			result.setStartAnalysisPhase(startAnalysisPhase);
			result.setEndExtractionPhase(endExtractionPhase);
			result.setStartPreparationPhase(startPreparationPhase);
			result.setStartExtractionPhase(startExtractionPhase);
			result.setEndPreparationPhase(finishPreparationPhase);
			result.setEndTime(endTime);
			result.setStartTime(startTime);
			result.setPartialAnalysis(partial);

		}

	}

}
