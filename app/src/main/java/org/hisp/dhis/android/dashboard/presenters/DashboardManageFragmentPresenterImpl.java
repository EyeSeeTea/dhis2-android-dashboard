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

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardManageFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.models.event.Event;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

// TODO Consult if UiEventSync method is required
public class DashboardManageFragmentPresenterImpl implements DashboardManageFragmentPresenter {
    private static final String TAG = DashboardManageFragmentPresenterImpl.class.getSimpleName();
    private final DashboardInteractor dashboardInteractor;
    private DashboardManageFragmentView dashboardManageFragmentView;
    private final ApiExceptionHandler apiExceptionHandler;

    private final Logger logger;

    private CompositeSubscription subscription;

    Dashboard dashboard;

    public DashboardManageFragmentPresenterImpl(DashboardInteractor dashboardInteractor,
                                                ApiExceptionHandler apiExceptionHandler,
                                                Logger logger) {

        this.dashboardInteractor = dashboardInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dashboardManageFragmentView = (DashboardManageFragmentView) view;
    }

    @Override
    public void detachView() {
        dashboardManageFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    @Override
    public void setDashboard(String dashboardUId) {

        logger.e(TAG, "onGetDashboards()");

        Observable<Dashboard> dashboards = dashboardInteractor.get(dashboardUId);
        dashboards.subscribeOn(Schedulers.newThread());
        dashboards.observeOn(AndroidSchedulers.mainThread());
        dashboards.subscribe(new Action1<Dashboard>() {
            @Override
            public void call(Dashboard dashboard) {
                logger.d(TAG ,"onGetDashboards " + dashboard.toString());
                dashboardManageFragmentView.setCurrentDashboard(dashboard);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onGetDashboards failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void updateDashboard(Dashboard dashboard, String dashboardName) {
        // Do something to update
        // After updation do the following
        logger.e(TAG, "onUpdateDashboards()");

        // Change name
        dashboard.updateDashboard(dashboardName);

        // Save tghe changes
        Observable<Boolean> dashboards = dashboardInteractor.save(dashboard);
        dashboards.subscribeOn(Schedulers.io());
        dashboards.observeOn(AndroidSchedulers.mainThread());
        dashboards.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                logger.d(TAG ,"onUpdateDashboards " + aBoolean.toString());
                // TODO trigger syncing of dashboards

                dashboardManageFragmentView.dismissDialogFragment();
                dashboardManageFragmentView.dashboardNameClearFocus();
                UiEventSync();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onUpdateDashboards failed");
                handleError(throwable);
            }
        });

    }

    @Override
    public void deleteDashboard(Dashboard dashboard) {

        logger.e(TAG, "onDeleteDashboards()");

        Observable<Boolean> dashboards = dashboardInteractor.remove(dashboard);
        dashboards.subscribeOn(Schedulers.io());
        dashboards.observeOn(AndroidSchedulers.mainThread());
        dashboards.subscribe(new Action1<Boolean>() {
            @Override
            public void call(Boolean aBoolean) {
                logger.d(TAG ,"onDeleteDashboards " + aBoolean.toString());
                // TODO trigger syncing of dashboards
                UiEventSync();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onDeleteDashboards failed");
                handleError(throwable);
            }
        });
    }

    // TODO handle UiEventSync
    @Override
    public void UiEventSync() {
        /**
        if (isDhisServiceBound()) {
            getDhisService().syncDashboards();
            EventBusProvider.post(new UiEvent(UiEvent.UiEventType.SYNC_DASHBOARDS));
        }
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
                        dashboardManageFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardManageFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardManageFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
