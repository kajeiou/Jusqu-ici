package paci.main.activities.logged;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import paci.main.LoginActivity;
import paci.main.R;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private TextView utilisateurTextView;
    private EditText editTextStart, editTextDestination;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Intent intent = getIntent();
        if (intent != null) {
            String userId = intent.getStringExtra("userId");

            if (userId != null) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                if (user != null) {
                    Toast.makeText(HomeActivity.this, "User trouvé", Toast.LENGTH_SHORT).show();
                    String username = user.getDisplayName();
                    if (username != null && !username.isEmpty()) {
                        utilisateurTextView = findViewById(R.id.username);
                        utilisateurTextView.setText("Bienvenue " + username);
                    }
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
                    Toast.makeText(HomeActivity.this, "User non trouvé", Toast.LENGTH_SHORT).show();
                    displayErrorAndNavigateToMain();
                }
            } else {
                displayErrorAndNavigateToMain();
            }
        }


    }

    private void checkLocationPermission() {
        Toast.makeText(this, "Bouton cliqué", Toast.LENGTH_SHORT).show();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Demande de permission de localisation", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Permission de localisation déjà accordée", Toast.LENGTH_SHORT).show();
            getUserLocation();
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission de localisation accordée", Toast.LENGTH_SHORT).show();
                getUserLocation();
            } else {
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void getUserLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (locationManager != null) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        // Une fois que la localisation change, utilisez la nouvelle localisation
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String userLocation = latitude + ", " + longitude;

                        String address = getAddressFromCoordinates(userLocation);

                        EditText editTextStart = findViewById(R.id.editTextStart);
                        editTextStart.setText(address);

                        Toast.makeText(getApplicationContext(), "Nouvelle adresse : " + address, Toast.LENGTH_SHORT).show();

                        locationManager.removeUpdates(this);
                    }

                });
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        } else {
            Toast.makeText(this, "Le gestionnaire de localisation n'est pas disponible", Toast.LENGTH_SHORT).show();
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
        String startPoint = editTextStart.getText().toString();
        String destination = editTextDestination.getText().toString();

        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey(getString(R.string.google_client_id))
                .build();

        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(startPoint)
                    .destination(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            // Récupérez la durée estimée du trajet en secondes.
            long durationInSeconds = result.routes[0].legs[0].duration.inSeconds;

            // Convertissez la durée en heures.
            double durationInHours = durationInSeconds / 3600.0;

            // Convertir la durée en heures en minutes sans changer la fonction calculateTotalCost
            double durationInMinutes = durationInHours * 60;

            // Calcul du coût total du trajet (prix du trajet + rémunération du chauffeur).
            double totalCost = calculateTotalCost(durationInHours);

            // Récupérez la distance estimée du trajet en kilomètres.
            double distanceInKm = result.routes[0].legs[0].distance.inMeters / 1000.0;

            // Calcul de la quantité estimée de carburant utilisée (hypothétique, à adapter à vos besoins).
            double fuelConsumptionRate = 0.12; // exemple : 0.12 litre par kilomètre
            double estimatedFuelUsage = distanceInKm * fuelConsumptionRate;

            // Affichez les résultats avec une justification détaillée.
            // Supposons que vous avez des TextView correspondant à chaque donnée dans votre mise en page XML avec des IDs appropriés.
            TextView durationTextView = findViewById(R.id.durationTextView);
            TextView distanceTextView = findViewById(R.id.distanceTextView);
            TextView fuelUsageTextView = findViewById(R.id.fuelUsageTextView);
            TextView costTextView = findViewById(R.id.costTextView);

            // Utilisez les valeurs calculées pour mettre à jour les TextView correspondants.
                        String durationText = String.format("Durée estimée : %.2f minutes", durationInMinutes);
                        String distanceText = String.format("Distance estimée : %.2f km", distanceInKm);
                        String fuelUsageText = String.format("Quantité estimée de carburant : %.2f litres", estimatedFuelUsage);
                        String costText = String.format("Coût du trajet : %.2f euros (incluant 30 euros de rémunération pour le chauffeur)", totalCost);

            // Mettez à jour les TextView avec les valeurs calculées.
                        durationTextView.setText(durationText);
                        distanceTextView.setText(distanceText);
                        fuelUsageTextView.setText(fuelUsageText);
                        costTextView.setText(costText);


        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur lors du calcul du trajet", Toast.LENGTH_SHORT).show();
        }
    }

    private double calculateTotalCost(double durationInHours) {
        // Rémunération du chauffeur (30 euros par heure).
        double driverPaymentRate = 30.0;
        double totalCost = durationInHours * driverPaymentRate;

        // Vous pouvez également ajouter d'autres coûts tels que le coût du trajet lui-même.

        return totalCost;
    }
}
