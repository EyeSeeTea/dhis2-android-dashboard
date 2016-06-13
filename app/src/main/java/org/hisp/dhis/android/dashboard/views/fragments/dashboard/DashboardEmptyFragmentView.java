package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

    //TODO  Write Code for DashboardEmptyFragmentView

public interface DashboardEmptyFragmentView extends View{

    void showProgressBar();

    void hideProgressBar();

    void showError(String message);

    void showUnexpectedError(String message);

}
