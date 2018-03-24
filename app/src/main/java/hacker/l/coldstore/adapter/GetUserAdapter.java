package hacker.l.coldstore.adapter;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hacker.l.coldstore.R;
import hacker.l.coldstore.database.DbHelper;
import hacker.l.coldstore.fragments.GetAllUsersFragment;
import hacker.l.coldstore.fragments.RackFragment;
import hacker.l.coldstore.model.Result;
import hacker.l.coldstore.utility.Contants;
import hacker.l.coldstore.utility.FontManager;
import hacker.l.coldstore.utility.Utility;

/**
 * Created by lalitsingh on 23/03/18.
 */

public class GetUserAdapter extends RecyclerView.Adapter<GetUserAdapter.MyViewHolder> {
    private Typeface materialdesignicons_font, ProximaNovaRegular;
    private Context mContext;
    private List<Result> userList, FilteruserList;
    GetAllUsersFragment fragment;
    ProgressDialog pd;

    public GetUserAdapter(Context mContext, List<Result> userList, GetAllUsersFragment fragment) {
        this.mContext = mContext;
        this.userList = userList;
        this.FilteruserList = userList;
        this.fragment = fragment;
        this.ProximaNovaRegular = FontManager.getFontTypeface(mContext, "fonts/ProximaNova-Regular.otf");
        this.materialdesignicons_font = FontManager.getFontTypefaceMaterialDesignIcons(mContext, "fonts/materialdesignicons-webfont.otf");
    }

    @Override
    public GetUserAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_get_users, parent, false);

        return new GetUserAdapter.MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(GetUserAdapter.MyViewHolder holder, final int position) {
        if (position % 2 == 1) {
            holder.linearLayout.setBackgroundColor(Color.parseColor("#ffffff"));
        } else {
            holder.linearLayout.setBackgroundColor(Color.parseColor("#33A5DC86"));
        }

        holder.tv_name.setText(FilteruserList.get(position).getEmpName());
        holder.tv_fname.setText(FilteruserList.get(position).getEmpFName());
        holder.tv_type.setText(FilteruserList.get(position).getEmpType());
        holder.tv_phone.setText(FilteruserList.get(position).getEmpPhone());
        holder.tv_gender.setText(FilteruserList.get(position).getEmpGender());
        holder.tv_qly.setText(FilteruserList.get(position).getEmpQly());
        holder.tv_dob.setText(FilteruserList.get(position).getEmpdob());
        holder.tv_salery.setText(String.valueOf(FilteruserList.get(position).getEmpsalary()));
        holder.tv_address.setText(FilteruserList.get(position).getEmpAddress());
        holder.tv_doj.setText(FilteruserList.get(position).getEmpjoindate());
        Picasso.with(mContext).load(FilteruserList.get(position).getEmpProfile()).into(holder.image_profile);

        holder.tv_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                fragment.updateRackData(true, FilteruserList.get(position).getRack(), FilteruserList.get(position).getCapacity(), FilteruserList.get(position).getRackId());
//                fragment.setRackAdapter();
            }
        });
        holder.tv_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteData(position);
            }
        });
    }

    private void deleteData(final int position) {
        if (Utility.isOnline(mContext)) {
            pd = new ProgressDialog(mContext);
            pd.setMessage("Deleting wait......");
            pd.show();
            pd.setCancelable(false);
            DbHelper dbHelper = new DbHelper(mContext);
//            final Result result = dbHelper.getUserData();
//            if (result != null) {
                StringRequest stringRequest = new StringRequest(Request.Method.POST, Contants.SERVICE_BASE_URL + Contants.deleteflor,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                pd.dismiss();
                                FilteruserList.remove(position);
//                                fragment.setRackAdapter();
                                Toast.makeText(mContext, "Delete Successully", Toast.LENGTH_LONG).show();
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
                        params.put("floorId", String.valueOf(FilteruserList.get(position).getFloorId()));
                        return params;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(mContext);
                requestQueue.add(stringRequest);
//            }
        } else {
            Toast.makeText(mContext, "Enable Internet Connection.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return FilteruserList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_type, tv_fname, tv_phone, tv_gender, tv_dob, tv_qly, tv_doj, tv_address, tv_salery, tv_edit, tv_delete;
        LinearLayout linearLayout;
        ImageView image_profile;

        public MyViewHolder(View itemView) {
            super(itemView);
            tv_type = (TextView) itemView.findViewById(R.id.tv_type);
            tv_name = (TextView) itemView.findViewById(R.id.tv_name);
            tv_fname = (TextView) itemView.findViewById(R.id.tv_fname);
            tv_phone = (TextView) itemView.findViewById(R.id.tv_phone);
            tv_gender = (TextView) itemView.findViewById(R.id.tv_gender);
            tv_qly = (TextView) itemView.findViewById(R.id.tv_qly);
            tv_dob = (TextView) itemView.findViewById(R.id.tv_dob);
            tv_salery = (TextView) itemView.findViewById(R.id.tv_salery);
            tv_address = (TextView) itemView.findViewById(R.id.tv_address);
            tv_doj = (TextView) itemView.findViewById(R.id.tv_doj);
            tv_edit = (TextView) itemView.findViewById(R.id.tv_edit);
            image_profile = (ImageView) itemView.findViewById(R.id.image_profile);
            tv_delete = (TextView) itemView.findViewById(R.id.tv_delete);
            linearLayout = (LinearLayout) itemView.findViewById(R.id.linearLayout);
            tv_name.setTypeface(ProximaNovaRegular);
            tv_fname.setTypeface(ProximaNovaRegular);
            tv_phone.setTypeface(ProximaNovaRegular);
            tv_gender.setTypeface(ProximaNovaRegular);
            tv_qly.setTypeface(ProximaNovaRegular);
            tv_dob.setTypeface(ProximaNovaRegular);
            tv_salery.setTypeface(ProximaNovaRegular);
            tv_address.setTypeface(ProximaNovaRegular);
            tv_doj.setTypeface(ProximaNovaRegular);
//            tv_edit.setTypeface(materialdesignicons_font);
//            tv_delete.setTypeface(materialdesignicons_font);
//            tv_edit.setText(Html.fromHtml("&#xf64f;"));
//            tv_delete.setText(Html.fromHtml("&#xf5ad;"));
        }
    }
}
