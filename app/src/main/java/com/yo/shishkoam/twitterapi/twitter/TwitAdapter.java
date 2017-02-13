package com.yo.shishkoam.twitterapi.twitter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TweetTimelineListAdapter;
import com.twitter.sdk.android.tweetui.internal.OverlayImageView;
import com.yo.shishkoam.twitterapi.R;

/**
 * Created by User on 11.02.2017
 */

public class TwitAdapter extends TweetTimelineListAdapter {

    private OnAuthorClickCallback onAuthorClickCallback;

    public TwitAdapter(Context context, Timeline<Tweet> timeline) {
        super(context, timeline);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        //disable subviews to avoid links are clickable
        if (view instanceof ViewGroup) {
            disableViewAndSubViews((ViewGroup) view);
        }

        View.OnClickListener onAuthorClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Tweet tweet = getItem(position).retweetedStatus;
                if (tweet != null) {
                    String newUser = tweet.user.screenName;
                    onAuthorClickCallback.onAuthorClick(newUser);
                }
            }
        };

        //set author click listeners
        initView(view.findViewById(R.id.tw__tweet_author_avatar), onAuthorClickListener);
        initView(view.findViewById(R.id.tw__tweet_author_full_name), onAuthorClickListener);
        initView(view.findViewById(R.id.tw__tweet_author_screen_name), onAuthorClickListener);

        //enable links in main text
        view.findViewById(R.id.tw__tweet_text).setEnabled(true);
        return view;
    }

    private void initView(View view, View.OnClickListener onAuthorClickListener) {
        view.setEnabled(true);
        view.setOnClickListener(onAuthorClickListener);
    }

    public void setOnAuthorClickCallback(final OnAuthorClickCallback onAuthorClickCallback) {
        this.onAuthorClickCallback = onAuthorClickCallback;
    }

    //helper method to disable subviews
    private void disableViewAndSubViews(ViewGroup layout) {
        layout.setEnabled(false);
        layout.setClickable(false);
        layout.setLongClickable(false);
        for (int i = 0; i < layout.getChildCount(); i++) {
            View child = layout.getChildAt(i);
            if (child instanceof ViewGroup) {
                disableViewAndSubViews((ViewGroup) child);
            } else if (!(child instanceof OverlayImageView)) {
                child.setEnabled(false);
                child.setClickable(false);
                child.setLongClickable(false);
            }
        }
    }

}