package paci.main.activities.logged;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.Button;

import android.widget.Toast;
import paci.main.R;
import pl.droidsonroids.gif.GifImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.DirectionsApi;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsLeg;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DriverArrivingActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String CHANNEL_ID = "DriverArrivalChannel";
    private static final String TAG = "DriverArrivingActivity";

    private String startPoint;
    private String destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_arriving);

        Intent intent = getIntent();
        if (intent != null) {
            startPoint = intent.getStringExtra("startPoint");
            destination = intent.getStringExtra("destination");

            if (startPoint != null && destination != null) {
                String message = String.format("Trajet de %s à %s", startPoint, destination);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            } else {
                redirectToHomeActivity();
            }
        } else {
            redirectToHomeActivity();
        }
        GifImageView imageViewCar = findViewById(R.id.imageViewCar);
        Glide.with(this).load(R.drawable.car_gif).into(imageViewCar);



        try {
            showDriverArrivalNotification();

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            } else {
                Toast.makeText(this, "Erreur lors de l'initialisation de la carte Google Maps", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors de la création de l'activité : " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        Button buttonClose = findViewById(R.id.buttonClose);

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                redirectToHomeActivity();
            }
        });
    }

    private void redirectToHomeActivity() {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
        finish();
    }

    private void showDriverArrivalNotification() {
        try {
            Random random = new Random();

            String[] driverNames = {"Nabil Kajeiou", "Tharsshan Pathmanathan", "Mody Kane", "Salaheddine Bensalah", "Jaouad Assabour", "Yanhua Irène GU"};

            int randomDriverIndex = random.nextInt(driverNames.length);

            String randomDriver = driverNames[randomDriverIndex];

            int randomDelayMinutes = random.nextInt(16);

            String notificationText = String.format("%s arrive dans %d minutes", randomDriver, randomDelayMinutes);

            Intent intent = new Intent(this, DriverArrivingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification_icon)
                    .setContentTitle("Chauffeur en route")
                    .setContentText(notificationText)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                    .setAutoCancel(true);

            createNotificationChannel();

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.notify(0, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur lors de l'affichage de la notification", e);
        }
    }

    private void createNotificationChannel() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                CharSequence name = "DriverArrivalChannel";
                String description = "Channel for driver arrival notifications";
                int importance = NotificationManager.IMPORTANCE_DEFAULT;
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                channel.setDescription(description);

                NotificationManager notificationManager = getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(channel);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur lors de la création du canal de notification", e);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        try {
            if (startPoint != null && destination != null) {
                LatLng pointA = getLocationFromAddress(startPoint);
                LatLng pointB = getLocationFromAddress(destination);

                if (pointA != null && pointB != null) {
                    googleMap.addMarker(new MarkerOptions().position(pointA).title("Départ"));
                    googleMap.addMarker(new MarkerOptions().position(pointB).title("Arrivée"));

                    calculateDirections(googleMap, pointA, pointB);
                } else {
                    Log.e(TAG, "Erreur lors de la conversion des adresses en coordonnées LatLng");
                }
            } else {
                Log.e(TAG, "startPoint ou destination est null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur lors de l'affichage de la carte Google Maps", e);
        }
    }

    private GeoApiContext createGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_client_id))
                .build();
    }

    private void calculateDirections(GoogleMap googleMap, LatLng startPoint, LatLng destination) {
        GeoApiContext context = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_client_id))
                .build();

        DirectionsApiRequest request = DirectionsApi.getDirections(context,
                startPoint.latitude + "," + startPoint.longitude,
                destination.latitude + "," + destination.longitude);

        try {
            DirectionsResult result = request.await();

            if (result != null && result.routes != null && result.routes.length > 0) {
                DirectionsRoute route = result.routes[0];
                DirectionsLeg leg = route.legs[0];

                List<LatLng> points = decodePolyline(route.overviewPolyline.getEncodedPath());
                PolylineOptions lineOptions = new PolylineOptions()
                        .addAll(points)
                        .width(5)
                        .color(ContextCompat.getColor(this, R.color.colorAccent));
                googleMap.addPolyline(lineOptions);

                LatLng carMarkerPosition = new LatLng(startPoint.latitude + 0.010, startPoint.longitude + 0.010);
                MarkerOptions carMarkerOptions = new MarkerOptions()
                        .position(carMarkerPosition)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_car));
                googleMap.addMarker(carMarkerOptions);

                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(startPoint);
                builder.include(destination);
                LatLngBounds bounds = builder.build();
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, 100);
                googleMap.moveCamera(cameraUpdate);

            } else {
                Log.e(TAG, "Aucun itinéraire trouvé");
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur de calcul de l'itineraire", e);
        }


    }


    private List<LatLng> decodePolyline(String encodedPolyline) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encodedPolyline.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encodedPolyline.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng(((double) lat / 1E5), ((double) lng / 1E5));
            poly.add(p);
        }

        return poly;
    }


    private LatLng getLocationFromAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses;
        LatLng location = null;

        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && addresses.size() > 0) {
                double latitude = addresses.get(0).getLatitude();
                double longitude = addresses.get(0).getLongitude();
                location = new LatLng(latitude, longitude);
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Erreur lors de la conversion de l'adresse en coordonnées LatLng", e);
        }

        return location;
    }
}
