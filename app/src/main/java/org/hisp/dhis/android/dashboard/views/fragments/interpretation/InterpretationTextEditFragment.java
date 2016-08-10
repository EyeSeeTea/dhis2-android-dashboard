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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.raizlabs.android.dbflow.sql.language.Select;

import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.views.fragments.BaseDialogFragment;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.views.FontButton;

/**
 * Handles editing (changing text) of given interpretation.
 */
public final class InterpretationTextEditFragment extends BaseDialogFragment {
    private static final String TAG = InterpretationTextEditFragment.class.getSimpleName();
    private static final String ARG_INTERPRETATION_ID = "arg:interpretationId";

    TextView mDialogLabel;

    EditText mInterpretationText;

    Interpretation mInterpretation;

    ImageView mCloseDialogButton ;
    FontButton mCancelInterpretationTextEditButton;
    FontButton mUpdateInterpretationText;

    public static InterpretationTextEditFragment newInstance(long interpretationId) {
        Bundle args = new Bundle();
        args.putLong(ARG_INTERPRETATION_ID, interpretationId);

        InterpretationTextEditFragment fragment = new InterpretationTextEditFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NO_TITLE,
                R.style.Theme_AppCompat_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_interpretation_text_edit, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mDialogLabel = (TextView) view.findViewById(R.id.dialog_label);
        mInterpretationText = (EditText) view.findViewById(R.id.interpretation_text);

        mCloseDialogButton = (ImageView) view.findViewById(R.id.close_dialog_button);
        mCancelInterpretationTextEditButton = (FontButton) view.findViewById(R.id.cancel_interpretation_text_edit);
        mUpdateInterpretationText = (FontButton) view.findViewById(R.id.update_interpretation_text);

        mCloseDialogButton.setOnClickListener(onClickListener);
        mCancelInterpretationTextEditButton.setOnClickListener(onClickListener);
        mUpdateInterpretationText.setOnClickListener(onClickListener);

        mInterpretation = new Select()
                .from(Interpretation.class)
                .where(Condition.column(Interpretation$Table
                        .ID).is(getArguments().getLong(ARG_INTERPRETATION_ID)))
                .querySingle();

        mDialogLabel.setText(getString(R.string.interpretation_text));
        mInterpretationText.setText(mInterpretation.getText());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.update_interpretation_text: {
                    mInterpretation.updateInterpretation(
                            mInterpretationText.getText().toString());

                    if (isDhisServiceBound()) {
                        getDhisService().syncInterpretations();
                        EventBusProvider.post(new UiEvent(UiEvent
                                .UiEventType.SYNC_INTERPRETATIONS));
                    }
                    break;
                }
            }

            dismiss();
        }
    };

    public void show(FragmentManager manager) {
        super.show(manager, TAG);
    }
}
