package com.example.empty;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddItemActivity extends AppCompatActivity {

    TodoItemDB db;
    TextView editItemTitle;
    TextView editItemDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        db = TodoItemDB.getInstance(getApplicationContext());
        editItemTitle = findViewById(R.id.editItemTitle);
        editItemDetail = findViewById(R.id.editItemDetail);
        if (itemCache.state == itemCache.STATE.BEFORE_EDIT) {
            TodoItem item = itemCache.get();
            editItemTitle.setText(item.title);
            editItemDetail.setText(item.detail);
        }

        Toolbar myToolbar = findViewById(R.id.addItemToolBar);
        myToolbar.setTitle("mew");
        myToolbar.inflateMenu(R.menu.menu_add_item);
        myToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // for more items, use 'switch' instead
                submit();
                return true;
            }
        });

        Button deleteBtn = findViewById(R.id.btnDeleteItem);
        if (itemCache.state == itemCache.STATE.BEFORE_ADD) {
            deleteBtn.setVisibility(View.GONE);
        }
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                itemCache.state = itemCache.STATE.TO_BE_DELETED;
                AddItemActivity.this.finish();
            }
        });
    }

    void submit() {
        String itemTitle = editItemTitle.getText().toString();
        String itemDetail = editItemDetail.getText().toString();
        // TODO: check if input is legal
        switch (itemCache.state) {
            case BEFORE_ADD:
                itemCache.set(new TodoItem(itemTitle, itemDetail));
                itemCache.state = itemCache.STATE.AFTER_ADD;
                AddItemActivity.this.finish();
                break;
            case BEFORE_EDIT:
                TodoItem item = itemCache.get();
                item.title = itemTitle;
                item.detail = itemDetail;
                itemCache.state = itemCache.STATE.AFTER_EDIT;
                AddItemActivity.this.finish();
                break;
        }
    }
}
