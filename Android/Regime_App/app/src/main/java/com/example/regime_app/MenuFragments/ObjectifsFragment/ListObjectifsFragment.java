package com.example.regime_app.MenuFragments.ObjectifsFragment;

import android.app.Dialog;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.example.regime_app.Adapters.ListObjectifsAdapter;
import com.example.regime_app.Commom.Commom;
import com.example.regime_app.Interface.ObjectifInterface;
import com.example.regime_app.Models.ObjectifsModel.IntensiteObjectif;
import com.example.regime_app.Models.ObjectifsModel.ObjectifAmeliorationSilhouette;
import com.example.regime_app.Models.ObjectifsModel.ObjectifMangerSain;
import com.example.regime_app.Models.ObjectifsModel.ObjectifPerdeDeGraisseLocalise;
import com.example.regime_app.Models.ObjectifsModel.ObjectifPerteDePoids;
import com.example.regime_app.Models.ObjectifsModel.ObjectifPriseDeMuscle;
import com.example.regime_app.Models.ObjectifsModel.ObjectifReduireGlucides;
import com.example.regime_app.Models.ObjectifsModel.ThemeMangerSainObjectif;
import com.example.regime_app.Models.ObjectifsModel.TypeObjectif;
import com.example.regime_app.Models.ObjectifsModel.ZoneCorpsGraisseObjectif;
import com.example.regime_app.R;
import com.google.gson.JsonArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ListObjectifsFragment extends Fragment {

    private ArrayList<ObjectifInterface> objectifs = new ArrayList<>();
    private Dialog myDialog;
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String urlObjectifs = "http://" + Commom.ipLocal + ":8081/api/objectifs";
        final View view = inflater.inflate(R.layout.list_objectifs, container, false);

        chargementTicket(urlObjectifs, view);

        Button buttonsupprimer = view.findViewById(R.id.boutonSupprimer);
        buttonsupprimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                suppression (view, urlObjectifs);
            }
        });

        Button buttomAjouter = view.findViewById(R.id.boutonAjouter);
        buttomAjouter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getChildFragmentManager().beginTransaction().add(R.id.ListObjectifLayout, new AjoutObjectifFragment()).commit();
            }
        });
        Commom.listObjectifView = view;
        return view;
    }

    private void miseAjoutObjectif (View view, JSONArray objectifArray){
        for (int i=0; i<objectifArray.length(); i++){
            String typeObjectif = null;
            Date dateDebut = null;
            Date dateFin = null;
            String infoComplementaire = null;
            int id = -1;

            try {
                JSONObject jsonObject = objectifArray.getJSONObject(i);
                typeObjectif = jsonObject.get("typeObjectif").toString();
                dateDebut = format.parse(jsonObject.get("dateDebut").toString());
                dateFin= format.parse(jsonObject.get("dateFin").toString());
                infoComplementaire = jsonObject.get("infoComplementaire").toString();
                id = jsonObject.getInt("id");
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (ParseException e) {
                e.printStackTrace();
            }


            if (typeObjectif.equals(TypeObjectif.AmeliorationSilhouette.toString())){
                this.objectifs.add(new ObjectifAmeliorationSilhouette(dateDebut, dateFin, IntensiteObjectif.valueOf(infoComplementaire), id));
            }
            if (typeObjectif.equals(TypeObjectif.MangerSain.toString())){
                this.objectifs.add(new ObjectifMangerSain(dateDebut, dateFin, ThemeMangerSainObjectif.valueOf(infoComplementaire), id));
            }
            if (typeObjectif.equals(TypeObjectif.PerteDePoids.toString())){
                this.objectifs.add(new ObjectifPerteDePoids(dateDebut, dateFin, Integer.valueOf(infoComplementaire), id));
            }
            if (typeObjectif.equals(TypeObjectif.PerteGraisse.toString())){
                this.objectifs.add(new ObjectifPerdeDeGraisseLocalise(dateDebut, dateFin, ZoneCorpsGraisseObjectif.valueOf(infoComplementaire), id));
            }
            if (typeObjectif.equals(TypeObjectif.PriseDeMuscle.toString())){
                this.objectifs.add(new ObjectifPriseDeMuscle(dateDebut, dateFin, Integer.valueOf(infoComplementaire), id));
            }
            if (typeObjectif.equals(TypeObjectif.ReduireGlucides.toString())){
                this.objectifs.add(new ObjectifReduireGlucides(dateDebut, dateFin, IntensiteObjectif.valueOf(infoComplementaire), id));
            }
        }
        Commom.objectifSelected = this.objectifs.get(0);
        initRecyclerView(view);
    }

    private void initRecyclerView(View view){

        LinearLayoutManager layoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.HORIZONTAL, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        ListObjectifsAdapter adapter = new ListObjectifsAdapter(view.getContext(), this.objectifs);
        recyclerView.setAdapter(adapter);
    }

    public void chargementTicket(String urlObjectifs, View view) {
        RequestQueue requestQueue = Volley.newRequestQueue(this.getContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.GET,
                urlObjectifs,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        miseAjoutObjectif(view, response);
                        Log.e("Response ", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error ", error.toString());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
    }

    public void suppression (View view, String urlObjectifs) {
        myDialog = new Dialog(view.getContext());
        myDialog.setContentView(R.layout.pop_up_confirmation_suppression_objectif);
        TextView textBut = (TextView) myDialog.findViewById(R.id.questionSuppression);
        textBut.setText("Voulez vous vraiment arreter votre objectif : " + Commom.objectifSelected.toString().toLowerCase() + "?");
        Button buttonOui = (Button) myDialog.findViewById(R.id.boutonOui);
        buttonOui.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                suppressionTicket(view, urlObjectifs);
                myDialog.dismiss();
            }
        });

        Button buttonNon = (Button) myDialog.findViewById(R.id.boutonNon);
        buttonNon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myDialog.dismiss();
            }
        });

        myDialog.show();
    }

    public void suppressionTicket(View view, String urlObjectifs){
        RequestQueue requestQueue = Volley.newRequestQueue(view.getContext());
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(
                Request.Method.DELETE,
                urlObjectifs + '/' + Commom.objectifSelected.getId(),
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("Responce ", response.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("Error ", error.toString());
                    }
                }
        );
        requestQueue.add(jsonArrayRequest);
        Commom.objectifSelected = objectifs.get(0);
        getChildFragmentManager().beginTransaction().add(R.id.ListObjectifLayout, new ListObjectifsFragment()).commit();
    }
}