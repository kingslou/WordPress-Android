//package org.wordpress.android.ui.themes;
//
//import android.app.Activity;
//import android.support.v4.app.Fragment;
//import android.os.Bundle;
//import android.support.v7.widget.RecyclerView;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.AdapterView;
//import android.widget.GridView;
//import android.widget.TextView;
//
//import org.wordpress.android.R;
//import org.wordpress.android.util.helpers.SwipeToRefreshHelper;
//
///**
// * A placeholder fragment containing a simple view.
// */
//public class ThemeBrowserActivityFragment extends Fragment implements AdapterView.OnItemClickListener, RecyclerView.RecyclerListener {
//    protected GridView mGridView;
//    protected TextView mEmptyView;
//    protected TextView mNoResultText;
//    protected ThemeTabAdapter mAdapter;
//    protected ThemeTabFragment.ThemeTabFragmentCallback mCallback;
//    protected int mSavedScrollPosition = 0;
//    private boolean mShouldRefreshOnStart;
//    private SwipeToRefreshHelper mSwipeToRefreshHelper;
//
//    public interface ThemeTabFragmentCallback {
//        public void onThemeSelected(String themeId);
//    }
//
//    public ThemeBrowserActivityFragment() {
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        return inflater.inflate(R.layout.fragment_theme_browser, container, false);
//    }
//
//    @Override
//    public void onAttach(Activity activity) {
//        super.onAttach(activity);
//
//        try {
//            mCallback = (ThemeTabFragmentCallback) activity;
//        } catch (ClassCastException e) {
//            throw new ClassCastException(activity.toString() + " must implement ThemeTabFragmentCallback");
//        }
//    }
//}
