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

package org.hisp.dhis.android.dashboard.presenters.interpretation;

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationCreateFragmentView;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardElementInteractor;
import org.hisp.dhis.client.sdk.android.dashboard.DashboardItemInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationElementInteractor;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.core.common.network.ApiException;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardElement;
import org.hisp.dhis.client.sdk.models.dashboard.DashboardItem;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.models.interpretation.InterpretationElement;
import org.hisp.dhis.client.sdk.models.user.User;
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

public class InterpretationCreateFragmentPresenterImpl implements InterpretationCreateFragmentPresenter {
    private static final String TAG = InterpretationCreateFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationInteractor interpretationInteractor;
    private final DashboardElementInteractor dashboardElementInteractor;
    private final DashboardItemInteractor dashboardItemInteractor;
    private final InterpretationElementInteractor interpretationElementInteractor;

    private InterpretationCreateFragmentView interpretationCreateFragmentView;

    private final ApiExceptionHandler apiExceptionHandler;
    private final Logger logger;

    private CompositeSubscription subscription;

    public InterpretationCreateFragmentPresenterImpl(InterpretationInteractor interpretationInteractor,
                                                     DashboardElementInteractor dashboardElementInteractor,
                                                     DashboardItemInteractor dashboardItemInteractor,
                                                     InterpretationElementInteractor interpretationElementInteractor,
                                                     ApiExceptionHandler apiExceptionHandler,
                                                     Logger logger) {
        this.interpretationInteractor = interpretationInteractor;
        this.dashboardElementInteractor = dashboardElementInteractor;
        this.dashboardItemInteractor = dashboardItemInteractor;
        this.interpretationElementInteractor = interpretationElementInteractor;
        this.apiExceptionHandler = apiExceptionHandler;
        this.logger = logger;

        this.subscription = new CompositeSubscription();
    }

    public void attachView(View view) {
        isNull(view, "InterpretationEmptyFragmentView must not be null");
        interpretationCreateFragmentView = (InterpretationCreateFragmentView) view;

    }

    @Override
    public void detachView() {
        interpretationCreateFragmentView = null;
    }


    @Override
    public User getUser() {
        return interpretationInteractor.getCurrentUserLocal();
    }

    @Override
    public void createInterpretation(DashboardItem dashboardItem, User user, String text){

        Observable<Interpretation> interpretation =  interpretationInteractor.create(dashboardItem,
                user, text);
        interpretation.subscribeOn(Schedulers.newThread());
        interpretation.observeOn(AndroidSchedulers.mainThread());
        interpretation.subscribe(new Action1<Interpretation>() {
            @Override
            public void call(Interpretation interpretation) {
                logger.d(TAG ,"onCreateInterpretation " + interpretation.toString());

                List<InterpretationElement> elements = interpretation
                        .getInterpretationElements();

                // save interpretation
                interpretationInteractor.save(interpretation).toBlocking().first();;
//        interpretation.save();
                if (elements != null && !elements.isEmpty()) {
                    for (InterpretationElement element : elements) {
                        // save corresponding interpretation elements
                        interpretationElementInteractor.save(element).toBlocking().first();;
                        interpretationInteractor.syncInterpretations();
//                element.save();
                    }
                }
            }

        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onCreateInterpretation failed");
                handleError(throwable);
            }
        });
    }

    @Override
    public void getDashboardItem(String dashboardItemUId) {

//        mDashboardItem = new Select()
//                .from(DashboardItem.class)
//                .where(Condition.column(DashboardItem$Table
//                        .ID).is(dashboardItemId))
//                .querySingle();

        Observable<DashboardItem> dashboardItem = dashboardItemInteractor.get(dashboardItemUId);
        dashboardItem.subscribeOn(Schedulers.newThread());
        dashboardItem.observeOn(AndroidSchedulers.mainThread());
        dashboardItem.subscribe(new Action1<DashboardItem>() {
            @Override
            public void call(DashboardItem dashboardItem) {
                logger.d(TAG ,"onGetDashboardItem " + dashboardItem.toString());

                interpretationCreateFragmentView.setCurrentDashboardItem(dashboardItem);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "onGetDashboardItem failed");
                handleError(throwable);
            }
        });

    }

    @Override
    public void getDashboardElements(String dashboardItemUId) {

//        new Select()
//                .from(DashboardElement.class)
//                .where(Condition.column(DashboardElement$Table
//                        .DASHBOARDITEM_DASHBOARDITEM).is(dashboardItemId))
//                .and(Condition.column(DashboardElement$Table
//                        .STATE).isNot(State.TO_DELETE.toString()))
//                .queryList();

        Observable<List<DashboardElement>> dashboardElements = dashboardElementInteractor.list(dashboardItemUId);
        dashboardElements.subscribeOn(Schedulers.newThread());
        dashboardElements.observeOn(AndroidSchedulers.mainThread());
        dashboardElements.subscribe(new Action1<List<DashboardElement>>() {
            @Override
            public void call(List<DashboardElement> dashboardElements) {
                logger.d(TAG ,"LoadedDashboardElements " + dashboardElements.toString());
                interpretationCreateFragmentView.setDashboardElements(dashboardElements);
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                logger.d(TAG , "LoadedDashboardElements failed");
                handleError(throwable);
            }
        });

    }

    @Override
    public void handleError(final Throwable throwable) {
        AppError error = apiExceptionHandler.handleException(TAG, throwable);

        if (throwable instanceof ApiException) {
            ApiException exception = (ApiException) throwable;

            if (exception.getResponse() != null) {
                switch (exception.getResponse().getStatus()) {
                    case HttpURLConnection.HTTP_UNAUTHORIZED: {
                        interpretationCreateFragmentView.showError(error.getDescription());
                        break;
                    }
                    case HttpURLConnection.HTTP_NOT_FOUND: {
                        interpretationCreateFragmentView.showError(error.getDescription());
                        break;
                    }
                    default: {
                        interpretationCreateFragmentView.showUnexpectedError(error.getDescription());
                        break;
                    }
                }
            }
        } else {
            logger.e(TAG, "handleError", throwable);
        }
    }
}
