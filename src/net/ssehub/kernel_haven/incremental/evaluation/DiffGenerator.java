package net.ssehub.kernel_haven.incremental.evaluation;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

import net.ssehub.kernel_haven.util.Logger;
import net.ssehub.kernel_haven.util.Util;

/**
 * Class that helps with generating a set of diff files for a given commit
 * range.
 * @author moritz
 */
public class DiffGenerator {

	/** The Constant EMPTY_REPOSITORY_HASH. */
	public static final String EMPTY_REPOSITORY_HASH = "4b825dc642cb6eb9a060e54bf8d69288fbee4904";

	/** The Constant CURRENT_COMMIT_HASH. */
	public static final String CURRENT_COMMIT_HASH = "HEAD";

	/** The Constant LOGGER. */
	private static final Logger LOGGER = Logger.get();

	/** The git repository. */
	private File gitRepository;

	/**
	 * Instantiates a new diff generator.
	 *
	 * @param gitRepository
	 *            the git repository
	 */
	public DiffGenerator(final File gitRepository) {
		this.gitRepository = gitRepository;
	}

	/**
	 * Generate diff.
	 *
	 * @param oldCommitHash
	 *            the old commit hash
	 * @param newCommitHash
	 *            the new commit hash
	 * @param resultFile
	 *            the result file
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean generateDiff(String oldCommitHash, String newCommitHash, File resultFile) throws IOException {
		ProcessBuilder processBuilder = new ProcessBuilder("git", "diff", "--no-renames", "--binary", oldCommitHash,
				newCommitHash);
		processBuilder.directory(gitRepository);

		FileOutputStream stdoutStream = new FileOutputStream(resultFile);
		ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

		boolean success = false;
		try {
			success = Util.executeProcess(processBuilder, "git diff", stdoutStream, stderrStream, 0);
		} catch (IOException e) {
			LOGGER.logException("Could not merge changes", e);
		}

		String stderr = stderrStream.toString();

		if (stderr != null && !stderr.equals("")) {
			if (!success) {
				LOGGER.logError(("git diff stderr:\n" + stderr).split("\n"));
			} else {
				LOGGER.logDebug(("git diff stderr:\n" + stderr).split("\n"));
			}
		}

		return success;
	}

	/**
	 * Generate diffs.
	 *
	 * @param commits
	 *            the commits
	 * @param outputDir
	 *            the output dir
	 * @return true, if successful
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public boolean generateDiffs(List<String> commits, File outputDir) throws IOException {
		boolean success = true;

		String thisCommit = null;
		String nextCommit = null;

		outputDir.mkdirs();

		int counter = 1;
		for (String commit : commits) {
			thisCommit = nextCommit;
			nextCommit = commit;
			if (thisCommit != null) {
				String counterString = String.format("%05d", counter);
				File outputFile = outputDir.toPath().resolve(counterString + "-git.diff").toFile();
				generateDiff(thisCommit, nextCommit, outputFile);
				counter++;
			}
		}

		return success;
	}

	/**
	 * List all commits in range.
	 *
	 * @param startCommitHash
	 *            the start commit hash
	 * @param endCommitHash
	 *            the end commit hash
	 * @return the list
	 */
	public List<String> listAllCommitsInRange(String startCommitHash, String endCommitHash) {
		ProcessBuilder processBuilder = new ProcessBuilder("git", "log", "--first-parent", "--pretty=oneline",
				startCommitHash + "^.." + endCommitHash);
		processBuilder.directory(gitRepository);

		ByteArrayOutputStream stdoutStream = new ByteArrayOutputStream();
		ByteArrayOutputStream stderrStream = new ByteArrayOutputStream();

		boolean success = false;
		try {
			success = Util.executeProcess(processBuilder, "git log", stdoutStream, stderrStream, 0);
		} catch (IOException e) {
			LOGGER.logException("Could not list commit hashes in given range", e);
		}

		String stderr = stderrStream.toString();
		String stdout = stdoutStream.toString();

		if (stderr != null && !stderr.equals("")) {
			if (!success) {
				LOGGER.logError(("git log stderr:\n" + stderr).split("\n"));
			} else {
				LOGGER.logDebug(("git log stderr:\n" + stderr).split("\n"));
			}
		}

		if (success) {
			List<String> commits = Arrays.asList(stdout.split("\n"));
			Collections.reverse(commits);

			List<String> cleanedCommitLines = new ArrayList<String>();
			commits.forEach(commit -> cleanedCommitLines.add(commit.substring(0, EMPTY_REPOSITORY_HASH.length())));
			return cleanedCommitLines;
		} else {
			return null;
		}
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

		System.out.println("Enter path to Linux repo:");
		Scanner scanner = new Scanner(System.in);
		String linuxRepoString = scanner.nextLine();
		System.out.println("Enter path to store diffs to:");
		String outputDirString = scanner.nextLine();

		File linuxRepo = new File(linuxRepoString);
		File outputDir = new File(outputDirString);

		DiffGenerator diffGen = new DiffGenerator(linuxRepo);

		System.out.println("Enter the commit hash of the commit that you want to be considered the initial commit:");

		String commitStart = scanner.nextLine();

		System.out.println("Enter the commit hash of the commit that you want to be considered the last commit:");

		String commitEnd = scanner.nextLine();

		System.out.println("Creating list of commits ...");
		List<String> commits = diffGen.listAllCommitsInRange(commitStart, commitEnd);
		List<String> commitsWithEmptyStart = new ArrayList<String>();
		commitsWithEmptyStart.add(EMPTY_REPOSITORY_HASH);

		commitsWithEmptyStart.addAll(commits);

		System.out.println("Generating diffs for commits ...");
		diffGen.generateDiffs(commitsWithEmptyStart, outputDir);

		scanner.close();
	}

}
