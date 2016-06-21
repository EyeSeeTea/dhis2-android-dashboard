package org.hisp.dhis.android.dashboard.views.fragments.dashboard;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.DashboardComponent;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.UserComponent;
import org.hisp.dhis.android.dashboard.presenters.DashboardContainerFragmentPresenter;
import org.hisp.dhis.android.dashboard.views.fragments.ImageViewFragment;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;


import javax.inject.Inject;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 *         This fragment is used to make decision, whether to show fragment with
 *         dashboards or fragment with message.
 */

    //TODO  Code for checking fetched data to make decision between ViewPager and EmptyFragment

public class DashboardContainerFragment extends BaseFragment implements DashboardContainerFragmentView{
    private static final String TAG = DashboardContainerFragment.class.getSimpleName();

    @Inject
    DashboardContainerFragmentPresenter dashboardContainerFragmentPresenter;

    @Inject
    Logger logger;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_empty, container, false);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO consult Araz about decision between User and Dashboard Component
        // chosen one will contain the Presenter in the corresponding Module

//        UserComponent userComponent = ((DashboardApp) getActivity().getApplication()).getUserComponent();
//        userComponent.inject(this);

         DashboardComponent dashboardComponent = ((DashboardApp) getActivity().getApplication()).getDashboardComponent();
         // first time fragment is created
        if (savedInstanceState == null) {
            // it means we found old component and we have to release it
            if (dashboardComponent != null) {
                // create new instance of component
                ((DashboardApp) getActivity().getApplication()).releaseDashboardComponent();
            }
            dashboardComponent = ((DashboardApp) getActivity().getApplication()).createDashboardComponent();
        } else {
            dashboardComponent = ((DashboardApp) getActivity().getApplication()).getDashboardComponent();
        }
        // inject dependencies
         dashboardComponent.inject(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        dashboardContainerFragmentPresenter.attachView(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        logger.d(TAG, "onLoadLocalData()");
        dashboardContainerFragmentPresenter.onLoadLocalData();
    }

    @Override
    public void onResume() {
        super.onResume();
        logger.d(TAG, "onResume()");
        dashboardContainerFragmentPresenter.attachView(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        logger.d(TAG, "onPause()");
        dashboardContainerFragmentPresenter.detachView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.d(TAG, "onDestroy()");
    }

    @Override
    public boolean onBackPressed() {
        return true;
    }

    @Override
    public void navigationAfterLoadingData(Boolean hasData) {
        if (hasData) {
            // we don't want to attach the same fragment
            if (!isFragmentAttached(DashboardViewPagerFragment.TAG)) {
                attachFragment(new DashboardViewPagerFragment(),
                        DashboardViewPagerFragment.TAG);
            }
        } else {
            if (!isFragmentAttached(DashboardEmptyFragment.TAG)) {
                attachFragment(new DashboardEmptyFragment(),
                        DashboardEmptyFragment.TAG);
            }
        }
    }

    private void attachFragment(Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_content_frame, fragment, tag)
                .commitAllowingStateLoss();
    }

    private boolean isFragmentAttached(String tag) {
        return getChildFragmentManager().findFragmentByTag(tag) != null;
    }

    @NonNull
    protected Toolbar getToolbar() {
        return getParentToolbar();
    }

}
