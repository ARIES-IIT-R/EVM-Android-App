package com.example.abhay.testevm;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.example.androidbtcontrol.MainActivity;
import com.example.androidbtcontrol.R;

import java.util.ArrayList;

public class candidate extends AppCompatActivity {

    EditText inputno,input_names;
    Button enter,enter2;
    LinearLayout inputgone;
    ArrayList<String> ar = new ArrayList<String>();
    int n,i=0;
    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candidate);
        inputno = (EditText)findViewById(R.id.candidate_no);
        input_names = (EditText)findViewById(R.id.candidate_name);
        enter= (Button)findViewById(R.id.enter);
        enter2 = (Button)findViewById(R.id.enter2);
        inputgone = (LinearLayout)findViewById(R.id.inputgone);

        enter.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                n = Integer.parseInt(inputno.getText().toString());
                inputgone.setVisibility(View.VISIBLE);
            }});

        enter2.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                name = input_names.getText().toString();
                ar.add(name);
                i++;
                if(i==n)
                {
                    Intent intent = new Intent(candidate.this,MainActivity.class);
                    startActivity(intent);
                }
                else
                {
                    input_names.setText("");
                }
            }});
    }
}
