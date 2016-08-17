package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface DashboardContainerFragmentView extends View{

    void navigationAfterLoadingData(Boolean hasData);

}
