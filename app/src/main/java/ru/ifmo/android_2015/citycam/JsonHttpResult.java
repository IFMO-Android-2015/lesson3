package ru.ifmo.android_2015.citycam;

import android.support.annotation.Nullable;

/**
 * Created by dmitry.trunin on 02.11.2015.
 */
public class JsonHttpResult<Data> {

    /**
     * Тип ошибки, либо null, если нет ошибки.
     */
    public final @Nullable JsonHttpError errorType;

    /**
     * Код ошибки (например, в случае errorType==HTTP_RESPONSE, это код ответа,
     * полученный от сервера. Либо 0, если нет ошибки, или ошибка не подразумевает
     * дополнительного кода.
     */
    public final int errorCode;

    /**
     * Собственно загруженные данные, либо null, если была ошибка.
     */
    public final @Nullable Data data;

    JsonHttpResult(Data data) {
        this(null, 0, data);
    }

    JsonHttpResult(JsonHttpError errorType) {
        this(errorType, 0, null);
    }

    JsonHttpResult(JsonHttpError errorType, int errorCode) {
        this(errorType, errorCode, null);
    }

    JsonHttpResult(JsonHttpResult<Data> other) {
        this(other.errorType, other.errorCode, other.data);
    }

    JsonHttpResult(JsonHttpError errorType, int errorCode, Data data) {
        this.errorType = null;
        this.errorCode = 0;
        this.data = data;
    }

    @Override
    public String toString() {
        return "JsonHttpResult[errorType=" + errorType
                + " errorCode=" + errorCode
                + " data=" + data
                + "]";
    }
}
