package com.example.jh.gdmapntv;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.InfoWindowAdapter;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.TranslateAnimation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import java.lang.reflect.Type;

public class MarkerClickActivity extends CheckPermissionsActivity implements View.OnClickListener,
        AMap.OnMarkerClickListener, AMap.OnMapClickListener, AMap.OnInfoWindowClickListener,
        GeocodeSearch.OnGeocodeSearchListener, InfoWindowAdapter, AMap.OnCameraChangeListener {

    private MarkerOptions markerOption;
    private Marker screenMarker;
    private AMap aMap;
    private MapView mapView;
    private GeocodeSearch mGeocodeSearch;
    private CameraPosition mCameraPosition;
    private LatLng latlng = new LatLng(39.91746, 116.396481);
    private boolean startJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_marker_click);
        /*
		 * 设置离线地图存储目录，在下载离线地图或初始化地图设置; 使用过程中可自行设置, 若自行设置了离线地图存储的路径，
		 * 则需要在离线地图下载和使用地图页面都进行路径设置
		 */
        // Demo中为了其他界面可以使用下载的离线地图，使用默认位置存储，屏蔽了自定义设置
        // MapsInitializer.sdcardDir =OffLineMapUtils.getSdCacheDir(this);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState); // 此方法必须重写
        init();
    }

    /**
     * 初始化AMap对象
     */
    private void init() {
        Button clearMap = (Button) findViewById(R.id.clearMap);
        clearMap.setOnClickListener(this);
        Button resetMap = (Button) findViewById(R.id.resetMap);
        resetMap.setOnClickListener(this);
        if (aMap == null) {
            aMap = mapView.getMap();
        }
        setUpMap();
    }

    private void setUpMap() {
        mGeocodeSearch = new GeocodeSearch(getApplicationContext());
        mGeocodeSearch.setOnGeocodeSearchListener(this);
        aMap.setOnMarkerClickListener(this);
        aMap.setOnMapClickListener(null);
        aMap.setOnInfoWindowClickListener(this);
        aMap.setOnMapLoadedListener(new AMap.OnMapLoadedListener() {
            @Override
            public void onMapLoaded() {
                addMarkerInScreenCenter();
            }
        });

        aMap.setOnCameraChangeListener(this);

        aMap.setInfoWindowAdapter(this);
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {

    }

    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        aMap.setOnCameraChangeListener(null);
        mCameraPosition = cameraPosition;
        startJumpAnimation();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * 方法必须重写
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    /**
     * 在地图上添加marker
     */
    private void addMarkersToMap(LatLng llng) {
        aMap.clear();
        markerOption = new MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .position(llng)
                .draggable(true);
        aMap.addMarker(markerOption);
    }

    /**
     * 对marker标注点点击响应事件
     */
    @Override
    public boolean onMarkerClick(final Marker marker) {
        if (aMap != null) {
//            jumpPoint(marker);
            startJumpAnimation();
        }
        Toast.makeText(MarkerClickActivity.this, "您点击了Marker", Toast.LENGTH_LONG).show();
        return true;
    }

    @Override
    public void onRegeocodeSearched(RegeocodeResult regeocodeResult, int i) {
        //地图移动的监听
        aMap.setOnCameraChangeListener(this);
        RegeocodeAddress regeocodeAddress = regeocodeResult.getRegeocodeAddress();
        screenMarker.setTitle(regeocodeResult.getRegeocodeAddress().getDistrict());
        if(regeocodeAddress != null){
            String city =
                    //regeocodeAddress.getCity()
                    // + regeocodeAddress.getDistrict()
                    regeocodeAddress.getTownship()
                            + regeocodeAddress.getStreetNumber().getStreet()
                            + regeocodeAddress.getStreetNumber().getNumber()
                            + regeocodeAddress.getBuilding();
            screenMarker.setSnippet(city);
        }
        screenMarker.showInfoWindow();
    }

    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
        aMap.setOnCameraChangeListener(this);
    }

    /**
     * marker点击时跳动一下
     */
    public void jumpPoint(final Marker marker) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = aMap.getProjection();
        final LatLng markerLatlng = marker.getPosition();
        Point markerPoint = proj.toScreenLocation(markerLatlng);
        markerPoint.offset(0, -100);
        final LatLng startLatLng = proj.fromScreenLocation(markerPoint);
        final long duration = 1500;

        final Interpolator interpolator = new BounceInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * markerLatlng.longitude + (1 - t)
                        * startLatLng.longitude;
                double lat = t * markerLatlng.latitude + (1 - t)
                        * startLatLng.latitude;
                marker.setPosition(new LatLng(lat, lng));
                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public void onMapClick(LatLng latLng) {
        addMarkersToMap(latLng);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            /**
             * 清空地图上所有已经标注的marker
             */
            case R.id.clearMap:
                if (aMap != null) {
                    aMap.clear();
                }
                break;
            /**
             * 重新标注所有的marker
             */
            case R.id.resetMap:
                if (aMap != null) {
                    aMap.clear();
                    addMarkersToMap(latlng);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(MarkerClickActivity.this, "点击了" + marker.getTitle(), Toast.LENGTH_LONG).show();
    }

    public void addMarkerInScreenCenter() {
        LatLng latLng = aMap.getCameraPosition().target;
        Point point = aMap.getProjection().toScreenLocation(latLng);
        screenMarker = aMap.addMarker(new
                MarkerOptions().icon(BitmapDescriptorFactory.
                fromResource(R.drawable.purple_pin)).draggable(true).title("标题").snippet("内容").position(latLng).anchor(0.5f, 0.5f));
        screenMarker.showInfoWindow();
        screenMarker.setPositionByPixels(point.x, point.y);
    }

    /**
     * 屏幕中心marker 跳动
     */
    public void startJumpAnimation() {

        if (screenMarker != null) {
            //根据屏幕距离计算需要移动的目标点
            final LatLng latLng = screenMarker.getPosition();
            Point point = aMap.getProjection().toScreenLocation(latLng);
            point.y -= dip2px(this, 125);
            LatLng target = aMap.getProjection()
                    .fromScreenLocation(point);
            //使用TranslateAnimation,填写一个需要移动的目标点
            Animation animation = new TranslateAnimation(target);
            animation.setInterpolator(new Interpolator() {
                @Override
                public float getInterpolation(float input) {
                    // 模拟重加速度的interpolator
                    if (input <= 0.5) {
                        return (float) (0.5f - 2 * (0.5 - input) * (0.5 - input));
                    } else {
                        return (float) (0.5f - Math.sqrt((input - 0.5f) * (1.5f - input)));
                    }
                }
            });
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart() {
                    LatLonPoint latLngPoint = new LatLonPoint(mCameraPosition.target.latitude, mCameraPosition.target.longitude);
                    RegeocodeQuery regeocodeQuery = new RegeocodeQuery(latLngPoint, 1000, GeocodeSearch.AMAP);
                    mGeocodeSearch.getFromLocationAsyn(regeocodeQuery);
                }

                @Override
                public void onAnimationEnd() {
                    startJump = false;

                }
            });
            //整个移动所需要的时间
            animation.setDuration(1000);
            //设置动画
            screenMarker.setAnimation(animation);
            //开始动画
            screenMarker.startAnimation();

        } else {
            Log.e("amap", "screenMarker is null");
        }
    }

    @Override
    public View getInfoWindow(Marker marker) {

        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View view = LayoutInflater.from(this).inflate(R.layout.custom_info_window, null);
        render(view, marker);
        return view;
    }

    public void render(View view, Marker marker) {
        ImageView imageView = (ImageView) view.findViewById(R.id.badge);
        imageView.setImageResource(R.mipmap.badge_wa);
        String title = marker.getTitle();
        TextView mTvTitle = view.findViewById(R.id.title);
        if (!TextUtils.isEmpty(title)) {
            SpannableString span = new SpannableString(title);
            span.setSpan(new ForegroundColorSpan(Color.BLUE), 0, title.length(), 0);
            mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP,15);
            mTvTitle.setText(span);
        }

        String snippet = marker.getSnippet();
        TextView mTvSnippet = view.findViewById(R.id.snippet);
        if (!TextUtils.isEmpty(snippet)) {
            SpannableString spanSnippet = new SpannableString(snippet);
            spanSnippet.setSpan(new ForegroundColorSpan(Color.RED), 0, snippet.length(), 0);
            mTvSnippet.setTextSize(TypedValue.COMPLEX_UNIT_SP,13);
            mTvSnippet.setText(spanSnippet);
        }
    }

    //dip和px转换
    private static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }
}
