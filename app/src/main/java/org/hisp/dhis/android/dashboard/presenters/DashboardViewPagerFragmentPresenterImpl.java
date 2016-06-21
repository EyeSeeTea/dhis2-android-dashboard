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

import org.hisp.dhis.android.dashboard.sync.SyncWrapper;
import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardViewPagerFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO Remove getFakeData()
// TODO loadLocalData properly
// TODO Handle syncing properly
public class DashboardViewPagerFragmentPresenterImpl implements DashboardViewPagerFragmentPresenter {
    private static final String TAG = DashboardViewPagerFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private DashboardViewPagerFragmentView dashboardViewPagerFragmentView;

    private final SessionPreferences sessionPreferences;
    private final SyncDateWrapper syncDateWrapper;
    private final SyncWrapper syncWrapper;
    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;
    private boolean hasSyncedBefore;
    private boolean isSyncing;

    public DashboardViewPagerFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                               SessionPreferences sessionPreferences,
                                               SyncDateWrapper syncDateWrapper,
                                               SyncWrapper syncWrapper,
                                               ApiExceptionHandler apiExceptionHandler,
                                               Logger logger) {
        this.dashboardInteractor = dashboardInteractor;
        this.sessionPreferences = sessionPreferences;
        this.syncDateWrapper = syncDateWrapper;
        this.syncWrapper = syncWrapper;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
        this.hasSyncedBefore = false;
    }

    public void attachView(View view) {
        logger.d(TAG, "viewAttached");
        isNull(view, "DashboardViewPagerFragmentView must not be null");
        dashboardViewPagerFragmentView = (DashboardViewPagerFragmentView) view;

        // TODO conditions to check if Syncing has to be done
        /**
         if (isDhisServiceBound() &&
         !getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS) &&
         !SessionManager.getInstance().isResourceTypeSynced(ResourceType.DASHBOARDS)) {
         syncDashboards();
         }
         **/

        if (isSyncing) {
            dashboardViewPagerFragmentView.showProgressBar();
        } else {
            dashboardViewPagerFragmentView.hideProgressBar();
        }

        // check if metadata was synced,
        // if not, syncMetaData
         if (!isSyncing && !hasSyncedBefore) {
             logger.d(TAG, "!Syncing & !SyncedBefore");
             sync();
         }
    }

    @Override
    public void detachView() {
        dashboardViewPagerFragmentView.hideProgressBar();
        dashboardViewPagerFragmentView = null;
    }

    // Set hasSyncedBefore boolean to True
    @Override
    public void sync() {
        logger.d(TAG, "Syncing");
        /** TODO Write code for syncing
        dashboardViewPagerFragmentView.showProgressBar();
        isSyncing = true;
        subscription.add(syncWrapper.syncMetaData()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<Dashboard>>() {
                    @Override
                    public void call(List<Dashboard> dashboards) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        syncDateWrapper.setLastSyncedNow();

                        if (dashboardViewPagerFragmentView != null) {
                            dashboardViewPagerFragmentView.hideProgressBar();
                        }
                        logger.d(TAG, "Synced dashboards successfully");
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        isSyncing = false;
                        hasSyncedBefore = true;
                        if (dashboardViewPagerFragmentView != null) {
                            dashboardViewPagerFragmentView.hideProgressBar();
                        }
                        logger.e(TAG, "Failed to sync dashboards", throwable);
                        handleError(throwable);
                    }
                }));
         **/
    }

    @Override
    public void loadLocalDashboards() {
        logger.e(TAG, "onLoadDashboards()");
        /** TODO loadDashboardItems() Code using RxAndroid
        logger.d(TAG, "loadDashboards()");
        subscription.add(dashboardInteractor.list()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Dashboard>() {
                    @Override
                    public void call(Dashboard dashboard) {
                        if (dashboardViewPagerFragmentView != null) {
                            dashboardViewPagerFragmentView.showDashboards(dashboard);
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.e(TAG, "Failed listing dashboards.", throwable);
                    }
                }));
         **/

        //Temporary Hack
        dashboardViewPagerFragmentView.setDashboards(getFakeData());
    }

    @Override
    public boolean isSyncing() {
        return isSyncing;
        /**
         boolean isLoading = isDhisServiceBound() &&
         getDhisService().isJobRunning(DhisService.SYNC_DASHBOARDS);
         **/
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardViewPagerFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardViewPagerFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardViewPagerFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    // Temporary hack for creating Dashboards like legacy.
    // TODO Use DashboardInteractor to create new Dashboard wherever required
    private List<Dashboard> getFakeData(){
        // BaseIdentifiableObject {id=11, uId=nghVC4wtyzi, name=Antenatal Care, displayName=Antenatal Care, created=2013-09-08T21:47:17.960Z, lastUpdated=2016-06-19T14:42:27.046Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}, BaseIdentifiableObject {id=3, uId=iMnYyBfSxmM, name=Delivery, displayName=Delivery, created=2013-09-09T21:14:14.696Z, lastUpdated=2016-06-19T14:42:42.720Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}, BaseIdentifiableObject {id=15, uId=vqh4MBWOTi4, name=Disease Surveillance, displayName=Disease Surveillance, created=2014-04-06T11:46:01.989Z, lastUpdated=2014-04-14T13:04:29.916Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}, BaseIdentifiableObject {id=4, uId=TAMlzYkstb7, name=Immunization, displayName=Immunization, created=2013-09-09T21:13:49.245Z, lastUpdated=2016-06-14T04:33:33.326Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}, BaseIdentifiableObject {id=17, uId=L1BtjXgpUpd, name=Immunization data, displayName=Immunization data, created=2014-11-03T11:13:52.812Z, lastUpdated=2016-04-21T18:20:14.942Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}, BaseIdentifiableObject {id=16, uId=SCtS6Szuubz, name=Info Videos, displayName=Info Videos, created=2016-04-20T15:43:41.790Z, lastUpdated=2016-04-20T19:45:50.735Z, access=Access {manage=true, externalize=true, write=true, read=true, update=true, delete=true}}];
        List<Dashboard> data = new ArrayList<Dashboard>() ;
        data.add(Dashboard.createDashboard("Antenatal Care"));
        data.add(Dashboard.createDashboard("Delivery"));
        data.add(Dashboard.createDashboard("Disease Surveillance"));

        return data;
    }
}
