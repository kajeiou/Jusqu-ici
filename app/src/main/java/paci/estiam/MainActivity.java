package paci.estiam;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import paci.estiam.activities.logged.HomeActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText utilisateurEditText;
    private EditText motDePasseEditText;
    private Button loginButton;
    private CheckBox rester_connecteBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        utilisateurEditText = findViewById(R.id.utilisateur);
        motDePasseEditText = findViewById(R.id.motdepasse);
        rester_connecteBox = findViewById(R.id.rester_connecte);
        loginButton = findViewById(R.id.login);
        utilisateurEditText.setText("utilisateur_test");
        motDePasseEditText.setText("toto123");

        utilisateurEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.contains(" ")) {
                    text = text.replace(" ", "");
                    utilisateurEditText.setText(text);
                    utilisateurEditText.setSelection(text.length());
                    Toast.makeText(MainActivity.this, "Ne pas mettre d'espace dans le nom d'utilisateur", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoginButtonClick();
            }
        });
    }

    private void handleLoginButtonClick() {
        String username = utilisateurEditText.getText().toString();
        String password = motDePasseEditText.getText().toString();
        boolean resterConnecte = rester_connecteBox.isChecked();

        if (!username.isEmpty() && !password.isEmpty()) {
            Log.d(TAG, "Tentative de connexion : login : " + username + " password : " + password);
            if (authenticate(username, password, resterConnecte)) {
                Toast.makeText(MainActivity.this, "Authentification réussie", Toast.LENGTH_SHORT).show();
                navigateToHome(username);
            } else {
                Toast.makeText(MainActivity.this, "Authentification échouée", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(MainActivity.this, "Veuillez renseigner tous les champs du formulaire.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean authenticate(String username, String password, boolean resterConnecte) {
        if (!username.isEmpty() && !password.isEmpty()) {
            Log.d(TAG, "Tentative de connexion : login : " + username + " password : " + password + "resterConnecte : " + resterConnecte);
            return true;
        }
        return false;
    }

    private void navigateToHome(String username) {
        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }
}
