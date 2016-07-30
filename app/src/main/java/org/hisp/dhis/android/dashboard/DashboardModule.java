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

package org.hisp.dhis.android.dashboard;

import org.hisp.dhis.android.dashboard.presenters.DashboardAddFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardAddFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardElementDetailActivityPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardManageFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardManageFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.sync.SyncWrapper;
import org.hisp.dhis.android.dashboard.presenters.DashboardEmptyFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardEmptyFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardElementDetailActivityPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardItemAddFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardItemAddFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardViewPagerFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardViewPagerFragmentPresenterImpl;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardContentInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardElementInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardItemInteractor;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;

import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;

import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class DashboardModule {

    public DashboardModule() {
        // explicit empty constructor
    }

    @Provides
    @Nullable
    @PerUser
    public DashboardContentInteractor providesDashboardContentInteractor() {
        if (D2.isConfigured()) {
            return D2.dashboardContent();
        }
        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public DashboardItemInteractor providesDashboardItemInteractor() {
        if (D2.isConfigured()) {
            return D2.dashboardItems();
        }
        return null;
    }

    @Provides
    @Nullable
    @PerUser
    public DashboardElementInteractor providesDashboardElementsInteractor() {
        if (D2.isConfigured()) {
            return D2.dashboardElements();
        }
        return null;
    }

    // TODO Add more arguements to SyncWrapper
    @Provides
    @PerUser
    public SyncWrapper providesSyncWrapper(
            @Nullable DashboardInteractor dashboardInteractor) {
        return new SyncWrapper(dashboardInteractor);
    }

    //  TODO    SyncDateWrapper syncDateWrapper, SyncWrapper syncWrapper
    @Provides
    @PerUser
    public DashboardEmptyFragmentPresenter providesDashboardEmptyFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor,
            @Nullable DashboardContentInteractor dashboardContentInteractor,
            SessionPreferences sessionPreferences,
            SyncDateWrapper syncDateWrapper,
            SyncWrapper syncWrapper,
            ApiExceptionHandler apiExceptionHandler, Logger logger
    ) {
        return new DashboardEmptyFragmentPresenterImpl(dashboardInteractor,
                dashboardContentInteractor, sessionPreferences, syncDateWrapper, syncWrapper,
                apiExceptionHandler, logger);
    }

    //  TODO    SyncDateWrapper syncDateWrapper, SyncWrapper syncWrapper
    @Provides
    @PerUser
    public DashboardViewPagerFragmentPresenter providesDashboardViewPagerFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor,
            @Nullable DashboardContentInteractor dashboardContentInteractor,
            ApiExceptionHandler apiExceptionHandler, Logger logger
    ) {
        return new DashboardViewPagerFragmentPresenterImpl(dashboardInteractor,
                dashboardContentInteractor, apiExceptionHandler, logger);
    }

    @Provides
    @PerUser
    public DashboardFragmentPresenter providesDashboardFragmentPresenter(
            @Nullable DashboardItemInteractor dashboardItemInteractor,
            @Nullable DashboardElementInteractor dashboardElementInteractor,
            PreferencesModule preferencesModule, ApiExceptionHandler apiExceptionHandler,
            Logger logger
    ) {
        return new DashboardFragmentPresenterImpl(dashboardItemInteractor,
                dashboardElementInteractor, apiExceptionHandler, preferencesModule,logger);
    }

    @Provides
    @PerUser
    public DashboardElementDetailActivityPresenter providesDashboardElementDetailActivityPresenter(
            @Nullable DashboardElementInteractor dashboardElementInteractor,
            ApiExceptionHandler apiExceptionHandler,
            PreferencesModule preferencesModule, Logger logger
    ) {
        return new DashboardElementDetailActivityPresenterImpl(dashboardElementInteractor,
                apiExceptionHandler, logger,  preferencesModule);
    }

    @Provides
    @PerUser
    public DashboardItemAddFragmentPresenter providesDashboardItemAddFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor, Logger logger
    ) {
        return new DashboardItemAddFragmentPresenterImpl(dashboardInteractor, logger);
    }

    @Provides
    @PerUser
    public DashboardAddFragmentPresenter providesDashboardAddFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor, Logger logger
    ) {
        return new DashboardAddFragmentPresenterImpl(dashboardInteractor, logger);
    }

    @Provides
    @PerUser
    public DashboardManageFragmentPresenter providesDashboardManageFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor, Logger logger
    ) {
        return new DashboardManageFragmentPresenterImpl(dashboardInteractor, logger);
    }

}
