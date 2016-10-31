package com.sag.retrofitlibrary.base;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Retrofit接收字符串解析器，Retrofit必须包含一个解析器
 * <p>
 * ps：该解析器的实体并未做任何操作，解析已工作交给了OkHttp的拦截器
 * <p>
 * Created by CY on 2016/3/16.
 */
class ConverterFactory extends Converter.Factory {

    public static ConverterFactory create() {
        return new ConverterFactory();
    }

    @Override
    public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
        return new StringResponseConverter();
    }

    @Override
    public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
        return super.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit);
    }

    private static class StringResponseConverter implements Converter<ResponseBody, String> {

        @Override
        public String convert(ResponseBody value) throws IOException {
            String result = value.string();
            return result;
        }
    }

}
