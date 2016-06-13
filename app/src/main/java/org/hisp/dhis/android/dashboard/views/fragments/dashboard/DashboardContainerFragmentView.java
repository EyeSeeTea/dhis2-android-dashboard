package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;


//TODO  Write Code for DashboardContainerFragmentView

public interface DashboardContainerFragmentView extends View{

    void showProgressBar();

    void hideProgressBar();

    void showError(String message);

    void showUnexpectedError(String message);

    void navigationAfterLoadingData(Event event, Boolean hasData);

}
