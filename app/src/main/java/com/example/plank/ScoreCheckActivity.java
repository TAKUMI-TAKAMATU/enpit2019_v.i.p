package com.example.plank;

import android.os.Bundle;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout;

public class ScoreCheckActivity extends AppCompatActivity{

    private TextView textView;
    private EditText editTextKey, editTextValue;
    private TestOpenHelper helper;
    private SQLiteDatabase db;

    private int cap[];
    private int caption[];
    private int haiten[];
    private int categoryPoint[];
    private int averagePoint[];
    private int categoryNum = 0;
    private int dataNum =0;
    private final int MP = TableLayout.LayoutParams.MATCH_PARENT;
    private final int WC = TableLayout.LayoutParams.WRAP_CONTENT;

    public ScoreCheckActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scorecheck);

      //  textView = findViewById(R.id.text_view);
        readData();
      /**

        textView = findViewById(R.id.text_view);
        readData();
        Button readButton = findViewById(R.id.button_read);
        readButton.setOnClickListener(new View.OnClickListener() {
            @Override
           public void onClick(View v) {

                if(helper == null){
                    helper = new TestOpenHelper(getApplicationContext());
                }

                if(db == null){
                    db = helper.getWritableDatabase();
                }
                insertData(db, "2/1(例)", 100);
                readData();
            }
        });
       */
    }


    private void readData(){
        if(helper == null){
            helper = new TestOpenHelper(getApplicationContext());
        }

        if(db == null){
            db = helper.getReadableDatabase();
        }else{
            //db = helper.getReadableDatabase();
           // helper.onUpgrade(db ,1,2);
        }
       // Log.d("debug","**********Cursor");

        Cursor cursor = db.query(
                "testdb",
                new String[] { "date", "score","level","sec" },
                null,
                null,
                null,
                null,
                null
        );

        cursor.moveToFirst();

        StringBuilder sbuilder = new StringBuilder();

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);


        for (int i = 1; i < cursor.getCount()+1; i++) {
            sbuilder.append(cursor.getString(0));
            sbuilder.append(": ");
            sbuilder.append(cursor.getInt(1));
            sbuilder.append("\n");

            //上四行使わん
                if(dataNum==0) {//この中で表示
                    TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
                    TextView name = (TextView) tableRow.findViewById(R.id.rowtext1);

                    // name.setText(cap[i] + caption[i]);配列ないに何も入ってないからエラー出る
                    name.setText(cursor.getString(0));//日付
                    TextView point = (TextView) tableRow.findViewById(R.id.rowtext2);

                    //  point.setText(Integer.toString(haiten[i]));
                    point.setText(Integer.toString(cursor.getInt(1)));//スコア
                    TextView score = (TextView) tableRow.findViewById(R.id.rowtext3);
                    //    score.setText(Integer.toString(categoryPoint[i]));
                    score.setText(cursor.getString(2));
                    TextView ave = (TextView) tableRow.findViewById(R.id.rowtext4);
                    // ave.setText(Integer.toString(averagePoint[i]));
                    ave.setText(Integer.toString(cursor.getInt(3)));
                    if ((i + 1) % 2 == 0) {
                        int color = getResources().getColor(R.color.colorPrimary);
                        name.setBackgroundColor(color);
                        point.setBackgroundColor(color);
                        score.setBackgroundColor(color);
                        ave.setBackgroundColor(color);
                    }

                    tableLayout.addView(tableRow, new TableLayout.LayoutParams(MP, WC));

                }
            categoryNum = cursor.getCount();

            cursor.moveToNext();
        }

        dataNum=cursor.getCount();
        // 忘れずに！
        cursor.close();

        Log.d("debug","**********"+sbuilder.toString());
       // textView.setText(sbuilder.toString());
    }

    private void insertData(SQLiteDatabase db, String com, int price) {

        ContentValues values = new ContentValues();
        values.put("date", com);
        values.put("score", price);

        db.insert("testdb", null, values);

        TableLayout tableLayout = (TableLayout) findViewById(R.id.tableLayout);
        TableRow tableRow = (TableRow) getLayoutInflater().inflate(R.layout.table_row, null);
        TextView name = (TextView) tableRow.findViewById(R.id.rowtext1);

            // name.setText(cap[i] + caption[i]);配列ないに何も入ってないからエラー出る
        name.setText(com);
        TextView point = (TextView) tableRow.findViewById(R.id.rowtext2);

            //  point.setText(Integer.toString(haiten[i]));
        point.setText(Integer.toString(price));
        TextView score = (TextView) tableRow.findViewById(R.id.rowtext3);
            //    score.setText(Integer.toString(categoryPoint[i]));
        score.setText("score111");
        TextView ave = (TextView) tableRow.findViewById(R.id.rowtext4);
            // ave.setText(Integer.toString(averagePoint[i]));
        ave.setText("ave");
        if ((dataNum + 1) % 2 == 0) {
            int color = getResources().getColor(R.color.colorPrimary);
            name.setBackgroundColor(color);
            point.setBackgroundColor(color);
            score.setBackgroundColor(color);
            ave.setBackgroundColor(color);
            }
        dataNum++;
        tableLayout.addView(tableRow, new TableLayout.LayoutParams(MP, WC));
        }


    }

