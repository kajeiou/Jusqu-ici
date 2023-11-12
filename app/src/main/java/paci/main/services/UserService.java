package paci.main.services;

import com.google.gson.JsonElement;

import paci.main.classes.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

import java.util.List;

public interface UserService {
    @GET("users")
    Call<List<User>> getUsers();

    @GET("search/users")
    Call<JsonElement> searchUsers(@Query("q") String query);
}
