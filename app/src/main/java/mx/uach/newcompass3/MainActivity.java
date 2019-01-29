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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnseguir, btnProv;
    private TextView help;
    private int spOption, travelWay = 0;
    //Layouts
    private ConstraintLayout clpaq, clapoyo, clfood;
    private RadioButton fromCurrent, fromMarker;
    private CheckBox chFlatTire, chGas, chLeak, chBrake, chBatery;
    private EditText ofOrder;

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
        btnProv = findViewById(R.id.btnprov);
        btnseguir.setEnabled(false);
        help = findViewById(R.id.help);
        help.setEnabled(false);
        spservice.setOnItemSelectedListener(this);
        //Menús
        clapoyo = findViewById(R.id.clapoyo);
        clpaq = findViewById(R.id.clpaq);
        clfood = findViewById(R.id.clfood);
        fromCurrent = findViewById(R.id.fromCurrent);
        fromMarker = findViewById(R.id.fromMarker);
        chBatery = findViewById(R.id.chbbateria);
        chBrake = findViewById(R.id.chbfreno);
        chLeak = findViewById(R.id.chbfuga);
        chGas = findViewById(R.id.chbgas);
        chFlatTire = findViewById(R.id.chbponche);
        ofOrder = findViewById(R.id.ofOrder);
    }

    public void showMap (View view){
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("spOption", spOption);
        intent.putExtra("travelWay", travelWay);
        //Servicios viales
        intent.putExtra("rsBatery", chBatery.isChecked());
        intent.putExtra("rsBrake", chBrake.isChecked());
        intent.putExtra("rsLeak", chLeak.isChecked());
        intent.putExtra("rsGas", chGas.isChecked());
        intent.putExtra("rsFlatTire", chFlatTire.isChecked());
        //Orden de comida
        intent.putExtra("fOrder", ofOrder.getText());
        startActivity(intent);
    }
    public void showProv (View view){
        Intent intent = new Intent(this, ProviderMap.class);
        startActivity(intent);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String item = parent.getItemAtPosition(position).toString();
        spOption = position;
        clpaq.setVisibility(View.GONE);
        clapoyo.setVisibility(View.GONE);
        clfood.setVisibility(View.GONE);
        help.setText(item);
        //clcuentas.setVisibility(View.GONE);
        //Opciones de spinner
        switch (position){
            case 0:
                travelWay = 0;
                help.setText(R.string.ptHelp);
                break;
            case 1:
                clpaq.setVisibility(View.VISIBLE);
                if(fromCurrent.isChecked()){
                    travelWay = 0;
                }else if (fromMarker.isChecked()){
                    travelWay = 1;
                }else{
                    fromCurrent.setChecked(true);
                    travelWay = 0;
                }
                help.setText(R.string.psHelp);
                break;
            case 2:
                travelWay = 0;
                help.setText(R.string.pbHelp);
                break;
            case 3:
                travelWay = 0;
                clapoyo.setVisibility(View.VISIBLE);
                help.setText(R.string.rsHelp);
                break;
            case 4:
                travelWay = 1;
                clfood.setVisibility(View.VISIBLE);
                help.setText(R.string.ofHelp);
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
