package com.yo.shishkoam.twitterapi;

import java.util.LinkedList;

/**
 * Created by User on 12.02.2017
 * This class manages history of users that was viewed
 */

public class UserManager {
    private static UserManager ourInstance = new UserManager();

    public static UserManager getInstance() {
        return ourInstance;
    }

    private LinkedList<String> users = new LinkedList<>();

    private UserManager() {
    }

    public void addUser(String user) {
        boolean isEmpty = users.isEmpty();
        if (isEmpty || !users.getLast().equals(user)) {
            users.add(user);
        }
    }

    public void removeLastUser() {
        users.removeLast();
    }

    public String getLastUser() {
        return users.getLast();
    }

    public boolean isEmpty() {
        return users.isEmpty();
    }
}
