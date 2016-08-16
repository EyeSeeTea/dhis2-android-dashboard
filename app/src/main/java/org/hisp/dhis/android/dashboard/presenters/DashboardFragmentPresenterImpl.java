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


import android.util.Log;

import org.hisp.dhis.android.dashboard.views.fragments.dashboard.DashboardFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardElementInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardItemInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppError;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.net.HttpURLConnection;
import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class DashboardFragmentPresenterImpl implements DashboardFragmentPresenter {
    private static final String TAG = DashboardFragmentPresenterImpl.class.getSimpleName();
    private final DashboardItemInteractor dashboardItemInteractor;
    private final DashboardElementInteractor dashboardElementInteractor;
    private DashboardFragmentView dashboardFragmentView;
    private final ApiExceptionHandler apiExceptionHandler;
    private final PreferencesModule preferencesModule;
    private final Logger logger;

    private CompositeSubscription subscription;

    public DashboardFragmentPresenterImpl(DashboardItemInteractor dashboardItemInteractor,
                                          DashboardElementInteractor dashboardElementInteractor,
                                          ApiExceptionHandler apiExceptionHandler,
                                          PreferencesModule preferencesModule,
                                          Logger logger) {

        this.dashboardItemInteractor = dashboardItemInteractor;
        this.dashboardElementInteractor = dashboardElementInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.preferencesModule = preferencesModule;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    @Override
    public void attachView(View view) {
        isNull(view, "view must not be null");
        dashboardFragmentView = (DashboardFragmentView) view;
    }

    @Override
    public void detachView() {
        dashboardFragmentView = null;

        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
            subscription = null;
        }
    }

    //TODO loadDashboardItems() Code using RxAndroid
    @Override
    public void loadLocalDashboardItems(String uId) {
        logger.d(TAG, "LoadDashboardItems()");

        final Observable<List<DashboardItem>> dashboardItems = dashboardItemInteractor.list(uId);
        dashboardItems.subscribeOn(Schedulers.newThread());
        dashboardItems.observeOn(AndroidSchedulers.mainThread());
        dashboardItems.subscribe(new Action1<List<DashboardItem>>() {
            @Override
            public void call(List<DashboardItem> dashboardItemList) {
                logger.d(TAG ,"LoadDashboardItems " + dashboardItemList.toString());

                if (dashboardItemList != null && !dashboardItemList.isEmpty()) {
                    for (DashboardItem dashboardItem : dashboardItemList) {
                        List<DashboardElement> dashboardElements = dashboardElementInteractor.list(dashboardItem.getUId()).toBlocking().first();
                        Log.d(TAG +" DElements", dashboardElements.toString());
                        dashboardItem.setDashboardElements(dashboardElements);
                    }
                }

                dashboardFragmentView.showDashboardItems(dashboardItemList);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "LoadDashboardItems failed");
                handleError(throwable);
            }
        });
        // TODO replace this by actual loading from SDK
    }

    // TODO Add deleteDashboardItem() method to DashboardInteractor in SDK
    @Override
    public void deleteDashboardItem(DashboardItem dashboardItem) {
//        dashboardItemInteractor.deleteDashboardItem();
        // TODO syncDashboards() in parentViewPager
    }

    // TODO Add deleteDashboardElement() method to DashboardInteractor in SDK
    @Override
    public void deleteDashboardElement(DashboardElement dashboardElement) {
//        dashboardInteractor.deleteDashboardElement();
        // TODO syncDashboards() in parentViewPager
    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        dashboardFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        dashboardFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        dashboardFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }

    @Override
    public PreferencesModule getPreferenceModule(){
        return preferencesModule;
    }
}
