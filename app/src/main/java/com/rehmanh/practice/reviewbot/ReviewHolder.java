package com.rehmanh.practice.reviewbot;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Created by Habib_MBP on 8/26/17.
 */

public class ReviewHolder
{
    public HashMap<String, WordEntry> mReviewMap;

    public ReviewHolder(HashMap<String, WordEntry> mReviewMap)
    {
        this.mReviewMap = mReviewMap;
    }

    public String serialize()
    {
        //serialize this class into a JSON string using GSON
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    static public ReviewHolder create(String serializedData)
    {
        //use Gson to instantiate this class using the JSON representation of the state
        Gson gson = new Gson();
        return gson.fromJson(serializedData, ReviewHolder.class);
    }
}
