package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import net.ssehub.kernel_haven.util.Logger;

// TODO: Auto-generated Javadoc
/**
 * The Class Evaluator.
 */
public class QualityEvaluator {

	/** The Constant LOG_INCREMENTAL_DIR. */
	public static final Path LOG_INCREMENTAL_DIR = Paths.get("log/incremental");

	/** The Constant LOG_REFERENCE_DIR. */
	public static final Path LOG_REFERENCE_DIR = Paths.get("log/reference");

	/** The Constant RESULTS_INCREMENTAL_DIR. */
	public static final Path RESULTS_INCREMENTAL_DIR = Paths.get("output/incremental");

	/** The Constant RESULTS_REFERENCE_DIR. */
	public static final Path RESULTS_REFERENCE_DIR = Paths.get("output/reference");

	/** The base dir. */
	private Path baseDir;

	/** The incremental results. */
	private Map<String, QualityResult> incrementalResults = new HashMap<String, QualityResult>();

	/** The reference results. */
	private Map<String, QualityResult> referenceResults = new HashMap<String, QualityResult>();

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The variability evaluation mode. */
	private boolean variabilityEvaluationMode;

	/**
	 * Instantiates a new evaluator.
	 *
	 * @param variabilityEvaluationMode
	 *            the variability evaluation mode
	 * @param path
	 *            the path
	 */
	public QualityEvaluator(boolean variabilityEvaluationMode, Path path) {
		this.variabilityEvaluationMode = variabilityEvaluationMode;
		this.baseDir = path;

	}

	/**
	 * The main method.
	 *
	 * @param args
	 *            the arguments
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public static void main(String[] args) throws IOException {
		Path baseDir = null;
		boolean variabiltyMode = false;

		// Parse arguments
		if (args.length == 1) {
			baseDir = Paths.get(args[0]);
		} else if (args.length == 2) {
			baseDir = Paths.get(args[1]);
			if (args[0].equals("-variability") || args[0].equals("-v")) {
				variabiltyMode = true;
			} else if (!(args[0].equals("-change") || args[0].equals("-c"))) {
				LOGGER.logError("unknown option " + args[0]);
				System.exit(1);
			}
		}

		LOGGER.logInfo("Working on directory \"" + baseDir + "\". VariabilityMode=" + variabiltyMode + ".");

		if (baseDir != null && !baseDir.toFile().exists()) {
			LOGGER.logError("Directory \"" + baseDir + "\" does not exist!");
			System.exit(1);
		}

		if (baseDir != null) {
			QualityEvaluator evaluator = new QualityEvaluator(variabiltyMode, baseDir);
			List<String> extractedDiffFilenames = evaluator.extractDiffFilenamesFromReferenceResults();
			Collections.sort(extractedDiffFilenames);

			for (int i = 0; i < extractedDiffFilenames.size(); i++) {
				if (i == 0) {
					evaluator.compareForInputDiffName(extractedDiffFilenames.get(i), null);

				} else {
					evaluator.compareForInputDiffName(extractedDiffFilenames.get(i), extractedDiffFilenames.get(i - 1));
				}

			}
		}

	}

	/**
	 * Extract diff filenames from reference results.
	 *
	 * @return the list
	 */
	public List<String> extractDiffFilenamesFromReferenceResults() {
		List<String> diffFileNames = new ArrayList<String>();
		for (File file : baseDir.resolve(RESULTS_REFERENCE_DIR).toFile().listFiles()) {
			diffFileNames.add(file.getName().substring("output-".length(), file.getName().length() - ".csv".length()));
		}

		return diffFileNames;
	}

	/**
	 * Compare for input diff name.
	 *
	 * @param diffFileName
	 *            the diff file name
	 * @param previousDiffFileName
	 *            the previous diff file name
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void compareForInputDiffName(String diffFileName, String previousDiffFileName) throws IOException {

		QualityResult referenceResult = new QualityResult(diffFileName);
		QualityResult incrementalResult = new QualityResult(diffFileName);

		File previousReferenceOutputFile = null;

		if (previousDiffFileName != null) {
			previousReferenceOutputFile = baseDir.resolve(RESULTS_REFERENCE_DIR)
					.resolve("output-" + previousDiffFileName + ".csv").toFile();
		}

		File referenceOutputFile = baseDir.resolve(RESULTS_REFERENCE_DIR).resolve("output-" + diffFileName + ".csv")
				.toFile();
		File incrementalResultFile = baseDir.resolve(RESULTS_INCREMENTAL_DIR).resolve("output-" + diffFileName + ".csv")
				.toFile();

		referenceResult.setResultQuality(QualityResult.ResultQuality.BASELINE);
		incrementalResult.setResultQuality(QualityResult.ResultQuality.DIFFERENT);

		if (contentIdentical(referenceOutputFile, incrementalResultFile)) {
			incrementalResult.setResultQuality(QualityResult.ResultQuality.SAME);
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as SAME");
		} else if (contentEquivalent(referenceOutputFile, previousReferenceOutputFile, incrementalResultFile)) {
			incrementalResult.setResultQuality(QualityResult.ResultQuality.EQUIVALENT);
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as EQUIVALENT");
		} else {
			LOGGER.logInfo("Marked " + incrementalResult.getResultFileName() + " as DIFFERENT");
		}

		incrementalResults.put(diffFileName, incrementalResult);
		referenceResults.put(diffFileName, incrementalResult);

	}

	/**
	 * Removes the line numbers.
	 *
	 * @param listOfResults
	 *            the list of results
	 * @return the list
	 */
	private List<String> removeLineNumbers(Collection<String> listOfResults) {
		List<String> newList = new ArrayList<String>();
		for (String entry : listOfResults) {
			String[] entryParts = entry.split(";");
			if (entryParts.length == 5) {
				entry = entryParts[0] + ";" + entryParts[1] + ";" + entryParts[4];
			}
			newList.add(entry);
		}
		return newList;
	}

	/**
	 * Content identical.
	 *
	 * @param referenceResult
	 *            the reference result
	 * @param incrementalResult
	 *            the incremental result
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean contentIdentical(File referenceResult, File incrementalResult) throws IOException {

		List<String> referenceLines = null;
		List<String> incrementalLines = null;

		if (this.variabilityEvaluationMode) {
			referenceLines = removeLineNumbers(Files.readAllLines(referenceResult.toPath()));
			incrementalLines = removeLineNumbers(Files.readAllLines(incrementalResult.toPath()));
		} else {
			referenceLines = Files.readAllLines(referenceResult.toPath());
			incrementalLines = Files.readAllLines(incrementalResult.toPath());
		}

		return referenceLines.containsAll(incrementalLines) && incrementalLines.containsAll(referenceLines);
	}

	/**
	 * Content equivalent.
	 *
	 * @param referenceResult
	 *            the reference result
	 * @param previousReferenceResult
	 *            the previous reference result
	 * @param incrementalResult
	 *            the incremental result
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	private boolean contentEquivalent(File referenceResult, File previousReferenceResult, File incrementalResult)
			throws IOException {

		List<String> referenceLines = null;
		List<String> incrementalLines = null;
		List<String> previousReferenceLines = null;

		if (this.variabilityEvaluationMode) {
			referenceLines = removeLineNumbers(Files.readAllLines(referenceResult.toPath()));
			incrementalLines = removeLineNumbers(Files.readAllLines(incrementalResult.toPath()));
		} else {
			referenceLines = Files.readAllLines(referenceResult.toPath());
			incrementalLines = Files.readAllLines(incrementalResult.toPath());
		}

		if (previousReferenceResult != null && this.variabilityEvaluationMode) {
			previousReferenceLines = removeLineNumbers(Files.readAllLines(previousReferenceResult.toPath()));
		} else if (previousReferenceResult != null) {
			previousReferenceLines = Files.readAllLines(previousReferenceResult.toPath());
		}

		// first make sure that the result of the reference analysis contains all
		// entries that the incremental analysis produced
		boolean isEquivalent = referenceLines.containsAll(incrementalLines);

		if (isEquivalent) {
			// check if the result of the incremental analysis covers all lines that changed
			// within the reference analysis compared to its predecessor
			List<String> referenceChanges = new ArrayList<String>(referenceLines);
			if (previousReferenceLines != null) {
				referenceChanges.removeAll(previousReferenceLines);
			}

			if (this.variabilityEvaluationMode) {
				// remove all lines that represent non-variability relevant information
				referenceChanges = removeNonVariabilityLines(referenceChanges);
			}

			isEquivalent = incrementalLines.containsAll(referenceChanges);
			if (!isEquivalent) {
				List<String> referenceWithoutIncrementalLines = new ArrayList<String>(referenceChanges);
				referenceWithoutIncrementalLines.removeAll(incrementalLines);
				StringJoiner joiner = new StringJoiner("\n");
				referenceWithoutIncrementalLines.forEach(line -> joiner.add(line));
				LOGGER.logInfo("Results in reference analysis for " + referenceResult.getName()
						+ " contained new results (compared to the previous reference) that were not present for the incremental result : ",
						joiner.toString());
			}
		} else {
			List<String> incrementalWithoutRefLines = new ArrayList<String>(incrementalLines);
			incrementalWithoutRefLines.removeAll(referenceLines);
			StringJoiner joiner = new StringJoiner("\n");
			incrementalWithoutRefLines.forEach(line -> joiner.add(line));
			LOGGER.logInfo("Results in incremental analysis for " + referenceResult.getName()
					+ " contained results that were not present for the reference : ", joiner.toString());
		}

		return isEquivalent;
	}

	/**
	 * Removes the non variability lines by looking at the presence condition.
	 * Discards lines where the presence Condition does not contain CONFIG_.
	 *
	 * @param lines
	 *            the lines
	 * @return the list
	 */
	private List<String> removeNonVariabilityLines(List<String> lines) {
		List<String> newList = new ArrayList<String>();
		for (String entry : lines) {
			String[] entryParts = entry.split(";");
			String presenceCondition = entryParts[entryParts.length - 1];
			if (presenceCondition.contains("CONFIG_")) {
				newList.add(entry);
			}
		}
		return newList;
	}

}
