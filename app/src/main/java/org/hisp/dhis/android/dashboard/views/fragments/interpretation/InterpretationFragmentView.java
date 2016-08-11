package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import java.util.List;

public interface InterpretationFragmentView extends View{

    void showInterpretations(List<Interpretation> interpretations);

    void showError(String message);

    void showUnexpectedError(String message);

    void showProgressBar();

    void hideProgressBar();

}
