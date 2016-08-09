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

import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationContainerFragment;
import org.hisp.dhis.android.dashboard.views.fragments.interpretation.InterpretationContainerFragmentView;
import org.hisp.dhis.client.sdk.android.api.utils.DefaultOnSubscribe;
import org.hisp.dhis.client.sdk.android.interpretation.InterpretationInteractor;
import org.hisp.dhis.client.sdk.models.interpretation.Interpretation;
import org.hisp.dhis.client.sdk.ui.bindings.views.View;
import org.hisp.dhis.client.sdk.utils.Logger;

import java.util.List;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static org.hisp.dhis.client.sdk.utils.Preconditions.isNull;

public class InterpretationContainerFragmentPresenterImpl implements InterpretationContainerFragmentPresenter {
    private static final String TAG = InterpretationContainerFragmentPresenterImpl.class.getSimpleName();
    private final InterpretationInteractor interpretationInteractor;
    private InterpretationContainerFragmentView interpretationContainerFragmentView;
    private final Logger logger;

    public InterpretationContainerFragmentPresenterImpl(
            InterpretationInteractor interpretationInteractor, Logger logger) {
        this.interpretationInteractor = interpretationInteractor;
        this.logger = logger;
    }

    public void attachView(View view) {
        isNull(view, "InterpretationContainerFragmentView must not be null");
        if(interpretationContainerFragmentView==null) {
            interpretationContainerFragmentView = (InterpretationContainerFragment) view;
        }
    }

    @Override
    public void detachView() {
        interpretationContainerFragmentView = null;
    }

    @Override
    public void onLoadLocalData() {
        logger.d(TAG, "InterpretationOnLoadLocalData()");
        Observable<Boolean> hasData = checkIfSHasData();
        hasData.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Boolean>() {
                    @Override
                    public void call(Boolean hasData) {
                        handleNavigation(hasData);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        logger.d(TAG , "datError");
                        logger.e(TAG, "HD", throwable);
                        //handle error
                    }
                });
    }

    // TODO Replace by listByActions later
    private Observable<Boolean> checkIfSHasData() {
//        EnumSet<Action> updateActions = EnumSet.of(Action.TO_POST, Action.TO_UPDATE, Action.TO_DELETE);
//        return interpretationInteractor.listByActions(DbAction.INSERT)
        return interpretationInteractor.list()
                .switchMap(new Func1<List<Interpretation>, Observable<Boolean>>() {
                    @Override
                    public Observable<Boolean> call(final List<Interpretation> interpretations) {
                        return Observable.create(new DefaultOnSubscribe<Boolean>() {
                            @Override
                            public Boolean call() {
                                return interpretations != null && !interpretations.isEmpty();
                            }
                        });
                    }
                });
    }

    private void handleNavigation(Boolean hasData){
        if(hasData){
            // 2 Conditions :
            // if Empty fragment of container has to be loaded first, check for !=null
            // if onLoad() has to be done before loading empty fragment, do not check for !=null
            logger.d(TAG, "hasData");
            if (interpretationContainerFragmentView != null) {
                interpretationContainerFragmentView.navigationAfterLoadingData(true);
            }
        } else{
            logger.d(TAG , "hasNoData");
            if (interpretationContainerFragmentView != null) {
                interpretationContainerFragmentView.navigationAfterLoadingData(false);
            }
        }
    }
}