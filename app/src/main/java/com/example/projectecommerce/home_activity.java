package com.example.projectecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.projectecommerce.Model.ProfilePicUrl;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class home_activity extends AppCompatActivity implements ImageAdapter.onClickListener {

    NavigationView nav;
    ActionBarDrawerToggle toggle;
    DrawerLayout drawerLayout;

    private RecyclerView mRecyclerView;
    private ImageAdapter mAdapter;
    private DatabaseReference mDatabaseRef;
    private List<Upload> mUploads;

    private Uri profile_pic_uri;
    private ImageView profile_pic;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_activity2);

        // of no use
        initViews();

        // these are the views for custom navigation drawer
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // find navs and drawer layouts
        nav = findViewById(R.id.nav_menu);
        drawerLayout = findViewById(R.id.drawer);

        toggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,         R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // setting onclick listeners for all the menu items
        nav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.home:
                        Intent intent = new Intent(home_activity.this,profile_activity.class);
                        startActivity(intent);

                        Toast.makeText(home_activity.this, "home is clicked", Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.cart:
                        Intent intent1 = new Intent(home_activity.this,cart.class);
                        startActivity(intent1);

                        Toast.makeText(home_activity.this, "cart is Clicked", Toast.LENGTH_SHORT).show();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.logout:
                        logout();
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                }
                return true;
            }
        });

        // initialize views for the recycler views for showing products at the homepages
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // initializing the arrayList that holds the products
        mUploads = new ArrayList<>();
        
        // initialize the database
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("uploads");

        mDatabaseRef.addValueEventListener(new ValueEventListener(){
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot postSnapshot : snapshot.getChildren()){
                    Upload upload = postSnapshot.getValue(Upload.class);
                    mUploads.add(upload);
                }
                // attaching adapter to the recycler view
                mAdapter = new ImageAdapter(home_activity.this,mUploads,home_activity.this);
                mRecyclerView.setAdapter(mAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(home_activity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });



        // we cannot directly access views from header_menu
        // this is how we can
        View headView = nav.getHeaderView(0);
        profile_pic = (ImageView) headView.findViewById(R.id.user_profile_image);

        // open gallery when profile pic image is clicked
        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });
    }

    // create intent to open gallery
    private void openGallery(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data!=null && data.getData()!=null){
            profile_pic_uri = data.getData();
            // set profile image as the selected image from the gallery
            profile_pic.setImageURI(profile_pic_uri);

            uploadProfilePic();
        }
    }

    private void uploadProfilePic(){
        // create different database reference for having details of all profile pic uploaded
        final DatabaseReference databaseReferenceForProfilePics = FirebaseDatabase.getInstance().getReference("profile_pics");
        // create a storage reference for the uploaded profile pic items
        storageReference  = FirebaseStorage.getInstance().getReference("profile_pics");
        // give unique name for all the uploaded pics to storage
        final StorageReference ref = storageReference.child(System.currentTimeMillis()+"."+getFileExtension(profile_pic_uri));

        // below code is for updating detail of uploaded file in the database
        ref.putFile(profile_pic_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        // create object of profile pic url. This is basically a model for uploading profile pics
                        ProfilePicUrl profilePicUrl = new ProfilePicUrl(uri.toString());
                        profile_pic.setImageURI(uri);
                        // generate random key
                        String modelId = mDatabaseRef.push().getKey();
                        // finally update detail to database
                        databaseReferenceForProfilePics.child(modelId).setValue(profilePicUrl);

                        Glide.with(home_activity.this).load(uri).into(profile_pic);

                        Toast.makeText(home_activity.this, "Upload successful", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                // yet to code
                // handle the progress bar here
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // handle error
                Toast.makeText(home_activity.this, "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // this method return the extension of file as a string
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void logout(){
        // go to login activity and clear all data from previous activity
        Intent intent = new Intent(home_activity.this,login_activity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        this.finish();
        Toast.makeText(this, "Logout successfully", Toast.LENGTH_SHORT).show();
    }

    // handle back presses at home activity so that it doesn't go to login activity instead ecd the app
    @Override
    public void onBackPressed() {
        finishAffinity();
        System.exit(0);
    }

    public void initViews(){
//        profile_img = findViewById(R.id.user_profile_image);
//        user_name = findViewById(R.id.user_name);
    }

    @Override
    public void onProductCLick(final int position) {
        Toast.makeText(this, "item is clicked", Toast.LENGTH_SHORT).show();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Upload upload = mUploads.get(position);
                Intent intent = new Intent(home_activity.this,item_detail.class);
                assert upload != null;
                intent.putExtra("price",upload.getM_price());
                intent.putExtra("des",upload.getM_description());
                intent.putExtra("image",upload.getmImageUrl());
                intent.putExtra("name",upload.getmName());
                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}