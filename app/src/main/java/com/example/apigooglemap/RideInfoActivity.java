package com.example.apigooglemap;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;  // Importez cette ligne
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;  // Importez cette ligne
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RideInfoActivity extends AppCompatActivity {

    private ImageView imageViewCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_info);

        // Récupérez les données passées depuis MainActivity
        Intent intent = getIntent();
        String justification = intent.getStringExtra("justification");

        // Affichez les informations dans le TextView approprié
        TextView textViewJustification = findViewById(R.id.textViewJustification);
        textViewJustification.setText(justification);

        // Référencez l'ImageView
        imageViewCar = findViewById(R.id.imageViewCar);

        // Ajoutez des actions aux boutons Accepter et Refuser
        Button buttonAccept = findViewById(R.id.buttonAccept);
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code à exécuter lorsque le bouton Accepter est cliqué
                showCongratulationsMessage();
                startCarAnimation();
            }
        });

        Button buttonDecline = findViewById(R.id.buttonDecline);
        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Code à exécuter lorsque le bouton Refuser est cliqué
                navigateToHome();
            }
        });
    }

    private void showCongratulationsMessage() {
        // Affichez un message de félicitations à l'utilisateur
        Toast.makeText(this, "Félicitations ! Votre chauffeur est en route.", Toast.LENGTH_SHORT).show();
    }

    private void startCarAnimation() {
        // Démarrez l'animation de la voiture
        imageViewCar.setBackgroundResource(R.drawable.ic_car_animation);
        AnimationDrawable animationDrawable = (AnimationDrawable) imageViewCar.getBackground();
        animationDrawable.start();

        // Ouvrez la nouvelle activité
        openDriverArrivingActivity();
    }

    private void openDriverArrivingActivity() {
        Intent intent = new Intent(this, DriverArrivingActivity.class);
        startActivity(intent);
    }

    private void navigateToHome() {
        // Retournez à l'activité d'accueil (MainActivity)
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}
