package com.example.projectecommerce;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

public class item_detail extends AppCompatActivity {

    private ImageView product_img;
    private TextView price,des;
    private Button add_to_cart;

    String productPrice;
    String productDes;
    String productUrl;
    String productName;

    private DatabaseReference cartRef;
    private StorageReference cartStoreRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        initViews();

        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        productPrice = bundle.getString("price");
        productDes = bundle.getString("des");
        productUrl = bundle.getString("image");
        productName = bundle.getString("name");

        price.setText(productPrice);
        des.setText(productDes);

        Uri imageUri = Uri.parse(productUrl);
        Picasso.get().load(imageUri).into(product_img);

        add_to_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addToCart();
            }
        });
    }

    private void addToCart(){
        cartRef = FirebaseDatabase.getInstance().getReference("cartItems");
        cartStoreRef = FirebaseStorage.getInstance().getReference("cartItems");


    }

    private void initViews(){
        product_img = findViewById(R.id.product_detail_img);
        price = findViewById(R.id.product_detail_price);
        des = findViewById(R.id.product_detail_des);
        add_to_cart = findViewById(R.id.btn_cart);
    }
}