package paci.main.activities.logged;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.maps.GeoApiContext;
import com.google.maps.DirectionsApi;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import paci.main.LoginActivity;
import paci.main.R;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private EditText editTextStart, editTextDestination;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.menu_item1:
                            return true;
                        case R.id.menu_item2:
                            Intent intent = new Intent(HomeActivity.this, ProfileActivity.class);
                            startActivity(intent);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            editTextStart = findViewById(R.id.editTextStart);
            Button buttonMyLocation = findViewById(R.id.buttonMyLocation);
            buttonMyLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkLocationPermission();
                }
            });

            editTextDestination = findViewById(R.id.editTextDestination);

            Button submitButton = findViewById(R.id.buttonSubmit);
            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onSubmitClick(v);
                }
            });

        } else {
            displayErrorAndNavigateToMain();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getUserLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getUserLocation() {
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String userLocation = latitude + ", " + longitude;

                        String address = getAddressFromCoordinates(userLocation);

                        EditText editTextStart = findViewById(R.id.editTextStart);
                        editTextStart.setText(address);

                        Toast.makeText(getApplicationContext(), "Nouvelle adresse : " + address, Toast.LENGTH_SHORT).show();

                        locationManager.removeUpdates(this);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
                progressBar.setVisibility(View.INVISIBLE);
            }
        } else {
            Toast.makeText(this, "Le gestionnaire de localisation n'est pas disponible", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private String getAddressFromCoordinates(String coordinates) {
        String[] latLng = coordinates.split(", ");
        double latitude = Double.parseDouble(latLng[0]);
        double longitude = Double.parseDouble(latLng[1]);

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                StringBuilder addressStringBuilder = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    addressStringBuilder.append(address.getAddressLine(i)).append("\n");
                }

                return addressStringBuilder.toString().trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "Adresse introuvable";
    }

    private void displayErrorAndNavigateToMain() {
        Toast.makeText(HomeActivity.this, "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
        Intent intentDisconnect = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intentDisconnect);
    }

    public void onSubmitClick(View view) {
        String startPoint = editTextStart.getText().toString().trim();
        String destination = editTextDestination.getText().toString().trim();

        if (!startPoint.isEmpty() && !destination.isEmpty()) {
            showCarTypeDialog(startPoint, destination);
        } else {
            showAlertDialog("Veuillez remplir le point de départ et d'arrivée.");
        }
    }

    private void showAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle("Attention")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showCarTypeDialog(final String startPoint, final String destination) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sélectionnez le type de voiture");

        final CharSequence[] carTypes = {"Voiture classique", "Voiture Van", "Voiture de luxe"};

        builder.setItems(carTypes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedCarType = carTypes[which].toString();

                // Récupérez la durée estimée du trajet en minutes.
                double durationInMinutes = getDurationInMinutes(startPoint, destination);

                // Mettez à jour le coût total en fonction du type de voiture sélectionné.
                double totalCost = calculateTotalCost(durationInMinutes, selectedCarType);

                Intent intent = new Intent(HomeActivity.this, RideInfoActivity.class);

                intent.putExtra("justification", getJustification(durationInMinutes, startPoint, destination, selectedCarType, totalCost));
                intent.putExtra("totalCost", totalCost);
                intent.putExtra("carType", selectedCarType);
                intent.putExtra("startPoint", startPoint);
                intent.putExtra("destination", destination);

                startActivity(intent);
            }
        });

        builder.show();
    }

    private GeoApiContext createGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_client_id))
                .build();
    }
    private double getDurationInMinutes(String startPoint, String destination) {
        GeoApiContext geoApiContext = createGeoApiContext();

        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(startPoint)
                    .destination(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            // Récupérez la durée estimée du trajet en secondes.
            long durationInSeconds = result.routes[0].legs[0].duration.inSeconds;

            // Convertissez la durée en minutes.
            return durationInSeconds / 60.0;

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du calcul de la durée du trajet", Toast.LENGTH_SHORT).show();
            return 0.0;
        }
    }

    private double calculateTotalCost(double durationInMinutes, String carType) {
        double driverPaymentRatePerMinute = 0.5;
        double additionalCost = 0.0;

        if (carType.equals("Voiture Van")) {
            additionalCost = 7.0;
        } else if (carType.equals("Voiture de luxe")) {
            additionalCost = 15.0;
        }

        additionalCost += (durationInMinutes / 60.0) * 30.0;

        double driverPayment = durationInMinutes * driverPaymentRatePerMinute;
        double totalCost = driverPayment + additionalCost;

        return totalCost;
    }

    private String getJustification(double durationInMinutes, String startPoint, String destination, String carType, double totalCost) {

        GeoApiContext geoApiContext = createGeoApiContext();

        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(startPoint)
                    .destination(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            double distanceInKm = result.routes[0].legs[0].distance.inMeters / 1000.0;

            // Calcul de la quantité estimée de carburant utilisée (hypothétique, à adapter à vos besoins).
            double fuelConsumptionRate = 0.12; // exemple : 0.12 litre par kilomètre
            double estimatedFuelUsage = distanceInKm * fuelConsumptionRate;

            // Calcul de la rémunération totale du chauffeur.
            double driverPayment = (durationInMinutes / 60.0) * 30.0;

            // Calcul du supplément catégorie.
            double categorySurcharge = 0.0;
            if (!carType.equals("Voiture classique")) {
                if (carType.equals("Voiture Van")) {
                    categorySurcharge = 7.0;
                } else if (carType.equals("Voiture de luxe")) {
                    categorySurcharge = 15.0;
                }
            }

            return String.format("Durée estimée : %.2f minutes\n" +
                            "Distance estimée : %.2f km\n" +
                            "Quantité estimée de carburant : %.2f litres\n" +
                            "Rémunération totale du chauffeur : %.2f euros\n" +
                            "Supplément catégorie : %.2f euros\n" +
                            "Prix de la course : %.2f euros",
                    durationInMinutes, distanceInKm, estimatedFuelUsage, driverPayment, categorySurcharge, totalCost);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du calcul de la justification", Toast.LENGTH_SHORT).show();
            return "Erreur de justification";
        }
    }
}
