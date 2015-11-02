package ru.ifmo.android_2015.citycam;

import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.List;

import ru.ifmo.android_2015.citycam.model.City;
import ru.ifmo.android_2015.citycam.webcams.Webcam;
import ru.ifmo.android_2015.lesson3.cammap.R;

public class CamMapActivity extends AppCompatActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<JsonHttpResult<List<Webcam>>>,
        GoogleMap.OnCameraChangeListener {

    /**
     * Обязательный extra параметр - объект City, карту которого надо показать.
     */
    public static final String EXTRA_CITY = "city";

    private GoogleMap map;
    private MapView mapView;

    // Город, который надо показать (из экстра параметров интента)
    private City city;
    // Место на карте, которое надо показать при первом открытии экрана
    private LatLng initialLocation;

    private static final int LOADER_WEBCAMS_ON_MAP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_map);

        city = getIntent().getParcelableExtra(EXTRA_CITY);
        if (city == null) {
            Log.w(TAG, "City object not provided in extra parameter: " + EXTRA_CITY);
            finish();
            return;
        }

        getSupportActionBar().setTitle(city.name);

        if (savedInstanceState == null) {
            // Устанавливаем начальное положение только для первого открытия активности.
            // При повороте экрана или при возвращении назад устанавливать начальное положение
            // не надо, потому что 1) оно уже было установлено, 2) пользователь мог переместить
            // карту
            initialLocation = new LatLng(city.latitude, city.longitude);
        }

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);


        // Этот вызов запускает асинхронную инициализацию карты в фоновом потоке, включая проверку
        // наличия Google Services.
        // Когда все будет готово для работы с картой -- в UI потоке будет вызван метод onMapReady
        mapView.getMapAsync(this);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Точка входа для всех операций с картой.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        map.setOnCameraChangeListener(this);
        map.getUiSettings().setZoomControlsEnabled(true);

        if (initialLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 14f));
        }
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        getSupportLoaderManager().restartLoader(LOADER_WEBCAMS_ON_MAP, getWebcamArgs(map), this);
    }

    static Bundle getWebcamArgs(GoogleMap map) {
        Projection projection = map.getProjection();
        VisibleRegion region = projection.getVisibleRegion();
        LatLngBounds bounds = region.latLngBounds;
        CameraPosition camera = map.getCameraPosition();
        return WebcamsOnMapLoader.createArgs(bounds.southwest, bounds.northeast, camera.zoom);
    }

    @Override
    public Loader<JsonHttpResult<List<Webcam>>> onCreateLoader(int id, Bundle args) {
        if (id == LOADER_WEBCAMS_ON_MAP) {
            return new WebcamsOnMapLoader(this, args);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<JsonHttpResult<List<Webcam>>> loader,
                               JsonHttpResult<List<Webcam>> result) {
        Log.d(TAG, "onLoadFinished: result=" + result + ", loader=" + loader);
        if (map == null) {
            Log.w(TAG, "onLoadFinished: map not initialized");
            return;
        }
        List<Webcam> webcams = result.data;
        if (webcams != null) {
            map.clear();
            for (Webcam webcam : webcams) {
                LatLng camPosition = new LatLng(webcam.latitude, webcam.longitude);
                map.addMarker(new MarkerOptions()
                        .title(webcam.title)
                        .position(camPosition));
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<JsonHttpResult<List<Webcam>>> loader) {
        Log.d(TAG, "onLoaderReset: loader=" + loader);
        if (map != null) {
            map.clear();
        }
    }

    private static final String TAG = "CityMap";
}
