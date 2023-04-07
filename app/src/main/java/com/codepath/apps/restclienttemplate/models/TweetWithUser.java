package com.codepath.apps.restclienttemplate.models;

import androidx.room.Embedded;

import com.codepath.apps.restclienttemplate.Entities;

import java.util.ArrayList;
import java.util.List;

public class TweetWithUser {
    @Embedded
    User user;

    @Embedded(prefix = "tweet_")
    Tweet tweet;

    @Embedded
    Entities entities;

    public static List<Tweet> getTweetList(List<TweetWithUser> tweetWithUsers) {
        List <Tweet> tweets = new ArrayList<>();

        for (int i = 0 ; i < tweetWithUsers.size(); i++){
            Tweet tweet = tweetWithUsers.get(i).tweet;
            tweet.user = tweetWithUsers.get(i).user;
            tweet.entities =tweetWithUsers.get(i).entities;
            tweets.add(tweet);
        }
        return tweets;
    }
}
