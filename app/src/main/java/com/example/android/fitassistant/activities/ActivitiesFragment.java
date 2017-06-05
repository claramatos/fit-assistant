package com.example.android.fitassistant.activities;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.support.v4.app.LoaderManager;

import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import com.example.android.fitassistant.R;
import com.example.android.fitassistant.data.ActivityContract.ActivityEntry;


/**
 * Encapsulates fetching the activities data and displaying it as a {@link ListView} layout.
 */
public class ActivitiesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "ActivitiesFragment";
    private static final int URL_LOADER = 0;

    private OnButtonSelectedListener mButtonListener;
    private ActivityCursorAdapter mCursorAdapter;

    // These are the Contacts rows that we will retrieve
    static final String[] PROJECTION = new String[]{ActivityEntry.COLUMN_ACTIVITY_ID,
            ActivityEntry.COLUMN_ACTIVITY_NAME,
            ActivityEntry.COLUMN_ACTIVITY_TIME,
            ActivityEntry.COLUMN_ACTIVITY_TYPE,
            ActivityEntry.COLUMN_ACTIVITY_DURATION,
            ActivityEntry.COLUMN_ACTIVITY_DISTANCE,
            ActivityEntry.COLUMN_ACTIVITY_WEATHER};

    // interface to notify the activity that the floating buttons were clicked
    public interface OnButtonSelectedListener {
        void onChatButtonClick();

        void onStartButtonClick();

        void onAddButtonClick();
    }

    public ActivitiesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_activities, container, false);

        final FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab_chat);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonListener.onChatButtonClick();
            }
        });

        final FloatingActionButton fabAct = (FloatingActionButton) view.findViewById(R.id.fab_act);
        fabAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonListener.onStartButtonClick();
            }
        });

        final FloatingActionButton fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mButtonListener.onAddButtonClick();
            }
        });


        ListView petListView = (ListView) view.findViewById(R.id.list);

        mCursorAdapter = new ActivityCursorAdapter(getContext(), null);

        petListView.setAdapter(mCursorAdapter);

        View emptyView = view.findViewById(R.id.empty_view);
        petListView.setEmptyView(emptyView);

        // when a list item is clicked the EditorActivity is started with the data from this item
        // to allow edition and parameter modification
        petListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), EditorActivity.class);
                Uri contentActUri = ContentUris.withAppendedId(ActivityEntry.CONTENT_URI, id);
                intent.setData(contentActUri);

                startActivity(intent);
            }
        });

        // when listview is being scrolled hide the floating buttons
        petListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
            }

            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    Log.i(TAG, "scrolling stopped");
                    fab.setVisibility(View.VISIBLE);
                    fabAct.setVisibility(View.VISIBLE);
                    fabAdd.setVisibility(View.VISIBLE);
                }

                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    Log.i(TAG, "scrolling started");
                    fab.setVisibility(View.INVISIBLE);
                    fabAct.setVisibility(View.INVISIBLE);
                    fabAdd.setVisibility(View.INVISIBLE);
                }
            }
        });

        return view;
    }

    /**
     * To ensure that the activity that created this fragment implements the OnButtonSelectedListener
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity activity = (Activity) context;
            if (activity instanceof OnButtonSelectedListener) {
                mButtonListener = (OnButtonSelectedListener) activity;
            } else {
                throw new ClassCastException(activity.toString()
                        + " must implement OnNameSelectedListener");
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "onActivityCreated");
        getLoaderManager().initLoader(URL_LOADER, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        switch (loaderID) {
            case URL_LOADER:
                // Returns a new CursorLoader
                return new CursorLoader(
                        getContext(),   // Parent activity context
                        ActivityEntry.CONTENT_URI,        // Table to query
                        PROJECTION,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

}
