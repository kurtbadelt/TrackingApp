package tech.e32.lendme_rastreo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ToggleButton;
import android.util.Log;

import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private String placasUnidad="";
    private String nombreConductor="";
    private boolean terminosYCondiciones=false;
    private ToggleButton botonEncendido;
    EditText textPlacas = null;
    EditText textConductor = null;
    CheckBox checkTnC = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        this.textPlacas = (EditText) findViewById(R.id.TFPlacas);
        this.textConductor = (EditText) findViewById(R.id.TFConductor);
        this.checkTnC = (CheckBox) findViewById(R.id.checkBoxTnC);
        this.botonEncendido = (ToggleButton) findViewById(R.id.botonEncendido);
        this.botonEncendido.setEnabled(false);

        this.checkTnC.setOnClickListener(new View.OnClickListener(){
            public void onClick(View view){
                botonEncendido.setEnabled(true);
            }
        }
        );


        this.botonEncendido.setOnClickListener(new View.OnClickListener(){

            public void onClick(View view){
                execute(view);

            }

        });





    }

    public void execute(View v){


        this.placasUnidad = textPlacas.getText().toString();
        this.nombreConductor = textConductor.getText().toString();
        this.terminosYCondiciones = checkTnC.isChecked();




        Log.w("execute ",""+this.placasUnidad+","+this.nombreConductor+","+this.terminosYCondiciones+" TS: "+getCurrentTimeStamp());
    }

    /**
     *
     * @return yyyy-MM-dd HH:mm:ss formate date as string
     */
    public static String getCurrentTimeStamp(){
        try {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String currentDateTime = dateFormat.format(new Date()); // Find todays date

            return currentDateTime;
        } catch (Exception e) {
            e.printStackTrace();

            return null;
        }
    }
}
