package com.example.giftsapp.Controller.Fragment_Accounts;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.giftsapp.Adapter.BillAdapter;
import com.example.giftsapp.Controller.BillAdmin;
import com.example.giftsapp.Controller.LoginForm;
import com.example.giftsapp.Controller.SettingAccountForm;
import com.example.giftsapp.Model.Bill;
import com.example.giftsapp.Model.Products;
import com.example.giftsapp.Model.StatusBill;
import com.example.giftsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Date;
import java.util.Map;

public class BillStatusAdmin extends Fragment {

    ListView listViewBill;
    static ArrayList<Bill> billsArrayList;
    FirebaseAuth fAuth;
    FirebaseUser user;

    FirebaseFirestore fStore;
    BillAdapter billAdapter;
    static String statusRequest;
    String userID;
    private BillAdmin billAdmin;

    public BillStatusAdmin() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_bill_status_admin, container, false);

        assert getArguments() != null;
        statusRequest = getArguments().getString("status");

        RenameStatus();
        Init(view);

        if (user == null) {
            startActivity(new Intent(billAdmin, LoginForm.class));
            getActivity().finish();
        }
        return view;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof BillAdmin) {
            this.billAdmin = (BillAdmin) context;
        }
    }

    private void Init(View view){
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        user = fAuth.getCurrentUser();
        userID = user.getUid();
        listViewBill = view.findViewById(R.id.listViewBill);
        billsArrayList = new ArrayList<>();
        billAdapter = new BillAdapter(billAdmin, R.layout.list_bill_admin, billsArrayList);
        listViewBill.setAdapter(billAdapter);
        GetDataFromFireStore();
    }

    private void GetDataFromFireStore() {
        billsArrayList.clear();
        fStore.collection("Bill").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            if (!task.getResult().isEmpty()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    ArrayList<Map<String, Object>> billArrayList = new ArrayList<>();
                                    billArrayList.add(document.getData());
                                    ArrayList<Map<String, Object>> statusArrayList = (ArrayList<Map<String, Object>>) billArrayList.get(0).get("status");
                                    String statusBill = "";
//
                                    for (int i = 0; i < statusArrayList.size(); i++) {
                                        if (statusArrayList.get(i).get("isPresent").toString().equals("true")) {
                                            statusBill = statusArrayList.get(i).get("name").toString();
                                            break;
                                        }
                                    }

                                    if (statusBill.equals(statusRequest)) {
                                        ArrayList<StatusBill> statusBillArrayList = new ArrayList<>();
                                        for (int i = 0; i < statusArrayList.size(); i++) {
                                            Boolean isPresent = Boolean.valueOf(statusArrayList.get(i).get("isPresent").toString());
                                            String name = statusArrayList.get(i).get("name").toString();
                                            Timestamp ts = (Timestamp) statusArrayList.get(i).get("createAt");
                                            Date createAt = ts.toDate();
                                            StatusBill status = new StatusBill(isPresent, name, createAt);
                                            statusBillArrayList.add(status);
                                        }

                                        ArrayList<Map<String, Object>> productArrayList = (ArrayList<Map<String, Object>>) billArrayList.get(0).get("products");
                                        ArrayList<Products> productsArrayList = new ArrayList<>();
                                        for (int i = 0; i < productArrayList.size(); i++) {
                                            String id = productArrayList.get(i).get("productID").toString();
                                            String name = productArrayList.get(i).get("name").toString();
                                            String price = productArrayList.get(i).get("price").toString();
                                            String imgUrl = productArrayList.get(i).get("imageUrl").toString();
                                            Integer quantity = Integer.parseInt(productArrayList.get(i).get("quantity").toString());
                                            Products product = new Products(id, name, price, imgUrl, quantity);
                                            productsArrayList.add(product);
                                        }
                                        String id = document.getId();
                                        String addressID = billArrayList.get(0).get("addressID").toString();
                                        Timestamp ts = (Timestamp) billArrayList.get(0).get("createAt");
                                        Date createAt = ts.toDate();
                                        String paymentType = billArrayList.get(0).get("paymentType").toString();
                                        Integer quantity = Integer.parseInt(billArrayList.get(0).get("quantityProduct").toString());
                                        String totalPrice = billArrayList.get(0).get("totalPrice").toString();
                                        String uID = billArrayList.get(0).get("userID").toString();
                                        String feeShip = billArrayList.get(0).get("feeShip").toString();
                                        String message = billArrayList.get(0).get("message").toString();
                                        Bill bill = new Bill(id, addressID, createAt, paymentType, productsArrayList, statusBillArrayList, totalPrice, uID, quantity, feeShip, message);
                                        billsArrayList.add(bill);
                                        billAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        } else {
                            Log.d("TAG", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void RenameStatus() {
        switch (statusRequest) {
            case "WaitForConfirm":
                statusRequest = "Chờ xác nhận";
                break;
            case "WaitForTheGift":
                statusRequest = "Chờ lấy hàng";
                break;
            case "Delivering":
                statusRequest = "Đang giao hàng";
                break;
            case "Delivered":
                statusRequest = "Đã giao hàng";
                break;
            default:
                statusRequest = "Error";
                break;
        }
    }
}