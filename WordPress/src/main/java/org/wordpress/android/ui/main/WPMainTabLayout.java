package org.wordpress.android.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.design.widget.TabLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;

import org.wordpress.android.R;
import org.wordpress.android.ui.notifications.utils.SimperiumUtils;

/**
 * tab layout for main activity
 */
public class WPMainTabLayout extends TabLayout {
    private boolean mIsCheckingNoteBadge;

    public WPMainTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public WPMainTabLayout(Context context) {
        super(context);
    }

    public WPMainTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /*
     * creates a unique string to use as a tag for tab-specific badges
     */
    private static String makeBadgeTag(int position) {
        return "wpmaintablayout-badgeview-" + Integer.toString(position);
    }

    public void createTabs() {
        addTab(R.drawable.main_tab_sites, R.string.tabbar_accessibility_label_my_site);
        addTab(R.drawable.main_tab_reader, R.string.reader);
        addTab(R.drawable.main_tab_me, R.string.tabbar_accessibility_label_me);
        addTab(R.drawable.main_tab_notifications, R.string.notifications);
        checkNoteBadge();
        // TODO: mTabLayout.setSelectedIndicatorColor(getResources().getColor(R.color.tab_indicator));
    }

    private void addTab(@DrawableRes int iconId, @StringRes int contentDescriptionId) {
        View customView = LayoutInflater.from(getContext()).inflate(R.layout.tab_icon, (ViewGroup) null);

        ImageView icon = (ImageView) customView.findViewById(R.id.tab_icon);
        icon.setImageResource(iconId);
        icon.setContentDescription(getContext().getString(contentDescriptionId));

        View badge = customView.findViewById(R.id.tab_badge);
        badge.setTag(makeBadgeTag(this.getTabCount()));

        TabLayout.Tab tab = newTab();
        tab.setCustomView(customView);
        addTab(tab);
    }

    void checkNoteBadge() {
        if (mIsCheckingNoteBadge) {
            return;
        }

        mIsCheckingNoteBadge = true;
        new Thread() {
            @Override
            public void run() {
                final boolean hasUnreadNotes = SimperiumUtils.hasUnreadNotes();
                boolean isBadged = isBadged(WPMainTabAdapter.TAB_NOTIFS);
                if (isBadged == hasUnreadNotes) {
                    mIsCheckingNoteBadge = false;
                    return;
                }
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        setBadge(WPMainTabAdapter.TAB_NOTIFS, hasUnreadNotes);
                        mIsCheckingNoteBadge = false;
                    }
                });
            }
        }.start();
    }

    /*
     * adds or removes a badge for the tab at the passed index - only enabled when showing icons
     */
    public void setBadge(int position, boolean isBadged) {
        final View badgeView = findViewWithTag(makeBadgeTag(position));
        if (badgeView == null) {
            return;
        }
        boolean wasBadged = (badgeView.getVisibility() == View.VISIBLE);
        if (isBadged == wasBadged) {
            return;
        }

        float start = isBadged ? 0f : 1f;
        float end = isBadged ? 1f : 0f;

        PropertyValuesHolder scaleX = PropertyValuesHolder.ofFloat(View.SCALE_X, start, end);
        PropertyValuesHolder scaleY = PropertyValuesHolder.ofFloat(View.SCALE_Y, start, end);
        ObjectAnimator animScale = ObjectAnimator.ofPropertyValuesHolder(badgeView, scaleX, scaleY);

        if (isBadged) {
            animScale.setInterpolator(new BounceInterpolator());
            animScale.setDuration(getContext().getResources().getInteger(android.R.integer.config_longAnimTime));
            animScale.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    badgeView.setVisibility(View.VISIBLE);
                }
            });
        } else {
            animScale.setInterpolator(new AccelerateInterpolator());
            animScale.setDuration(getContext().getResources().getInteger(android.R.integer.config_shortAnimTime));
            animScale.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    badgeView.setVisibility(View.GONE);
                }
            });
        }

        animScale.start();
    }

    public boolean isBadged(int position) {
        final View badgeView = findViewWithTag(makeBadgeTag(position));
        return badgeView != null && badgeView.getVisibility() == View.VISIBLE;
    }
}
