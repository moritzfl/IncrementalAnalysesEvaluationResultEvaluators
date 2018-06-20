package net.ssehub.kernel_haven.incremental.evaluation;

// TODO: Auto-generated Javadoc
/**
 * The Class QualityResult.
 */
public class QualityResult {

	/**
	 * The Enum ResultQuality.
	 */
	enum ResultQuality {
		
		/** The equivalent. */
		EQUIVALENT, 
 /** The same. */
 SAME, 
 /** The different. */
 DIFFERENT, 
 /** The baseline. */
 BASELINE
	}

	/** The result file name. */
	private String resultFileName;


	/** The result quality. */
	private ResultQuality resultQuality;
	
	

	/**
	 * Instantiates a new quality result.
	 *
	 * @param resultFileName the result file name
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
	 * @param resultQuality the new result quality
	 */
	public void setResultQuality(ResultQuality resultQuality) {
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
	 * @param resultFileName the new result file name
	 */
	public void setResultFileName(String resultFileName) {
		this.resultFileName = resultFileName;
	}


}
