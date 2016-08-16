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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.InterpretationComponent;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.adapters.InterpretationAdapter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationFragmentPresenter;
import org.hisp.dhis.android.dashboard.views.activities.DashboardElementDetailActivity;
import org.hisp.dhis.android.dashboard.views.activities.InterpretationCommentsActivity;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.ui.views.GridDividerDecoration;
import org.hisp.dhis.client.sdk.utils.Logger;
import java.util.List;

import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

public final class InterpretationFragment extends BaseFragment implements
        InterpretationAdapter.OnItemClickListener, InterpretationFragmentView {
    public static final String TAG = InterpretationFragment.class.getSimpleName();
    private static final String STATE_IS_LOADING = "state:isLoading";

    @Inject
    InterpretationFragmentPresenter interpretationFragmentPresenter;

    @Inject
    Logger logger;

    // Progress bar
    SmoothProgressBar mProgressBar;

    RecyclerView mRecyclerView;

    AlertDialog alertDialog;

    InterpretationAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        InterpretationComponent interpretationComponent = ((DashboardApp) getActivity().getApplication()).getInterpretationComponent();
        // first time fragment is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (interpretationComponent != null) {
                // create new instance of component
                ((DashboardApp) getActivity().getApplication()).releaseInterpretationComponent();
            }
            interpretationComponent = ((DashboardApp) getActivity().getApplication()).createInterpretationComponent();
        } else {
            interpretationComponent = ((DashboardApp) getActivity().getApplication()).getInterpretationComponent();
        }
        // inject dependencies
        interpretationComponent.inject(this);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interpretations, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        logger.d(TAG, "onActivityCreated()");
        if(isAdded()) {
            interpretationFragmentPresenter.loadLocalInterpretations();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        logger.d(TAG, "onResume()");
        // Have to add attachView here , because Container Fragment's onResume()
        // and InterpretationFragment's onResume() is called after setInterpretations()
        // Syncing is checked here with isSyncing and hasSyncedBefore booleans
//        interpretationFragmentPresenter.attachView(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d(TAG, "onDestroy()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        interpretationFragmentPresenter.detachView();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        logger.d(TAG, "onViewCreated()");
        mProgressBar = (SmoothProgressBar) view.findViewById(R.id.progress_bar);
        setupToolbar();
        setupRecyclerView(view, savedInstanceState);
        interpretationFragmentPresenter.attachView(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_LOADING, mProgressBar
                .getVisibility() == View.VISIBLE);
        super.onSaveInstanceState(outState);
    }

    /** TODO Handle this if required
    @Override
    public void onLoaderReset(Loader<List<Interpretation>> loader) {
        if (loader != null && loader.getId() == LOADER_ID) {
            mAdapter.swapData(null);
        }
    }
     **/

    @Override
    public void onInterpretationContentClick(Interpretation interpretation) {
        InterpretationElement element = null;
        switch (interpretation.getType()) {
            case Interpretation.TYPE_CHART: {
                element = interpretation.getChart();
                break;
            }
            case Interpretation.TYPE_MAP: {
                element = interpretation.getMap();
                break;
            }
            case Interpretation.TYPE_REPORT_TABLE: {
                element = interpretation.getReportTable();
                break;
            }
            default: {
                String message = getString(R.string.unsupported_interpretation_type);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }

        if (element != null) {
            Intent intent = DashboardElementDetailActivity
                    .newIntentForInterpretationElement(getActivity(), element.getId());
            startActivity(intent);
        }
    }

    @Override
    public void onInterpretationTextClick(Interpretation interpretation) {
        InterpretationTextFragment
                .newInstance(interpretation.getUId())
                .show(getChildFragmentManager());
    }

    @Override
    public void onInterpretationDeleteClick(Interpretation interpretation) {
        int position = mAdapter.getData().indexOf(interpretation);
        if (!(position < 0)) {
            mAdapter.getData().remove(position);
            mAdapter.notifyItemRemoved(position);
            interpretationFragmentPresenter.deleteInterpretation(interpretation);
            // Handle syncing in presenterImpl only
        }
    }

    @Override
    public void onInterpretationEditClick(Interpretation interpretation) {
        InterpretationTextEditFragment
                .newInstance(interpretation.getUId())
                .show(getChildFragmentManager());
    }

    @Override
    public void onInterpretationCommentsClick(Interpretation interpretation) {
        Intent intent = InterpretationCommentsActivity
                .newIntent(getActivity(), interpretation.getUId());
        startActivity(intent);
    }

    @Override
    public void showProgressBar() {
        logger.d(TAG, "showProgressBar()");
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        logger.d(TAG, "hideProgressBar()");
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showInterpretations(List<Interpretation> interpretations) {
        if (interpretations != null) {
            logger.d(TAG, "showInterpretations()");
            mAdapter.swapData(interpretations);
        }
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    private void setupToolbar() {
        if (getToolbarOfContainer() != null) {
            logger.d(TAG, "nonNullToolbar");
            getToolbarOfContainer().inflateMenu(R.menu.menu_interpretations_fragment);
            getToolbarOfContainer().setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    if (item.getItemId() == R.id.refresh) {
                        interpretationFragmentPresenter.syncInterpretations();
                        return true;
                    }

                    return false;
                }
            });
        }
    }

    @Nullable
    protected Toolbar getToolbarOfContainer() {
        if (getParentFragment() != null && getParentFragment() instanceof InterpretationContainerFragment) {
            return ((InterpretationContainerFragment) getParentFragment()).getToolbar();
        }
        return null;
    }


    private void setupRecyclerView(View view,  @Nullable Bundle savedInstanceState) {

        final PreferencesModule preferencesModule = interpretationFragmentPresenter.getPreferenceModule();

        mAdapter = new InterpretationAdapter(getActivity(),  getLayoutInflater(savedInstanceState), preferencesModule, this);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        final int spanCount = getResources().getInteger(R.integer.column_nums);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridDividerDecoration(getActivity()
                .getApplicationContext()));
        mRecyclerView.setAdapter(mAdapter);

//        interpretationFragmentPresenter.attachView(this);

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


    @Override
    public boolean onBackPressed() {
        return true;
    }
}
