package mx.uach.newcompass3;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnseguir;
    private TextView help;
    private int spOption, travelWay = 0;
    //Layouts
    private ConstraintLayout clpaq, clapoyo, clcuentas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Spinner spservice = (Spinner) findViewById(R.id.spservice);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.slabels, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spservice.setAdapter(adapter);
        btnseguir = findViewById(R.id.btnseguir);
        btnseguir.setEnabled(false);
        help = findViewById(R.id.help);
        help.setEnabled(false);
        spservice.setOnItemSelectedListener(this);
        //Menús
        clapoyo = findViewById(R.id.clapoyo);
        clpaq = findViewById(R.id.clpaq);
    }

    public void showMap (View view){
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("spOption", spOption);
        intent.putExtra("travelWay", travelWay);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        spOption = position;
        clpaq.setVisibility(View.GONE);
        clapoyo.setVisibility(View.GONE);
        help.setText(item);
        //clcuentas.setVisibility(View.GONE);
        //Opciones de spinner
        switch (position){
            case 0:
                break;
            case 1:
                clpaq.setVisibility(View.VISIBLE);
                help.setText(R.string.psHelp);
                break;
            case 2:
                break;
            case 3:
                clapoyo.setVisibility(View.VISIBLE);
                help.setText(R.string.rsHelp);
                break;
            case 4:
                break;
        }
        btnseguir.setEnabled(true);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();
        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.fromCurrent:
                if (checked) {
                    travelWay = 0;
                    help.setText(R.string.ps1Help);
                }
                break;
            case R.id.fromMarker:
                if (checked) {
                    travelWay = 1;
                    help.setText(R.string.ps2Help);
                }
                break;
        }
    }
}
