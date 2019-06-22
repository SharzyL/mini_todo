package com.example.empty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private final ArrayList<TodoItem> items = new ArrayList<>();
    private TodoItemDB db;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar myToolbar = findViewById(R.id.MainToolbar);
        myToolbar.inflateMenu(R.menu.menu_main);

        FloatingActionButton fab = findViewById(R.id.AddItem);
        fab.setOnClickListener(new FloatingActionButton.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                itemCache.state = itemCache.STATE.BEFORE_ADD;
                startActivity(intent);
            }
        });

        db = TodoItemDB.getInstance(getApplicationContext());
        recyclerView = findViewById(R.id.ItemList);
        recyclerView.setHasFixedSize(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);
        recyclerView.setLayoutManager(layoutManager);
        TodoItemAdapter todoItemAdapter = new TodoItemAdapter(items);
        todoItemAdapter.setOnItemClickListener(new TodoItemAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
                itemCache.state = itemCache.STATE.BEFORE_EDIT;
                itemCache.set(items.get(position));
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(View view, int position) {
//                Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
//                itemCache.state = itemCache.STATE.BEFORE_EDIT;
//                itemCache.set(items.get(position));
//                startActivity(intent);
            }
        });
        recyclerView.setAdapter(todoItemAdapter);

        new getItems(this).execute();
    }

    @Override
    protected void onResume() {
        super.onResume();
        switch (itemCache.state) {
            case AFTER_ADD:
                new insertItems(this).execute();
                break;
            case AFTER_EDIT:
                new updateItems(this, itemCache.get()).execute();
                break;
            case TO_BE_DELETED:
                new deleteItems(this, itemCache.get()).execute();
                break;
        }
    }

    abstract static class dbOperation extends AsyncTask<Void, Void, Void>{

        private final WeakReference<MainActivity> weakActivity;

        dbOperation(MainActivity activity) {
            this.weakActivity = new WeakReference<>(activity);
        }

        MainActivity getRef() {
            MainActivity ref = weakActivity.get();
            if (ref == null || ref.isDestroyed() || ref.isFinishing()) {
                return null;
            } else {
                return ref;
            }
        }
        abstract void getData(MainActivity activity);

        abstract void renderData(MainActivity activity);

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity ref = getRef();
            if (ref == null) return null;
            getData(ref);
            return null;
        }
        @Override
        protected void onPostExecute(Void voids) {
            MainActivity ref = getRef();
            if (ref == null) return;
            renderData(ref);
        }

    }

    static class getItems extends dbOperation {

        getItems(MainActivity activity) {
            super(activity);
        }

        @Override
        void getData(MainActivity activity) {
            activity.items.addAll(Arrays.asList(activity.db.todoItemDao().getAllItems()));
        }

        @Override
        void renderData(MainActivity activity) {
            Objects.requireNonNull(activity.recyclerView.getAdapter()).notifyDataSetChanged();
        }
    }

    static class insertItems extends dbOperation {

        insertItems(MainActivity activity) {
            super(activity);
        }

        @Override
        void getData(MainActivity activity) {
            TodoItem item = itemCache.get();
            activity.items.add(item);
            activity.db.todoItemDao().insertItems(item);
            itemCache.clear();
        }

        @Override
        void renderData(MainActivity activity) {
            Objects.requireNonNull(activity.recyclerView.getAdapter()).notifyItemInserted(activity.items.size());
        }
    }

    static class updateItems extends dbOperation {

        TodoItem item;

        updateItems(MainActivity activity, TodoItem item) {
            super(activity);
            this.item = item;
        }

        @Override
        void getData(MainActivity activity) {
            activity.db.todoItemDao().updateItems(item);
        }

        @Override
        void renderData(MainActivity activity) {
            int pos = activity.items.indexOf(item);
            Objects.requireNonNull(activity.recyclerView.getAdapter()).notifyItemChanged(pos);
        }
    }

    static class deleteItems extends dbOperation {
        TodoItem item;
        deleteItems(MainActivity activity, TodoItem item) {
            super(activity);
            this.item = item;
        }

        @Override
        void getData(MainActivity activity) {
            activity.db.todoItemDao().deleteItems(item);
        }

        @Override
        void renderData(MainActivity activity) {
            int pos = activity.items.indexOf(item);
            activity.items.remove(pos);
            Objects.requireNonNull(activity.recyclerView.getAdapter()).notifyItemRemoved(pos);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        return super.onOptionsItemSelected(item);
    }
}

