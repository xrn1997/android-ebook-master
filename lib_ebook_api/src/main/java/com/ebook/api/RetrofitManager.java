package com.ebook.api;


import com.ebook.api.config.API;
import com.ebook.api.service.CommentService;
import com.ebook.api.service.UserService;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitManager {
    private volatile static RetrofitManager retrofitManager;
    private final Retrofit mRetrofit;
    public String TOKEN;

    private RetrofitManager() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder okHttpBuilder = new OkHttpClient.Builder();
        okHttpBuilder.interceptors().add(logging);
//        okHttpBuilder.addInterceptor(new EncodingInterceptor("UTF-8"));
        mRetrofit = new Retrofit.Builder()
                .client(okHttpBuilder.build())
                .baseUrl(API.URL_HOST_USER)
                //增加返回值为Observable<T>的支持
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                //增加返回值为字符串的支持(以实体类返回)
                .addConverterFactory(GsonConverterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
    }

    public static RetrofitManager getInstance() {
        if (retrofitManager == null) {
            synchronized (RetrofitManager.class) {
                if (retrofitManager == null) {
                    retrofitManager = new RetrofitManager();
                }
            }
        }
        return retrofitManager;
    }

    public UserService getUserService() {

        return mRetrofit.create(UserService.class);
    }

    public CommentService getCommentService() {
        return mRetrofit.create(CommentService.class);
    }
}