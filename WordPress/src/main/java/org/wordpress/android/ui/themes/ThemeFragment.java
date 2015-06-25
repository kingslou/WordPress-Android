package org.wordpress.android.ui.themes;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;

import org.wordpress.android.R;

import org.wordpress.android.WordPress;
import org.wordpress.android.ui.themes.dummy.DummyContent;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.util.helpers.SwipeToRefreshHelper;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class ThemeFragment extends Fragment implements AdapterView.OnItemClickListener, AbsListView.RecyclerListener {

    public interface ThemeTabFragmentCallback {
        public void onThemeSelected(String themeId);
    }

    protected static final String ARGS_SORT = "ARGS_SORT";
    protected static final String ARGS_PAGE = "ARGS_PAGE";
    protected static final String BUNDLE_SCROLL_POSTION = "BUNDLE_SCROLL_POSTION";

    protected GridView mGridView;
    protected TextView mEmptyView;
    protected TextView mNoResultText;
    protected ThemeTabAdapter mAdapter;
    protected ThemeFragment.ThemeFragmentCallback mCallback;
    protected int mSavedScrollPosition = 0;
    private boolean mShouldRefreshOnStart;

    public interface ThemeFragmentCallback {
        public void onThemeSelected(String themeId);
    }

    // TODO: Rename and change types of parameters
    public static ThemeFragment newInstance(String param1, String param2) {
        ThemeFragment fragment = new ThemeFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_SORT, param1);
        args.putString(ARGS_PAGE, param2);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ThemeFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.theme_tab_fragment, container, false);

        setRetainInstance(true);

        mNoResultText = (TextView) view.findViewById(R.id.theme_no_search_result_text);
        mEmptyView = (TextView) view.findViewById(R.id.text_empty);
        mGridView = (GridView) view.findViewById(R.id.theme_gridview);
        mGridView.setRecyclerListener(this);

        restoreState(savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Cursor cursor = fetchThemes(getThemeSortType());
        if (cursor == null) {
            return;
        }
        mAdapter = new ThemeTabAdapter(getActivity(), cursor, false);
        setEmptyViewVisible(mAdapter.getCount() == 0);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(this);
        mGridView.setSelection(mSavedScrollPosition);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (ThemeFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mGridView != null)
            outState.putInt(BUNDLE_SCROLL_POSTION, mGridView.getFirstVisiblePosition());
    }

    private ThemeTabFragment.ThemeSortType getThemeSortType() {
        return ThemeTabFragment.ThemeSortType.NEWEST;
    }

    private void setEmptyViewVisible(boolean visible) {
        if (getView() == null || !isAdded()) {
            return;
        }
        mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        mGridView.setVisibility(visible ? View.GONE : View.VISIBLE);
        if (visible && !NetworkUtils.isNetworkAvailable(getActivity())) {
            mEmptyView.setText(R.string.no_network_title);
        }
    }

    /**
     * Fetch themes for a given ThemeSortType.
     *
     * @return a db Cursor or null if current blog is null
     */
    private Cursor fetchThemes(ThemeTabFragment.ThemeSortType themeSortType) {
        if (WordPress.getCurrentBlog() == null) {
            return null;
        }
        String blogId = String.valueOf(WordPress.getCurrentBlog().getRemoteBlogId());
        switch (themeSortType) {
            case POPULAR:
                return WordPress.wpDB.getThemesPopularity(blogId);
            case NEWEST:
                return WordPress.wpDB.getThemesNewest(blogId);
            case TRENDING:
            default:
                return WordPress.wpDB.getThemesTrending(blogId);
        }
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mSavedScrollPosition = savedInstanceState.getInt(BUNDLE_SCROLL_POSTION, 0);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = ((ThemeTabAdapter) parent.getAdapter()).getCursor();
        String themeId = cursor.getString(cursor.getColumnIndex("themeId"));
        mCallback.onThemeSelected(themeId);
    }

    @Override
    public void onMovedToScrapHeap(View view) {
        // cancel image fetch requests if the view has been moved to recycler.

        NetworkImageView niv = (NetworkImageView) view.findViewById(R.id.theme_grid_item_image);
        if (niv != null) {
            // this tag is set in the ThemeTabAdapter class
            ThemeTabAdapter.ScreenshotHolder tag =  (ThemeTabAdapter.ScreenshotHolder) niv.getTag();
            if (tag != null && tag.requestURL != null) {
                // need a listener to cancel request, even if the listener does nothing
                ImageLoader.ImageContainer container = WordPress.imageLoader.get(tag.requestURL, new ImageLoader.ImageListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) { }

                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) { }

                });
                container.cancelRequest();
            }
        }
    }
}
