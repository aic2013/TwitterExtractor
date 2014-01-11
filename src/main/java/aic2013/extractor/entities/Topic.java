/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aic2013.extractor.entities;

import java.util.Arrays;

/**
 *
 * @author Christian
 */
public class Topic {
    
    private String[] keywords;

    public Topic(String[] keywords) {
        this.keywords = keywords;
    }
    
    public String toNeo4j() {
        StringBuilder sb = new StringBuilder("Topic {keywords: [");
        for(String keyword : keywords){
        	sb.append("'").append(keyword).append("',");
        }
        if(keywords.length > 0)
        sb.deleteCharAt(sb.length() - 1);
        sb.append("]}");
        return sb.toString();
    }

    public String[] getName() {
        return keywords;
    }

    public void setName(String[] keywords) {
        this.keywords = keywords;
    }

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(keywords);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Topic other = (Topic) obj;
		if (!Arrays.equals(keywords, other.keywords))
			return false;
		return true;
	}

	
    
}
