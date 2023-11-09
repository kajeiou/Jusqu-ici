package paci.estiam.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import paci.estiam.R;
import java.util.List;

import paci.estiam.classes.User;

public class UserAdapter extends ArrayAdapter<User> {

    public UserAdapter(Context context, List<User> users) {
        super(context, 0, users);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        User user = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_user_item, parent, false);
        }

        ImageView avatarImageView = convertView.findViewById(R.id.avatarImageView);
        TextView userNameTextView = convertView.findViewById(R.id.userNameTextView);

        // Affichez l'avatar et le nom d'utilisateur
        Picasso.get().load(user.getAvatarUrl()).into(avatarImageView);

        userNameTextView.setText(user.getLogin());


        return convertView;
    }
}
