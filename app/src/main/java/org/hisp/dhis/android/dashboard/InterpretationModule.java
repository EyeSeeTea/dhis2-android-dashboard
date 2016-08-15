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

import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCommentEditFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCommentEditFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCommentsFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCommentsFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCreateFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationCreateFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationEmptyFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationEmptyFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationTextEditFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationTextEditFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationTextFragmentPresenter;
import org.hisp.dhis.android.dashboard.presenters.interpretation.InterpretationTextFragmentPresenterImpl;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCommentEditFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCommentsFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCreateFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationEmptyFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationTextEditFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationTextFragment;
import org.hisp.dhis.client.sdk.android.api.D2;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardElementInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardItemInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationCommentInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationElementInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationComment;
import org.hisp.dhis.client.sdk.ui.bindings.commons.ApiExceptionHandler;
import org.hisp.dhis.client.sdk.utils.Logger;

import javax.annotation.Nullable;

import dagger.Module;
import dagger.Provides;

@Module
public class InterpretationModule {

    public InterpretationModule() {
        // explicit empty constructor
    }

    @Provides
    @Nullable
    @PerUser
    public InterpretationCommentInteractor providesInterpretationCommentInteractor() {
        if (D2.isConfigured()) {
            return D2.interpretationComments();
        }
        return null;
    }

    @Provides
    @PerUser
    public InterpretationEmptyFragmentPresenter providesInterpretationEmptyFragmentPresenter(
            @Nullable InterpretationInteractor interpretationInteractor,
            ApiExceptionHandler apiExceptionHandler, Logger logger
    ) {
        return new InterpretationEmptyFragmentPresenterImpl(interpretationInteractor,
                 apiExceptionHandler, logger);
    }

    @Provides
    @PerUser
    public InterpretationFragmentPresenter providesInterpretationFragmentPresenter(
            @Nullable InterpretationInteractor interpretationInteractor,
            @Nullable InterpretationElementInteractor interpretationElementInteractor,
            @Nullable InterpretationCommentInteractor interpretationCommentInteractor,
            ApiExceptionHandler apiExceptionHandler,
            PreferencesModule preferencesModule,
            Logger logger
    ) {
        return new InterpretationFragmentPresenterImpl(interpretationInteractor,
                interpretationElementInteractor, interpretationCommentInteractor,
                 apiExceptionHandler, preferencesModule, logger);
    }

    @Provides
    @PerUser
    public InterpretationCreateFragmentPresenter providesInterpretationCreateFragmentPresenter(
            @Nullable InterpretationInteractor interpretationInteractor,
            @Nullable DashboardElementInteractor dashboardElementInteractor,
            @Nullable DashboardItemInteractor dashboardItemInteractor,
            @Nullable InterpretationElementInteractor interpretationElementInteractor,
            ApiExceptionHandler apiExceptionHandler, Logger logger
    ) {
        return new InterpretationCreateFragmentPresenterImpl(interpretationInteractor,
                dashboardElementInteractor, dashboardItemInteractor, interpretationElementInteractor,
                apiExceptionHandler, logger);
    }


}
