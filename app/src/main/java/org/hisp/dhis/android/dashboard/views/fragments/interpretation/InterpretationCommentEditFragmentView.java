package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

public interface InterpretationCommentEditFragmentView extends View{

    void showError(String message);

    void showUnexpectedError(String message);

    void setInterpretationComment(InterpretationComment interpretationComment);

    void updateCommentCallback();
}
