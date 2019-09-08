package softagi.chatad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.chatad.Models.UserModel;

public class MainActivity extends AppCompatActivity
{
    CircleImageView circleImageView;
    EditText username_field,email_field,password_field,confirm_password_field;
    String username,email,password,confirmpassword;
    Uri picture;

    FirebaseAuth auth;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initFirebase();

        FirebaseUser user = auth.getCurrentUser();
        if (user != null)
        {
            startActivity(new Intent(getApplicationContext(), StartActivity.class));
            finish();
        }
    }

    private void initFirebase()
    {
        auth = FirebaseAuth.getInstance();

        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();

        firebaseStorage = FirebaseStorage.getInstance();
    }

    private void initViews()
    {
        circleImageView = findViewById(R.id.user_picture);
        username_field = findViewById(R.id.username_field);
        email_field = findViewById(R.id.email_field);
        password_field = findViewById(R.id.password_field);
        confirm_password_field = findViewById(R.id.confirm_passwprd_field);

        circleImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(MainActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == Activity.RESULT_OK)
            {
                if (result != null)
                {
                    picture = result.getUri();

                    Picasso.get()
                            .load(picture)
                            .placeholder(R.drawable.supermario)
                            .error(R.drawable.supermario)
                            .into(circleImageView);
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void register(View view)
    {
        username = username_field.getText().toString();
        email = email_field.getText().toString();
        password = password_field.getText().toString();
        confirmpassword = confirm_password_field.getText().toString();

        if (TextUtils.isEmpty(username))
        {
            Toast.makeText(getApplicationContext(), "enter your name", Toast.LENGTH_SHORT).show();
            username_field.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email))
        {
            Toast.makeText(getApplicationContext(), "enter your email", Toast.LENGTH_SHORT).show();
            email_field.requestFocus();
            return;
        }

        if (password.length() < 6)
        {
            Toast.makeText(getApplicationContext(), "password is too short", Toast.LENGTH_SHORT).show();
            password_field.requestFocus();
            return;
        }

        if (!confirmpassword.equals(password))
        {
            Toast.makeText(getApplicationContext(), "password is not matching", Toast.LENGTH_SHORT).show();
            confirm_password_field.requestFocus();
            return;
        }

        if (picture == null)
        {
            Toast.makeText(getApplicationContext(), "select your picture", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Wait ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        createUser(username,email,password);
    }

    private void createUser(final String username, final String email, String password)
    {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            String id = task.getResult().getUser().getUid();
                            uploadPicture(username,email,picture,id);
                        } else
                            {
                                progressDialog.dismiss();
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                    }
                });
    }

    private void uploadPicture(final String username, final String email, Uri picture, final String id)
    {
        UploadTask uploadTask;
        storageReference = FirebaseStorage.getInstance().getReference().child("images/" + picture.getLastPathSegment());
        uploadTask = storageReference.putFile(picture);

        Task<Uri> task = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
        {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
            {
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>()
        {
            @Override
            public void onComplete(@NonNull Task<Uri> task)
            {
                Uri image = task.getResult();
                String pictureurl = image.toString();

                saveTodb(username,email,pictureurl,id);
            }
        });
    }

    private void saveTodb(String username, String email, String pictureurl, String id)
    {
        UserModel userModel = new UserModel(username,email,pictureurl);
        databaseReference.child("Users").child(id).setValue(userModel);

        progressDialog.dismiss();
        startActivity(new Intent(getApplicationContext(), StartActivity.class));
        finish();
    }

    public void already(View view)
    {
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }
}
