package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.byted.camp.todolist.beans.Note;
import com.byted.camp.todolist.beans.State;
import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;
import com.byted.camp.todolist.operation.activity.DatabaseActivity;
import com.byted.camp.todolist.operation.activity.DebugActivity;
import com.byted.camp.todolist.operation.activity.SettingActivity;
import com.byted.camp.todolist.ui.NoteListAdapter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE_ADD = 1002;

    private RecyclerView recyclerView;
    private NoteListAdapter notesAdapter;
    private TodoDbHelper todoDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        todoDbHelper = new TodoDbHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(
                        new Intent(MainActivity.this, NoteActivity.class),
                        REQUEST_CODE_ADD);
            }
        });

        recyclerView = findViewById(R.id.list_todo);
        recyclerView.setLayoutManager(new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        notesAdapter = new NoteListAdapter(new NoteOperator() {
            @Override
            public void deleteNote(Note note) throws ParseException {
                MainActivity.this.deleteNote(note);
            }

            @Override
            public void updateNote(Note note) throws ParseException {
                MainActivity.this.updateNode(note);
            }
        });
        recyclerView.setAdapter(notesAdapter);

        try {
            notesAdapter.refresh(loadNotesFromDatabase());
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        todoDbHelper.close();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                startActivity(new Intent(this, SettingActivity.class));
                return true;
            case R.id.action_debug:
                startActivity(new Intent(this, DebugActivity.class));
                return true;
            case R.id.action_database:
                startActivity(new Intent(this, DatabaseActivity.class));
                return true;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD
                && resultCode == Activity.RESULT_OK) {
            try {
                notesAdapter.refresh(loadNotesFromDatabase());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Note> loadNotesFromDatabase() throws ParseException {
        // TODO 从数据库中查询数据，并转换成 JavaBeans
        List<Note> notes = new ArrayList<>();
        SQLiteDatabase db = todoDbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                TodoContract.TodoEntry.COLUMN_NAME_CONTENT,
                TodoContract.TodoEntry.COLUMN_NAME_DATE,
                TodoContract.TodoEntry.COLUMN_NAME_STATE,
                TodoContract.TodoEntry.COLUMN_NAME_LEVEL
        };

        String sortOrder = TodoContract.TodoEntry.COLUMN_NAME_LEVEL + " ASC";

        Cursor cursor = db.query(
                TodoContract.TodoEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()){
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(TodoContract.TodoEntry._ID));
            String content = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_CONTENT));
            String dateStr = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_DATE));
            String stateStr = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_STATE));
            String level = cursor.getString(cursor.getColumnIndex(TodoContract.TodoEntry.COLUMN_NAME_LEVEL));

            try {
                Note note  = new Note(itemId);
                note.setContent(content);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss", Locale.ENGLISH);
                Date date = simpleDateFormat.parse(dateStr);
                note.setDate(date);
                State state = State.TODO;
                if(!stateStr.equals("TODO")){
                    state = State.DONE;
                }
                note.setState(state);
                note.setLevel(Integer.parseInt(level));
                //Log.d(TAG, String.valueOf(date));
                notes.add(note);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        cursor.close();
        return notes;
    }

    private void deleteNote(Note note) throws ParseException {
        // TODO 删除数据
        Log.d(TAG,"delete");
        SQLiteDatabase db = todoDbHelper.getReadableDatabase();

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = new String[]{String.valueOf(note.id)};

        int deletedRows = db.delete(TodoContract.TodoEntry.TABLE_NAME, selection, selectionArgs);

        notesAdapter.refresh(loadNotesFromDatabase());
    }

    private void updateNode(Note note) throws ParseException {
        // 更新数据
        Log.d(TAG,"update");
        SQLiteDatabase db = todoDbHelper.getReadableDatabase();

        String title;
        if(note.getState() == State.DONE){
            title = "DONE";
        }
        else{
            title = "TODO";
        }

        ContentValues values = new ContentValues();
        Log.d(TAG,"title"+title);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_STATE,title);

        String selection = TodoContract.TodoEntry._ID + " LIKE ?";
        String[] selectionArgs = new String[]{String.valueOf(note.id)};

        int count = db.update(
                TodoContract.TodoEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        notesAdapter.refresh(loadNotesFromDatabase());
    }

}
