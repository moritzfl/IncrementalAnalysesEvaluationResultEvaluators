package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.ssehub.kernel_haven.util.Logger;

/**
 * Class used to evaluate the quality (content/found dead code blocks) of
 * results produced by IncrementalAnalysesEvaluation.
 * 
 * @author moritz
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

    /** The Logger. */
    private static final Logger LOGGER = Logger.get();

    private static final String LOG_LINE_UPDATE_PATTERN = "Updating lines for file: ";

    private static final String LOG_LINE_NO_VARCHANGE_PATTERN =
            " because it introduced no variability related changes.";

    /** The base dir. */
    private Path baseDir;

    /** The incremental results. */
    private Map<String, QualityResult> incrementalResults = new HashMap<String, QualityResult>();

    /** The reference results. */
    private Map<String, QualityResult> referenceResults = new HashMap<String, QualityResult>();

    private Mode mode;

    public enum Mode {
        VARIABILITY, BLOCK_CHANGE, CHANGE,
    }

    /**
     * Instantiates a new evaluator.
     *
     * @param variabilityEvaluationMode the variability evaluation mode
     * @param path                      the path
     */
    public QualityEvaluator(Mode mode, Path path) {
        this.baseDir = path;
        this.mode = mode;

    }

    /**
     * The main method.
     *
     * @param args the arguments
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public static void main(String[] args) throws IOException {
        Path baseDir = null;
        Mode mode = Mode.CHANGE;

        // Parse arguments
        if (args.length == 1) {
            baseDir = Paths.get(args[0]);
        } else if (args.length == 2) {
            baseDir = Paths.get(args[1]);
            if (args[0].equals("-variability") || args[0].equals("-v")) {
                mode = Mode.VARIABILITY;
            } else if (args[0].equals("-blockchange") || args[0].equals("-bc")) {
                mode = Mode.BLOCK_CHANGE;
            } else if (!(args[0].equals("-change") || args[0].equals("-c"))) {
                LOGGER.logError("unknown option " + args[0]);
                System.exit(1);
            }
        }

        LOGGER.logInfo("Working on directory \"" + baseDir + "\". Mode=" + mode + ".");

        if (baseDir != null && !baseDir.toFile().exists()) {
            LOGGER.logError("Directory \"" + baseDir + "\" does not exist!");
            System.exit(1);
        }

        if (baseDir != null) {
            QualityEvaluator evaluator = new QualityEvaluator(mode, baseDir);
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
            if (file.getName().startsWith("output-")) {
                diffFileNames
                        .add(file.getName().substring("output-".length(), file.getName().length() - ".csv".length()));
            }
        }

        return diffFileNames;
    }

    /**
     * Compare results for the name of a git-diff file that was used as input within
     * IncrementalAnalysesEvalauation.
     *
     * @param diffFileName         the diff file name
     * @param previousDiffFileName the previous diff file name
     * @throws IOException Signals that an I/O exception has occurred.
     */
    public void compareForInputDiffName(String diffFileName, String previousDiffFileName) throws IOException {

        QualityResult referenceResult = new QualityResult(diffFileName);
        QualityResult incrementalResult = new QualityResult(diffFileName);

        File previousReferenceOutputFile = null;

        if (previousDiffFileName != null) {
            previousReferenceOutputFile =
                    baseDir.resolve(RESULTS_REFERENCE_DIR).resolve("output-" + previousDiffFileName + ".csv").toFile();
        }

        File referenceOutputFile =
                baseDir.resolve(RESULTS_REFERENCE_DIR).resolve("output-" + diffFileName + ".csv").toFile();
        File incrementalResultFile =
                baseDir.resolve(RESULTS_INCREMENTAL_DIR).resolve("output-" + diffFileName + ".csv").toFile();

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
     * Checks if content is considered identical.
     *
     * @param referenceResult   the reference result
     * @param incrementalResult the incremental result
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean contentIdentical(File referenceResult, File incrementalResult) throws IOException {

        List<String> referenceLines = null;
        List<String> incrementalLines = null;

        referenceLines = Files.readAllLines(referenceResult.toPath());
        incrementalLines = Files.readAllLines(incrementalResult.toPath());

        return referenceLines.containsAll(incrementalLines) && incrementalLines.containsAll(referenceLines);
    }

    /**
     * Checks if content is considered equivalent.
     *
     * @param referenceResult         the reference result
     * @param previousReferenceResult the previous reference result
     * @param incrementalResult       the incremental result
     * @return true, if successful
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private boolean contentEquivalent(File referenceResult, File previousReferenceResult, File incrementalResult)
            throws IOException {
        List<String> referenceLines = null;
        List<String> incrementalLines = null;
        List<String> previousReferenceLines = null;

        referenceLines = Files.readAllLines(referenceResult.toPath());
        incrementalLines = Files.readAllLines(incrementalResult.toPath());

        if (previousReferenceResult != null) {
            previousReferenceLines = Files.readAllLines(previousReferenceResult.toPath());
        } else {
            previousReferenceLines = new ArrayList<String>();
        }

        /*
         * first make sure that the result of the reference analysis contains all
         * entries that the incremental analysis produced
         */
        boolean isEquivalent = referenceLines.containsAll(incrementalLines);
        if (isEquivalent) {
            /*
             * check if the result of the incremental analysis covers all lines that changed
             * within the reference analysis compared to its predecessor
             */
            List<String> referenceChanges = new ArrayList<String>(referenceLines);

            referenceChanges.removeAll(previousReferenceLines);

            if (this.mode.equals(Mode.VARIABILITY)) {

                // remove all entries resulting of line updates from reference
                // result

                String fileName = incrementalResult.getName().replace("output", "log").replace(".csv", ".log");
                Path pathToIncrementalLog = this.baseDir.resolve(LOG_INCREMENTAL_DIR + "/" + fileName);
                List<String> logLines = Files.readAllLines(pathToIncrementalLog);

                for (String line : logLines) {
                    if (line.contains(LOG_LINE_UPDATE_PATTERN)) {
                        String updatedFile = line
                                .substring(line.indexOf(LOG_LINE_UPDATE_PATTERN) + LOG_LINE_UPDATE_PATTERN.length());
                        for (int i = 0; i < referenceChanges.size(); i++) {
                            if (referenceChanges.get(i).startsWith(updatedFile)) {
                                LOGGER.logInfo("Found new entry in reference that is likely to be a line update: "
                                        + referenceChanges);
                                referenceChanges.get(i);
                                referenceChanges.remove(i);
                                i--;
                            }
                        }
                    } else if (line.contains(LOG_LINE_NO_VARCHANGE_PATTERN)) {
                        String updatedFile = line.substring(line.indexOf("Skipping ") + "Skipping ".length(),
                                line.indexOf(LOG_LINE_NO_VARCHANGE_PATTERN));
                        for (int i = 0; i < referenceChanges.size(); i++) {
                            if (referenceChanges.get(i).startsWith(updatedFile)) {
                                LOGGER.logInfo(
                                        "Found new entry in reference that is probably not the result of a change to variability: "
                                                + referenceChanges.get(i));
                                referenceChanges.remove(i);
                                i--;
                            }
                        }
                    }
                }

                // remove all lines that represent non-variability relevant
                // information
                referenceChanges = removeNonVariabilityLines(referenceChanges);
            } else if (this.mode.equals(Mode.BLOCK_CHANGE)) {
                // remove all entries resulting of line updates from reference
                // result
                String fileName = incrementalResult.getName().replace("output", "log").replace(".csv", ".log");
                Path pathToIncrementalLog = this.baseDir.resolve(LOG_INCREMENTAL_DIR + "/" + fileName);
                List<String> logLines = Files.readAllLines(pathToIncrementalLog);

                for (String line : logLines) {
                    if (line.contains(LOG_LINE_UPDATE_PATTERN)) {
                        String updatedFile = line
                                .substring(line.indexOf(LOG_LINE_UPDATE_PATTERN) + LOG_LINE_UPDATE_PATTERN.length());
                        for (int i = 0; i < referenceChanges.size(); i++) {
                            if (referenceChanges.get(i).startsWith(updatedFile)) {
                                LOGGER.logInfo("Found new entry in reference that is likely to be a line update: "
                                        + referenceChanges);
                                referenceChanges.get(i);
                                referenceChanges.remove(i);
                                i--;
                            }
                        }
                    }
                }
            }

            isEquivalent = incrementalLines.containsAll(referenceChanges);
            if (!isEquivalent) {
                List<String> referenceWithoutIncrementalLines = new ArrayList<String>(referenceChanges);
                referenceWithoutIncrementalLines.removeAll(incrementalLines);
                StringJoiner joiner = new StringJoiner("\n");
                referenceWithoutIncrementalLines.forEach(line -> joiner.add(line));
                LOGGER.logInfo(
                        "Results in reference analysis for " + referenceResult.getName()
                                + " contained new results (compared to the previous"
                                + " reference) that were not present for the " + "incremental result : ",
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
     * Discards lines where the presence condition does not contain CONFIG_ at the
     * beginning of a variable.
     *
     * @param lines the lines
     * @return the list
     */
    private List<String> removeNonVariabilityLines(List<String> lines) {
        List<String> newList = new ArrayList<String>();
        Pattern pattern = Pattern.compile("(?<!(_|\\w|\\d))CONFIG_");
        for (String entry : lines) {
            String[] entryParts = entry.split(";");
            String presenceCondition = entryParts[entryParts.length - 1];
            String filePc = entryParts[1];
            Matcher matcher = pattern.matcher(presenceCondition);
            Matcher filePcMatcher = pattern.matcher(filePc);
            boolean pcFound = matcher.find();
            boolean filePcFound = filePcMatcher.find();
            if (pcFound || (filePcFound && presenceCondition.isEmpty())) {
                newList.add(entry);
            }

        }
        return newList;
    }

}
