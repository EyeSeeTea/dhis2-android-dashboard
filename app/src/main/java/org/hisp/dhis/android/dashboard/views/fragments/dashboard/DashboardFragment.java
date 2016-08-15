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

package org.hisp.dhis.android.dashboard.views.fragments.dashboard;

import android.content.Intent;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.adapters.DashboardItemAdapter;
import org.hisp.dhis.android.dashboard.presenters.DashboardFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardViewPagerFragmentPresenter;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCreateFragment;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.ui.views.GridDividerDecoration;
import org.hisp.dhis.android.dashboard.views.activities.DashboardElementDetailActivity;
import org.hisp.dhis.client.sdk.models.common.Access;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardContent;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import javax.inject.Inject;

public class DashboardFragment extends BaseFragment
        implements DashboardFragmentView, DashboardItemAdapter.OnItemClickListener {
    static final String TAG = DashboardFragment.class.getSimpleName();
    private static final String DASHBOARD_UID = "arg:dashboardUid";
    private static final String DELETE = "arg:delete";
    private static final String UPDATE = "arg:update";
    private static final String READ = "arg:read";
    private static final String WRITE = "arg:write";
    private static final String MANAGE = "arg:manage";
    private static final String EXTERNALIZE = "arg:externalize";

    @Inject
    DashboardFragmentPresenter dashboardFragmentPresenter;

    @Inject
    DashboardViewPagerFragmentPresenter dashboardViewPagerFragmentPresenter;

    @Inject
    Logger logger;

    ViewSwitcher mViewSwitcher;

    RecyclerView mRecyclerView;

    DashboardItemAdapter mDashboardItemsAdapter;

    AlertDialog alertDialog;

    public static DashboardFragment newInstance(Dashboard dashboard) {
        DashboardFragment fragment = new DashboardFragment();
        Access access = dashboard.getAccess();

        Bundle args = new Bundle();
        args.putString(DASHBOARD_UID, dashboard.getUId());
        args.putBoolean(DELETE, access.isDelete());
        args.putBoolean(UPDATE, access.isUpdate());
        args.putBoolean(READ, access.isRead());
        args.putBoolean(WRITE, access.isWrite());
        args.putBoolean(MANAGE, access.isManage());
        args.putBoolean(EXTERNALIZE, access.isExternalize());

        fragment.setArguments(args);
        return fragment;
    }

    private static Access getAccessFromBundle(Bundle args) {
        Access access = new Access();

        access.setDelete(args.getBoolean(DELETE));
        access.setUpdate(args.getBoolean(UPDATE));
        access.setRead(args.getBoolean(READ));
        access.setWrite(args.getBoolean(WRITE));
        access.setManage(args.getBoolean(MANAGE));
        access.setExternalize(args.getBoolean(EXTERNALIZE));

        return access;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ((DashboardApp) getActivity().getApplication())
                .getDashboardComponent().inject(this);

        dashboardFragmentPresenter.attachView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        return inflater.inflate(R.layout.fragment_dashboard_list, group, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        logger.d(TAG, "onViewCreated()");
        setupRecyclerView(view);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        logger.d(TAG, "onActivityCreated()");
        if(isAdded()) {
            dashboardFragmentPresenter.loadLocalDashboardItems(getArguments().getString(DASHBOARD_UID));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d(TAG, "onDestroy()");
        if (alertDialog != null) {
            alertDialog.dismiss();
        }
        dashboardFragmentPresenter.detachView();
    }

    @Override
    public void showDashboardItems(List<DashboardItem> dashboardItems) {
        boolean isDashboardItemListEmpty = dashboardItems == null || dashboardItems.isEmpty();
        boolean isEmptyListMessageShown = mViewSwitcher.getCurrentView().getId() ==
                R.id.text_view_empty_dashboard_message;

        if (isDashboardItemListEmpty && !isEmptyListMessageShown) {
            mViewSwitcher.showNext();
        } else if (!isDashboardItemListEmpty && isEmptyListMessageShown) {
            mViewSwitcher.showNext();
        }
        mDashboardItemsAdapter.swapData(dashboardItems);
    }

    /** TODO when to set null
     @Override
     public void onLoaderReset(Loader<List<DashboardItem>> loader) {
     if (loader.getId() == LOADER_ID) {
     mDashboardItemsAdapter.swapData(null);
     }
     }
     **/

    @Override
    public void onContentClick(DashboardElement element) {
        switch (element.getDashboardItem().getType()) {
            case DashboardContent.TYPE_CHART:
            case DashboardContent.TYPE_EVENT_CHART:
            case DashboardContent.TYPE_MAP:
            case DashboardContent.TYPE_REPORT_TABLE: {
                Intent intent = DashboardElementDetailActivity
                        .newIntentForDashboardElement(getActivity(), element.getId());
                startActivity(intent);
                break;
            }
            default: {
                String message = getString(R.string.unsupported_dashboard_item_type);
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onContentDeleteClick(DashboardElement element) {
        if (element != null) {
            dashboardFragmentPresenter.deleteDashboardElement(element);
        }
    }

    @Override
    public void onItemDeleteClick(DashboardItem item) {
        if (item != null) {
            dashboardFragmentPresenter.deleteDashboardItem(item);
        }
    }

    // TODO
    @Override
    public void onItemShareClick(DashboardItem item) {
        InterpretationCreateFragment
                .newInstance(item.getUId())
                .show(getChildFragmentManager());
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void showError(String message) {
        showErrorDialog(getString(R.string.title_error), message);
    }

    @Override
    public void showUnexpectedError(String message) {
        showErrorDialog(getString(R.string.title_error_unexpected), message);
    }

    @Override
    public void uiSync() {
        dashboardViewPagerFragmentPresenter.syncDashboard();
    }

    private void setupRecyclerView(View view) {
        mViewSwitcher = (ViewSwitcher) view;
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        final int spanCount = getResources().getInteger(R.integer.column_nums);

        final PreferencesModule preferencesModule = dashboardFragmentPresenter.getPreferenceModule();

        mDashboardItemsAdapter = new DashboardItemAdapter(getActivity(),
                getAccessFromBundle(getArguments()), spanCount, preferencesModule, this);

        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);
        gridLayoutManager.setOrientation(GridLayoutManager.VERTICAL);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return mDashboardItemsAdapter.getSpanSize(position);
            }
        });

        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new GridDividerDecoration(getActivity()
                .getApplicationContext()));
        mRecyclerView.setAdapter(mDashboardItemsAdapter);
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