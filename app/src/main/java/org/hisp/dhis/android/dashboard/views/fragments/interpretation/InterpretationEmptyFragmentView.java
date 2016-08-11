package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface InterpretationEmptyFragmentView extends View{

    void showProgressBar();

    void hideProgressBar();

    void showError(String message);

    void showUnexpectedError(String message);

}
