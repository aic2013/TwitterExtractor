/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package aic2013.extractor;

import java.sql.SQLException;

/**
 *
 * @author Christian
 */
public interface Neo4jUnitOfWork {
    
    public void process() throws SQLException; 
}
