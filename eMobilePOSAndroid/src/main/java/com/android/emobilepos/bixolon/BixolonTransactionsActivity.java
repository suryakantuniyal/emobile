package com.android.emobilepos.bixolon;

import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.android.emobilepos.R;
import com.android.emobilepos.adapters.BixolonTransactionAdapter;
import com.android.emobilepos.models.realms.BixolonTransaction;
import com.android.support.fragmentactivity.BaseFragmentActivityActionBar;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;

public class BixolonTransactionsActivity extends BaseFragmentActivityActionBar{

    private RecyclerView list;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        realm = Realm.getDefaultInstance();
        setContentView(R.layout.activity_bixolon_transactions);
        list = (RecyclerView) findViewById(R.id.bixolonTransactionlistView);
        OrderedRealmCollection<BixolonTransaction> collection = realm.where(BixolonTransaction.class).findAll();
        BixolonTransactionAdapter adapter = new BixolonTransactionAdapter(this, collection, true);
        list.setAdapter(adapter);
        list.setLayoutManager(new LinearLayoutManager(this));
        list.setHasFixedSize(true);
        list.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        findViewById(R.id.noTransFounttextView).setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        list.setAdapter(null);
        realm.close();
    }

}
