/**
 * 
 */
package aic2013.extractor;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public class BaseFilter implements TextFilter {

	/* (non-Javadoc)
	 * @see aic2013.extractor.TextFilter#filter(java.lang.String)
	 */
	@Override
	public String filter(String input) {
		return input;
	}

}
