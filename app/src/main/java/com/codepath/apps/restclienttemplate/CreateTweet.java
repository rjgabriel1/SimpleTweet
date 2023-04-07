package com.codepath.apps.restclienttemplate;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.asynchttpclient.callback.JsonHttpResponseHandler;

import org.json.JSONException;
import org.parceler.Parcels;

import okhttp3.Headers;
// ...

public class CreateTweet extends DialogFragment {
    public static String TAG = "ComposeActivity";

    public static final int MAX_TWEET_LENGTH = 140;
    public  static  final  String KEY = "DRAFT";

    Context context;
    TwitterClient client;
    ImageButton close;
    private EditText mEditText;
    Button btnTweet;

    public CreateTweet() {
        // Empty constructor is required for DialogFragment
        // Make sure not to add arguments to the constructor
        // Use `newInstance` instead as shown below
    }


// interface listener for the fragment

    public interface CreateTweetListener{
        void onFinishCreatetweet(Tweet tweet);
    }


    public static CreateTweet newInstance(String title) {
        CreateTweet frag = new CreateTweet();
        Bundle args = new Bundle();
        args.putString("title", title);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.createtweet, container);

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get field from view
        client = TwitterApp.getRestClient(context);
        btnTweet = view.findViewById(R.id.btnTweet);
        mEditText = (EditText) view.findViewById(R.id.etCompose);
        close = view.findViewById(R.id.closeWindow);

        // Fetch arguments from bundle and set title
        String title = getArguments().getString("title", "Make new tweet");
        getDialog().setTitle(title);

        // Show soft keyboard automatically and request focus to field
        mEditText.requestFocus();
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().getWindow().setLayout(700,950);

//        Saving Draft
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String draft = preferences.getString(KEY,"");

        if (!draft.isEmpty()){
            mEditText.setText(draft);
        }

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { openAlert();
                dismiss();

            }
        });
    btnTweet.setOnClickListener(new View.OnClickListener() {
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
                            CreateTweetListener listener = (CreateTweetListener) getTargetFragment();
                            listener.onFinishCreatetweet(tweet);

                            Log.i(TAG, "published tweet" + tweet);

                            Edit edit   = (Edit)  getTargetFragment();
                            edit.onFinishEdit(tweet);

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

    // Alert draft
    public void openAlert(){
        AlertDialog.Builder alertDialogBuilder= new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage("Save to Draft");
        alertDialogBuilder.setPositiveButton("Keep", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) { save();}
        });
       alertDialogBuilder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
           @Override
           public void onClick(DialogInterface dialog, int which) { dismiss();}
       });
       AlertDialog dialog = alertDialogBuilder.create();
       dialog.show();
    }

//     save to draft
    private  void save(){
        String create =  mEditText.getText().toString();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(KEY,create);
        editor.commit();
        dismiss();
    }
    public  interface Edit{
        void onFinishEdit(Tweet tweet);
    }
}


