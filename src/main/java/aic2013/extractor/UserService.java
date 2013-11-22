/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor;

import aic2013.extractor.entities.TwitterUser;
import com.mongodb.DBObject;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

/**
 *
 * @author Christian
 */
public class UserService {

    private final EntityManager em;

    public UserService(EntityManager em) {
        this.em = em;
    }

    public void persist(TwitterUser user) {
        EntityTransaction tx = null;
        boolean started = false;

        try {
            tx = em.getTransaction();

            if (!tx.isActive()) {
                started = true;
                tx.begin();
            }

            em.merge(user);

            if (started) {
                tx.commit();
            }
        } catch (RuntimeException ex) {
            if (tx != null) {
                tx.setRollbackOnly();
            }
        } finally {
            if (tx != null && started && tx.isActive() && tx.getRollbackOnly()) {
                tx.rollback();
            }
        }
    }

}
