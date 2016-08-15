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

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.adapters.InterpretationCommentsAdapter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCommentsFragmentPresenter;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.models.user.User;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

public class InterpretationCommentsFragment extends BaseFragment implements InterpretationCommentsFragmentView,
        InterpretationCommentsAdapter.OnCommentClickListener {
    private static final String INTERPRETATION_UID = "arg:interpretationUId";
    private static final String EMPTY_FIELD = "";

    @Inject
    InterpretationCommentsFragmentPresenter interpretationCommentsFragmentPresenter;

    @Inject
    Logger logger;

    Toolbar mToolbar;

    RecyclerView mRecyclerView;

    EditText mNewCommentText;

    ImageView mAddNewComment;

    InterpretationCommentsAdapter mAdapter;

    Interpretation mInterpretation;
    User mUser;

    AlertDialog alertDialog;

    public static InterpretationCommentsFragment newInstance(String interpretationUId) {
        Bundle arguments = new Bundle();
        arguments.putString(INTERPRETATION_UID, interpretationUId);

        InterpretationCommentsFragment fragment
                = new InterpretationCommentsFragment();
        fragment.setArguments(arguments);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DashboardApp) getActivity().getApplication())
                .getInterpretationComponent().inject(this);

        interpretationCommentsFragmentPresenter.attachView(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interpretation_comments, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        mNewCommentText = (EditText) view.findViewById(R.id.interpretation_comment_edit_text);
        mAddNewComment = (ImageView) view.findViewById(R.id.add_interpretation_comment_button);

        mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        mAdapter = new InterpretationCommentsAdapter(getActivity(),
                getLayoutInflater(null), this, mUser);

        /** TODO Handle savedInstanceState
         mAdapter = new InterpretationCommentsAdapter(getActivity(),
         getLayoutInflater(savedInstanceState), this, mUser);
         **/

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        linearLayoutManager.setStackFromEnd(true);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        Drawable buttonIcon = ContextCompat.getDrawable(
                getActivity(), R.drawable.ic_send);
        DrawableCompat.setTintList(buttonIcon, getResources()
                .getColorStateList(R.color.button_blue_color_state_list));
        mAddNewComment.setImageDrawable(buttonIcon);

        mAddNewComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddComment();
            }
        });

        mNewCommentText.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                onCommentChanged(s);
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });

        handleAddNewCommentButton(EMPTY_FIELD);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        interpretationCommentsFragmentPresenter.getInterpretationComments(getArguments().getString(INTERPRETATION_UID));
    }

    public void onCommentChanged(Editable text) {
        handleAddNewCommentButton(text.toString());
    }

    public void onAddComment() {
        String newCommentText = mNewCommentText.getText().toString();
    }

    @Override
    public void onCommentEdit(InterpretationComment comment) {
        InterpretationCommentEditFragment
                .newInstance(comment.getUId())
                .show(getChildFragmentManager());
    }

    @Override
    public void onCommentDelete(InterpretationComment comment) {
        int position = mAdapter.getData().indexOf(comment);
        if (!(position < 0)) {
            mAdapter.getData().remove(position);
            mAdapter.notifyItemRemoved(position);

        }
    }

    private void handleAddNewCommentButton(String text) {
        mAddNewComment.setEnabled(!isEmpty(text));
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
