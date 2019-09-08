package softagi.chatad;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import de.hdodenhof.circleimageview.CircleImageView;
import softagi.chatad.Models.RoomModel;

public class CreateRoomActivity extends AppCompatActivity
{
    CircleImageView circleImageView;
    EditText room_title_field;
    String roomTitle;
    Uri photopath;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_room);

        toolBar();
        initViews();
        initFirebase();
    }

    private void initFirebase()
    {
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    private void initViews()
    {
        circleImageView = findViewById(R.id.room_image);
        room_title_field = findViewById(R.id.room_title_field);

        circleImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON_TOUCH)
                        .setAspectRatio(1,1)
                        .start(CreateRoomActivity.this);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            photopath = result.getUri();

            Picasso.get()
                    .load(photopath)
                    .into(circleImageView);
        }
    }

    public void toolBar ()
    {
        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_keyboard_arrow_left_black_24dp);
        getSupportActionBar().setTitle("");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home :
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void createRoom(View view)
    {
        roomTitle = room_title_field.getText().toString();

        if (TextUtils.isEmpty(roomTitle))
        {
            Toast.makeText(getApplicationContext(), "enter room title", Toast.LENGTH_SHORT).show();
            room_title_field.requestFocus();
            return;
        }

        if (photopath == null)
        {
            Toast.makeText(getApplicationContext(), "select a photo", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new ProgressDialog(CreateRoomActivity.this);
        progressDialog.setMessage("Wait ...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.show();

        uploadImage(roomTitle,photopath);
    }

    private void uploadImage(final String roomTitle, Uri photopath)
    {
        UploadTask uploadTask;
        final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + photopath.getLastPathSegment());
        uploadTask = storageReference.putFile(photopath);

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
                if (task.isSuccessful())
                {
                    Uri image = task.getResult();
                    String photoUrl = image.toString();

                    addTodb(roomTitle,photoUrl);
                } else
                    {
                        Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
            }
        });
    }

    private void addTodb(String roomTitle, String photoUrl)
    {
        String key = databaseReference.child("Rooms").push().getKey();
        RoomModel model = new RoomModel(roomTitle,photoUrl,key);
        databaseReference.child("Rooms").child(key).setValue(model);

        progressDialog.dismiss();

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
        finish();
    }
}
