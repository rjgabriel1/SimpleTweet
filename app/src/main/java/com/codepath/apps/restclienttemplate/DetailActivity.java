package com.codepath.apps.restclienttemplate;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;

import org.parceler.Parcel;
import org.parceler.Parcels;

public class DetailActivity extends AppCompatActivity {
    ImageView ivProfile;
    TextView screenName1;
    TextView tvUserName1;
    TextView tvBody1;
    TextView tvdate;
    TextView amountLike;
    TextView amountRetweet;
    TextView tvlike;
    TextView tvretweet;
    TextView tvliked;
    TextView tvretweeted;
    ImageView imageBody2;
    EditText editText;

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent i = new Intent(DetailActivity.this,TimelineActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityIfNeeded(i, 0);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);


        Toolbar toolbar = findViewById(R.id.toolBar);
        setSupportActionBar(toolbar);

        //Display toolbar's icons

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.arrow_back);
        getSupportActionBar().setLogo(R.drawable.ic_twitter);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(" Tweet");


        ivProfile = findViewById(R.id.ivProfile);
        screenName1 = findViewById(R.id.screenName1);
        tvUserName1 =findViewById(R.id.tvUserName1);
        tvBody1 = findViewById(R.id.tvBody1);
        tvdate = findViewById(R.id.tvdate);
        amountLike = findViewById(R.id.amountLike);
        amountRetweet = findViewById(R.id.amountRetweet);
        tvlike = findViewById(R.id.tvlike1);
        tvretweet = findViewById(R.id.tvretweet);
        imageBody2= findViewById(R.id.imageBody2);
        tvliked = findViewById(R.id.tvliked);
        tvretweeted = findViewById(R.id.tvretweetD);
        editText = findViewById(R.id.editText);

        Tweet tweet = Parcels.unwrap(getIntent().getParcelableExtra("Tweet"));
        screenName1.setText(tweet.user.name);
        tvUserName1.setText(tweet.user.screenName);
        tvBody1.setText(tweet.body);
        amountLike.setText(tweet.favorite + " Likes");
        amountRetweet.setText(tweet.retweet + " Retweets");
        editText.setHint("Reply"+ tweet.user.name);

        if (tweet.retweeted){
            tvretweeted.setVisibility(View.VISIBLE);
            tvretweet.setVisibility(View.INVISIBLE);}

        if (tweet.favorited){
            tvliked.setVisibility(View.VISIBLE);
            tvlike.setVisibility(View.INVISIBLE);
        }

        //retweet hover
        if (tweet.retweeted){
            tvretweeted.setVisibility(View.VISIBLE);
            tvretweet.setVisibility(View.INVISIBLE);
        }else{
            tvretweeted.setVisibility(View.INVISIBLE);
            tvretweet.setVisibility(View.VISIBLE);
        }

        //     Onclick for  retweet  icon
        tvretweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet.retweet ++;
                tvretweeted.setVisibility(View.VISIBLE);
                tvretweet.setVisibility(View.INVISIBLE);
                tvretweeted.setText(tweet.getRetweet());

            }
        });

        tvretweeted.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet.retweet --;
                tvretweeted.setVisibility(View.INVISIBLE);
                tvretweet.setVisibility(View.VISIBLE);
                tvretweet.setText(tweet.getRetweet());

            }
        });




        if (tweet.favorited){
            tvliked.setVisibility(View.VISIBLE);
            tvlike.setVisibility(View.INVISIBLE);
        }else{
            tvliked.setVisibility(View.INVISIBLE);
            tvlike.setVisibility(View.VISIBLE);
        }

        // onClick
        tvlike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet.favorite ++;
                tvliked.setVisibility(View.VISIBLE);
                tvlike.setVisibility(View.INVISIBLE);
                tvliked.setText(tweet.getFavorite());

            }
        });

        tvliked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tweet.favorite --;
                tvliked.setVisibility(View.INVISIBLE);
                tvlike.setVisibility(View.VISIBLE);
                tvlike.setText(tweet.getFavorite());

            }
        });





        if(!tweet.entities.mediaUrl.isEmpty()) {
            imageBody2.setVisibility(View.VISIBLE);
            Glide.with(this).load(tweet.entities.mediaUrl).transform(new RoundedCorners(10)).into(imageBody2);
        }
            tvdate.setText(tweet.getFormattedTime(tweet.createdAt));
        Glide.with(this).load(tweet.user.profileImageUrl).transform(new RoundedCorners(90)).into(ivProfile);



    }
}