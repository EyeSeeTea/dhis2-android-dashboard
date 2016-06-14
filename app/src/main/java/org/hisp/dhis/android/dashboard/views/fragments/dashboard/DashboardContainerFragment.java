package org.hisp.dhis.android.dashboard.views.fragments.dashboard;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.android.dashboard.R;
import org.hisp.dhis.android.dashboard.presenters.DashboardContainerFragmentPresenter;
import org.hisp.dhis.client.sdk.ui.fragments.BaseFragment;
import org.hisp.dhis.client.sdk.utils.Logger;


import javax.inject.Inject;

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
        ((DashboardApp) getActivity().getApplication())
                .getDashboardComponent().inject(this);

        //TODO  Write onLoadData() code in DashboardContainerFragmentPresenterImpl
        checkForData();
    }

    private void attachFragment(Fragment fragment, String tag) {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.fragment_content_frame, fragment, tag)
                .commitAllowingStateLoss();
    }

    private boolean isFragmentAttached(String tag) {
        return getChildFragmentManager().findFragmentByTag(tag) != null;
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

    private void checkForData(){
        dashboardContainerFragmentPresenter.onLoadData();
    }

}
