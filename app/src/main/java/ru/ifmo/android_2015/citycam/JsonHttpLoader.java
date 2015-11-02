package ru.ifmo.android_2015.citycam;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dmitry.trunin on 02.11.2015.
 */
public abstract class JsonHttpLoader<Data> extends AsyncTaskLoader<JsonHttpResult<Data>> {

    private JsonHttpResult<Data> lastDeliveredResult;

    public JsonHttpLoader(Context context) {
        super(context);
    }

    protected abstract URL createURL() throws MalformedURLException;

    protected abstract Data parseResponse(JsonReader reader) throws IOException;

    @Override
    public JsonHttpResult<Data> loadInBackground() {
        HttpURLConnection conn = null;
        InputStream in = null;
        // по этому флагу определяем, было ли хоть что-то получено в ответ (для типа ошибки)
        boolean responseReceived = false;

        try {
            URL url = createURL();
            Log.d(TAG, "Performing request: " + url);

            conn = (HttpURLConnection) url.openConnection();

            // Проверяем HTTP код ответа. Ожидаем только ответ 200 (ОК).
            // Остальные коды считаем ошибкой.
            int responseCode = conn.getResponseCode();
            responseReceived = true;

            Log.d(TAG, "Received HTTP response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_OK) {
                throw new FileNotFoundException("Unexpected HTTP response: " + responseCode
                        + ", " + conn.getResponseMessage());
            }

            in = new BufferedInputStream(conn.getInputStream());
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            Data data = parseResponse(reader);
            return new JsonHttpResult<>(data);

        } catch (IOException e) {
            Log.e(TAG, "Failed to execute request: " + e, e);
            if (responseReceived) {
                return new JsonHttpResult<>(JsonHttpError.IO);
            } else {
                return new JsonHttpResult<>(JsonHttpError.NETWORK);
            }

        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    Log.w(TAG, "Error closing input stream: " + e, e);
                }
            }
            if (conn != null) {
                conn.disconnect();
            }
        }
    }



    @Override
    protected void onStartLoading() {
        if (lastDeliveredResult != null) {
            // Не выполняем новый запрос, если уже есть результат
            // Доставляем тот же результат снова (делаем копию, потому что код Loader
            // требует, чтобы каждый раз был новый объект -- иначе результат не доставляется
            // в onLoadFinished
            deliverResult(new JsonHttpResult<>(lastDeliveredResult));
        } else {
            forceLoad();
        }
    }

    @Override
    public void deliverResult(JsonHttpResult<Data> result) {
        lastDeliveredResult = result;
        super.deliverResult(result);
    }

    protected final String TAG = getClass().getSimpleName();
}
