/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aic2013.extractor.entities;

/**
 *
 * @author Christian
 */
public class Topic {
    
    private String name;

    public Topic(String name) {
        this.name = name;
    }
    
    public String toNeo4j() {
        StringBuilder sb = new StringBuilder("Topic {");
        sb.append("name: '").append(name);
        sb.append("'}");
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
