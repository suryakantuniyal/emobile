package com.android.emobilepos.security;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.dao.ClerkDAO;
import com.android.emobilepos.R;
import com.android.emobilepos.models.realms.Clerk;

import java.util.List;

public class ClerkManagementActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clerk_management);
        List<Clerk> clerks = ClerkDAO.getAll();
        ClerksCursorAdapter adapter = new ClerksCursorAdapter(clerks);
        RecyclerView listView = (RecyclerView) findViewById(R.id.clerksListView);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        listView.setLayoutManager(mLayoutManager);
        listView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        listView.setItemAnimator(new DefaultItemAnimator());
        listView.setAdapter(adapter);
    }

    private class ClerksCursorAdapter extends RecyclerView.Adapter<ClerksCursorAdapter.ViewHolder> {
        private List<Clerk> clerks;

        public ClerksCursorAdapter(List<Clerk> clerks) {
            this.clerks = clerks;
        }

        @Override
        public ClerksCursorAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.custselec_lvadapter, parent, false);
            return new ClerksCursorAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ClerksCursorAdapter.ViewHolder holder, int position) {
            Clerk clerk = clerks.get(position);
            holder.clerk = clerk;
            holder.title.setText(clerk.getEmpName());
            holder.subtitle.setText(String.format("%s %s", getString(R.string.pay_details_clerk_id), String.valueOf(clerk.getEmpId())));
        }

        @Override
        public int getItemCount() {
            return clerks.size();
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView title, subtitle;
            ImageView imageView;
            Clerk clerk;

            public ViewHolder(View v) {
                super(v);
                title = (TextView) v.findViewById(R.id.custSelecName);
                subtitle = (TextView) v.findViewById(R.id.custSelecID);
                imageView = (ImageView) v.findViewById(R.id.custSelecIcon);
                imageView.setVisibility(View.GONE);
                v.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(ClerkManagementActivity.this, ClerkManagementDetailActivity.class);
                        intent.putExtra("clerkId", clerk.getEmpId());
                        startActivity(intent);
                    }
                });
            }
        }
    }
}
