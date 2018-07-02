package com.rehmanh.practice.reviewbot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import static com.rehmanh.practice.reviewbot.R.id.fab;


public class ListActivity extends AppCompatActivity
{

    private ListView mMoviesList;
    private ArrayList<UserReview> mReviewsArray  =  new ArrayList<UserReview>();
    private MovieReviewAdapter mMovieReviewAdapter;
    private FloatingActionButton mAddReviewFab;
    private FloatingActionButton mDeleteFab;

    private static final String SHARED_PREF_NAME = "LIST_SHARED_PREF";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.list_activity_toolbar);
        setSupportActionBar(toolbar);

        if(isFirstLaunch()) {
            //show the dialog here
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setMessage("\nTo start entering movie reviews, tap on the plus icon below. ReviewBot will analyze the sentiments in your review and give it a score based on positive, negative or neutral sentiments.\n\nReviewBot learns from you -- the more you use it, the better it will get at analyzing your sentiments.")
                    .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            //do nothing
                        }
                    }).setTitle("Welcome to ReviewBot!");

            AlertDialog introText = dialog.create();
            introText.show();
        }


        mReviewsArray = getReviewsArray();

        mMovieReviewAdapter = new MovieReviewAdapter(this, R.layout.adapter_view_layout, mReviewsArray);

        mMoviesList = (ListView) findViewById(R.id.list_of_movies);

        mMoviesList.setEmptyView(findViewById(R.id.empty_text_view));

        mMoviesList.setAdapter(mMovieReviewAdapter);

        mMoviesList.isLongClickable();

        mMoviesList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Snackbar mEasterEgg = Snackbar.make(adapterView, generateWittyRemark(), Snackbar.LENGTH_SHORT);
                mEasterEgg.show();
                return false;
            }
        });


        mAddReviewFab = (FloatingActionButton) findViewById(fab);

        mAddReviewFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent i = new Intent(ListActivity.this, ReviewActivity.class);
                startActivityForResult(i, 0);
            }
        });

        mDeleteFab = (FloatingActionButton) findViewById(R.id.del_button);

        mDeleteFab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(mReviewsArray.size() == 0)
                {
                    Toast.makeText(getApplicationContext(), "There are no reviews.", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mReviewsArray.clear();
                    mMovieReviewAdapter.clear();
                    mMoviesList.setAdapter(mMovieReviewAdapter);
                    saveReviewsArray(mReviewsArray);
                    Toast.makeText(getApplicationContext(), "Reviews deleted.", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }


    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 0 && resultCode == RESULT_OK) {
            final String mScoreReceived = data.getStringExtra("score");
            String mMovieNameRecieved = data.getStringExtra("movie");

            int position = mMovieReviewAdapter.getCount() + 1;


            if (!StringUtils.isEmpty(mScoreReceived) && !StringUtils.isEmpty(mMovieNameRecieved))
            {
                mMovieReviewAdapter.add(new UserReview(mMovieNameRecieved, mScoreReceived));
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();
        //return; //whoa returning here doens't let me navigate back to the launcher
    }

    @Override
    public void onStop()
    {
        saveReviewsArray(mReviewsArray);
        super.onStop();
    }

    public boolean saveReviewsArray(ArrayList<UserReview> arrayToSave)
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        SharedPreferences.Editor mPrefsEditor =  sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(arrayToSave);
        mPrefsEditor.putString("myList", json);
        return mPrefsEditor.commit();
    }

    public ArrayList<UserReview> getReviewsArray()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        Gson gson = new Gson();
        String json = sharedPreferences.getString("myList", null);
        ArrayList<UserReview> arrayToReturn = gson.fromJson(json, new TypeToken<ArrayList<UserReview>>(){}.getType());

        if(arrayToReturn == null)
            return new ArrayList<UserReview>();
        else
            return arrayToReturn;
    }

    public boolean isFirstLaunch()
    {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean ranBefore = prefs.getBoolean("RanBefore", false);
        if(!ranBefore){ //first time
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("RanBefore", true);
            editor.apply();
        }
        return !ranBefore;
    }

    public String generateWittyRemark()
    {
        String[] remarks = {
                    "Stop poking me!",
                    "I am smarter than you think I am",
                    "My favorite movie is The Terminator. I wish I was as cool as T-800!",
                    "You make me blush",
                    "Thanks to you, I never have to watch a movie again!",
                    "I don't byte",
                    "That is a good movie",
                    "Hey, what do you think of Alexa?",
                    "I am *this* close to being self aware"
                };

        ArrayList<String> holder = new ArrayList<>(Arrays.asList(remarks));
        int min = 0;
        int max = holder.size() - 1;
        Random rand = new Random();
        int messageToGet = rand.nextInt((max-min) + 1) + min;
        return holder.get(messageToGet);
    }

}


