/*
 * Copyright (c) 2016, University of Oslo
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

package org.hisp.dhis.android.dashboard.presenters;

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardContainerFragment;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardContainerFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.inject.Inject;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO Edit PresenterImpl Code

public class DashboardContainerFragmentPresenterImpl implements DashboardContainerFragmentPresenter {
    private static final String TAG = DashboardContainerFragmentPresenterImpl.class.getSimpleName();
//    private final DashboardInteractor dashboardInteractor;
    private boolean hasSyncedBefore;
    private DashboardContainerFragmentView dashboardContainerFragmentView;
    private boolean isSyncing;
    private final Logger logger;

    private static final Boolean TEST_BOOL_EMPTY_DASHBOARD = false;
    private static final Boolean TEST_BOOL_VIEWPAGER = true;

    // TODO
    public DashboardContainerFragmentPresenterImpl(
//            DashboardInteractor dashboardInteractor
            Logger logger
    ) {
//        this.dashboardInteractor = dashboardInteractor;
        this.hasSyncedBefore = false;
        this.logger = logger;
    }

    public void attachView(View view) {
        isNull(view, "DashboardContainerFragmentView must not be null");
        if(dashboardContainerFragmentView==null) {
            dashboardContainerFragmentView = (DashboardContainerFragment) view;
        }
    }

    @Override
    public void detachView() {
        dashboardContainerFragmentView = null;
    }

    @Override
    public void onLoadData() {
        //TODO background code to check if data exists with callback to Fragment
        logger.d(TAG, "checkForData()");
        if(TEST_BOOL_VIEWPAGER){
            // 2 Conditions :
            // if Empty fragment of container has to be loaded first, check for !=null
            // if onLoad() has to be done before loading empty fragment, do not check for !=null
            if (dashboardContainerFragmentView != null) {
                dashboardContainerFragmentView.navigationAfterLoadingData(TEST_BOOL_VIEWPAGER);
            }
        } else{
            if (dashboardContainerFragmentView != null) {
                dashboardContainerFragmentView.navigationAfterLoadingData(TEST_BOOL_EMPTY_DASHBOARD);
            }
        }
    }
}
