/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor;

import aic2013.extractor.entities.TwitterUser;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.User;

/**
 *
 * @author Christian
 */
public class TwitterDataAccess {

    private final Twitter twitter;

    public TwitterDataAccess(Twitter twitter) {
        this.twitter = twitter;
    }

    public void forAllFollowers(TwitterUser twitterUser, Processor<User> processor) throws Exception {
        long nextCursor = -1;

        do {
            PagableResponseList<User> usersResponse = twitter.getFollowersList(twitterUser.getId(), nextCursor);
            nextCursor = usersResponse.getNextCursor();

            for (User u : usersResponse) {
                processor.process(u);
            }
        } while (nextCursor > 0);
    }

    public void forAllFavorites(TwitterUser twitterUser, Processor<Status> processor) throws Exception {
        int batchSize = 20;

        for (int start = 1;; start++) {
            ResponseList<Status> usersResponse = twitter.favorites()
                .getFavorites(twitterUser.getId(), new Paging(start, batchSize));

            if (usersResponse.isEmpty()) {
                break;
            }

            for (Status u : usersResponse) {
                processor.process(u);
            }
        }
    }
}
