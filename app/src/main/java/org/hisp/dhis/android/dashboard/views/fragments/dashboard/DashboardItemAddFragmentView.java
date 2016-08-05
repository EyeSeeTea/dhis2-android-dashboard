package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.android.dashboard.adapters.DashboardItemSearchDialogAdapter.OptionAdapterValue;

import java.util.List;

public interface DashboardItemAddFragmentView extends View{

    void showOptionAdapterValues(List<OptionAdapterValue> optionAdapterValues);

    void dismissDialogFragment();

    void addItemContent(DashboardContent resource);

    void setDashboard(Dashboard dashboard);

}
