/*
 * Copyright (c) 2016, University of Oslo
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * Neither the name of the HISP project nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.hisp.dhis.android.dashboard.views.fragments.interpretation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCreateFragmentPresenter;
import org.hisp.dhis.android.dashboard.views.fragments.BaseDialogFragment;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.models.user.User;
import org.hisp.dhis.client.sdk.ui.views.FontButton;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

/**
 * Fragment responsible for creation of new interpretations.
 */
public final class InterpretationCreateFragment extends BaseDialogFragment implements InterpretationCreateFragmentView {
    private static final String TAG = InterpretationCreateFragment.class.getSimpleName();
    private static final String ARG_DASHBOARD_ITEM_UID = "arg:dashboardItemUId";

    @Inject
    InterpretationCreateFragmentPresenter interpretationCreateFragmentPresenter;

    @Inject
    Logger logger;

    TextView mDialogLabel;

    EditText mInterpretationText;

    DashboardItem mDashboardItem;

    ImageView mCloseDialogButton;
    FontButton mCancelInterpretationCreateButton;
    FontButton mCreateInterpretationButton;

    AlertDialog alertDialog;

    public static InterpretationCreateFragment newInstance(String itemUId) {
        Bundle args = new Bundle();
        args.putString(ARG_DASHBOARD_ITEM_UID, itemUId);

        InterpretationCreateFragment fragment = new InterpretationCreateFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);

        ((DashboardApp) getActivity().getApplication())
                .getInterpretationComponent().inject(this);

        interpretationCreateFragmentPresenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_interpretation_create, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mDialogLabel = (TextView) view.findViewById(R.id.dialog_label);

        mInterpretationText = (EditText) view.findViewById(R.id.interpretation_text);

        mCloseDialogButton = (ImageView) view.findViewById(R.id.close_dialog_button);
        mCancelInterpretationCreateButton = (FontButton) view.findViewById(R.id.cancel_interpretation_create);
        mCreateInterpretationButton = (FontButton) view.findViewById(R.id.create_interpretation);

        mCloseDialogButton.setOnClickListener(onClickListener);
        mCancelInterpretationCreateButton.setOnClickListener(onClickListener);
        mCreateInterpretationButton.setOnClickListener(onClickListener);

        String dashboardItemUId = getArguments().getString(ARG_DASHBOARD_ITEM_UID);

        interpretationCreateFragmentPresenter.getDashboardItem(dashboardItemUId);

        interpretationCreateFragmentPresenter.getDashboardElements(dashboardItemUId);

        mDialogLabel.setText(getString(R.string.create_interpretation));
    }

    @Override
    public void onPause() {
        super.onPause();

        logger.d(TAG, "onPause()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        interpretationCreateFragmentPresenter.detachView();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.create_interpretation) {

                User user = interpretationCreateFragmentPresenter.getUser();

                interpretationCreateFragmentPresenter.createInterpretation(mDashboardItem, user,
                        mInterpretationText.getText().toString());

                // Call createInterpretation from presenter only, after User is retreived
                // interpretationCreateFragmentPresenter.createInterpretation(mDashboardItem,
                //         mUser, mInterpretationText.getText().toString());


//                // read user
//                UserAccount userAccount = UserAccount
//                        .getCurrentUserAccountFromDb();
//                User user = new Select()
//                        .from(User.class)
//                        .where(Condition.column(User$Table
//                                .UID).is(userAccount.getUId()))
//                        .querySingle();


//                // create interpretation
//                Interpretation interpretation = createInterpretation(mDashboardItem,
//                        user, mInterpretationText.getText().toString());
//                List<InterpretationElement> elements = interpretation
//                        .getInterpretationElements();
//
//                // save interpretation
//                interpretation.save();
//                if (elements != null && !elements.isEmpty()) {
//                    for (InterpretationElement element : elements) {
//                        // save corresponding interpretation elements
//                        element.save();
//                    }
//                }

                // TODO SYNCING !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
//                if (isDhisServiceBound()) {
//                    getDhisService().syncInterpretations();
//                    EventBusProvider.post(new UiEvent(UiEvent.UiEventType.SYNC_INTERPRETATIONS));
//                }

                Toast.makeText(getActivity(),
                        getString(R.string.successfully_created_interpretation), Toast.LENGTH_SHORT).show();
            }
            dismiss();
        }
    };

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void showErrorDialog(String title, String message) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setPositiveButton(R.string.option_confirm, null);
            alertDialog = builder.create();
        }
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.show();
    }
}
