package paci.main.activities.logged;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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

import paci.main.LoginActivity;
import paci.main.R;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private TextView utilisateurTextView;
    private EditText editTextStart, editTextDestination;
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
                    editTextDestination = findViewById(R.id.editTextDestination);

                } else {
                    Toast.makeText(HomeActivity.this, "User non trouvé", Toast.LENGTH_SHORT).show();
                    displayErrorAndNavigateToMain();
                }
            } else {
                displayErrorAndNavigateToMain();
            }
        }


    }
    private void displayErrorAndNavigateToMain() {
        Toast.makeText(HomeActivity.this, "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
        Intent intentDisconnect = new Intent(HomeActivity.this, LoginActivity.class);
        startActivity(intentDisconnect);
    }

    public void onSubmitClick(View view) {
        String startPoint = editTextStart.getText().toString();
        String destination = editTextDestination.getText().toString();

        // Utilisez l'API Directions de Google Maps pour obtenir des informations sur le trajet.
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

            // Calcul du coût total du trajet (prix du trajet + rémunération du chauffeur).
            double totalCost = calculateTotalCost(durationInHours);

            // Récupérez la distance estimée du trajet en kilomètres.
            double distanceInKm = result.routes[0].legs[0].distance.inMeters / 1000.0;

            // Calcul de la quantité estimée de carburant utilisée (hypothétique, à adapter à vos besoins).
            double fuelConsumptionRate = 0.12; // exemple : 0.12 litre par kilomètre
            double estimatedFuelUsage = distanceInKm * fuelConsumptionRate;

            // Affichez les résultats avec une justification détaillée.
            String justification = String.format("Durée estimée : %.2f heures\n" +
                            "Distance estimée : %.2f km\n" +
                            "Quantité estimée de carburant : %.2f litres\n" +
                            "Coût du trajet : %.2f euros (incluant 30 euros de rémunération pour le chauffeur)",
                    durationInHours, distanceInKm, estimatedFuelUsage, totalCost);

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
