package com.example.empty;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.Update;

import java.util.ArrayList;
import java.util.List;


@Entity
class TodoItem {
    @PrimaryKey(autoGenerate = true)
    int id;

    String title;
    String detail;
    boolean completed;

    TodoItem(String title, String detail) {
        this.title = title;
        this.detail = detail;
        this.completed = false;
    }
}

@Dao
interface TodoItemDao {
    @Insert
    void insertItems(TodoItem... item);

    @Update
    void updateItems(TodoItem... item);

    @Delete
    void deleteItems(TodoItem... item);

    @Query("SELECT * FROM TodoItem")
    List<TodoItem> getAllItems();

    @Query("SELECT * FROM TodoItem WHERE completed = 1")
    List<TodoItem> getCompletedItems();

    @Query("SELECT * FROM TodoItem WHERE completed = 0")
    List<TodoItem> getUncompletedItems();
}

@Database(entities = {TodoItem.class}, version = 1)
abstract class TodoItemDB extends RoomDatabase {
    abstract TodoItemDao todoItemDao();

    private static TodoItemDB INSTANCE;
    private static final Object sLock = new Object();

    static TodoItemDB getInstance(Context context) {
        synchronized (sLock) {
            if (INSTANCE == null) {
                INSTANCE =
                        Room.databaseBuilder(context.getApplicationContext(), TodoItemDB.class, "TodoItemDB")
                            .build();
            }
        }
        return INSTANCE;
    }
}

class TodoItemAdapter extends RecyclerView.Adapter<TodoItemAdapter.TodoItemHolder> {
    private ArrayList<TodoItem> todoItems;

    static class TodoItemHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CheckBox todoTitle;
        TextView todoDetail;
        TodoItemHolder(@NonNull View itemView) {
            super(itemView);
            todoTitle = itemView.findViewById(R.id.todo_title);
            todoDetail = itemView.findViewById(R.id.todo_detail);
        }
    }

    TodoItemAdapter(ArrayList<TodoItem> myDataset) {
        todoItems = myDataset;
    }

    public interface onItemClickListener {
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
        void onItemChecked(View view, int position, boolean b);
    }

    private onItemClickListener onItemClickListener;

    void setOnItemClickListener(TodoItemAdapter.onItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public TodoItemHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                             int viewType) {
        // create a new view
        return new TodoItemHolder(
                LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_item, parent, false)
        );
    }

    @Override
    public void onBindViewHolder(@NonNull final TodoItemHolder holder, int position) {
        TodoItem todoItem = todoItems.get(position);
        holder.todoTitle.setChecked(todoItem.completed);
        holder.todoTitle.setText(todoItem.title);
        holder.todoDetail.setText(todoItem.detail);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(holder.itemView, holder.getLayoutPosition());
                }
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemLongClick(holder.itemView, holder.getLayoutPosition());
                }
                return true;
            }
        });

        holder.todoTitle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemChecked(holder.itemView, holder.getLayoutPosition(), b);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return todoItems.size();
    }
}

class itemCache {
    enum STATE {
        EMPTY,
        BEFORE_ADD,
        AFTER_ADD,
        BEFORE_EDIT,
        AFTER_EDIT,
        TO_BE_DELETED,
    }
    static STATE state = STATE.EMPTY;
    private static TodoItem currentItem;
    static void set(TodoItem item) {
        currentItem = item;
    }
    static TodoItem get() {
        return currentItem;
    }
    static void clear() {
        currentItem = null;
        state = STATE.EMPTY;
    }
}
