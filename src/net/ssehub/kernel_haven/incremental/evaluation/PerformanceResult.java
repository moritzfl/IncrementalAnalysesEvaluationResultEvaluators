package net.ssehub.kernel_haven.incremental.evaluation;

import java.time.LocalDateTime;

/**
 * Storage class for performance results.
 * 
 * @author moritz
 */
public class PerformanceResult {

	private boolean partialAnalysis = false;

	/** The start preparation phase. */
	private LocalDateTime startPreparationPhase = null;

	/** The finish preparation phase. */
	private LocalDateTime finishPreparationPhase = null;

	/** The start extraction phase. */
	private LocalDateTime startExtractionPhase = null;

	/** The end extraction phase. */
	private LocalDateTime endExtractionPhase = null;

	/** The start analysis phase. */
	private LocalDateTime startAnalysisPhase = null;

	/** The end analysis phase. */
	private LocalDateTime endAnalysisPhase = null;

	/** The end time. */
	private LocalDateTime endTime = null;

	/** The diff file name. */
	private String diffFileName;

	/** The start time. */
	private LocalDateTime startTime;

	/**
	 * Instantiates a new performance result.
	 *
	 * @param diffFileName
	 *            the diff file name
	 */
	public PerformanceResult(String diffFileName) {
		this.setDiffFileName(diffFileName);
	}

	/**
	 * Gets the start preparation phase.
	 *
	 * @return the start preparation phase
	 */
	public LocalDateTime getStartPreparationPhase() {
		return startPreparationPhase;
	}

	/**
	 * Sets the start preparation phase.
	 *
	 * @param startPreparationPhase
	 *            the new start preparation phase
	 */
	public void setStartPreparationPhase(LocalDateTime startPreparationPhase) {
		this.startPreparationPhase = startPreparationPhase;
	}

	/**
	 * Gets the finish preparation phase.
	 *
	 * @return the finish preparation phase
	 */
	public LocalDateTime getEndPreparationPhase() {
		return finishPreparationPhase;
	}

	/**
	 * Sets the finish preparation phase.
	 *
	 * @param finishPreparationPhase
	 *            the new finish preparation phase
	 */
	public void setEndPreparationPhase(LocalDateTime finishPreparationPhase) {
		this.finishPreparationPhase = finishPreparationPhase;
	}

	/**
	 * Gets the start extraction phase.
	 *
	 * @return the start extraction phase
	 */
	public LocalDateTime getStartExtractionPhase() {
		return startExtractionPhase;
	}

	/**
	 * Sets the start extraction phase.
	 *
	 * @param startExtractionPhase
	 *            the new start extraction phase
	 */
	public void setStartExtractionPhase(LocalDateTime startExtractionPhase) {
		this.startExtractionPhase = startExtractionPhase;
	}

	/**
	 * Gets the end extraction phase.
	 *
	 * @return the end extraction phase
	 */
	public LocalDateTime getEndExtractionPhase() {
		return endExtractionPhase;
	}

	/**
	 * Sets the end extraction phase.
	 *
	 * @param endExtractionPhase
	 *            the new end extraction phase
	 */
	public void setEndExtractionPhase(LocalDateTime endExtractionPhase) {
		this.endExtractionPhase = endExtractionPhase;
	}

	/**
	 * Gets the start analysis phase.
	 *
	 * @return the start analysis phase
	 */
	public LocalDateTime getStartAnalysisPhase() {
		return startAnalysisPhase;
	}

	/**
	 * Sets the start analysis phase.
	 *
	 * @param startAnalysisPhase
	 *            the new start analysis phase
	 */
	public void setStartAnalysisPhase(LocalDateTime startAnalysisPhase) {
		this.startAnalysisPhase = startAnalysisPhase;
	}

	/**
	 * Gets the end analysis phase.
	 *
	 * @return the end analysis phase
	 */
	public LocalDateTime getEndAnalysisPhase() {
		return endAnalysisPhase;
	}

	/**
	 * Sets the end analysis phase.
	 *
	 * @param endAnalysisPhase
	 *            the new end analysis phase
	 */
	public void setEndAnalysisPhase(LocalDateTime endAnalysisPhase) {
		this.endAnalysisPhase = endAnalysisPhase;
	}

	/**
	 * Gets the end time.
	 *
	 * @return the end time
	 */
	public LocalDateTime getEndTime() {
		return endTime;
	}

	/**
	 * Sets the end time.
	 *
	 * @param endTime
	 *            the new end time
	 */
	public void setEndTime(LocalDateTime endTime) {
		this.endTime = endTime;
	}

	/**
	 * Gets the diff file name.
	 *
	 * @return the diff file name
	 */
	public String getDiffFileName() {
		return diffFileName;
	}

	/**
	 * Sets the diff file name.
	 *
	 * @param diffFileName
	 *            the new diff file name
	 */
	public void setDiffFileName(String diffFileName) {
		this.diffFileName = diffFileName;
	}

	/**
	 * Sets the start time.
	 *
	 * @param startTime
	 *            the new start time
	 */
	public void setStartTime(LocalDateTime startTime) {
		this.startTime = startTime;

	}

	/**
	 * Gets the start time.
	 *
	 * @return the start time
	 */
	public LocalDateTime getStartTime() {
		return startTime;

	}

	/**
	 * NOT IMPLEMENTED: Adds the analysis component time.
	 * 
	 * @param finishedComponent
	 *            the finished component
	 * @param componentTime
	 *            the component time
	 */
	public void addAnalysisComponentTime(String finishedComponent, long componentTime) {
		// NOT IMPLEMENTED

	}

	public boolean isPartialAnalysis() {
		return partialAnalysis;
	}

	public void setPartialAnalysis(boolean partialAnalysis) {
		this.partialAnalysis = partialAnalysis;
	}

}
