package org.hisp.dhis.android.dashboard.views.activities;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.MenuItem;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardContainerFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationContainerFragment;
import org.hisp.dhis.client.sdk.ui.bindings.views.DefaultHomeActivity;
import org.hisp.dhis.client.sdk.ui.fragments.WrapperFragment;

public class HomeActivity extends DefaultHomeActivity {

    @IdRes
    private static final int DRAWER_DASHBOARDS_ID = 324322;

    @IdRes
    private static final int DRAWER_INTERPRETATIONS_ID = 2342342;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addMenuItem(DRAWER_DASHBOARDS_ID, R.drawable.ic_dashboards,
                R.string.drawer_item_dashboards);
        addMenuItem(DRAWER_INTERPRETATIONS_ID, R.drawable.ic_interpretations,
                R.string.drawer_item_interpretations);

        if (savedInstanceState == null) {
            onNavigationItemSelected(getNavigationView().getMenu()
                    .findItem(DRAWER_DASHBOARDS_ID));
        }
    }

    @Override
    protected boolean onItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case DRAWER_DASHBOARDS_ID: {
                attachFragment(WrapperFragment.newInstance(DashboardContainerFragment.class,
                        getString(R.string.drawer_item_dashboards)));
                break;
            }
            case DRAWER_INTERPRETATIONS_ID: {
                attachFragment(WrapperFragment.newInstance(InterpretationContainerFragment.class,
                        getString(R.string.drawer_item_interpretations)));
                break;
            }
        }
        return true;
    }
}
