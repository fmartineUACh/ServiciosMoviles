package mx.uach.newcompass3;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    Button btnseguir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText result = (EditText) findViewById(R.id.result);
        result.setEnabled(false);
        final ConstraintLayout clapoyo = (ConstraintLayout) findViewById(R.id.clapoyo);

        final Spinner spservice = (Spinner) findViewById(R.id.spservice);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.slabels, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spservice.setAdapter(adapter);
        btnseguir = (Button) findViewById(R.id.btnseguir);
        btnseguir.setEnabled(false);

        spservice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                String service = spservice.getSelectedItem().toString();
                result.setText(service);

                clapoyo.setVisibility(View.GONE);

                if (service.equals("Apoyo vial")){
                    clapoyo.setVisibility(View.VISIBLE);
                }
                btnseguir.setEnabled(true);
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnseguir.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mapa = new Intent(v.getContext(), MapActivity.class);
                startActivity(mapa);
            }
        });
    }
}
