package com.example.mapagps;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static java.util.Locale.getDefault;

public class Mapa extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationClient;
    //referencias a base de datos
    DatabaseReference mDatabase;

    private String situacionrep,descrep,correo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapa);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //intanciacion base de datos
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        miUbicaion();
    }

    private void miUbicaion() {

        final LocationManager loc = (LocationManager) Mapa.this.getSystemService(Context.LOCATION_SERVICE);


        if (ActivityCompat.checkSelfPermission(Mapa.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(Mapa.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;       }

        mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                int zoom = 15;
                LatLng posicion  = new LatLng(location.getLatitude(),location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory
                        .newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),zoom));
                mMap.addMarker(new MarkerOptions()
                .position(posicion)
                .title("JASON")
                .snippet("Estoy aqui")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);

                markerOptions.title(latLng.latitude+" , "+latLng.longitude).snippet("Reporta");
                mMap.clear();

                mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));


                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick(Marker marker) {
                        boolean retorno=true;


                        final AlertDialog.Builder mydialog = new AlertDialog.Builder(Mapa.this);
                        mydialog.setTitle("Vetnata de reporte");

                        final EditText situacion = new EditText(Mapa.this);
                        final EditText Descripcion = new EditText(Mapa.this);

                        //***
                        View mView = getLayoutInflater().inflate(R.layout.reporteview,null);
                        final EditText tvxSIT = (EditText) mView.findViewById(R.id.repSitu);
                        final EditText txvDes = (EditText) mView.findViewById(R.id.desSitu);
                        final EditText txvCorreo = (EditText) mView.findViewById(R.id.desCorre);
                        final EditText txvNombre = (EditText) mView.findViewById(R.id.desNombre);



                        if (tvxSIT.getText().toString().isEmpty()){
                            tvxSIT.setError("Este Campo no puede quedar vacio");
                            retorno=false;
                        }

                        //situacion.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);


                        //Descripcion.setInputType(InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
                        mydialog.setView(mView);
                        //mydialog.setView(Descripcion);

                        mydialog.setPositiveButton("Reportar", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {

                                //situacionrep = situacion.getText().toString();
                                //descrep = Descripcion.getText().toString();

                                Geocoder geocoder = new Geocoder(getApplicationContext(), getDefault());
                                try {
                                    List<Address> direccion = geocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
                                    boolean retorno=true;
                                    //System.out.println(direccion.get(0).getAddressLine(0));
                                    //miDir.setText("Mi direccion:\n"+direccion.get(0).getAddressLine(0));

                                    String dir = "" +direccion.get(0).getAddressLine(0);
                                    String sitt = tvxSIT.getText().toString();
                                    String des = txvDes.getText().toString();
                                    String cor = txvCorreo.getText().toString();
                                    String nom = txvNombre.getText().toString();
                                    String nm = "" + latLng.latitude;
                                    String mm = ""+ latLng.longitude;
                                    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
                                    String ID = UUID.randomUUID().toString();




                                    Map<String,Object> registroAct = new HashMap<>();
                                    registroAct.put("Nombre",nom);
                                    registroAct.put("Correo",cor);
                                    registroAct.put("Descripcion",des);
                                    registroAct.put("Situacion",sitt);
                                    registroAct.put("Direccion",dir);
                                    registroAct.put("Fecha",date);
                                    registroAct.put("Latitud",nm);
                                    registroAct.put("Longitud",mm);
                                    mDatabase.child("Reportes").child(ID).setValue(registroAct);

                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                            }
                        });

                        mydialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        mydialog.show();
                    }
                });

                mMap.addMarker(markerOptions);

            }
        });



    }
}