package com.example.pchatingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.PrimitiveIterator;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private Button upadateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference Rootref;
    private ProgressDialog loadingBar;
    private final int PICK_IMAGE_REQUEST = 22;
    FirebaseStorage storage;
    private Uri filePath;
    StorageReference storageReference;

    private Toolbar settingsToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid().toString();
        Rootref = FirebaseDatabase.getInstance().getReference();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        initializeFields();

        /**  userName.setVisibility(View.INVISIBLE);
         * This will hide the edit text from settings and you won't see it there.
         * this is implemented to just not to show or edit the userName again and again.
         */

        upadateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
                UpdateSetting();

            }
        });

        RetrieveUserInfo();


        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode,
                                    int resultCode,
                                    Intent data) {

        super.onActivityResult(requestCode,
                resultCode,
                data);

        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null
                && data.getData() != null) {

            // Get the Uri of data
            filePath = data.getData();
            try {

                // Setting image on image view using Bitmap showing picture offline after selecting an image from gallery
                Bitmap bitmap = MediaStore
                        .Images
                        .Media
                        .getBitmap(
                                getContentResolver(),
                                filePath);
                userProfileImage.setImageBitmap(bitmap);
            } catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }

    }




    private void SelectImage() {

        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(
                        intent,
                        "Select Image from here..."),
                PICK_IMAGE_REQUEST);

    }



    private void UpdateSetting() {


        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if (TextUtils.isEmpty(setUserName)){
            Toast.makeText(SettingsActivity.this, "Please write your User Name first...", Toast.LENGTH_SHORT).show();

        }
        if (TextUtils.isEmpty(setStatus)){
            Toast.makeText(SettingsActivity.this, "Please write your status ...", Toast.LENGTH_SHORT).show();

        }
        else {

            HashMap<String, Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            Rootref.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()){

                                sendUserToMainActivity();

                                Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();

                            }
                            else {

                                String message =task.getException().toString();
                                Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
                            }

                        }
                    });

        }
    }

    private void initializeFields() {

        upadateAccountSettings= (Button) findViewById(R.id.set_update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_user_status);
        userProfileImage= (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);
        settingsToolbar = (Toolbar) findViewById(R.id.settings_toolbar);

        setSupportActionBar(settingsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");


    }
    // UploadImage method
    private void uploadImage() {

        if (filePath != null) {

            // Code for showing progressDialog while uploading

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            // Defining the child of storageReference
            StorageReference ref = storageReference.child("Profile Images/" + UUID.randomUUID().toString());



            // adding listeners on upload
            // or failure of image




            ref.putFile(filePath)
                    .addOnSuccessListener(
                            new OnSuccessListener<UploadTask.TaskSnapshot>() {

                                @Override
                                public void onSuccess(
                                        UploadTask.TaskSnapshot taskSnapshot) {

                                    // Image uploaded successfully
                                    // Dismiss dialog
                                    // progressDialog.dismiss();

                                    Toast
                                            .makeText(SettingsActivity.this,
                                                    "Image Uploaded!!",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                    loadingBar.dismiss();

                                }
                            })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            // Error, Image not uploaded
                            // progressDialog.dismiss();
                            Toast
                                    .makeText(SettingsActivity.this,
                                            "Failed " + e.getMessage(),
                                            Toast.LENGTH_SHORT)
                                    .show();
                        }
                    })
                    .addOnProgressListener(
                            new OnProgressListener<UploadTask.TaskSnapshot>() {

                                // Progress Listener for loading
                                // percentage on the dialog box
                                @Override
                                public void onProgress(
                                        UploadTask.TaskSnapshot taskSnapshot) {
                                    double progress
                                            = (100.0
                                            * taskSnapshot.getBytesTransferred()
                                            / taskSnapshot.getTotalByteCount());
                                    progressDialog.setMessage(
                                            "Uploaded "
                                                    + (int) progress + "%");
                                }
                            })
                    .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {

                                        String URL = uri.toString();

                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");


                                        reference.child(currentUserID).child("image").setValue(URL).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                progressDialog.dismiss();

                                            }
                                        });

                                        //This is your image url do whatever you want with it.

                                    }
                                });

                            }
                        }

                    });
        }

    }


    //uploading done



//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
//    {
//        super.onActivityResult(requestCode, resultCode, data);
//
//        if (requestCode==GalleryPick && resultCode == RESULT_OK && data!=null)
//        {
//
//            Uri ImageUri = data.getData();
//            CropImage.activity()
//                    .setGuidelines(CropImageView.Guidelines.ON)
//                    .setAspectRatio(1,1)
//                    .start(this);
//        }
//
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
//        {
//            CropImage.ActivityResult result = CropImage.getActivityResult(data);
//}
//            if (resultCode ==  RESULT_OK)
//            {
//
//                loadingBar.setTitle("Set Profile Image");
//                loadingBar.setMessage("Please wait, your profile image is uploading...");
//                loadingBar.setCanceledOnTouchOutside(false);
//                loadingBar.show();
//
//
//
//
//
////                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
////                    @Override
////                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
////                    {
////                        if (task.isSuccessful())
////                        {
////
////                            Toast.makeText(SettingsActivity.this, "Profile picture uploaded Successfully", Toast.LENGTH_SHORT).show();
////                            loadingBar.dismiss();
////
//////                            UserProfileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
//////                                @Override
//////                                public void onSuccess(Uri uri) {
//////                                    String url = uri.toString();
//////
//////                                    Rootref.child("Users").child(currentUserID).child("image")
//////                                            .setValue(url).addOnCompleteListener(new OnCompleteListener<Void>() {
//////                                        @Override
//////                                        public void onComplete(@NonNull Task<Void> task)
//////                                        {
//////                                            Toast.makeText(SettingsActivity.this, "completed what i wanted", Toast.LENGTH_SHORT).show();
//////
//////                                        }
//////                                    });
//////                                }
//////                            });
////
////                            //it's going to be an error here due to new version of firebase
////                   //        final String downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
////
////                          final String  downloadUrl = task.getResult().getStorage().getDownloadUrl().toString();
////
////
////                           Rootref.child("Users").child(currentUserID).child("image")
////                                   .setValue(downloadUrl)
////                                   .addOnCompleteListener(new OnCompleteListener<Void>() {
////                                       @Override
////                                       public void onComplete(@NonNull Task<Void> task)
////                                       {
////                                           if (task.isSuccessful())
////                                           {
////                                               Toast.makeText(SettingsActivity.this, "Image saved in Database successfully", Toast.LENGTH_SHORT).show();
////
////                                               loadingBar.dismiss();
////                                           }
////                                           else
////                                           {
////                                               String message = task.getException().toString();
////                                               Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
////                                               loadingBar.dismiss();
////                                           }
////                                       }
////                                   });
////
////                        }
////                        else
////                        {
////                            String message = task.getException().toString();
////                            Toast.makeText(SettingsActivity.this, "Error: "+message, Toast.LENGTH_SHORT).show();
////                            loadingBar.dismiss();
////                        }
////                    }
////                });
//            }
//        }
//    }


    private void sendUserToMainActivity() {

        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }


    private void RetrieveUserInfo() {


        Rootref.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        if ((snapshot.exists()) && (snapshot.hasChild("name") && (snapshot.hasChild("image"))))
                        {
                            String retrieveUserName= snapshot.child("name").getValue().toString();
                            String retrieveStatus= snapshot.child("status").getValue().toString();
                            String retrieveProfileImage= snapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                            Picasso.get().load(retrieveProfileImage).into(userProfileImage);

                        }

                        else if ((snapshot.exists()) && (snapshot.hasChild("name")))
                        {
                            String retrieveUserName= snapshot.child("name").getValue().toString();
                            String retrieveStatus= snapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveStatus);
                        }
                        else {

                            //it'll show it in creating new id.
                           // userName.setVisibility(View.VISIBLE);

                            Toast.makeText(SettingsActivity.this, "Please set & update your profile information ", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

}