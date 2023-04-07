package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;

public class ReplyFragment extends DialogFragment {

    public static String TAG = "ReplyFragment";

    public static final int MAX_TWEET_LENGTH = 140;

    Context context;
    Button btnReply;
    TwitterClient client;
    private EditText mEditText;
    TextView tvname;
    TextView tvusername;
    ImageView profile;
    TextInputLayout counter;

    public ReplyFragment() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }


// interface listener for the fragment

    public  interface ReplyListener{
        void onFinishReply(Tweet tweet);
    }




    public static ReplyFragment newInstance(String title) {
        ReplyFragment frag = new ReplyFragment();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.replyfragment, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view
        client = TwitterApp.getRestClient(context);
        btnReply = view.findViewById(R.id.btnReply);
        mEditText = (EditText) view.findViewById(R.id.etReply);
        tvname = view.findViewById(R.id.tvname);
        tvusername = view.findViewById(R.id.tvusername);
        profile = view.findViewById(R.id.profile);
        counter = view.findViewById(R.id.counterText);

        Bundle bundle = getArguments();
        User user = Parcels.unwrap(bundle.getParcelable("profile"));
        Tweet tweet = Parcels.unwrap(bundle.getParcelable("tweets"));

        tvname.setText(user.name);
        tvusername.setText(user.screenName);
        Glide.with(getContext()).load(user.profileImageUrl).transform(new RoundedCorners(90)).into(profile);

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Make new tweet");
        getDialog().setTitle(title);

        // Show soft keyboard automatically and request focus to field
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().setLayout(700,950);

        counter.setHint("Reply to @"  + tweet.user.screenName);


        btnReply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String tweetContent = mEditText.getText().toString();
                if (tweetContent.isEmpty()){
                    Toast.makeText(context, "Sorry your tweet cannot be empty", Toast.LENGTH_LONG).show();
                    return;
                }
                if(tweetContent.length()> MAX_TWEET_LENGTH){
                    Toast.makeText(context, "Sorry your tweet is too long", Toast.LENGTH_LONG).show();
                    return;

                }



                // Make an API call to Twitter to Publish the tweet
                client.publishTweet(tweetContent, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Headers headers, JSON json) {
                        Log.i(TAG, "onSuccess to publish");
                        try {
                            Tweet tweet = Tweet.fromJson(json.jsonObject);
                            ReplyFragment.ReplyListener listener = (ReplyFragment.ReplyListener) getTargetFragment();
                            listener.onFinishReply(tweet);

                            Log.i(TAG, "published tweet" + tweet);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }

                    @Override
                    public void onFailure(int statusCode, Headers headers, String response, Throwable throwable) {
                        Log.e(TAG, "onFailure to publish", throwable);
                    }
                });
                dismiss();

            }

        });
    }


}
