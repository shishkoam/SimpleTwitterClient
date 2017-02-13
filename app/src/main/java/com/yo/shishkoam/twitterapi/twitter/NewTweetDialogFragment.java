package com.yo.shishkoam.twitterapi.twitter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.yo.shishkoam.twitterapi.R;

import retrofit2.Call;

/**
 * Created by User on 13.02.2017 to save dialog during rotation
 */

public class NewTweetDialogFragment extends DialogFragment {
    private Context context;
    private static final int MAX_CHARACTERS = 140;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View content = LayoutInflater.from(getActivity()).inflate(R.layout.tweet_layout, null);
        final EditText tweetEditText = (EditText) content.findViewById(R.id.message_edit_text);
        final TextView numbersLeftTextView = (TextView) content.findViewById(R.id.n_chars_text_view);
        tweetEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                numbersLeftTextView.setText(
                        getString(R.string.left_n_characters, MAX_CHARACTERS - s.length()));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(content)
                .setPositiveButton(R.string.publish_tweet, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        publishTweet(tweetEditText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setTitle(R.string.new_tweet);
        return builder.create();
    }

    private void publishTweet(String status) {
        if (status.length() == 0) {
            Toast.makeText(context, R.string.empty_status, Toast.LENGTH_SHORT).show();
            return;
        }
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();
        Call<Tweet> call = statusesService.update(status, null, null, null, null, null, null, null, null);
        call.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> tweetResult) {
                Toast.makeText(context, R.string.status_published, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void failure(TwitterException e) {
                Toast.makeText(context, R.string.failed_to_publish, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
