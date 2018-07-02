package com.rehmanh.practice.reviewbot;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class ReviewActivity extends AppCompatActivity
{

    private Button mReviewButton;
    private Button mClearButton;
    private EditText mMovieName;
    private EditText mUserReviewInput;
    private TextView mReviewScoreOutput;
    private TextView mReviewSentiment;
    private ProgressBar mProgressBar;
    private HashMap<String, WordEntry> mWordMap; //here
    private ReviewHolder mBot;

    private String mScoreToPass;
    private String mMovieToPass;

    private int range = 0;

    private static final String FILENAME = "bot_data.txt";
    private static final String PREFS_NAME = "MY_PREFS_NAME";
    private static final String PREFS_KEY = "MY_PREFS_KEY";


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);
        Toolbar toolbar = (Toolbar) findViewById(R.id.review_activity_toolbar);
        setSupportActionBar(toolbar);

        mBot = new ReviewHolder(mWordMap);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        if(isFirstLaunch())
        {
            if(checkIfFileExists(FILENAME)) deleteFile(FILENAME); //statement will return true, and then the file will be deleted
            mBot.mReviewMap = buildDataSet(FILENAME);
            setWordMap(mBot);
        }
        else
        {
            mBot.mReviewMap = getWordMap();
        }

        mMovieName = (EditText) findViewById(R.id.movie_name);
        mUserReviewInput = (EditText) findViewById(R.id.user_review);
        mReviewScoreOutput = (TextView) findViewById(R.id.review_score);
        mReviewSentiment = (TextView) findViewById(R.id.review_sentiment);
        mReviewButton = (Button) findViewById(R.id.analyze_button);
        mProgressBar = (ProgressBar) findViewById(R.id.horizontal_progress_bar);

        mProgressBar.setVisibility(View.INVISIBLE);

        mReviewButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String mUserInput = mUserReviewInput.getText().toString();
                String mMovieToReview = mMovieName.getText().toString();
                double progressToSet = 0.0;

                if(StringUtils.isBlank(mUserInput)) //using StringUtils from Apache Commons
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid review", Toast.LENGTH_SHORT).show();
                    mUserReviewInput.setText(null);
                }
                else if(StringUtils.isBlank(mMovieToReview))
                {
                    Toast.makeText(getApplicationContext(), "Please enter a valid movie name", Toast.LENGTH_SHORT).show();
                    mMovieName.setText(null);
                }
                else if(StringUtils.length(mMovieToReview) > 30)
                {
                    Toast.makeText(getApplicationContext(), "Movie name is too long", Toast.LENGTH_SHORT).show();
                    mMovieName.setText(null);
                }
                else
                {
                    String mFinalScore = parseUserEntry(mUserInput, mBot.mReviewMap);
                    mScoreToPass = mFinalScore;
                    mMovieToPass = mMovieToReview;

                    writeToStorage(mUserInput + "\n", mFinalScore, FILENAME);
                    updateMap(mBot.mReviewMap, mUserInput, mFinalScore);

                    mProgressBar.setVisibility(View.VISIBLE);

                    progressToSet = Double.parseDouble(mFinalScore) * 20;

                    range = (int)progressToSet;

                    mProgressBar.setProgress(range);
                    mReviewScoreOutput.setText("Your review scored: " + range + " out of 100");

                    if(range >= 85 && range <= 100)
                        mReviewSentiment.setText("Extremely positive sentiment");
                    if(range >= 66 && range <= 84)
                        mReviewSentiment.setText("Positive sentiment");
                    if(range >= 45 && range <= 65)
                        mReviewSentiment.setText("Neutral sentiment");
                    if(range >= 21 && range <= 44)
                        mReviewSentiment.setText("Negative sentiment");
                    if(range >= 0 && range <= 20)
                        mReviewSentiment.setText("Extremely negative sentiment");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
                        if(range >= 85 && range <= 100)
                            mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.GREEN));
                        if(range >= 66 && range <= 84) //lighter green
                            mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(188, 230, 114)));
                        if(range >= 45 && range <= 65)
                            mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.YELLOW));
                        if(range >= 21 && range <= 44)
                            mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.rgb(255, 128, 0)));
                        if(range >= 0 && range <= 20)
                            mProgressBar.setProgressTintList(ColorStateList.valueOf(Color.RED));
                    }
                }
            }
        });

        mClearButton = (Button) findViewById(R.id.clear_button);
        mClearButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                mUserReviewInput.setText(null);
                mMovieName.setText(null);
                mReviewSentiment.setText(null);
                mReviewScoreOutput.setText(null);
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        String scoreToPass = range + "";

        Intent i = new Intent();
        i.putExtra("score", scoreToPass);
        i.putExtra("movie", mMovieToPass);
        setResult(RESULT_OK, i);
        finish();
    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public HashMap<String, WordEntry> buildMap(String filename)
    {
        HashMap<String, WordEntry> mMapToBuild = new HashMap<String, WordEntry>();
        double mScoreFromFile = 0.0;
        ArrayList<String> mEntries = null;
        String mReceivedString = "";
        String mReviewLine = "";

        try
        {
            FileInputStream fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);

            while((mReceivedString = br.readLine()) != null)
            {
                StringBuilder builder = new StringBuilder();
                builder.append(mReceivedString + "\n");
                mReviewLine =builder.toString();

                String[] mPlaceHolder = mReviewLine.toLowerCase().split("\\s+");
                mEntries = new ArrayList<String>(Arrays.asList(mPlaceHolder));

                mScoreFromFile = Double.parseDouble(mEntries.get(0));

                mEntries.remove(0);

                for(int i = 0; i < mEntries.size(); i++)
                {
                    if(mMapToBuild.containsKey(mEntries.get(i)))
                    {
                        WordEntry mPreviousWord = mMapToBuild.get(mEntries.get(i));
                        mPreviousWord.updateEntry(mScoreFromFile);
                    }
                    else
                    {
                        mMapToBuild.put(mEntries.get(i), new WordEntry(mEntries.get(i), mScoreFromFile, 1));
                    }
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException io)
        {
            io.printStackTrace();
        }
        return mMapToBuild;
    }

    public void buildFile(String filename)
    {
        String mPlaceHolderString = "";
        String mLineToAdd = "";

        try
        {
            InputStream is = getAssets().open("data.txt");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);

            while((mPlaceHolderString = br.readLine()) != null)
            {
                FileOutputStream fileOutputStream = openFileOutput(filename, MODE_APPEND);

                StringBuilder builder = new StringBuilder();
                builder.append(mPlaceHolderString + "\n");
                mLineToAdd = builder.toString();

                fileOutputStream.write(mLineToAdd.getBytes());
                fileOutputStream.close();
            }
            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    // will take a string and a hashtable
    public String parseUserEntry(String userReview, HashMap<String, WordEntry> dataSet)
    {
        double runningTotal = 0.0;

        userReview = formatString(userReview);

        String[] temp = userReview.toLowerCase().split("\\s+");

        ArrayList<String> words = new ArrayList<String>(Arrays.asList(temp));

        for(int i = 0; i < words.size(); i++)
        {
            if(dataSet.containsKey(words.get(i)))
            {
                runningTotal += dataSet.get(words.get(i)).getScore();
            }
            else runningTotal += 3.5;
        }

        double average = runningTotal/words.size();
        DecimalFormat df = new DecimalFormat("#.##");
        average = Double.valueOf(df.format(average));

        return Double.toString(average);
    }

    public void updateMap(HashMap<String, WordEntry> mMapToUpdate, String review, String score)
    {
        ArrayList<String> arrayList = null;
        String[] holder = review.toLowerCase().split("\\s+");
        arrayList = new ArrayList<String>(Arrays.asList(holder));
        double mUpdatedScore = Double.parseDouble(score);

        for(int i = 0; i < arrayList.size(); i++)
        {
            if(mMapToUpdate.containsKey(arrayList.get(i)))
            {
                WordEntry mPrevious = mMapToUpdate.get(arrayList.get(i));
                mPrevious.updateEntry(mUpdatedScore);
            }
            else
            {
                mMapToUpdate.put(arrayList.get(i), new WordEntry(arrayList.get(i), mUpdatedScore, 1));
            }
        }
    }

    public void readFromStorage(String filename)
    {
        try
        {
            String line = "";
            FileInputStream fileInputStream = openFileInput(filename);
            InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
            BufferedReader br = new BufferedReader(inputStreamReader);
            StringBuilder buffer = new StringBuilder();

            while((line = br.readLine()) != null)
            {
                buffer.append(line + "\n");
            }

            mReviewScoreOutput.setText(buffer.toString());
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public void writeToStorage(String userReview, String scoreToAdd, String filename) //, HashMap<String, WordEntry> mapToAppend
    {
        String temp = scoreToAdd + " " + userReview;

        try
        {
            FileOutputStream fileOutputStream = openFileOutput(filename, MODE_APPEND);
            fileOutputStream.write(temp.getBytes()); //CHANGE THIS TO TEMP
            fileOutputStream.close();
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch(IOException ex)
        {
            ex.printStackTrace();
        }
    }

    public HashMap<String, WordEntry> buildDataSet(String filename)
    {
        HashMap<String, WordEntry> mMapToBuild = new HashMap<String, WordEntry>();
        double mScoreFromFile = 0.0;
        String mReviewLine = "";
        ArrayList<String> mEntries = null;
        String mReceivedString = "";

        try
        {
            InputStream is = getAssets().open("data.txt");
            InputStreamReader reader = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(reader);

            while ((mReceivedString = br.readLine()) != null)
            {
                FileOutputStream fileOutputStream = openFileOutput(filename, MODE_APPEND);

                StringBuilder builder = new StringBuilder(); //new stringbuilder must be called here so that it gets re-instantiated each time the loop runs
                builder.append(mReceivedString + "\n");
                mReviewLine = builder.toString();

                fileOutputStream.write(mReviewLine.getBytes());
                fileOutputStream.close();

                //Toast.makeText(getApplicationContext(), "String written", Toast.LENGTH_SHORT).show(); //leaving this here for now

                String[] mPlaceholder = mReviewLine.toLowerCase().split("\\s+");

                mEntries = new ArrayList<String>(Arrays.asList(mPlaceholder));

                mScoreFromFile = Double.parseDouble(mEntries.get(0));

                mEntries.remove(0);

                for(int i = 0; i < mEntries.size(); i++)
                {
                    if(mMapToBuild.containsKey(mEntries.get(i)))
                    {
                        WordEntry mPreviousWord = mMapToBuild.get(mEntries.get(i));
                        mPreviousWord.updateEntry(mScoreFromFile);
                    }
                    else
                    {
                        mMapToBuild.put(mEntries.get(i), new WordEntry(mEntries.get(i), mScoreFromFile, 1));
                    }
                }
            }
            is.close();
        }
        catch (FileNotFoundException f)
        {
            f.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return mMapToBuild;
    }

    public String formatString(String reviewToFormat)
    {
        String dot = ".";
        String comma = ",";
        String excl = "!";


        return reviewToFormat.replace(dot, "").replace(comma, "").replace(excl, "");
    }

    public boolean deleteFile(String filename)
    {
        File dir = getFilesDir();

        File file = new File(dir, filename);

        return file.delete();
    }

    public boolean checkIfFileExists(String filename)
    {
        File dir = getFilesDir();

        File file = new File(dir, filename);

        return file.exists();
    }

    public boolean isFirstLaunch()
    {
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        boolean ranBefore = prefs.getBoolean("ran before", false);

        if(!ranBefore) //first time
        {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("ran before", true);
            editor.commit();
        }

        return !ranBefore;
    }

    public HashMap<String, WordEntry> getWordMap()
    {
        SharedPreferences sharedPreferences = this.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        String serializedDataFromPreferences = sharedPreferences.getString(PREFS_KEY, null);

        ReviewHolder mRestoredHolder = ReviewHolder.create(serializedDataFromPreferences);

        return mRestoredHolder.mReviewMap;
    }

    public void setWordMap(ReviewHolder mHolderToSet)
    {
        String serializedData = mHolderToSet.serialize();

        SharedPreferences prefsReader = this.getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);

        SharedPreferences.Editor mEditor = prefsReader.edit();

        mEditor.putString(PREFS_KEY, serializedData);

        mEditor.commit();

    }
}
