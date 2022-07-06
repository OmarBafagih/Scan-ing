package com.example.healthinspector.Adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.healthinspector.Cart;
import com.example.healthinspector.Constants;
import com.example.healthinspector.FragmentSwitch;
import com.example.healthinspector.Models.RecommendedProduct;
import com.example.healthinspector.R;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class CartItemAdapter extends RecyclerView.Adapter<CartItemAdapter.ViewHolder>{
    private Context context;
    private List<RecommendedProduct> recommendedProducts;
    private FragmentSwitch fragmentSwitch;

    private static final String TAG = "ProductRecommendationsAdapter";
    public static final String SAVED = "item added to cart";
    public static final String REMOVED = "item removed from cart";


    public CartItemAdapter(Context context, List<RecommendedProduct> recommendedProducts, FragmentSwitch fragmentSwitch){
        this.context = context;
        this.recommendedProducts = recommendedProducts;
        this.fragmentSwitch = fragmentSwitch;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.recommended_product_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecommendedProduct recommendedProduct = recommendedProducts.get(position);
        holder.bind(recommendedProduct);
    }

    @Override
    public int getItemCount() {
        return recommendedProducts.size();
    }

    public void clear() {
        recommendedProducts.clear();
        notifyDataSetChanged();
    }

    public void addAll(List<RecommendedProduct> newPosts) {
        recommendedProducts.addAll(newPosts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView cartItemImageView, addToCartImageView;
        TextView cartItemFactsTextView, cartItemNameTextView;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            cartItemImageView = itemView.findViewById(R.id.cartItemImageView);
            cartItemFactsTextView = itemView.findViewById(R.id.cartItemFactsTextView);
            cartItemNameTextView = itemView.findViewById(R.id.cartItemNameTextView);
            addToCartImageView = itemView.findViewById(R.id.addtoCartImageView);
        }

        public void bind(RecommendedProduct recommendedProduct) {
            Glide.with(context).load(recommendedProduct.getProductImageUrl()).placeholder(R.drawable.ingredients_icon).into(cartItemImageView);
            cartItemNameTextView.setText(recommendedProduct.getProductName());
            String cartItemFacts = "";
            for(int i = 0; i < recommendedProduct.getNutrientLevels().size(); i++){
                if(i != recommendedProduct.getNutrientLevels().size()-1){
                    cartItemFacts += recommendedProduct.getNutrientLevels().get(i) + "\n";
                    continue;
                }
                cartItemFacts += recommendedProduct.getNutrientLevels().get(i);
            }
            cartItemFactsTextView.setText(cartItemFacts);
            if(fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS)){
                addToCartImageView.setImageResource(R.drawable.add_icon_2);
            }
            else{
                addToCartImageView.setImageResource(R.drawable.delete_icon);
            }
            addToCartImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   addOrRemoveFromCart(recommendedProduct);
                }
            });

        }

        public void addOrRemoveFromCart(RecommendedProduct recommendedProduct){
            //creating new json object to save to cart JsonArray
            JSONObject newCartItem = new JSONObject();
            try {
                newCartItem.put(Constants.KEYWORDS, recommendedProduct.getKeyWords());
                newCartItem.put(Constants.BRAND, recommendedProduct.getBrand());
                newCartItem.put(Constants.PRODUCT_IMAGE_URL, recommendedProduct.getProductImageUrl());
                newCartItem.put(Constants.PRODUCT_NAME, recommendedProduct.getProductName());
                newCartItem.put(Constants.NUTRIENT_LEVELS, recommendedProduct.getNutrientLevels());

            } catch (JSONException err) {
                err.printStackTrace();
            }
            //if the recommended product is not in the cart
            if(fragmentSwitch.equals(FragmentSwitch.RECOMMENDATIONS)){
                //if the user's cart has already been initialized
                if(!ParseUser.getCurrentUser().getParseObject(Constants.CART).equals(null)){
                    ParseQuery<Cart> parseQuery = ParseQuery.getQuery(Cart.class);
                    try {
                        Cart userCart = parseQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
                        userCart.setCartItems(userCart.getCartItems().put(newCartItem));
                        userCart.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                Toast.makeText(context, SAVED, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    Cart newCart = new Cart();
                    JSONArray newCartItems = new JSONArray();
                    newCartItems.put(newCartItem);
                    newCart.setCartItems(newCartItems);
                    newCart.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if(e == null){
                                Toast.makeText(context, SAVED, Toast.LENGTH_SHORT).show();
                                ParseUser.getCurrentUser().put(Constants.CART, newCart);
                                ParseUser.getCurrentUser().saveInBackground();
                            }
                        }
                    });
                }
            }
            //user wants the item to be removed from cart
            else{
                ParseQuery<Cart> parseQuery = ParseQuery.getQuery(Cart.class);
                try {
                    Cart userCart = parseQuery.get(ParseUser.getCurrentUser().getParseObject(Constants.CART).getObjectId());
                    for(int i = 0; i < userCart.getCartItems().length(); i++){
                        if(userCart.getCartItems().getJSONObject(i).getString(Constants.PRODUCT_NAME).equals(recommendedProduct.getProductName())){
                            JSONArray updatedCart = userCart.getCartItems();
                            updatedCart.remove(i);
                            userCart.setCartItems(updatedCart);
                        }
                    }
                    Toast.makeText(context, REMOVED, Toast.LENGTH_SHORT).show();
                    userCart.saveInBackground();
                } catch (ParseException | JSONException e) {
                    e.printStackTrace();
                }
                recommendedProducts.remove(recommendedProduct);
                notifyDataSetChanged();
            }
        }
    }

}
