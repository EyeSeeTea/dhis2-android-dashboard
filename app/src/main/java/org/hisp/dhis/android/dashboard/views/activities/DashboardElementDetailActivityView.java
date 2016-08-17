package org.hisp.dhis.android.dashboard.views.activities;

import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface DashboardElementDetailActivityView extends View{

    void handleDashboardElement(DashboardElement dashboardElement);

    void handleInterpretationElement(InterpretationElement interpretationElement);

    void showError(String message);

    void showUnexpectedError(String message);

}
