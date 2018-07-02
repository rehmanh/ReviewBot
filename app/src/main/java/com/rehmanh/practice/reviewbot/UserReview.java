package com.rehmanh.practice.reviewbot;

/**
 * Created by Habib_MBP on 9/19/17.
 */

public class UserReview
{
    private String mMovieName;
    private String mMovieScore;

    public UserReview(String mMovieName, String mMovieScore)
    {
        this.mMovieName = mMovieName;
        this.mMovieScore = mMovieScore;
    }

    public String getMovieName() {
        return mMovieName;
    }

    public void setMovieName(String movieName) {
        mMovieName = movieName;
    }

    public String getMovieScore() {
        return mMovieScore;
    }

    public void setMovieScore(String movieScore) {
        mMovieScore = movieScore;
    }
}
