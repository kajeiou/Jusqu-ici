package paci.estiam;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        EditText prenomEditText = findViewById(R.id.prenom);
        EditText nomEditText = findViewById(R.id.nom);
        EditText emailEditText = findViewById(R.id.email);
        EditText naissanceEditText = findViewById(R.id.naissance);
        EditText descriptionEditText = findViewById(R.id.description);
        EditText commentaireEditText = findViewById(R.id.commentaire);
        CheckBox acceptationCheckBox = findViewById(R.id.acceptation_conditions);
        Button annulerButton = findViewById(R.id.bouton_annuler);
        Button okButton = findViewById(R.id.bouton_ok);

    }

}