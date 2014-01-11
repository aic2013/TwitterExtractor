/**
 * 
 */
package aic2013.extractor;

/**
 * @author Moritz Becker (moritz.becker@gmx.at)
 *
 */
public abstract class TextFilterDecorator implements TextFilter{
	protected TextFilter decoratedFilter;
	
	public TextFilterDecorator(TextFilter decoratedFilter){
		this.decoratedFilter = decoratedFilter;
	}
	
	/* (non-Javadoc)
	 * @see aic2013.extractor.TextFilter#filter(java.lang.String)
	 */
	@Override
	public String filter(String input) {
		return decoratedFilter.filter(input);
	}
}
