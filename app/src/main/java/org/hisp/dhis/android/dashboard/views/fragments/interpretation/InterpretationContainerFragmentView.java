package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.ui.bindings.views.View;


public interface InterpretationContainerFragmentView extends View{

    void navigationAfterLoadingData(Boolean hasData);

}
