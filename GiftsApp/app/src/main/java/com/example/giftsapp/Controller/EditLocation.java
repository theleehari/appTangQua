package com.example.giftsapp.Controller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.giftsapp.Model.Address;
import com.example.giftsapp.Model.RandomId;
import com.example.giftsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class EditLocation extends AppCompatActivity {

    Address address;
    EditText edtName, edtPhone, edtAddAddress;
    TextView txtProvince, txtDistrict, txtVillage;
    Switch switchDefaultAddress;
    Button btnSave, btnDelete;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    FirebaseUser user;
    String       userID, name = "", phone = "", province = "", district = "", village = "", detailAddress = "", addressID = "";
    Integer      provinceID, districtID;
    Boolean      isDefault = true, onlyOneAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_location);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Sửa địa chỉ");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2C4CC3")));

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), LoginForm.class));
            finish();
        }

        Init();

        txtVillage.setEnabled(false);
        txtDistrict.setEnabled(false);

        txtProvince.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ProvinceForm.class);
                startActivityForResult(intent, 1);
            }
        });

        txtDistrict.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), DistrictForm.class);
                intent.putExtra("EXTRA_DOCUMENT_PROVINCE_ID", provinceID);
                startActivityForResult(intent, 2);
            }
        });

        txtVillage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), VillageForm.class);
                intent.putExtra("EXTRA_DOCUMENT_DISTRICT_ID", districtID);
                startActivityForResult(intent, 3);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GetDataFromUI();
                if (CheckRequired()) {
                    if (!address.isDefault() && isDefault) {
                        FindDefaultAddress();
                    }
                    DeleteAddress();
                    UpdateAddress();
                    finish();
                }
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (address.isDefault()) {
                    Toast.makeText(getApplicationContext(), "Không thể xóa địa chỉ mặc định", Toast.LENGTH_SHORT).show();
                    return;
                }
                DeleteAddress();
                finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(getApplicationContext(), SettingAccountForm.class);
            intent.putExtra("EXTRA_DOCUMENT_OPEN", "Address");
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case 1:
                    province = data.getStringExtra("EXTRA_DOCUMENT_PROVINCE");
                    provinceID = data.getIntExtra("EXTRA_DOCUMENT_PROVINCE_ID", 0);
                    txtProvince.setText(province);
                    txtProvince.setError(null);
                    txtDistrict.setText(null);
                    txtVillage.setText(null);
                    txtDistrict.setEnabled(true);
                    districtID = 0;
                    break;
                case 2:
                    district = data.getStringExtra("EXTRA_DOCUMENT_DISTRICT");
                    districtID = data.getIntExtra("EXTRA_DOCUMENT_DISTRICT_ID", 0);
                    txtDistrict.setText(district);
                    txtDistrict.setError(null);
                    txtVillage.setText(null);
                    txtVillage.setEnabled(true);
                    break;
                case 3:
                    village = data.getStringExtra("EXTRA_DOCUMENT_VILLAGE");
                    txtVillage.setText(village);
                    txtVillage.setError(null);
                    break;
                default:
                    break;
            }
        }
    }

    private void Init() {
        address = getIntent().getParcelableExtra("PARCEL_ADDRESS");
        onlyOneAddress          = getIntent().getBooleanExtra("ONLY_ONE_ADDRESS", false);
        edtName                 = findViewById(R.id.edtName);
        edtPhone                = findViewById(R.id.edtPhone);
        txtProvince             = findViewById(R.id.txtProvince);
        txtDistrict             = findViewById(R.id.txtDistrict);
        txtVillage              = findViewById(R.id.txtVillage);
        edtAddAddress           = findViewById(R.id.edtAddAddress);
        switchDefaultAddress    = findViewById(R.id.switchDefaultAddress);
        btnSave                 = findViewById(R.id.btnSave);
        btnDelete               = findViewById(R.id.btnDelete);
        fAuth                   = FirebaseAuth.getInstance();
        fStore                  = FirebaseFirestore.getInstance();
        user                    = fAuth.getCurrentUser();
        userID                  = user.getUid();
        LoadData();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void LoadData() {
        edtName.setText(address.getName());
        edtPhone.setText(address.getPhone());
        txtProvince.setText(address.getProvince());
        txtDistrict.setText(address.getDistrict());
        txtVillage.setText(address.getVillage());
        edtAddAddress.setText(address.getDetailAddress());
        if (!address.isDefault()) {
            switchDefaultAddress.setChecked(false);
        } else {
            switchDefaultAddress.setEnabled(false);
        }
    }

    private void UpdateAddress() {
        fStore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    HashMap<String, Object> addressUpdated = new HashMap<String, Object>();
                    addressUpdated.put("ID", addressID);
                    addressUpdated.put("isDefault", isDefault);
                    addressUpdated.put("name", name);
                    addressUpdated.put("phone", phone);
                    addressUpdated.put("province", province);
                    addressUpdated.put("district", district);
                    addressUpdated.put("village", village);
                    addressUpdated.put("detailAddress", detailAddress);

                    fStore.collection("Users").document(userID).update("address", FieldValue.arrayUnion(addressUpdated));
                } else {
                    Log.d("TAG", "DocumentSnapshot Fail" + task.getException());
                }
            }
        });
    }

    private void GetDataFromUI() {
        addressID = address.getID();
        isDefault = switchDefaultAddress.isChecked();
        name = edtName.getText().toString().trim();
        phone = edtPhone.getText().toString().trim();
        province = txtProvince.getText().toString().trim();
        district = txtDistrict.getText().toString().trim();
        village = txtVillage.getText().toString().trim();
        detailAddress = edtAddAddress.getText().toString().trim();
    }

    private void DeleteAddress() {
        fStore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getData().get("address") != null) {
                        ArrayList<Map<String, Object>> addressArray = (ArrayList<Map<String, Object>>) document.getData().get("address");
                        for (int i = 0; i < addressArray.size(); i++) {
                            if (addressArray.get(i).get("ID").toString().equals(address.getID())) {
                                HashMap<String, Object> addressDeleted = new HashMap<String, Object>();
                                addressDeleted.put("ID", address.getID());
                                addressDeleted.put("isDefault", address.isDefault());
                                addressDeleted.put("name", address.getName());
                                addressDeleted.put("phone", address.getPhone());
                                addressDeleted.put("province", address.getProvince());
                                addressDeleted.put("district", address.getDistrict());
                                addressDeleted.put("village", address.getVillage());
                                addressDeleted.put("detailAddress", address.getDetailAddress());

                                fStore.collection("Users").document(userID).update("address", FieldValue.arrayRemove(addressDeleted));
                                break;
                            }
                        }
                    }
                }else {
                    Log.d("TAG", "DocumentSnapshot Fail" + task.getException());
                }
            }
        });
    }

    private boolean CheckRequired() {
        if (name.trim().equals("")) {
            edtName.setError("Bạn chưa nhập tên");
            return false;
        }
        edtName.setError(null);

        if (phone.trim().equals("")) {
            edtPhone.setError("Bạn chưa nhập số điện thoại");
            return false;
        }
        edtPhone.setError(null);

        if (province.trim().equals("")) {
            txtProvince.setError("Bạn chưa chọn tỉnh");
            return false;
        }
        txtProvince.setError(null);

        if (district.trim().equals("")) {
            txtDistrict.setError("Bạn chưa chọn huyện");
            return false;
        }
        txtDistrict.setError(null);

        if (village.trim().equals("")) {
            txtVillage.setError("Bạn chưa chọn xã");
            return false;
        }
        txtVillage.setError(null);

        if (detailAddress.trim().equals("")) {
            edtAddAddress.setError("Bạn chưa nhập địa chỉ cụ thể");
            return false;
        }
        edtAddAddress.setError(null);

        return true;
    }

    private void SetNotDefault(String addressID, String name, String phone, String province, String district, String village, String detailAddress) {
        HashMap<String, Object> disableDefault = new HashMap<String, Object>();
        disableDefault.put("ID", addressID);
        disableDefault.put("isDefault", false);
        disableDefault.put("name", name);
        disableDefault.put("phone", phone);
        disableDefault.put("province", province);
        disableDefault.put("district", district);
        disableDefault.put("village", village);
        disableDefault.put("detailAddress", detailAddress);

        fStore.collection("Users").document(userID).update("address", FieldValue.arrayUnion(disableDefault));
    }

    private void FindDefaultAddress() {
        fStore.collection("Users").document(userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.getData().get("address") != null) {
                        ArrayList<Map<String, Object>> addressArray = (ArrayList<Map<String, Object>>) document.getData().get("address");
                        for (int i = 0; i < addressArray.size(); i++) {
                            if (addressArray.get(i).get("isDefault").toString().equals("true")) {
                                HashMap<String, Object> defaultAddress = new HashMap<String, Object>();
                                String addressID = addressArray.get(i).get("ID").toString();
                                String name = addressArray.get(i).get("name").toString().trim();
                                String phone = addressArray.get(i).get("phone").toString().trim();
                                String province = addressArray.get(i).get("province").toString().trim();
                                String district = addressArray.get(i).get("district").toString().trim();
                                String village = addressArray.get(i).get("village").toString().trim();
                                String detailAddress = addressArray.get(i).get("detailAddress").toString().trim();
                                defaultAddress.put("ID", addressID);
                                defaultAddress.put("isDefault", true);
                                defaultAddress.put("name", name);
                                defaultAddress.put("phone", phone);
                                defaultAddress.put("province", province);
                                defaultAddress.put("district", district);
                                defaultAddress.put("village", village);
                                defaultAddress.put("detailAddress", detailAddress);

                                fStore.collection("Users").document(userID).update("address", FieldValue.arrayRemove(defaultAddress));
                                SetNotDefault(addressID, name, phone, province, district, village, detailAddress);
                            }
                        }
                    }
                }else {
                    Log.d("TAG", "DocumentSnapshot Fail" + task.getException());
                }
            }
        });
    }
}