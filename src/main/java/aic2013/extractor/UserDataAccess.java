/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor;

import aic2013.extractor.entities.TwitterUser;
import com.mongodb.DBObject;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author Christian
 */
public class UserDataAccess {

    private final EntityManager em;

    public UserDataAccess(EntityManager em) {
        this.em = em;
    }

    public void forAll(Processor<TwitterUser> processor) {
        int batchSize = 1000;

        for (int start = 0;; start += batchSize) {
            em.clear();
            
            List<TwitterUser> users = em.createQuery("FROM TwitterUser", TwitterUser.class)
                .setFirstResult(start)
                .setMaxResults(batchSize)
                .getResultList();

            if (users.isEmpty()) {
                break;
            }
            
            for(TwitterUser user : users) {
                processor.process(user);
            }
        }
    }
}
