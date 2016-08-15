package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface InterpretationTextFragmentView extends View{

    void setCurrentInterpretation(Interpretation interpretation);

    void showError(String message);

    void showUnexpectedError(String message);
}
