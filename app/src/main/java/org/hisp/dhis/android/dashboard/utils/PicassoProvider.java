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

package org.hisp.dhis.android.dashboard.utils;

import android.content.Context;


import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.OkHttpDownloader;
import com.squareup.picasso.Picasso;

import org.hisp.dhis.android.dashboard.BuildConfig;
import org.hisp.dhis.android.dashboard.DashboardApp;
import org.hisp.dhis.client.sdk.core.common.network.UserCredentials;
import org.hisp.dhis.client.sdk.core.common.preferences.PreferencesModule;
import org.hisp.dhis.client.sdk.core.common.preferences.UserPreferences;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.text.TextUtils.isEmpty;
import static okhttp3.Credentials.basic;

public class PicassoProvider {
    private static final int DEFAULT_CONNECT_TIMEOUT_MILLIS = 15 * 1000;   // 15s
    private static final int DEFAULT_READ_TIMEOUT_MILLIS = 20 * 1000;      // 20s
    private static final int DEFAULT_WRITE_TIMEOUT_MILLIS = 20 * 1000;     // 20s

    public PicassoProvider() {
    }

    public Picasso getInstance(Context context , PreferencesModule preferencesModule) {

        AuthInterceptor authInterceptor = new AuthInterceptor(
                preferencesModule.getUserPreferences());

        OkHttpClient client = null;
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(authInterceptor)
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_WRITE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        if(BuildConfig.DEBUG){
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BASIC);
            builder.addInterceptor(loggingInterceptor);
            client = builder.build();
        }else{
            client = builder.build();
        }

        return new Picasso.Builder(context)
                .downloader(new OkHttp3Downloader(client))
                .build();
    }

    private static class AuthInterceptor implements Interceptor {
        private final UserPreferences mUserPreferences;

        public AuthInterceptor(UserPreferences preferences) {
            mUserPreferences = preferences;
        }

        @Override
        public Response intercept(Chain chain) throws IOException {
            UserCredentials userCredentials = mUserPreferences.get();
            if (isEmpty(userCredentials.getUsername()) ||
                    isEmpty(userCredentials.getPassword())) {
                return chain.proceed(chain.request());
            }

            String base64Credentials = basic(
                    userCredentials.getUsername(),
                    userCredentials.getPassword());
            Request request = chain.request().newBuilder()
                    .addHeader("Authorization", base64Credentials)
                    .build();

            Response response = chain.proceed(request);
            if (mUserPreferences.isUserConfirmed()) {
                if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mUserPreferences.invalidateUser();
                }
            } else {
                if (response.isSuccessful()) {
                    mUserPreferences.confirmUser();
                } else {
                    mUserPreferences.clear();
                }
            }

            return response;
        }
    }
}
