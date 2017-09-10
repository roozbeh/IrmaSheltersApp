package irmashelters.ipronto.net.irmashelters;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static irmashelters.ipronto.net.irmashelters.MapsActivity.MY_PERMISSIONS_CALL_PHONE;
import static irmashelters.ipronto.net.irmashelters.MapsActivity.MY_PERMISSIONS_REQUEST_LOCATION;

public class DetailActivity extends AppCompatActivity {

    TextView txtShelterName;
    TextView txtAddress1;
    TextView txtAddress2;
    TextView txtAccepting;
    Button btnPhoneNumber;
    TextView txtCounty;
    Button btnMoreInfo;
    Button btnDirections;

    JSONObject shelterInfo;

    String sourceInfoURL;
    String cleanPhone;
    String completeAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        txtShelterName = (TextView) findViewById(R.id.txtShelterName);
        txtAddress1 = (TextView) findViewById(R.id.txtAddress1);
        txtAddress2 = (TextView) findViewById(R.id.txtAddress2);
        txtAccepting = (TextView) findViewById(R.id.txtAccepting);
        txtCounty = (TextView) findViewById(R.id.txtCounty);

        btnPhoneNumber = (Button) findViewById(R.id.btnPhoneNumber);
        btnMoreInfo = (Button) findViewById(R.id.btnMoreInfo);
        btnDirections = (Button) findViewById(R.id.btnDirections);

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            Toast.makeText(this, "Could not find info", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String shelterInfoStr = extras.getString("shelterInfo");
            shelterInfo = new JSONObject(shelterInfoStr);

            txtShelterName.setText(shelterInfo.getString("shelter"));
            txtAddress1.setText(shelterInfo.getString("address"));

            String city = "", state = "", zip = "";
            if (!shelterInfo.isNull("city")) {
                city = shelterInfo.getString("city");
            }
            if (!shelterInfo.isNull("state")) {
                state = shelterInfo.getString("state");
            }
            if (!shelterInfo.isNull("zip")) {
                zip = shelterInfo.getString("zip");
            }

            txtAddress2.setText(city + " " + state + ", " + zip);
            completeAddress = shelterInfo.getString("address") + "," + city + " " + state + ", " + zip;

            txtAccepting.setText(shelterInfo.getBoolean("accepting") ? "ACCEPTING" : "Not accepting");
            txtCounty.setText(shelterInfo.getString("county"));
            btnPhoneNumber.setText(shelterInfo.getString("phone"));

            sourceInfoURL = shelterInfo.getString("source");
            cleanPhone = shelterInfo.getString("cleanPhone");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public void onPhoneCall(View v) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + cleanPhone));
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_CALL_PHONE);
        } else {
            startActivity(callIntent);
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_CALL_PHONE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPhoneCall(null);
                }
            }
        }
    }


    public void onMoreInfo(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(sourceInfoURL));
        startActivity(browserIntent);
    }

    public void onGetDirections(View v) {
        String uri = null;
        try {
            uri = "https://www.google.com/maps/search/?api=1&query="+ URLEncoder.encode(completeAddress, "utf-8");
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
            startActivity(intent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }
}
