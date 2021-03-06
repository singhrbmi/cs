package hacker.l.coldstore.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hacker.l.coldstore.R;
import hacker.l.coldstore.activity.MainActivity;
import hacker.l.coldstore.adapter.RackAdapter;
import hacker.l.coldstore.adapter.VarietyAdapter;
import hacker.l.coldstore.database.DbHelper;
import hacker.l.coldstore.model.MyPojo;
import hacker.l.coldstore.model.Result;
import hacker.l.coldstore.myalert.SweetAlertDialog;
import hacker.l.coldstore.utility.Contants;
import hacker.l.coldstore.utility.Utility;

public class RackFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    // TODO: Rename and change types and number of parameters
    public static RackFragment newInstance(String param1, String param2) {
        RackFragment fragment = new RackFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    View view;
    Context context;
    Button add;
    LinearLayoutManager linearLayoutManager;
    RecyclerView recyclerView;
    Boolean aBoolean = false;
    EditText edt_rack, edt_capacity;
    List<Result> resultList;
    AppCompatSpinner spinner;
    ProgressDialog pd;
    int rackId;
    String floor = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        context = getActivity();
        view = inflater.inflate(R.layout.fragment_rack, container, false);
        init();
        setFloorInSpinner();
        return view;
    }

    private void setFloorInSpinner() {
        if (Utility.isOnline(context)) {
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.show();
            pd.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.setContentView(new ProgressBar(context));
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Contants.SERVICE_BASE_URL + Contants.getAllFloor,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            pd.dismiss();
                            MyPojo myPojo = new Gson().fromJson(response, MyPojo.class);
                            List<Integer> spinnerList = new ArrayList<>();
                            for (Result result : myPojo.getResult()) {
                                spinnerList.addAll(Arrays.asList(result.getFloor()));
                            }
                            if (spinnerList != null) {
                                ArrayAdapter<Integer> integerArrayAdapter = new ArrayAdapter<Integer>(context, android.R.layout.simple_spinner_dropdown_item, spinnerList);
                                spinner.setAdapter(integerArrayAdapter);
                                integerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//                                if (spinner.getItemAtPosition(spinner.getSelectedItemPosition()) != null) {

//                                }
                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        floor = parent.getSelectedItem().toString();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            }
                        }
                    },
                    new Response.ErrorListener()

                    {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pd.dismiss();
                        }
                    })

            {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Sorry...")
                    .setContentText("You are Offline. Please check your Internet Connection.Thank You ")
                    .show();
        }
    }

    private void init() {
        MainActivity mainActivity = (MainActivity) context;
        mainActivity.setTitle("Rack");
        add = view.findViewById(R.id.btn_add);
        edt_rack = view.findViewById(R.id.edt_rack);
        edt_capacity = view.findViewById(R.id.edt_rackCapacity);
        recyclerView = view.findViewById(R.id.recycleView);
        spinner = view.findViewById(R.id.spinner);
        linearLayoutManager = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        resultList = new ArrayList<>();
        setRackAdapter();
        setAdapter();
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aBoolean) {
                    updateRackDataServer();
                } else {
                    addRackDataServer();
                }

            }
        });
    }

    private void addRackDataServer() {
        final String rack = edt_rack.getText().toString();
        final String capacity = edt_capacity.getText().toString();
        if (rack != null && !rack.equalsIgnoreCase("") && capacity != null && !capacity.equalsIgnoreCase("") && floor != null && !floor.equalsIgnoreCase("")) {
            DbHelper dbHelper = new DbHelper(context);
            Result result = dbHelper.getRackDataByRackFloor(rack, Integer.parseInt(floor));
            if (result != null) {
                new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Sorry...")
                        .setContentText("Already exists this rack. ")
                        .show();
            } else {
                if (Utility.isOnline(context)) {
                    pd = new ProgressDialog(context);
                    pd.setCancelable(false);
                    pd.show();
                    pd.getWindow()
                            .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                    pd.setContentView(new ProgressBar(context));
                    StringRequest stringRequest = new StringRequest(Request.Method.POST, Contants.SERVICE_BASE_URL + Contants.addRack,
                            new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    pd.dismiss();
                                    Toast.makeText(context, "Add Successfully", Toast.LENGTH_SHORT).show();
                                    setRackAdapter();
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    pd.dismiss();
                                }
                            }) {
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put("floor", floor);
                            params.put("rack", rack);
                            params.put("capacity", capacity);
                            return params;
                        }
                    };
                    RequestQueue requestQueue = Volley.newRequestQueue(context);
                    requestQueue.add(stringRequest);
                } else {
                    new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                            .setTitleText("Sorry...")
                            .setContentText("You are Offline. Please check your Internet Connection.Thank You ")
                            .show();

                }
            }
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.WARNING_TYPE)
                    .setTitleText("Sorry...")
                    .setContentText("Enter Values. ")
                    .show();
        }
    }

    private void updateRackDataServer() {
        final String rack = edt_rack.getText().toString();
        final String capacity = edt_capacity.getText().toString();
        if (Utility.isOnline(context)) {
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.show();
            pd.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.setContentView(new ProgressBar(context));
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Contants.SERVICE_BASE_URL + Contants.updateRack,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            pd.dismiss();
                            Toast.makeText(context, "Update Successfully", Toast.LENGTH_SHORT).show();
                            setRackAdapter();
                            add.setText("Add");
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pd.dismiss();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("rackId", String.valueOf(rackId));
                    params.put("floor", floor);
                    params.put("rack", rack);
                    params.put("capacity", capacity);
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Sorry...")
                    .setContentText("You are Offline. Please check your Internet Connection.Thank You ")
                    .show();
        }
    }

    public void updateRackData(Boolean aBoolean, String rack, String capacity, int rackId) {
        this.aBoolean = aBoolean;
        this.rackId = rackId;
        edt_rack.setText(rack);
        edt_capacity.setText(capacity);
        edt_rack.setSelection(edt_rack.length());
        edt_capacity.setSelection(edt_capacity.length());
        add.setText("Update");
    }

    public void setRackAdapter() {
        if (Utility.isOnline(context)) {
            pd = new ProgressDialog(context);
            pd.setCancelable(false);
            pd.show();
            pd.getWindow()
                    .setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            pd.setContentView(new ProgressBar(context));
            StringRequest stringRequest = new StringRequest(Request.Method.POST, Contants.SERVICE_BASE_URL + Contants.getAllRack,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            pd.dismiss();
                            MyPojo myPojo = new Gson().fromJson(response, MyPojo.class);
                            for (Result result : myPojo.getResult()) {
                                new DbHelper(context).upsertRackData(result);
                            }
                            setAdapter();
//                            resultList.addAll(Arrays.asList(myPojo.getResult()));
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pd.dismiss();
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            requestQueue.add(stringRequest);
        } else {
            new SweetAlertDialog(context, SweetAlertDialog.ERROR_TYPE)
                    .setTitleText("Sorry...")
                    .setContentText("You are Offline. Please check your Internet Connection.Thank You ")
                    .show();
        }
    }

    private void setAdapter() {
        DbHelper dbHelper = new DbHelper(context);
        resultList = dbHelper.getAllRackData();
        if (resultList != null && resultList.size() != 0) {
            Collections.reverse(resultList);
            RackAdapter rackAdapter = new RackAdapter(context, resultList, RackFragment.this);
            recyclerView.setAdapter(rackAdapter);
            edt_rack.setText("");
            edt_capacity.setText("");
        }
    }
}
