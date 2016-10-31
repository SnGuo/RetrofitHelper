package com.sag.retrofitlibrary.base;

import android.os.Handler;
import android.util.Log;

import com.google.gson.Gson;
import com.sag.foundationlibrary.base.initial.InitialHelper;
import com.sag.foundationlibrary.base.model.ModelStamp;
import com.sag.foundationlibrary.base.server.ServerStamp;
import com.sag.foundationlibrary.base.util.GsonUtil;
import com.sag.foundationlibrary.base.view.PersistentStamp;
import com.sag.foundationlibrary.base.view.ResponseStamp;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * 网络访问帮助器
 * <p>
 * Created by SAG on 2016/10/11 0011.
 */

public class RequestHelper implements ServerStamp {

    private final Handler HANDLER = new Handler();

    private final boolean isDebug = InitialHelper.isDebug();
    private final boolean isEncrypt = InitialHelper.isEncrypt();

    private final int TIME_MIN = InitialHelper.getTimeMine();
    private final int TIME_OUT = InitialHelper.getTimeOut();

    private String fixedMethod = null;

    private ApiService mService;

    public RequestHelper() {

        //初始化OkHttp，为OkHttp设置拦截器，拦截请求到的字符串进行解密、以及打印日志等操作
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            ResponseBody responseBody = response.body();
            String result = responseBody.string();
            result = isEncrypt ? DESUtil.decryptDoNet(result) : result;//解密
            if (isDebug && fixedMethod == null) {
                Log.e("ServiceResponseMessage", "服务器返回数据:" + result);
            }
            ResponseBody newResponseBody = ResponseBody.create(responseBody.contentType(), result);
            Response newResponse = response.newBuilder().body(newResponseBody).build();
            return newResponse;
        }).build();

        //初始化Retrofit，拼接http请求字符串主干，设置OkHttp，以及数据转化工具
        Retrofit retrofit = new Retrofit.Builder().baseUrl(InitialHelper.getServiceUrl())
                .client(client)
                .addConverterFactory(ConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();

        //创建网络发送器，RetrofitHelper初始化完成
        mService = retrofit.create(ApiService.class);
    }

    public void fixedMethod(String method) {
        fixedMethod = method;
    }

    private final int CACHE = 1024 * 4;

    private void debug(String message) {
        int n = message.length() / CACHE;
        for (int i = 0; i < n; i++) {
            Log.e("ServiceResponseMessage " + i, message.substring(i, (i + 1) * CACHE));
        }
        int surplus = message.length() % 1024;
        if (surplus != 0) {
            Log.e("ServiceResponseMessage " + n, message.substring(n * CACHE, message.length()));
        }
    }

    //数据转换器，负责对表单进行加密
    private Map<String, Object> transitionObj(String method, Object object) {

        String json = new Gson().toJson(object);

        if (isDebug) {
            if (fixedMethod == null) {
                Log.e("RequestHelper:" + method, json);
            } else {
                if (fixedMethod.equals(method)) {
                    Log.e("RequestHelper:" + method, json);
                }
            }
        }

        Map<String, Object> map = new HashMap<>();

        if (isEncrypt) {
            map.put("json", DESUtil.encryptAsDoNet(json));
        } else {
            map.put("json", json);
        }

        return map;
    }


    @Override
    public void request(ModelStamp stamp) {

        String method = stamp.getMethod();

        Subscription subscription = mService.post(method, transitionObj(method, stamp.getParameter()))
                .delay(TIME_MIN, TimeUnit.MILLISECONDS)
                .timeout(TIME_OUT, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.newThread())//线程调度，在支线程中访问网络
                .observeOn(AndroidSchedulers.mainThread())//线程调度，将网络请求到的数据合并到主线程中
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        HANDLER.postDelayed(() -> {
                            stamp.unSubscribeRequest();
                            stamp.callBack(ResponseStamp.Target_Request_Error, stamp);
                        }, TIME_MIN);
                    }

                    @Override
                    public void onNext(String s) {
                        stamp.unSubscribeRequest();
                        if (stamp instanceof PersistentStamp) {
                            stamp.storage(s);
                        } else {
                            stamp.callBack(ResponseStamp.Target_Request_Success, GsonUtil.getGson().fromJson(s, stamp.getClass()));
                        }
                        if (isDebug && fixedMethod != null) {
                            debug(s);
                        }
                    }
                });

        stamp.receiveRequestSubscription(subscription);

    }
}
