package org.wordpress.android.ui.themes;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.wordpress.android.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class ThemeBrowserActivityFragment extends Fragment {

    public ThemeBrowserActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_theme_browser, container, false);
    }
}
