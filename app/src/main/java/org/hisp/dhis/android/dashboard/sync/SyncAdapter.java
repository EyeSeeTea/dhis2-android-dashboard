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

package org.hisp.dhis.android.dashboard.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.client.sdk.models.dashboard.Dashboard;
import org.hisp.dhis.client.sdk.ui.AppPreferences;
import org.hisp.dhis.client.sdk.ui.SyncDateWrapper;
import org.hisp.dhis.client.sdk.ui.bindings.commons.DefaultNotificationHandler;

import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class SyncAdapter extends AbstractThreadedSyncAdapter {
    private static final String TAG = SyncAdapter.class.getSimpleName();

    @Inject
    SyncWrapper syncWrapper;

    @Inject
    SyncDateWrapper syncDateWrapper;

    @Inject
    AppPreferences appPreferences;

    @Inject
    DefaultNotificationHandler notificationHandler;


    public SyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        //inject the syncWrapper:
        ((DashboardApp) context.getApplicationContext()).getDashboardComponent().inject(this);
    }

    public SyncAdapter(Context context, boolean autoInitialize, boolean allowParallelSyncs) {
        super(context, autoInitialize, allowParallelSyncs);

        //inject the syncWrapper:
        ((DashboardApp) context.getApplicationContext()).getDashboardComponent().inject(this);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {

        syncWrapper.checkIfSyncIsNeeded()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .switchMap(new Func1<Boolean, Observable<List<Dashboard>>>() {
                    @Override
                    public Observable<List<Dashboard>> call(Boolean syncIsNeeded) {
                        if (syncIsNeeded) {
                            if (appPreferences.getSyncNotifications()) {
                                notificationHandler.showIsSyncingNotification();
                            }
                            return syncWrapper.backgroundSync();
                        }
                        return Observable.empty();
                    }
                })
                .subscribe(new Action1<List<Dashboard>>() {
                               @Override
                               public void call(List<Dashboard> events) {
                                   if (events != Observable.empty()) {
                                       syncDateWrapper.setLastSyncedNow();

                                       if (appPreferences.getSyncNotifications()) {
                                           notificationHandler.showSyncCompletedNotification(true);
                                       }
                                   }
                               }
                           }, new Action1<Throwable>() {
                               @Override
                               public void call(Throwable throwable) {
                                   Log.e(TAG, "Background synchronization failed.", throwable);
                                   if (appPreferences.getSyncNotifications()) {
                                       notificationHandler.showSyncCompletedNotification(false);
                                   }
                               }
                           }
                );
    }
}