package paci.estiam.activities.logged;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import paci.estiam.MainActivity;
import paci.estiam.R;

public class HomeActivity extends AppCompatActivity {

    private static final String TAG = "HomeActivity";
    private TextView utilisateurTextView;
    private Button viewUsersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            if (!extras.getString("username").isEmpty()) {
                String username = extras.getString("username");
                utilisateurTextView = findViewById(R.id.username);
                utilisateurTextView.setText("Bienvenue " + username);
                viewUsersButton = findViewById(R.id.viewUsers);
                viewUsersButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleViewUsersClick(username);
                    }
                });
            } else {
                displayErrorAndNavigateToMain();
            }
        } else {
            displayErrorAndNavigateToMain();
        }
    }

    private void handleViewUsersClick(String username) {
        Intent intent = new Intent(HomeActivity.this, UserActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void displayErrorAndNavigateToMain() {
        Toast.makeText(HomeActivity.this, "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
        Intent intentDisconnect = new Intent(HomeActivity.this, MainActivity.class);
        startActivity(intentDisconnect);
    }
}
