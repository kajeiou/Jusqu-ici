package paci.main.activities.logged;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import paci.main.LoginActivity;
import paci.main.R;

public class ProfileActivity extends AppCompatActivity {

    private ImageView profileImageView;
    private TextView usernameTextView;
    private TextView emailTextView;
    private Button changePhotoButton;
    private Button logoutButton;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialiser les vues
        profileImageView = findViewById(R.id.profileImageView);
        usernameTextView = findViewById(R.id.username);
        emailTextView = findViewById(R.id.email);
        changePhotoButton = findViewById(R.id.changePhotoButton);
        logoutButton = findViewById(R.id.logoutButton);
        bottomNavigationView = findViewById(R.id.bottom_navigation);

        // Charger les informations de l'utilisateur depuis Firebase
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Nom d'utilisateur
            String username = user.getDisplayName();
            if (username != null && !username.isEmpty()) {
                usernameTextView.setText(username);
            } else {
                // Si le nom d'utilisateur n'est pas défini, afficher l'adresse e-mail
                usernameTextView.setText(user.getEmail());
            }

            // Adresse e-mail
            String email = user.getEmail();
            if (email != null && !email.isEmpty()) {
                emailTextView.setText(email);
            }

            // TODO: Charger la photo de profil à partir de Firebase Storage ou autre source
            // Utilisez Picasso, Glide, ou une autre bibliothèque pour le chargement d'image asynchrone
        }

        // Gérer le clic sur le bouton de changement de photo
        changePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Implémenter la logique pour changer la photo de profil
                // Vous pouvez ouvrir la galerie ou la caméra et mettre à jour l'ImageView
            }
        });

        // Gérer le clic sur le bouton de déconnexion
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Se déconnecter de Firebase Authentication
                FirebaseAuth.getInstance().signOut();

                // Rediriger vers l'écran de connexion par exemple
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                startActivity(intent);
                finish(); // Fermer l'activité actuelle
            }
        });

        // Gérer la navigation en bas
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_item1:
                        Intent intentHome = new Intent(ProfileActivity.this, HomeActivity.class);
                        startActivity(intentHome);
                        finish(); // Fermer l'activité actuelle
                        return true;
                    case R.id.menu_item2:
                        // Fragment ou activité pour le menu_item2
                        return true;
                    // Ajoutez des cas pour les autres éléments du menu
                    default:
                        return false;
                }
            }
        });
    }
}
