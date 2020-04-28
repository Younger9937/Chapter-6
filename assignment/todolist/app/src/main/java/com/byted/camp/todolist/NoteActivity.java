package com.byted.camp.todolist;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.byted.camp.todolist.db.TodoContract;
import com.byted.camp.todolist.db.TodoDbHelper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class NoteActivity extends AppCompatActivity {

    private static final String TAG = "NoteActivity";

    private EditText editText;
    private Button addBtn;
    private TodoDbHelper todoDbHelper;
    private RadioGroup radioGroup;
    private int levelBtn = 3;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        setTitle(R.string.take_a_note);

        editText = findViewById(R.id.edit_text);
        editText.setFocusable(true);
        editText.requestFocus();
        InputMethodManager inputManager = (InputMethodManager)
                getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputManager != null) {
            inputManager.showSoftInput(editText, 0);
        }

        todoDbHelper = new TodoDbHelper(this);

        radioGroup = findViewById(R.id.radioGroup);
        radioGroup.check(R.id.lowButton);
        radioGroup.setOnCheckedChangeListener(listen);

        addBtn = findViewById(R.id.btn_add);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence content = editText.getText();
                if (TextUtils.isEmpty(content)) {
                    Toast.makeText(NoteActivity.this,
                            "No content to add", Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean succeed = saveNote2Database(content.toString().trim());
                if (succeed) {
                    Toast.makeText(NoteActivity.this,
                            "Note added", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                } else {
                    Toast.makeText(NoteActivity.this,
                            "Error", Toast.LENGTH_SHORT).show();
                }
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        todoDbHelper.close();
        super.onDestroy();
    }

    private RadioGroup.OnCheckedChangeListener listen = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            switch (radioGroup.getCheckedRadioButtonId()){
                case R.id.highButton: levelBtn = 1; break;
                case R.id.normalButton: levelBtn = 2; break;
                case R.id.lowButton: levelBtn = 3;break;
                default:break;
            }
        }
    };

    private boolean saveNote2Database(String content) {
        // TODO 插入一条新数据，返回是否插入成功
        SQLiteDatabase db = todoDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(TodoContract.TodoEntry.COLUMN_NAME_CONTENT,content);
        values.put(TodoContract.TodoEntry.COLUMN_NAME_DATE,findDate());
        values.put(TodoContract.TodoEntry.COLUMN_NAME_STATE,"TODO");
        Log.d(TAG, String.valueOf(levelBtn));
        values.put(TodoContract.TodoEntry.COLUMN_NAME_LEVEL,levelBtn);

        long newRowID = db.insert(TodoContract.TodoEntry.TABLE_NAME,null,values);
        Log.d(TAG, String.valueOf(newRowID));
        if(newRowID != 0){
            return true;
        }
        else{
            return false;
        }
    }

    private static String mYear;
    private static String mMonth;
    private static String mDay;
    private static String mWeek;

    private static String findDate()  {
        final Calendar c = Calendar.getInstance();
        c.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));
        mYear = String.valueOf(c.get(Calendar.YEAR));
        mMonth = String.valueOf(c.get(Calendar.MONTH)+1);
        mDay = String.valueOf(c.get(Calendar.DAY_OF_MONTH));
        mWeek = String.valueOf(c.get(Calendar.DAY_OF_WEEK));

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        String tempDate= simpleDateFormat.format(date);

        switch (mWeek){
            case "1":mWeek = "Mon";break;
            case "2":mWeek = "Tue";break;
            case "3":mWeek = "Wed";break;
            case "4":mWeek = "Thu";break;
            case "5":mWeek = "Fri";break;
            case "6":mWeek = "Sat";break;
            case "7":mWeek = "Sun";break;
            default:break;
        }
        switch (mMonth){
            case "1":mMonth = "Jan";break;
            case "2":mMonth = "Feb";break;
            case "3":mMonth = "Mar";break;
            case "4":mMonth = "Apr";break;
            case "5":mMonth = "May";break;
            case "6":mMonth = "Jun";break;
            case "7":mMonth = "Jul";break;
            case "8":mMonth = "Aug";break;
            case "9":mMonth = "Sep";break;
            case "10":mMonth = "Oct";break;
            case "11":mMonth = "Nov";break;
            case "12":mMonth = "Dec";break;
            default:break;
        }
        String finalDate = mWeek+", "+mDay+" "+mMonth+" "+mYear+" "+tempDate;
        Log.d(TAG,finalDate);
        return finalDate;
    }
}
