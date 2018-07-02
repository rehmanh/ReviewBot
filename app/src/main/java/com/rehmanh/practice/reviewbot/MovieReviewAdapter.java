package com.rehmanh.practice.reviewbot;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Habib_MBP on 9/19/17.
 */

public class MovieReviewAdapter extends ArrayAdapter<UserReview> {

    private Context mContext;
    private int mResource;
    private int lastPosition = -1;

    static class ViewHolder {
        TextView name;
        TextView score;
        TextView id;
    }


    public MovieReviewAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull ArrayList<UserReview> reviewList) {
        super(context, resource, reviewList);
        mContext = context;
        mResource = resource;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        String movieName = getItem(position).getMovieName();
        String movieScore = getItem(position).getMovieScore();
        String movieNumber = (position + 1) + ".";

        UserReview userReview = new UserReview(movieName, movieScore);

        final View result; //responsible for showing animation

        ViewHolder holder;

        if(convertView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(mResource, parent, false);
            holder = new ViewHolder();
            holder.name = (TextView) convertView.findViewById(R.id.movie_name);
            holder.score = (TextView) convertView.findViewById(R.id.movie_score);
            holder.id = (TextView) convertView.findViewById(R.id.movie_number);

            result = convertView;

            convertView.setTag(holder);
        }
        else
        {
            holder = (ViewHolder) convertView.getTag();
            result = convertView;
        }

        Animation animation = AnimationUtils.loadAnimation(mContext,
                (position > lastPosition) ? R.anim.fly_in_from_left : R.anim.fly_in_from_left);

        result.startAnimation(animation);

        lastPosition = position;

        holder.name.setText(movieName);
        holder.score.setText(movieScore);
        holder.id.setText(movieNumber);

        return convertView;
    }
}
