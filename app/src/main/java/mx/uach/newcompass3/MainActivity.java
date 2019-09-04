package mx.uach.newcompass3;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    private Button btnNext;
    private TextView help;
    private int spOption, travelWay = 0;
    //Layouts
    private ConstraintLayout clpar, clsupp, clfood;
    private RadioButton fromCurrent, fromMarker;
    private RadioButton rbFlatTire, rbGas, rbLeak, rbBrake, rbBatery;
    private EditText ofOrder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final Spinner spservice = (Spinner) findViewById(R.id.spservice);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.sLabels, android.R.layout.simple_spinner_item);

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spservice.setAdapter(adapter);
        btnNext = findViewById(R.id.btnseguir);
        btnNext.setEnabled(false);
        help = findViewById(R.id.help);
        help.setEnabled(false);
        spservice.setOnItemSelectedListener(this);
        //Menús
        clsupp = findViewById(R.id.clApoyo);
        clpar = findViewById(R.id.clpaq);
        clfood = findViewById(R.id.clfood);
        fromCurrent = findViewById(R.id.fromCurrent);
        fromMarker = findViewById(R.id.fromMarker);
        rbBatery = findViewById(R.id.rbBateria);
        rbBrake = findViewById(R.id.rbFreno);
        rbLeak = findViewById(R.id.rbFuga);
        rbGas = findViewById(R.id.rbGas);
        rbFlatTire = findViewById(R.id.rbPonche);
        ofOrder = findViewById(R.id.ofOrder);
    }

    public void showMap (View view){
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra("spOption", spOption);
        intent.putExtra("travelWay", travelWay);
        /*Servicios viales*/
        intent.putExtra("rsBatery", rbBatery.isChecked());
        intent.putExtra("rsBrake", rbBrake.isChecked());
        intent.putExtra("rsLeak", rbLeak.isChecked());
        intent.putExtra("rsGas", rbGas.isChecked());
        intent.putExtra("rsFlatTire", rbFlatTire.isChecked());
        /*Orden de comida*/
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
        clpar.setVisibility(View.GONE);
        clsupp.setVisibility(View.GONE);
        clfood.setVisibility(View.GONE);
        help.setText(item);
        //Opciones de spinner
        switch (position){
            case 0:
                travelWay = 0;
                help.setText(R.string.ptHelp);
                break;
            case 1:
                clpar.setVisibility(View.VISIBLE);
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
                clsupp.setVisibility(View.VISIBLE);
                help.setText(R.string.rsHelp);
                break;
            case 4:
                travelWay = 1;
                clfood.setVisibility(View.VISIBLE);
                help.setText(R.string.ofHelp);
                break;
        }
        btnNext.setEnabled(true);
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
