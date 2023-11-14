package paci.main.activities.logged;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import paci.main.R;

public class RideInfoActivity extends AppCompatActivity {

    private ImageView imageViewCar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride_info);

        Intent intent = getIntent();
        String justification = intent.getStringExtra("justification");

        String carType = intent.getStringExtra("carType");

        TextView textViewJustification = findViewById(R.id.textViewJustification);
        textViewJustification.setText(justification);

        imageViewCar = findViewById(R.id.imageViewCar);

        if (carType != null) {
            switch (carType) {
                case "Voiture classique":
                    imageViewCar.setImageResource(R.drawable.car_frame_1);
                    break;
                case "Voiture Van":
                    imageViewCar.setImageResource(R.drawable.car_frame_2);
                    break;
                case "Voiture de luxe":
                    imageViewCar.setImageResource(R.drawable.car_frame_3);
                    break;
                default:
                    imageViewCar.setImageResource(R.drawable.car_frame_1);
                    break;
            }
        }

        Button buttonAccept = findViewById(R.id.buttonAccept);
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCongratulationsMessage();
                openDriverArrivingActivity();
            }
        });

        Button buttonDecline = findViewById(R.id.buttonDecline);
        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                navigateToHome();
            }
        });
    }

    private void showCongratulationsMessage() {
        Toast.makeText(this, "Félicitations ! Votre chauffeur est en route.", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Vous avez reçu une nouvelle notification ! ", Toast.LENGTH_SHORT).show();
    }

    private void openDriverArrivingActivity() {
        Intent intent = getIntent();
        if (intent != null) {
            String startPoint = intent.getStringExtra("startPoint");
            String destination = intent.getStringExtra("destination");

            if (startPoint != null && destination != null) {
                Intent newIntent = new Intent(this, DriverArrivingActivity.class);
                newIntent.putExtra("startPoint", startPoint);
                newIntent.putExtra("destination", destination);

                startActivity(newIntent);
            }
        }
    }


    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}