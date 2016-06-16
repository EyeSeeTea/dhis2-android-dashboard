package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import java.util.List;

//TODO  Write Code for DashboardViewPagerFragmentView

public interface DashboardViewPagerFragmentView extends View{

    void showProgressBar();

    void hideProgressBar();

    void showError(String message);

    void showUnexpectedError(String message);

    void setDashboards(List<Dashboard> dashboards);
}
