package org.telegram.ui.Heymate.widget;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.core.content.res.ResourcesCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Heymate.AmplifyModels.ExpandableItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import works.heymate.core.Texts;
import works.heymate.core.Utils;

public class LocationInputItem extends ExpandableItem {

    public static class LocationInfo {

        public final String address;
        public final double latitude;
        public final double longitude;

        public LocationInfo(String address, double latitude, double longitude) {
            this.address = address;
            this.latitude = latitude;
            this.longitude = longitude;
        }

    }

    private AppCompatAutoCompleteTextView mInputAddress;
    private MapView mMapView;

    private GoogleMap mMap;
    private LatLng mLocation = null;

    public LocationInputItem(@NonNull Context context) {
        super(context);
        setTitle(LocaleController.getString("HtLocation", R.string.HtLocation));
        setIcon(ResourcesCompat.getDrawable(getResources(), R.drawable.location_on_24_px_1, null));
    }

    public LocationInfo getLocationInfo() {
        if (mLocation == null) {
            return null;
        }

        String address = mInputAddress.getText().toString().trim();

        if (address.length() == 0) {
            return null;
        }

        return new LocationInfo(address, mLocation.latitude, mLocation.longitude);
    }

    public void setLocationInfo(LocationInfo locationInfo) {
        setLocationInfo(locationInfo.address, locationInfo.latitude, locationInfo.longitude);
    }

    public void setLocationInfo(String address, double latitude, double longitude) {
        mInputAddress.setText(address);
        mLocation = new LatLng(latitude, longitude);

        if (mMap != null) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
        }
    }

    @Override
    protected View createContent() {
        FrameLayout content = new FrameLayout(getContext());

        mInputAddress = new AppCompatAutoCompleteTextView(getContext());
        mInputAddress.setInputType(EditorInfo.TYPE_CLASS_TEXT);
        mInputAddress.setSingleLine(true);
        mInputAddress.setMaxLines(1);
        mInputAddress.setImeOptions(EditorInfo.IME_ACTION_DONE);
        mInputAddress.setHint(Texts.get(Texts.SEARCH_ADDRESS));
        mInputAddress.setHintTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteHintText));
        mInputAddress.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        mInputAddress.setThreshold(2);
        mInputAddress.setBackground(Theme.createEditTextDrawable(getContext(), false));
        content.addView(mInputAddress, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 92, 4, 20, 0));

        mInputAddress.setAdapter(new ArrayAdapter<WrappedAddress>(getContext(), R.layout.autocomplete_item) {

            private Filter mFilter = null;

            @NonNull
            @Override
            public Filter getFilter() {
                if (mFilter == null) {
                    final Geocoder geocoder = new Geocoder(ApplicationLoader.applicationContext, LocaleController.getInstance().getSystemDefaultLocale());

                    mFilter = new Filter() {

                        @Override
                        protected FilterResults performFiltering(CharSequence constraint) {
                            FilterResults results = new FilterResults();

                            List<Address> addresses = null;

                            if (mLocation != null) {
                                class Holder {
                                    LatLng leftBottom = null;
                                    LatLng rightTop = null;
                                }

                                final Holder holder = new Holder();

                                Utils.runOnUIThread(() -> {
                                    if (mMap != null) {
                                        holder.leftBottom = mMap.getProjection().fromScreenLocation(new Point(0, mMapView.getHeight()));
                                        holder.rightTop = mMap.getProjection().fromScreenLocation(new Point(mMapView.getWidth(), 0));
                                    }
                                    synchronized (holder) {
                                        holder.notifyAll();
                                    }
                                });

                                synchronized (holder) {
                                    try {
                                        holder.wait();
                                    } catch (InterruptedException e) { }
                                }

                                if (holder != null) {
                                    try {
                                        addresses = geocoder.getFromLocationName(constraint.toString(), 5, holder.leftBottom.latitude, holder.leftBottom.longitude, holder.rightTop.latitude, holder.rightTop.longitude);
                                    } catch (IOException e) { }
                                }
                            }
                            else {
                                try {
                                    addresses = geocoder.getFromLocationName(constraint.toString(), 5);
                                } catch (IOException e) { }
                            }

                            if (addresses != null) {
                                results.values = wrap(addresses);
                                results.count = addresses.size();
                            }

                            return results;
                        }

                        @Override
                        protected void publishResults(CharSequence constraint, FilterResults results) {
                            clear();

                            if (results.values instanceof List) {
                                addAll(((List<WrappedAddress>) results.values));
                            }
                        }

                    };
                }

                return mFilter;
            }

        });
        mInputAddress.setOnItemClickListener((parent, view, position, id) -> {
            Address address = ((WrappedAddress) mInputAddress.getAdapter().getItem(position)).address;

            if (mMap != null) {
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 14));
            }

            InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mInputAddress.getWindowToken(), 0);

            mLocation = new LatLng(address.getLatitude(), address.getLongitude());
        });

        mMapView = new MapView(getContext()) {

            @Override
            public boolean dispatchTouchEvent(MotionEvent ev) {
                boolean result = super.dispatchTouchEvent(ev);

                getParent().requestDisallowInterceptTouchEvent(result);

                return result;
            }

        };
        content.addView(mMapView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 160, Gravity.TOP, 20, 48, 20, 18));

        ImageView imagePin = new ImageView(getContext());
        imagePin.setImageResource(R.drawable.map_pin);
        content.addView(imagePin, LayoutHelper.createFrame(26, 42, Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM, 0, 0, 0, 98));

        return content;
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mMapView.onCreate(null);

        mMapView.getMapAsync(googleMap -> {
            mMap = googleMap;

            try {
                googleMap.setMyLocationEnabled(true);
            } catch (Exception e) { }
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (mLocation == null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (getContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        Location location = getLastLocation();
                        if (location != null) {
                            mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        }
                    }
                }
                else {
                    Location location = getLastLocation();
                    if (location != null) {
                        mLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    }
                }
            }

            if (mLocation != null) {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mLocation, 13));
            }
            else {
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(0, 0), 1));
            }

            googleMap.setOnCameraIdleListener(() -> mLocation = mMap.getCameraPosition().target);
        });
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            mMapView.onStart();
            mMapView.onResume();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mMapView.onPause();
        mMapView.onStop();
        mMapView.onDestroy();
    }

    @SuppressLint("MissingPermission")
    private Location getLastLocation() {
        LocationManager lm = (LocationManager) ApplicationLoader.applicationContext.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = lm.getProviders(true);
        Location l = null;
        for (int i = providers.size() - 1; i >= 0; i--) {
            l = lm.getLastKnownLocation(providers.get(i));
            if (l != null) {
                break;
            }
        }
        return l;
    }

    private static List<WrappedAddress> wrap(List<Address> addresses) {
        List<WrappedAddress> wrapped = new ArrayList<>(addresses.size());

        for (Address address: addresses) {
            wrapped.add(new WrappedAddress(address));
        }

        return wrapped;
    }

    private static class WrappedAddress {

        public final Address address;

        public WrappedAddress(Address address) {
            this.address = address;
        }

        @Override
        public String toString() {
            return address.getAddressLine(0);
        }
    }

}