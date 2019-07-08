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
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;
import androidx.room.Update;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class DateConverter {
    @TypeConverter
    public static Date revertDate(long value) {
        if (value == 0) {
            return null;
        }
        return new Date(value);
    }

    @TypeConverter
    public static long converterDate(Date value) {
        if (value == null) {
            return 0;
        }
        return value.getTime();
    }

    static String toString(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd E\nhh:mm", Locale.US).format(date);
    }

}

@Entity
class TodoItem {
    @PrimaryKey(autoGenerate = true)
    int id;

    String title;
    String detail;
    boolean completed;
    boolean setAlarmed;
    Date alarmDate = null;

    TodoItem(String title, String detail, Date alarmDate) {
        this.title = title;
        this.detail = detail;
        this.completed = false;
        setAlarmDate(alarmDate);
    }

    void setAlarmDate(Date date) {
        if (date != null) {
            this.setAlarmed =true;
            this.alarmDate = date;
        } else {
            this.setAlarmed = false;
            this.alarmDate = null;
        }
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
@TypeConverters(DateConverter.class)
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
        TextView alarmDateView;
        TodoItemHolder(@NonNull View itemView) {
            super(itemView);
            todoTitle = itemView.findViewById(R.id.todo_title);
            todoDetail = itemView.findViewById(R.id.todo_detail);
            alarmDateView = itemView.findViewById(R.id.alarmDateView);
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
        if (todoItem.setAlarmed) {
            holder.alarmDateView.setText(
                    DateConverter.toString(todoItem.alarmDate)
                            .replace("\n", " ")
            );
        } else {
            holder.alarmDateView.setText("");
        }

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
