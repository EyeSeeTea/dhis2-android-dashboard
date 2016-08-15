package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import java.util.List;

public interface InterpretationCreateFragmentView extends View{

    void showError(String message);

    void showUnexpectedError(String message);

    void setCurrentDashboardItem(DashboardItem dashboardItem);

    void setDashboardElements(List<DashboardElement> dashboardElements);

    void uiSync();

}
