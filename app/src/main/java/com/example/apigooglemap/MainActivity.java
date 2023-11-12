package com.example.apigooglemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private EditText editTextStart, editTextDestination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextStart = findViewById(R.id.editTextStart);
        editTextDestination = findViewById(R.id.editTextDestination);

        // Demande la permission et obtient la localisation de l'utilisateur.
        checkLocationPermission();
    }

    // Vérifie si l'application a la permission de localisation, sinon demande la permission.
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Demande la permission d'accéder à la localisation.
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // La permission est déjà accordée.
            // Vous pouvez appeler la fonction pour obtenir la localisation ici.
            getUserLocation();
        }
    }

    // Gère la réponse de l'utilisateur à la demande de permission.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission accordée.
                // Vous pouvez appeler la fonction pour obtenir la localisation ici.
                getUserLocation();
            } else {
                // Permission refusée.
                Toast.makeText(this, "Permission de localisation refusée", Toast.LENGTH_SHORT).show();
            }
        }

        // Call the superclass implementation
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Utilisez LocationManager ou Google Play Services pour obtenir la localisation de l'utilisateur.
    // Assurez-vous d'ajouter le code nécessaire pour gérer les mises à jour de la localisation en continu si nécessaire.
    // Pour cet exemple, j'utiliserai une localisation statique pour simplifier.
    private void getUserLocation() {
        // Exemple de localisation statique (latitude et longitude de Paris, France).
        String userLocation = "48.8566, 2.3522";

        // Utiliser reverse geocoding pour obtenir l'adresse à partir des coordonnées.
        String address = getAddressFromCoordinates(userLocation);

        // Mettez à jour le champ de texte de départ avec l'adresse de l'utilisateur.
        editTextStart.setText(address);
    }

    private String getAddressFromCoordinates(String coordinates) {
        // Split the coordinates into latitude and longitude.
        String[] latLng = coordinates.split(", ");
        double latitude = Double.parseDouble(latLng[0]);
        double longitude = Double.parseDouble(latLng[1]);

        try {
            // Utiliser l'API de géocodage inverse pour obtenir l'adresse.
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                // Construire l'adresse à afficher.
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

    public void onSubmitClick(View view) {
        String startPoint = editTextStart.getText().toString();
        String destination = editTextDestination.getText().toString();

        // Utilisez l'API Directions de Google Maps pour obtenir des informations sur le trajet.
        GeoApiContext geoApiContext = new GeoApiContext.Builder()
                .apiKey("AIzaSyAz2goHIOGij1pt2zSQufKc3rQssBRMwIw")
                .build();

        try {
            DirectionsResult result = DirectionsApi.newRequest(geoApiContext)
                    .origin(startPoint)
                    .destination(destination)
                    .mode(TravelMode.DRIVING)
                    .await();

            // Récupérez la durée estimée du trajet en secondes.
            long durationInSeconds = result.routes[0].legs[0].duration.inSeconds;

            // Convertissez la durée en minutes (au lieu d'heures).
            double durationInMinutes = durationInSeconds / 60.0;

            // Modifiez la tarification en conséquence (par exemple, 0.5 euros par minute).
            double driverPaymentRatePerMinute = 0.5;
            double totalCost = durationInMinutes * driverPaymentRatePerMinute;

            // Récupérez la distance estimée du trajet en kilomètres.
            double distanceInKm = result.routes[0].legs[0].distance.inMeters / 1000.0;

            // Calcul de la quantité estimée de carburant utilisée (hypothétique, à adapter à vos besoins).
            double fuelConsumptionRate = 0.12; // exemple : 0.12 litre par kilomètre
            double estimatedFuelUsage = distanceInKm * fuelConsumptionRate;

            // Affichez les résultats avec une justification détaillée.
            String justification = String.format("Durée estimée : %.2f minutes\n" +
                            "Distance estimée : %.2f km\n" +
                            "Quantité estimée de carburant : %.2f litres\n" +
                            "Coût du trajet : %.2f euros (incluant 30 euros de rémunération pour le chauffeur)",
                    durationInMinutes, distanceInKm, estimatedFuelUsage, totalCost);

            Toast.makeText(this, justification, Toast.LENGTH_LONG).show();

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