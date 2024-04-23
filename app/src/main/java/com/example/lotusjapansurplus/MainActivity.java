package com.example.lotusjapansurplus;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lotusjapansurplus.Deliver.DeliverActivity;
import com.example.lotusjapansurplus.buyer.BuyerActivity;
import com.example.lotusjapansurplus.live.LiveActivity;
import com.example.lotusjapansurplus.payment.paymentActivity;
import com.example.lotusjapansurplus.transactions.TransactionActivity;
import com.example.lotusjapansurplus.upload.uploadActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button liveButton = findViewById(R.id.live);
        Button buyerButton = findViewById(R.id.buyer);
        Button transactionButton = findViewById(R.id.transaction);
        Button paymentButton = findViewById(R.id.payment);

        paymentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, paymentActivity.class);
                startActivity(intent);
            }
        });

        // Set click listeners for the buttons
        liveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Create an intent to go to the LiveActivity (replace LiveActivity.class with the actual class)
               /*
                DatabaseReference items = FirebaseDatabase.getInstance().getReference("Items");

                String[] itemNames = {
                        "Anik Anik", "Aquarium", "As is", "Ashtray", "Assorted", "Bag", "Bake", "Bake - Dish", "Bake - Lasagna",
                        "Bake - Ramekins", "Bake - Tarte", "Basket", "Bedsheet", "Beer Glass", "Beer Mug", "Bell", "Blanket",
                        "Bowl dragon", "Candle Holder", "Candle/ Kandila", "Canister", "Canvass Print", "Carpet Tiles", "Cat",
                        "Chair", "Chopping Board", "Chopstick Holder", "Claypot - large", "Claypot - medium", "Claypot - small",
                        "Closet", "Coaster", "Coffee dripper", "Coffee Press", "Coffee Siphon", "Comforter", "Condiments",
                        "Copper cookware", "Corningware", "Crystal", "Crystal - bell", "Crystal - bowl", "Crystal - footed",
                        "Crystal - Glass", "Curtain", "Decor", "Dining Chair", "Dining Set", "Display Plate", "Dog", "Doll",
                        "Figurine", "Food Keeper", "Frame", "Frame Big", "Frame Bundle", "Frame Medium", "Free", "Gift set",
                        "Gift set - Oriental", "Gift set - Arita", "Gift set - Bedding", "Gift set - Bento", "Gift set - Bowls",
                        "Gift set - Cups", "Gift set - Cups & Saucer", "Gift set - Drinking cups", "Gift set - Plate",
                        "Gift set - Stoneware", "Gift set - Towel", "Gift set - Vase", "Glass mug", "Glass Vase", "Goblet",
                        "Hand towel", "Ice Bucket", "Jackpot", "Jewelry Box", "Jewelry cabinet", "Kitchen cabinet", "Knife",
                        "Lamp", "Lazy Susan", "Mason Jar", "Measuring cup", "Milk Jug", "Milk Pourer", "Mini jar", "Mirror",
                        "Mug", "Mug UK", "No cover", "Oriental - Bowl", "Oriental - Plate", "Oriental - Ramen Bowl",
                        "Oriental - Saucer", "Oriental - Serving", "Oriental - Sushi plate", "Oval", "Painting",
                        "Pestle & Mortar", "Placemats", "Plant Pot", "Plate", "Platito", "Platter", "Porcelain coated pan",
                        "Pot Bundle", "Rug", "Sake Jar", "Saucer", "School supplies", "Serving", "Serving bowl",
                        "Serving Dish", "Sewing Box", "Shell Decor", "Shotting Glass", "Slipper", "Small Box", "Soup Bowl",
                        "Spoon & Fork", "Square", "Stainless", "Stainless - Basket", "Stainless - Bathroom", "Stainless - Dish Rack",
                        "Stainless - Fruit Bowl", "Stoneware - bowl", "Stoneware - cup", "Stoneware - mug", "Stoneware - plate",
                        "Stoneware - Vase", "Sushi Plate", "Table", "Takip", "Tall Glass", "Tureen", "Teapot - Japan",
                        "Teapot - UK", "Thermos", "Tin cans", "Towel", "Trashcan", "UK Vase", "Umbrella", "Utensil",
                        "Wall Decor", "Wax Warmer", "Wine Glass", "Wooden", "Wooden - Bowl", "Wooden - Coasters", "Wooden - Cup",
                        "Wooden - Plate", "Wooden - Tray", "White and blue plate", "Glass set", "Ceramic bowl", "Glass bundle",
                        "White and blue bowls", "White and blue saucers", "Gift set - Glass", "Narumi", "Tray", "Bento - Saucers",
                        "Toreen", "Salt shakers", "Gift set - Basket", "Matcha bowl", "Stoneware sushi plate", "Tea set",
                        "Ramen bowl", "Stoneware with cover", "Gift set - White and blue", "Coffee set", "Gift set - crystal",
                        "Coffee pot", "Xmas plate", "Stoneware - saucer", "Tricolor set", "White and blue bundle",
                        "White and blue serving", "Coffee mug", "Gift set - big bowl", "Ceramic bundle", "Hoya", "Mikasa",
                        "Royal doulton", "Nikko", "Biltons", "Tachikichi", "Arita", "Noritake", "Oriental", "Kutani", "Red",
                        "Orange", "Purple", "Violet", "Black", "White", "White", "Green", "Blue", "Yellow", "Bundle",
                        "Bundle - baso", "Bundle - Colored glass", "Bundle - Crystal", "Bundle - Cups", "Bundle - Oriental",
                        "Bundle - Plates", "Bundle - Saucer", "Bundle - Stoneware", "Bundle - Wineglass", "Bundle - Mug",
                        "Bundle - Bowls", "Bundle - Clear", "Bundle - blue", "Bundle - Clear", "Bundle - blue",
                        "Pyrex Measure", "Pyrex Bowl", "Pyrex Coffee pot", "Pyrex Bottle", "Arcopal - plate",
                        "Arcopal - Bundle", "Arcopal - cup", "Bento - Tray", "Arcopal - Bowl", "Arcopal - Bundle", "Arcopal - cup",
                        "Bento - Tray", "Bento - Saucers", "Toreen", "Salt shakers", "Gift set - Basket", "Matcha bowl",
                        "Stoneware sushi plate", "Tea set", "Ramen bowl", "Stoneware with cover", "Gift set - White and blue",
                        "Coffee set", "Gift set - crystal", "Coffee pot", "Xmas plate", "Stoneware - saucer", "Tricolor set",
                        "White and blue bundle", "White and blue serving", "Coffee mug", "Gift set - big bowl", "Ceramic bundle",
                        "Hoya", "Mikasa", "Royal doulton", "Nikko", "Biltons", "Tachikichi", "Arita", "Noritake", "Oriental",
                        "Kutani", "Red", "Orange", "Purple", "Violet", "Black", "White", "Green", "Blue", "Yellow", "Bundle",
                        "Bundle - baso", "Bundle - Colored glass", "Bundle - Crystal", "Bundle - Cups", "Bundle - Oriental",
                        "Bundle - Plates", "Bundle - Saucer", "Bundle - Stoneware", "Bundle - Wineglass", "Bundle - Mug",
                        "Bundle - Bowls", "Bundle - Clear", "Bundle - blue", "Bundle - Clear", "Bundle - blue",
                        "Pyrex Measure", "Pyrex Bowl", "Pyrex Coffee pot", "Pyrex Bottle", "Arcopal - plate",
                        "Arcopal - Bundle", "Arcopal - cup", "Bento - Tray"
                };

                for (int i = 0; i < itemNames.length; i++) {
                    items.child("items" + i).setValue(itemNames[i]);
                }

                */

                Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                startActivity(intent);
            }
        });

        buyerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to go to the BuyerActivity (replace BuyerActivity.class with the actual class)
                Intent intent = new Intent(MainActivity.this, BuyerActivity.class);
                startActivity(intent);
            }
        });

        Button deliverButton = findViewById(R.id.deliver);
        deliverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, DeliverActivity.class);
                startActivity(intent);
            }
        });
        transactionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create an intent to go to the TransactionActivity (replace TransactionActivity.class with the actual class)
                Intent intent = new Intent(MainActivity.this, TransactionActivity.class);
                startActivity(intent);
            }
        });


    }

}
