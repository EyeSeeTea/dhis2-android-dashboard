package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface DashboardManageFragmentView extends View{

    void dismissDialogFragment();

    void dashboardNameClearFocus();
}
