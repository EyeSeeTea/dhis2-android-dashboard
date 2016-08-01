package org.hisp.dhis.android.dashboard.views.fragments;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface WebViewFragmentView extends View{

    void onDataDownloaded(String data);

    void showError(String message);

    void showUnexpectedError(String message);
}
