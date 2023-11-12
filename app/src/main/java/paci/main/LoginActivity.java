package paci.main;

import androidx.annotation.NonNull;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import paci.main.activities.logged.HomeActivity;

public class LoginActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    private EditText utilisateurEditText;
    private EditText motDePasseEditText;
    private Button loginButton;
    private CheckBox rester_connecteBox;

    private GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        utilisateurEditText = findViewById(R.id.utilisateur);
        motDePasseEditText = findViewById(R.id.motdepasse);
        rester_connecteBox = findViewById(R.id.rester_connecte);
        loginButton = findViewById(R.id.login);
        utilisateurEditText.setText("utilisateur_test@gmail.com");
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
                    Toast.makeText(LoginActivity.this, "Ne pas mettre d'espace dans le nom d'utilisateur", Toast.LENGTH_SHORT).show();
                }
            }
        });
        utilisateurEditText.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {}
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String text = s.toString();
                if (text.contains(" ")) {
                    text = text.replace(" ", "");
                    utilisateurEditText.setText(text);
                    utilisateurEditText.setSelection(text.length());
                    Toast.makeText(LoginActivity.this, "Ne pas mettre d'espace dans le nom d'utilisateur", Toast.LENGTH_SHORT).show();
                }
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLoginButtonClick();
            }
        });

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
            .enableAutoManage(this, this)
            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
            .build();

        mAuth = FirebaseAuth.getInstance();

        SignInButton signInButton = findViewById(R.id.sign_in_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);
        signInButton.setOnClickListener(view -> signIn());

    }
    private void handleLoginButtonClick() {
        String username = utilisateurEditText.getText().toString();
        String password = motDePasseEditText.getText().toString();
        boolean resterConnecte = rester_connecteBox.isChecked();

        if (!username.isEmpty() && !password.isEmpty()) {
            Log.d(TAG, "Tentative de connexion : login : " + username + " password : " + password);
            authenticate(username, password, resterConnecte);
        } else {
            Toast.makeText(LoginActivity.this, "Veuillez renseigner tous les champs du formulaire.", Toast.LENGTH_SHORT).show();
        }
    }

    private void authenticate(String username, String password, boolean resterConnecte) {
        if (!username.isEmpty() && !password.isEmpty()) {
            mAuth.signInWithEmailAndPassword(username, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            // Authentification réussie
                            FirebaseUser user = mAuth.getCurrentUser();
                            navigateToHome(user.getEmail());
                        } else {
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(LoginActivity.this, "Veuillez renseigner tous les champs du formulaire.", Toast.LENGTH_SHORT).show();
        }
    }


    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.w(TAG, "result : " + result.getStatus());
            handleSignInResult(result);
        }
    }
    private void handleSignInResult(GoogleSignInResult result) {

        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            firebaseAuthWithGoogle(account);
        } else {
            Log.w(TAG, "signInResult:failed code=" + result.getStatus().getStatusCode());
            Toast.makeText(this, "signInResult:failed code=" + result.getStatus().getStatusCode(), Toast.LENGTH_SHORT).show();

        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        Toast.makeText(this, "Connexion réussis", Toast.LENGTH_SHORT).show();

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d(TAG, "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    navigateToHome(user.getUid());
                } else {
                    Log.w(TAG, "signInWithCredential:failure", task.getException());
                    Toast.makeText(this, "Authentication échoué.", Toast.LENGTH_SHORT).show();
                }
            });
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Gestion des erreurs de connexion GoogleApiClient
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }
    private void navigateToHome(String user) {
        if (user == null) {
            Toast.makeText(LoginActivity.this, "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
        } else {
            String userId = user;
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);

        }

    }
}
