package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.models.user.User;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;

import java.util.List;

public interface InterpretationCommentsFragmentView extends View{

    void showError(String message);

    void showUnexpectedError(String message);

    void setInterpretation(Interpretation interpretation);

    void setInterpretationComments(List<InterpretationComment> interpretationComments);

    void addCommentCallback(InterpretationComment comment);
}
