package com.example.giftsapp.Controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giftsapp.Adapter.CartAdapter;
import com.example.giftsapp.Adapter.ProductImageAdapter;
import com.example.giftsapp.Controller.DeliveryActivity;
import com.example.giftsapp.Controller.MainActivity;
import com.example.giftsapp.Model.CartItemModel;
import com.example.giftsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.errorprone.annotations.Var;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.example.giftsapp.Controller.LoginForm.currentUser;

public class MyCartFragment extends Fragment {

    private MainActivity mainActivity;
    private FirebaseFirestore firebaseFirestore;
    public MyCartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof MainActivity) {
            this.mainActivity = (MainActivity) context;
        }
    }

    private RecyclerView cartItemRecyclerView;
    private Button continueBtn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_cart, container, false);

        // ánh xạ
        cartItemRecyclerView = view.findViewById(R.id.cart_item_recyclerview);
        continueBtn = view.findViewById(R.id.cart_continue_btn);

        firebaseFirestore = FirebaseFirestore.getInstance();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(linearLayoutManager.VERTICAL);
        cartItemRecyclerView.setLayoutManager(linearLayoutManager);


        List<CartItemModel> cartItemModelList = new ArrayList<>();
        /*cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",1,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",0,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",1,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(0,R.drawable.teddy,"Gấu bông",0,"200000.VND","220000.VND",1,111));
        cartItemModelList.add(new CartItemModel(1,4,"800000.VND","10000.VND","80000.VND","814000.VND"));*/

        CartAdapter cartAdapter = new CartAdapter(cartItemModelList);
        cartItemRecyclerView.setAdapter(cartAdapter);

        // lấy thông tin các sản phẩm trong cart của USer
        Log.i("User",currentUser);
        try {

                firebaseFirestore.collection("Carts").document(currentUser).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if(documentSnapshot.exists()) {
                                ArrayList<Map<String, Object>> productArray = (ArrayList<Map<String, Object>>) documentSnapshot.getData().get("ListProducts");
                                for (int i = 0; i < productArray.size(); i++) {
                                    String ProID = productArray.get(i).get("ProductID").toString();
                                    firebaseFirestore.collection("Products").document(ProID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> taskPro) {
                                            if (taskPro.isSuccessful()) {
                                                DocumentSnapshot documentSnapshotPro = taskPro.getResult();
                                                Long price = Long.parseLong(documentSnapshotPro.get("price").toString());
                                                Long Cuttedprice = price + 20000;
                                                cartItemModelList.add(new CartItemModel(0
                                                        ,documentSnapshotPro.get("imageUrl").toString()
                                                        , documentSnapshotPro.get("name").toString(), 1
                                                        , documentSnapshotPro.get("price").toString(), Cuttedprice.toString(), 1, 111));
                                                cartAdapter.notifyDataSetChanged();
                                            } else {
                                                String error = taskPro.getException().getMessage();
                                                Toast.makeText(mainActivity, error, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }
                            }
                            else{
                                Toast.makeText(mainActivity, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(mainActivity, "Không có giỏ hàng", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


        }catch (Exception e)
        {
            Toast.makeText(mainActivity, "Chưa có giỏ hàng", Toast.LENGTH_SHORT).show();
        }


        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent deliveryIntent = new Intent(mainActivity, DeliveryActivity.class);
                getContext().startActivity(deliveryIntent);
                getActivity().finish();
            }
        });

        return view;
    }
}