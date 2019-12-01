package chtgupta.selectivesilence.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import chtgupta.selectivesilence.R;
import chtgupta.selectivesilence.data.Constants;
import chtgupta.selectivesilence.data.Contact;
import chtgupta.selectivesilence.utils.AppUtils;

public class ContactsActivity extends AppCompatActivity {

    private List<Contact> whitelist;
    private WhitelistAdapter adapter;
    private SharedPreferences db;

    private View nullText;
    private RecyclerView recyclerView;

    private static final int RC_PICK_CONTACT = 8008;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        whitelist = new ArrayList<>();
        adapter = new WhitelistAdapter(whitelist);
        db = getSharedPreferences(Constants.DB_NAME_CONTACTS, Context.MODE_PRIVATE);

        nullText = findViewById(R.id.nullText);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fab = findViewById(R.id.fab);

        setupActionBar();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadWhitelist();

        fab.setOnClickListener(v -> {
            Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
            startActivityForResult(contactPickerIntent, RC_PICK_CONTACT);
        });

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.title_whitelist);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void loadWhitelist() {

        whitelist.clear();

        Map<String, ?> map = db.getAll();
        for (Map.Entry entry : map.entrySet()) {
            whitelist.add(new Contact(String.valueOf(entry.getValue()), String.valueOf(entry.getKey())));
        }

        Collections.sort(whitelist, new ContactComparator());
        adapter.notifyDataSetChanged();

        if (whitelist.size() == 0) showNull();
        else showRecycler();

    }

    private void showNull() {
        nullText.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void showRecycler() {
        nullText.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PICK_CONTACT && resultCode == Activity.RESULT_OK && data != null) {

            Cursor cursor;
            try {

                Uri uri = data.getData();
                cursor = getContentResolver().query(uri, null, null, null, null);
                int phoneIndex;
                int nameIndex;
                if (cursor != null) {
                    cursor.moveToFirst();
                    phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                    nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);

                    String contactName = cursor.getString(nameIndex);
                    String phoneNumber = cursor.getString(phoneIndex);
                    cursor.close();

                    if (contactName == null || phoneNumber == null) return;

                    SharedPreferences.Editor editor = db.edit();
                    editor.putString(AppUtils.processPhoneNumber(phoneNumber), contactName);
                    editor.apply();
                    loadWhitelist();

                } else {
                    Toast.makeText(this, R.string.error_fetching_contact, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, R.string.error_fetching_contact, Toast.LENGTH_SHORT).show();
            }

        }
    }

    class WhitelistAdapter extends RecyclerView.Adapter<WhitelistAdapter.ViewHolder> {

        private List<Contact> whitelist;

        public WhitelistAdapter(List<Contact> whitelist) {
            this.whitelist = whitelist;
        }

        class ViewHolder extends RecyclerView.ViewHolder {

            TextView name;
            TextView number;
            View clear;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                name = itemView.findViewById(R.id.name);
                number = itemView.findViewById(R.id.number);
                clear = itemView.findViewById(R.id.clear);
            }
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_item_whitelist, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Contact contact = whitelist.get(position);

            holder.name.setText(contact.getName());
            holder.number.setText(contact.getNumber());
            holder.clear.setOnClickListener(v -> {
                SharedPreferences.Editor editor = db.edit();
                editor.remove(contact.getNumber());
                editor.apply();
                loadWhitelist();
            });
        }

        @Override
        public int getItemCount() {
            return whitelist.size();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    class ContactComparator implements Comparator<Contact> {
        @Override
        public int compare(Contact first, Contact second) {
            return first.getName().compareTo(second.getName());
        }
    }

    @Override
    public void finish() {
        Intent intent = new Intent();
        intent.putExtra(Constants.INTENT_KEY_WHITELIST_SIZE, whitelist.size());
        setResult(RESULT_OK, intent);
        super.finish();
    }
}
