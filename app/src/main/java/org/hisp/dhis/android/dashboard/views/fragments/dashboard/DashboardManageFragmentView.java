package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface DashboardManageFragmentView extends View{

    void dismissDialogFragment();

    void dashboardNameClearFocus();

    void setCurrentDashboard(Dashboard dashboard);

    void showError(String message);

    void showUnexpectedError(String message);

    void UiSync();
}
