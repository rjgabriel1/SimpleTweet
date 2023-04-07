package com.codepath.apps.restclienttemplate;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.TweetDao;
import com.codepath.apps.restclienttemplate.models.TweetWithUser;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import okhttp3.Headers;

public class TimelineActivity extends AppCompatActivity implements CreateTweet.CreateTweetListener{

    public static final int REQUUEST_CODE = 20;

    TweetDao tweetDao;
    public static User  user;
    TwitterClient client;
    RecyclerView rvTweets;
    List<Tweet> tweets;
    TweetsAdapter adapter;
    SwipeRefreshLayout swipeContainer;
    EndlessRecyclerViewScrollListener scrollListener;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUUEST_CODE && resultCode == RESULT_OK) {
            //Get tweet from the intent
           Tweet tweet=Parcels.unwrap(data.getParcelableExtra("tweet"));

            //modify data source
            tweets.add(0, tweet);

            //Update the adapter
            adapter.notifyItemInserted(0);

            //  Update the RV with the tweet
            rvTweets.smoothScrollToPosition(0);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static final String  TAG ="TimelineActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FloatingActionButton makeTweet;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
         client = TwitterApp.getRestClient(this);
         makeTweet = findViewById(R.id.makeTweet);

        //         find the recyclerview
        rvTweets = findViewById(R.id.rvTweets);

         tweetDao = ((TwitterApp) getApplicationContext()).getMyDatabase().tweetDao();




         Toolbar toolbar = findViewById(R.id.toolBar);
         setSupportActionBar(toolbar);

         //Display toolbar's icons
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setTitle(" ");

        // Configure the refreshing colors

            swipeContainer = findViewById(R.id.swipeContainer);
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
             @Override
             public void onRefresh() {
                 Log.i(TAG, "fetching new data");
                 populateHometimeline();

             }
         });




        makeTweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditDialog();


                }
        });


        //        init the list of tweets and adapter
        tweets = new ArrayList<>();
        adapter = new TweetsAdapter(this,tweets);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);

        //        recyclerview setup: layout manager and adapter
        rvTweets.setLayoutManager(layoutManager);
        rvTweets.setAdapter(adapter);
        scrollListener = new EndlessRecyclerViewScrollListener(layoutManager) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                        Log.i(TAG, "onLoadMore");
                        loadMoreData();
            }
        };

        // Adds the scroll listener to RecyclerView
        rvTweets.addOnScrollListener(scrollListener);

        //        QUERY EXISTING TWEETS
        AsyncTask.execute(new Runnable() {
            @Override
            public void run(){
               List<TweetWithUser> tweetWithUsers =tweetDao.recentItems();
                List<Tweet> tweetsFromDB = TweetWithUser.getTweetList(tweetWithUsers);

                adapter.clear();
                adapter.addAll(tweetsFromDB);
            }

        });

         populateHometimeline();


    }

    private void showEditDialog() {
        FragmentManager fm = getSupportFragmentManager();
        CreateTweet editNameDialogFragment = CreateTweet.newInstance("New tweet");
        editNameDialogFragment.show(fm, "create");


    }

    private void loadMoreData() {
        // Send an API request to retrieve appropriate paginated data
        client.getNextPageOfTweets(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"onSuccess Load more data" +json.toString());

                //  --> Deserialize and construct new model objects from the API response
                JSONArray jsonArray = json.jsonArray;
                try {
                    List<Tweet> tweets = Tweet.fromJsonArray(jsonArray);
                    //  --> Append the new data objects to the existing set of items inside the array of items
                    //  --> Notify the adapter of the new items made with `notifyItemRangeInserted()`
                    adapter.addAll(tweets);
                } catch (JSONException e) {
                    e.printStackTrace();
                }



            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"onFailure Load more data", throwable );

            }
        },tweets.get(tweets.size() -1).id);

    }

    private void populateHometimeline() {
        client.getCredentials(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                JSONObject jsonObject = json.jsonObject;
                try {
                    user = User.fromJson(jsonObject);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {

            }
        });

        client.getHomeTimeline(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Headers headers, JSON json) {
                Log.i(TAG,"onSuccess!");
                JSONArray jsonArray = json.jsonArray;

                try {
                    List<Tweet> tweetsFromNetwork = Tweet.fromJsonArray(jsonArray);
                    adapter.clear();
                    adapter.addAll( tweetsFromNetwork);
                    // Now we call setRefreshing(false) to signal refresh has finished
                    swipeContainer.setRefreshing(false);

                    AsyncTask.execute(new Runnable() {
                        @Override
                        public void run(){
                             //  insert users
                            List<User> userFromNetwork = User.fromJsonTweetsArray(tweetsFromNetwork);
                            tweetDao.insertModel(userFromNetwork.toArray(new User [0]));

                            //  insert entities
                            List<Entities> entitiesFromNetwork = Entities.fromJsonTweetsArray(tweetsFromNetwork);
                            tweetDao.insertModel(entitiesFromNetwork.toArray(new Entities [0]));

                            //  insert tweets
                            tweetDao.insertModel(tweetsFromNetwork.toArray(new Tweet [0]));
                        }


                        });

                } catch (JSONException e) {
                    Log.e(TAG,"json exception",e);

                }

            }

            @Override
            public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                Log.e(TAG,"onFailure!",throwable );
            }
        });
    }

    @Override
    public void onFinishCreatetweet(Tweet tweet) {
        //modify data source
        tweets.add(0, tweet);

        //Update the adapter
        adapter.notifyItemInserted(0);

        //  Update the RV with the tweet
        rvTweets.smoothScrollToPosition(0);
    }
}