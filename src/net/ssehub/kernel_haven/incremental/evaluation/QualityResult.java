package net.ssehub.kernel_haven.incremental.evaluation;

/**
 * Storage class for quality results.
 * 
 * @author moritz
 */
public class QualityResult {

    /**
     * enum used to indicate the quality of the result when compared against a
     * baseline.
     * 
     */
    enum ResultQuality {

        /**
         * signals that a result is equivalent compared to a baseline result.
         */
        EQUIVALENT,
        /** signals that a result is the same compared to a baseline result. */
        SAME,
        /**
         * signals that a result is the different compared to a baseline result.
         */
        DIFFERENT,
        /** signals that a result was used as baseline. */
        BASELINE
    }

    /** The result file name. */
    private String resultFileName;

    /** The result quality. */
    private ResultQuality resultQuality;

    /**
     * Instantiates a new quality result.
     *
     * @param resultFileName
     *            the result file name
     */
    public QualityResult(String resultFileName) {
        super();
        this.setResultFileName(resultFileName);
    }

    /**
     * Gets the result quality.
     *
     * @return the result quality
     */
    public ResultQuality getResultQuality() {
        return resultQuality;
    }

    /**
     * Sets the result quality.
     *
     * @param resultQuality
     *            the new result quality
     */
    public void setResultQuality(final ResultQuality resultQuality) {
        this.resultQuality = resultQuality;
    }

    /**
     * Gets the result file name.
     *
     * @return the result file name
     */
    public String getResultFileName() {
        return resultFileName;
    }

    /**
     * Sets the result file name.
     *
     * @param resultFileName
     *            the new result file name
     */
    public void setResultFileName(String resultFileName) {
        this.resultFileName = resultFileName;
    }

}
