package android_development.taskshare;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class UserSettingsActivity extends AppCompatActivity {

    EditText etUserName;
    EditText etUserMail;
    Button btnNameEditable;
    Button btnMailEditable;
    ImageView ivUserPicture;
    Button btnSaveUserData;
    Button btnChooseImage;
    Button btnUploadImage;
    Button btnDownloadImage;
    Button btnLogout;

    private Uri filePath;
    //PICK_IMAGE_REQUEST is the request code defined as an instance variable.
    private final int PICK_IMAGE_REQUEST = 71;

    private static final int REQUEST_RUNTIME_PERMISSION = 123;

    //Imagepath from downloaded userImage
    private Bitmap userImagePath;

    //Initialize FirebaseAuth instance
    public FirebaseAuth mAuth = FirebaseAuth.getInstance();

    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    FirebaseUser currentUser;

    String userID;
    String userName;
    Uri userProfilePhotoUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_settings);

        //sets the title of the activity and implements the back button
        setTitle("User Data");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Initialize EditText fields and set them to not editable
        ivUserPicture = (ImageView) findViewById(R.id.ivUserProfile);
        etUserName = (EditText) findViewById(R.id.etUserName);
        etUserMail = (EditText) findViewById(R.id.etUserMail);
        etUserName.setEnabled(false);
        etUserMail.setEnabled(false);

        //Get UserData from stored object of the PutExtra Method by the NavigationActivity
        UserData userData = (UserData) getIntent().getSerializableExtra("userData");

        //Set data values to displayfields
        etUserName.setText(userData.getUserDisplayName());
        etUserMail.setText(userData.getUserMail());
        ivUserPicture.setImageURI(Uri.parse(userData.getUserPhotoUrl()));
        userID = userData.getUserId();

        //------------------------------------------------------------------------------------------
        //Initialize Buttons and implement onClick Events
        //------------------------------------------------------------------------------------------
        btnNameEditable = (Button) findViewById(R.id.btnNameEditable);
        btnNameEditable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get focus on editText field for name and make it editable if field is not editable
                if (etUserName.isInEditMode() == false) {
                    etUserName.setEnabled(true);
                    etUserName.setFocusable(true);
                }
                else {
                    // do something else
                }
            }
        });

        btnMailEditable = (Button) findViewById(R.id.btnMailEditable);
        btnMailEditable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get focus on editText field for mail and make it editable if field is not editable
                if (etUserName.isInEditMode() == false) {
                    etUserMail.setEnabled(true);
                    etUserMail.setFocusable(true);
                }
                else {
                    // do something else
                }
            }
        });

        btnSaveUserData = (Button) findViewById(R.id.btnUpdateUserSettings);
        btnSaveUserData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Some action to save the userdata
                //TODO: Implement function to store user data to firebase auth database

                //make the fields not editable again.
                etUserName.setEnabled(false);
                etUserName.setFocusable(false);
                etUserMail.setEnabled(false);
                etUserMail.setFocusable(false);
            }
        });

        btnChooseImage = (Button) findViewById(R.id.btnChooseImage);
        btnChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the chooseImage() method
                chooseImage();
            }
        });

        btnUploadImage = (Button) findViewById(R.id.btnUploadImage);
        btnUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //call the uploadImage(); method
                uploadImage();
            }
        });

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        btnDownloadImage = (Button) findViewById(R.id.btnDownloadImage);
        btnDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //------------------------------------------------------------------------------------------
                //Check if user has Permission to download to phone storage
                // Code from: https://stackoverflow.com/questions/38141523/directory-creation-not-working-in-marshmallow-android/38141778#38141778
                //------------------------------------------------------------------------------------------
                if (CheckPermission(UserSettingsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // you have permission go ahead
                    downloadUserImage();
                } else {
                    // you do not have permission go request runtime permissions
                    RequestPermission(UserSettingsActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE, REQUEST_RUNTIME_PERMISSION);
                }
                //------------------------------------------------------------------------------------------

            }
        });

        //------------------------------------------------------------------------------------------

    }

        //TODO: implement save option to store changed data in Firebase Auth database
        //TODO: implement upload for new profile picture


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    //----------------------------------------------------------------------------------------------
    //FROM TUTORIAL
    //https://code.tutsplus.com/tutorials/image-upload-to-firebase-in-android-application--cms-29934
    //----------------------------------------------------------------------------------------------

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = storageReference.child("images/"+userID+"/"+ UUID.randomUUID().toString());
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(UserSettingsActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(UserSettingsActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void downloadUserImage(){
        //------------------------------------------------------------------------------------------
        //Download file from Firebase and put it into a local temp file.
        //Code from: https://stackoverflow.com/questions/45703901/how-do-i-download-an-image-from-firebase-storage-on-android
        //------------------------------------------------------------------------------------------
        StorageReference ref = storage.getReference().child("images/"+userID+"/ba2d415e-60e4-49d9-b8d8-e0c43be9897e.jpg");
        try {
            final File localFile = File.createTempFile("Images", "jpg");
            storageReference.getFile(localFile).addOnSuccessListener(new OnSuccessListener< FileDownloadTask.TaskSnapshot >() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    userImagePath = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                    // TESTPURPOSE
                    //TODO: Check if this is working
                    ivUserPicture.setImageURI(Uri.parse(localFile.getAbsolutePath()));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(UserSettingsActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //----------------------------------------------------------------------------------------------
    // Store user Data to Firebase server
    //----------------------------------------------------------------------------------------------
    public void saveUserData(){


        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            userID = currentUser.getUid();

            // Name, email address, and profile photo Url
            userName = "Lars Bürkner";
            userProfilePhotoUrl = Uri.parse("https://www.google.de/imgres?imgurl=https%3A%2F%2Fupload.wikimedia.org%2Fwikipedia%2Fcommons%2Fthumb%2Fe%2Fe0%2FSNice.svg%2F1200px-SNice.svg.png&imgrefurl=https%3A%2F%2Fen.wikipedia.org%2Fwiki%2FSmiley&docid=EB-7l6d3ePZ1CM&tbnid=2ibD-NzxUXqVVM%3A&vet=10ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg..i&w=1200&h=1200&client=firefox-b-ab&bih=750&biw=1536&q=smiley&ved=0ahUKEwid-oGvyrnZAhUD3aQKHdbCDFYQMwgwKAIwAg&iact=mrc&uact=8");

            //Change User data based on input
            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setPhotoUri(userProfilePhotoUrl)
                    .setDisplayName(userName).build();
            currentUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Log.d("Display name: ", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    }
                }
            });

        }
    }
    //---------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                ivUserPicture.setImageBitmap(bitmap);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------
    // CHECK PERMISSION FOR FILE UPLOAD
    // Code from: https://stackoverflow.com/questions/38141523/directory-creation-not-working-in-marshmallow-android/38141778#38141778
    //----------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int permsRequestCode, String[] permissions, int[] grantResults) {

        switch (permsRequestCode) {

            case REQUEST_RUNTIME_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // you have permission go ahead
                    downloadUserImage();
                } else {
                    // you do not have permission show toast.
                }
                return;
            }
        }
    }

    public void RequestPermission(Activity thisActivity, String Permission, int Code) {
        if (ContextCompat.checkSelfPermission(thisActivity,
                Permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,
                    Permission)) {
            } else {
                ActivityCompat.requestPermissions(thisActivity,
                        new String[]{Permission},
                        Code);
            }
        }
    }

    public boolean CheckPermission(Context context, String Permission) {
        if (ContextCompat.checkSelfPermission(context,
                Permission) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    //----------------------------------------------------------------------------------------------
    //----------------------------------------------------------------------------------------------
}
