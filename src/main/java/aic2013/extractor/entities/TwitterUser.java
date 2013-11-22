/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package aic2013.extractor.entities;

import com.mongodb.DBObject;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import twitter4j.User;

/**
 *
 * @author Christian
 */
@Entity
public class TwitterUser implements Serializable {

    @Id
    private Long id;
    private String name;
    private String screenName;
    private String language;
    private int followersCount;
    private int statusesCount;
    private int friendsCount;
    private int favouritesCount;
    private int listedCount;

    public TwitterUser() {
    }

    public TwitterUser(User user) {
        id = user.getId();
        name = user.getName();
        screenName = user.getScreenName();
        language = user.getLang();
        followersCount = user.getFollowersCount();
        statusesCount = user.getStatusesCount();
        friendsCount = user.getFriendsCount();
        favouritesCount = user.getFavouritesCount();
        listedCount = user.getListedCount();
    }

    public String toNeo4j() {
        StringBuilder sb = new StringBuilder("Person {");
        sb.append("id: ")
            .append(id);
        sb.append("}");
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getScreenName() {
        return screenName;
    }

    public void setScreenName(String screenName) {
        this.screenName = screenName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public int getFollowersCount() {
        return followersCount;
    }

    public void setFollowersCount(int followersCount) {
        this.followersCount = followersCount;
    }

    public int getStatusesCount() {
        return statusesCount;
    }

    public void setStatusesCount(int statusesCount) {
        this.statusesCount = statusesCount;
    }

    public int getFriendsCount() {
        return friendsCount;
    }

    public void setFriendsCount(int friendsCount) {
        this.friendsCount = friendsCount;
    }

    public int getFavouritesCount() {
        return favouritesCount;
    }

    public void setFavouritesCount(int favouritesCount) {
        this.favouritesCount = favouritesCount;
    }

    public int getListedCount() {
        return listedCount;
    }

    public void setListedCount(int listedCount) {
        this.listedCount = listedCount;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TwitterUser other = (TwitterUser) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

}
