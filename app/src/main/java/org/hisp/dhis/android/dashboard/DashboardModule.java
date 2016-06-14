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

import org.hisp.dhis.android.dashboard.presenters.DashboardContainerFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardContainerFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.DashboardEmptyFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.DashboardEmptyFragmentPresenterImpl;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardInteractor;
import org.hisp.dhis.client.sdk.android.organisationunit.UserOrganisationUnitInteractor;
import org.hisp.dhis.client.sdk.android.user.CurrentUserInteractor;
import org.hisp.dhis.client.sdk.core.common.network.Configuration;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.ui.bindings.commons.AppAccountManager;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultUserModule;
import org.hisp.dhis.client.sdk.ui.bindings.commons.SessionPreferences;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.HomePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LauncherPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.LoginPresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.ProfilePresenterImpl;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenter;
import org.hisp.dhis.client.sdk.ui.bindings.presenters.SettingsPresenterImpl;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

import static org.hisp.dhis.client.sdk.utils.StringUtils.isEmpty;

// TODO add DefaultDashboardModule to SDK ui bindings commons
@Module
public class DashboardModule implements DefaultDashboardModule {

    public DashboardModule() {
        // explicit empty constructor
    }

    // TODO Add dashboard interactor to SDK's D2.java
    @Provides
    @Nullable
    @PerUser
    public DashboardInteractor providesDashboardInteractor() {
        if (D2.isConfigured()) {
            return D2.dashboards();
        }
        return null;
    }

    // TODO
    @Provides
    @PerUser
    public SyncWrapper providesSyncWrapper(
    ) {
        return new SyncWrapper(
        );
    }

    //  TODO
    @Provides
    @PerUser
    public DashboardContainerFragmentPresenter providesDashboardContainerFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor
    ) {
        return new DashboardContainerFragmentPresenterImpl(dashboardInteractor);
    }

    //  TODO    SyncDateWrapper syncDateWrapper, SyncWrapper syncWrapper
    @Provides
    @PerUser
    public DashboardEmptyFragmentPresenter providesDashboardEmptyFragmentPresenter(
            @Nullable DashboardInteractor dashboardInteractor,
            SessionPreferences sessionPreferences,
            SyncDateWrapper syncDateWrapper, SyncWrapper syncWrapper,
            ApiExceptionHandler apiExceptionHandler, Logger logger
    ) {
        return new DashboardEmptyFragmentPresenterImpl(dashboardInteractor,
                sessionPreferences, syncDateWrapper, apiExceptionHandler, logger);
    }
}
