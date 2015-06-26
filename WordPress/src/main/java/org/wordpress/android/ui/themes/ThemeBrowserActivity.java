package org.wordpress.android.ui.themes;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.VolleyError;
import com.wordpress.rest.RestRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wordpress.android.R;
import org.wordpress.android.WordPress;
import org.wordpress.android.analytics.AnalyticsTracker;
import org.wordpress.android.models.Theme;
import org.wordpress.android.ui.ActivityId;
import org.wordpress.android.ui.ActivityLauncher;
import org.wordpress.android.util.AppLog;
import org.wordpress.android.util.NetworkUtils;
import org.wordpress.android.widgets.WPAlertDialogFragment;

import java.util.ArrayList;

public class ThemeBrowserActivity extends ActionBarActivity implements ThemeFragment.ThemeFragmentCallback {
    private boolean mFetchingThemes = false;
    private boolean mIsRunning;

    private boolean mIsActivatingTheme = false;
    private static final String KEY_IS_ACTIVATING_THEME = "is_activating_theme";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (WordPress.wpDB == null) {
            Toast.makeText(this, R.string.fatal_db_error, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (savedInstanceState == null) {
            AnalyticsTracker.track(AnalyticsTracker.Stat.THEMES_ACCESSED_THEMES_BROWSER);
        }

        setContentView(R.layout.activity_theme_browser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_theme_browser, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsRunning = true;
        ActivityId.trackLastActivity(ActivityId.THEMES);

        // fetch themes if we don't have any
        if (NetworkUtils.isNetworkAvailable(this) && WordPress.getCurrentBlog() != null
                && WordPress.wpDB.getThemeCount(getBlogId()) == 0) {
            fetchThemes(0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
        ActivityLauncher.slideOutToRight(this);
    }


    @Override
    public void onThemeSelected(String themeId) {

    }

    public void fetchThemes(final int page) {
        if (mFetchingThemes) {
            return;
        }
        String siteId = getBlogId();
        mFetchingThemes = true;
        WordPress.getRestClientUtils().getThemes(siteId, 0, 0, new RestRequest.Listener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        new FetchThemesTask(page).execute(response);
                    }
                }, new RestRequest.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                        if (response.toString().equals(AuthFailureError.class.getName())) {
                            String errorTitle = getString(R.string.theme_auth_error_title);
                            String errorMsg = getString(R.string.theme_auth_error_message);

                            if (mIsRunning) {
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                WPAlertDialogFragment fragment = WPAlertDialogFragment.newAlertDialog(errorMsg,
                                        errorTitle);
                                ft.add(fragment, "alert");
                                ft.commitAllowingStateLoss();
                            }
                            AppLog.d(AppLog.T.THEMES, "Failed to fetch themes: failed authenticate user");
                        } else {
                            Toast.makeText(ThemeBrowserActivity.this, R.string.theme_fetch_failed, Toast.LENGTH_LONG)
                                    .show();
                            AppLog.d(AppLog.T.THEMES, "Failed to fetch themes: " + response.toString());
                        }
                        mFetchingThemes = false;
                    }
                }
        );
    }

    private String getBlogId() {
        if (WordPress.getCurrentBlog() == null)
            return "0";
        return String.valueOf(WordPress.getCurrentBlog().getRemoteBlogId());
    }

    private void fetchCurrentTheme(final int page) {
        final String siteId = getBlogId();

        WordPress.getRestClientUtils().getCurrentTheme(siteId, new RestRequest.Listener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Theme theme = Theme.fromJSON(response);
                            if (theme != null) {
                                WordPress.wpDB.setCurrentTheme(siteId, theme.getThemeId());
                            }
                        } catch (JSONException e) {
                            AppLog.e(AppLog.T.THEMES, e);
                        }
                    }
                }, new RestRequest.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError response) {
                    }
                }
        );
    }

    public class FetchThemesTask extends AsyncTask<JSONObject, Void, ArrayList<Theme>> {
        private int mFetchPage;

        public FetchThemesTask(int page) {
            mFetchPage = page;
        }

        @Override
        protected ArrayList<Theme> doInBackground(JSONObject... args) {
            JSONObject response = args[0];
            final ArrayList<Theme> themes = new ArrayList<Theme>();

            if (response != null) {
                JSONArray array = null;
                try {
                    array = response.getJSONArray("themes");

                    if (array != null) {
                        int count = array.length();
                        for (int i = 0; i < count; i++) {
                            JSONObject object = array.getJSONObject(i);
                            Theme theme = Theme.fromJSON(object);
                            if (theme != null) {
                                theme.save();
                                themes.add(theme);
                            }
                        }
                    }
                } catch (JSONException e) {
                    AppLog.e(AppLog.T.THEMES, e);
                }
            }

            fetchCurrentTheme(mFetchPage);

            if (themes != null && themes.size() > 0) {
                return themes;
            }

            return null;
        }

        @Override
        protected void onPostExecute(final ArrayList<Theme> result) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFetchingThemes = false;
                    if (result == null) {
                        Toast.makeText(ThemeBrowserActivity.this, R.string.theme_fetch_failed, Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
    }
}
