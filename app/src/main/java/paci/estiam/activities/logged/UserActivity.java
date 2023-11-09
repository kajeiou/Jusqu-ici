package paci.estiam.activities.logged;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

import paci.estiam.MainActivity;
import paci.estiam.R;
import paci.estiam.adapters.UserAdapter;
import paci.estiam.classes.User;
import paci.estiam.services.UserService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UserActivity extends AppCompatActivity {
    private static final String TAG = "UserActivity";
    private Button backButton;

    private ListView userListView;
    private List<User> users;

    private static final int SEARCH_DELAY_MS = 500; // Délai en millisecondes
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        initializeUI();

    }
    private void initializeUI() {
        Bundle extras = getIntent().getExtras();
        if (extras == null || extras.getString("username").isEmpty()) {
            displayErrorAndNavigateToMain();
        } else {
            backButton = findViewById(R.id.back);
            userListView = findViewById(R.id.userListView);
            SearchView searchView = findViewById(R.id.searchView);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.github.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            UserService userService = retrofit.create(UserService.class);

            // Utilisez Retrofit pour récupérer la liste initiale d'utilisateurs
            Call<List<User>> call = userService.getUsers();

            call.enqueue(new Callback<List<User>>() {
                @Override
                public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        users = response.body();
                        displayUserList();
                    } else {
                        Toast.makeText(UserActivity.this, "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<List<User>> call, Throwable t) {
                    String errorMessage = t.getMessage();
                    if (errorMessage == null || errorMessage.isEmpty()) {
                        errorMessage = "Erreur inconnue";
                    }
                    Toast.makeText(UserActivity.this, "Erreur lors de la recherche : " + errorMessage, Toast.LENGTH_SHORT).show();
                }
            });

            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    // Gérer la soumission de la requête (si nécessaire)
                    return false;
                }

                @Override
                public boolean onQueryTextChange(final String newText) {

                    if (searchRunnable != null) {
                        searchHandler.removeCallbacks(searchRunnable);
                    }


                    searchRunnable = new Runnable() {
                        @Override
                        public void run() {
                            performApiSearch(newText, userService);
                        }
                    };


                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);

                    return true;
                }
            });

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    handleBackButtonClick();
                }
            });
        }
    }

    private void performApiSearch(String query, UserService userService) {
        Call<JsonElement> call = userService.searchUsers(query);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    JsonElement jsonElement = response.body();

                    if (jsonElement.isJsonObject()) {

                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        if (jsonObject.has("items") && jsonObject.get("items").isJsonArray()) {
                            JsonArray itemsArray = jsonObject.getAsJsonArray("items");


                            List<User> searchResults = new ArrayList<>();
                            for (JsonElement item : itemsArray) {
                                User user = new Gson().fromJson(item, User.class);
                                searchResults.add(user);
                            }

                            users.clear();
                            users.addAll(searchResults);
                            displayUserList();
                            Toast.makeText(UserActivity.this, "Liste à jour", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(UserActivity.this, "Réponse inattendue de l'API", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserActivity.this, "Réponse inattendue de l'API", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(UserActivity.this, "Erreur lors de la recherche", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                String errorMessage = t.getMessage();
                if (errorMessage == null || errorMessage.isEmpty()) {
                    errorMessage = "Erreur inconnue";
                }
                Toast.makeText(UserActivity.this, "Erreur lors de la recherche : " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void displayUserList() {
        UserAdapter adapter = new UserAdapter(this, users);
        userListView.setAdapter(adapter);

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedUsername = users.get(position).getLogin();
                Toast.makeText(UserActivity.this, "Utilisateur sélectionné : " + selectedUsername, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void displayErrorAndNavigateToMain() {
        Toast.makeText(UserActivity.this, "Une erreur est survenue.", Toast.LENGTH_SHORT).show();
        Intent intentDisconnect = new Intent(UserActivity.this, MainActivity.class);
        startActivity(intentDisconnect);
    }
    private void handleBackButtonClick() {
        Log.d(TAG, "Back button");
        Intent intent = new Intent(UserActivity.this, HomeActivity.class);
        startActivity(intent);
    }

}
