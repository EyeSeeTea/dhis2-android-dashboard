/*
 * Copyright (c) 2015, University of Oslo
 *
 * All rights reserved.
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

package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.views.fragments.BaseDialogFragment;
import org.hisp.dhis.client.sdk.ui.views.FontButton;


import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;


/**
 * Fragment responsible for creation of new dashboards.
 */
public final class DashboardAddFragment extends BaseDialogFragment {
    private static final String TAG = DashboardAddFragment.class.getSimpleName();

    TextView mDialogLabel;
    EditText mDashboardName;
    TextInputLayout mTextInputLayout;
    ImageView mCloseDialogButton;
    FontButton mCancelDashboardAddButton;
    FontButton mSaveDashboardButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard_add, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mDialogLabel = (TextView) view.findViewById(R.id.dialog_label);
        mDashboardName = (EditText) view.findViewById(R.id.dashboard_name);
        mTextInputLayout = (TextInputLayout) view.findViewById(R.id.text_input_dashboard_name);
        mCloseDialogButton = (ImageView) view.findViewById(R.id.close_dialog_button);
        mCancelDashboardAddButton = (FontButton) view.findViewById(R.id.cancel_dashboard_add);
        mSaveDashboardButton = (FontButton) view.findViewById(R.id.save_dashboard);

        mCloseDialogButton.setOnClickListener(onClickListener);
        mCancelDashboardAddButton.setOnClickListener(onClickListener);
        mSaveDashboardButton.setOnClickListener(onClickListener);

        mDialogLabel.setText(getString(R.string.add_dashboard));
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.save_dashboard) {
                boolean isEmptyName = isEmpty(mDashboardName.getText().toString().trim());
                String message = isEmptyName ? getString(R.string.enter_valid_name) : "";
                mTextInputLayout.setError(message);

                if (!isEmptyName) {

                }
            } else {
                dismiss();
            }
        }
    };

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }

}
