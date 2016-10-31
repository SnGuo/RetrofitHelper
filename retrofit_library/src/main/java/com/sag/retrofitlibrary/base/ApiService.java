package com.sag.retrofitlibrary.base;

import java.util.Map;

import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * 网络访问接口，负责拼接http请求字符串躯干
 * <p>
 * Created by SAG on 2016/3/16.
 */
interface ApiService {

    @FormUrlEncoded
    @POST("{method}")
    Observable<String> post(@Path("method") String method, @FieldMap Map<String, Object> fieldMap);

}
