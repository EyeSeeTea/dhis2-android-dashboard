/*
 * Copyright (c) 2015, University of Oslo
 * All rights reserved.
 *
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

package org.hisp.dhis.android.dashboard.api.utils;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.Interceptor;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Response;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.hisp.dhis.android.dashboard.api.controllers.DhisController;
import org.hisp.dhis.android.dashboard.api.network.RepoManager;
import org.hisp.dhis.android.dashboard.api.persistence.preferences.SettingsManager;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class PicassoProvider {

    private static Picasso mPicasso;
    private static OkHttpClient mClient;

    private PicassoProvider() {
    }

    public static Picasso init(Context context) {

        if (mPicasso == null) {

            mClient = buildClient(context);

            Picasso.Builder builder = new Picasso.Builder(context)
                    .downloader(new OkHttpDownloader(mClient));

            mPicasso = builder.build();
            mPicasso.setIndicatorsEnabled(false);
            mPicasso.setLoggingEnabled(false);
            Picasso.setSingletonInstance(mPicasso);
        }

        return mPicasso;
    }

    private static OkHttpClient buildClient(Context context){
        int networkTimeOut = Integer.parseInt(
                SettingsManager.getInstance(context).getPreference(
                        SettingsManager.NETWORK_TIMEOUT,
                        SettingsManager.MAXIMUM_NETWORK_TIMEOUT));
        int diskTimeOut = Integer.parseInt(
                SettingsManager.getInstance(context).getPreference(SettingsManager.DISK_TIMEOUT,
                        SettingsManager.MAXIMUM_DISK_TIMEOUT));
        final int imageTimeOut = Integer.parseInt(
                SettingsManager.getInstance(context).getPreference(
                        SettingsManager.IMAGE_TIMEOUT, SettingsManager.MAXIMUM_IMAGE_TIMEOUT));

        OkHttpClient client = RepoManager.provideOkHttpClient(
                DhisController.getInstance().getUserCredentials(), context);

        client.networkInterceptors().add(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                return originalResponse.newBuilder().header("Cache-Control",
                        "max-age=" + (imageTimeOut)).build();
            }
        });

        client.setConnectTimeout(networkTimeOut, TimeUnit.SECONDS);
        client.setReadTimeout(diskTimeOut, TimeUnit.SECONDS);
        File cachePath = context.getCacheDir();
        client.setCache(new Cache(cachePath, Integer.MAX_VALUE));
        return client;
    }

    public static void updateClientParameters(Context context){
        OkHttpClient client = buildClient(context);
        mClient = client;
        Picasso.Builder builder = new Picasso.Builder(context)
                .downloader(new OkHttpDownloader(mClient));
        mPicasso = builder.build();
    }

    public static Picasso getPicasso(){
        return mPicasso;
    }

    public static OkHttpClient getClient(){
        return mClient;
    }
}
