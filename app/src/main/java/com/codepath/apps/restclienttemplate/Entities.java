package com.codepath.apps.restclienttemplate;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.parceler.Parcel;

import java.util.ArrayList;
import java.util.List;

@Parcel
@Entity
public class Entities {
    @ColumnInfo
    public String mediaUrl;

    @PrimaryKey
    @ColumnInfo
    public  long id_media;

    public Entities() {}

    public static Entities fromJson(JSONObject jsonObject) throws JSONException{
        Entities entities = new Entities();

        if (!jsonObject.has("media")){
            entities.mediaUrl ="";
        }else if(jsonObject.has("media")){

            final JSONArray mediaArr =  jsonObject.getJSONArray("media");
            entities.mediaUrl=  mediaArr.getJSONObject(0).getString("media_url_https");
            entities.id_media = mediaArr.getJSONObject(0).getLong("id");
        }
        return entities;
    }

    public static List<Entities> fromJsonTweetsArray(List<Tweet> tweetsFromNetwork) {
        List<Entities> entities =  new ArrayList<>();

        for (int i = 0; i <tweetsFromNetwork.size(); i++){
            entities.add(tweetsFromNetwork.get(i).entities);
        }
        return entities;
    }
}
