package softagi.chatad;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class SignInActivity extends AppCompatActivity
{
    EditText email_field,password_field;
    String email,password;
    FirebaseAuth auth;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        initViews();
        initFirebase();
    }

    private void initFirebase()
    {
        auth = FirebaseAuth.getInstance();
    }

    private void initViews()
    {
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
    }

    public void login(View view)
    {

    }
}
