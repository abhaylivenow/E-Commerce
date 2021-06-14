package com.example.projectecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

public class admin_activity extends AppCompatActivity {

    private Spinner category_spinner;
    private ImageView product_img;
    private EditText edt_product_name,edt_product_des,edt_product_price;
    private Button btn_upload_product;
    private ProgressDialog loadingBar;

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    private DatabaseReference mDatabaseRef;
    private StorageReference mStorageRef;
    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_activity);

        initViews();

        // initialize database and storage
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");
        mStorageRef = FirebaseStorage.getInstance().getReference("uploads");

        // open gallery when admin clicks on the image
        product_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        // upload the file when button is clicked
        btn_upload_product.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadFile();
            }
        });
    }

    public void uploadFile(){

        if(imageUri!=null){
            loadingBar.setTitle("Uploading file");
            loadingBar.setMessage("PLease wait while we upload the file");
            loadingBar.setCanceledOnTouchOutside(false);
            loadingBar.show();

            // get storage reference and give unique name to the uploaded products
            StorageReference fileReference = mStorageRef.child(System.currentTimeMillis() + "." + getFileExtension(imageUri));

            // upload files to the storage and details about the product to the database
            mUploadTask = fileReference.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    loadingBar.dismiss();
                    Toast.makeText(admin_activity.this, "Upload successful", Toast.LENGTH_SHORT).show();

                    // get all the information about the product entered by admin in the editText
                    // these information will be shown to user with the product image
                    String productName = edt_product_name.getText().toString();
                    String productDes = edt_product_des.getText().toString();
                    String productPrice = edt_product_price.getText().toString();
                    String downloadUrl = taskSnapshot.getMetadata().getReference().getDownloadUrl().toString();

                    Task<Uri> UrlTask = taskSnapshot.getStorage().getDownloadUrl();
                    while (!UrlTask.isSuccessful());
                        Uri downloadUri = UrlTask.getResult();
                    assert downloadUri != null;
                    // upload is model for handling uploads
                    // these below information about the product will be stored in the database
                    Upload upload = new Upload(productName,downloadUri.toString(),productDes,productPrice);
                    // generate random key
                    String uploadId = mDatabaseRef.push().getKey();
                    // finally upload to the respective database
                    mDatabaseRef.child(uploadId).setValue(upload);
                }
                // handle failure below
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loadingBar.dismiss();
                    String message = e.getMessage();
                    Toast.makeText(admin_activity.this, "Error = "+message, Toast.LENGTH_SHORT).show();

                }
            });
        }
    }

    // create intent for opening gallery
    public void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent , PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            imageUri = data.getData();
            // set product image as elected by admin
            product_img.setImageURI(imageUri);
        }
    }

    // this method will return the extension of the file in the string format
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    public void initViews(){
//        category_spinner = findViewById(R.id.category_spinner);
        edt_product_name = findViewById(R.id.edt_product_name);
        edt_product_des = findViewById(R.id.edt_product_description);
        edt_product_price = findViewById(R.id.edt_product_price);
        product_img = findViewById(R.id.product_img_upload);
        btn_upload_product = findViewById(R.id.btn_upload_product);
        loadingBar = new ProgressDialog(this);
    }
}