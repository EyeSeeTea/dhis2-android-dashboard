package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.android.dashboard.adapters.DashboardItemAdapter;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import java.util.List;

//TODO  Write Code for DashboardFragmentView

public interface DashboardFragmentView extends View{

    void showDashboardItems(List<DashboardItem> dashboardItems);

}
