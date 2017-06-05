package com.example.android.fitassistant.message;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.fitassistant.R;

import java.util.ArrayList;

/**
 * Created by Clara Matos on 26/04/2017.
 *
 * Exposes a list of message suggestions to be sent to the bot
 */
public class SuggestionsRecyclerAdapter extends RecyclerView
        .Adapter<SuggestionsRecyclerAdapter
        .CustomViewHolder> {

    private ArrayList<String> mSuggestionsList;
    private static SuggestionsClickListener mItemClickListener;

    public SuggestionsRecyclerAdapter(ArrayList<String> myDataset, SuggestionsClickListener clickListener) {
        mSuggestionsList = myDataset;
        mItemClickListener = clickListener;
    }

    public static class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView suggestionItem;

        public CustomViewHolder(View itemView) {
            super(itemView);
            suggestionItem = (TextView) itemView.findViewById(R.id.suggestion_text);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mItemClickListener.onItemClick(suggestionItem.getText().toString());
        }
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.suggestion_card
                , viewGroup, false);
        return new CustomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CustomViewHolder viewHolder, int i) {
        viewHolder.suggestionItem.setText(mSuggestionsList.get(i));
    }

    @Override
    public int getItemCount() {
        return mSuggestionsList.size();
    }


    public interface SuggestionsClickListener {
        void onItemClick(String itemContent);
    }
}
