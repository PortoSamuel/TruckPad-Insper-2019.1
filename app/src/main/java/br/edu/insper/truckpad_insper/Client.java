package br.edu.insper.truckpad_insper;

import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client extends AppCompatActivity {

    Retrofit.Builder builder = new Retrofit.Builder()
            .baseUrl(BuildConfig.GEO_URL)
            .addConverterFactory(GsonConverterFactory.create());

    private GeoCodingReceived codeReceived;
    private GeoCodingPayload payload = new GeoCodingPayload();
    private GeoRouteReceived routeReceived;
    private int axisNumber;
    private boolean originPlaced, destinyPlaced, originCompleted, destinyCompleted;
    private String loadType;
    private String[] originPlaces, destinyPlaces;
    private Retrofit retrofit = builder.build();
    private GeoCodingInterface client = retrofit.create(GeoCodingInterface.class);
    private MainActivity main;

    public Client(MainActivity main){
        this.main = main;
    }

    public void getAddress(String address){
        Call<GeoCodingReceived> call = client.getGeoCoding(address);
        call.enqueue(new Callback<GeoCodingReceived>() {
            @Override
            public void onResponse(Call<GeoCodingReceived> call, Response<GeoCodingReceived> response) {
                codeReceived = response.body();

                if(originPlaced){
                    try {
                        originPlaces = new String[Objects.requireNonNull(codeReceived).getPlaces().size()];
                        for (int i = 0; i < codeReceived.getPlaces().size(); i++) { originPlaces[i] = codeReceived.getPlaces().get(i).getDisplay_name(); }
                        setOriginPlaced(false);
                    } catch(Exception e){ }
                }
                if(destinyPlaced){
                    try {
                        destinyPlaces = new String[Objects.requireNonNull(codeReceived).getPlaces().size()];
                        for (int i = 0; i < codeReceived.getPlaces().size(); i++) { destinyPlaces[i] = codeReceived.getPlaces().get(i).getDisplay_name(); }
                        setDestinyPlaced(false);
                    } catch(Exception e){ }
                }

                if(destinyCompleted){
                    try{
                        assert codeReceived != null;
                        payload.putPlaceDestiny(codeReceived.getPlaces().get(0));
                        setDestinyCompleted(false);
                    }catch (Exception e){ main.showToast("O destino não existe"); }
                }
                if(originCompleted){
                    try{
                        assert codeReceived != null;
                        payload.putPlaceOrigin(codeReceived.getPlaces().get(0));
                        setOriginCompleted(false);
                    }catch (Exception e){ main.showToast("A origem não existe"); }
                }
            }
            @Override
            public void onFailure(Call<GeoCodingReceived> call, Throwable t) { }
        });
    }

    public void postAddress(){
        System.out.println(payload.getPlaces().get(0).getDisplay_name());
        System.out.println(payload.getPlaces().get(1).getDisplay_name());
        Call<GeoRouteReceived> call = client.getGeoRoute(payload);

        call.enqueue(new Callback<GeoRouteReceived>() {

        @Override
        public void onResponse(Call<GeoRouteReceived> call, Response<GeoRouteReceived> response) {
            routeReceived = response.body();
            PriceClient client = ServiceGenerator.createService(PriceClient.class);
            PriceInformation priceInformation = new PriceInformation(getAxisNumber(), routeReceived.getDistance()/1000, true, getLoadType().replace("carga ", ""));

            Call<Price> callprice = client.calculatePrice(priceInformation);

            callprice.enqueue(new Callback<Price>() {
                @Override
                public void onResponse(Call<Price> call, Response<Price> response){
                    double result = Objects.requireNonNull(response.body()).getShipmentValue();
                    main.setOnResponsePrice(result, priceInformation.getDistance());
                    main.setAllState("showResult");
                }

                @Override
                public void onFailure(Call<Price> call, Throwable t) { }
            });
        }

            @Override
            public void onFailure(Call<GeoRouteReceived> call, Throwable t) { }
        });
    }

    public int getAxisNumber() { return axisNumber; }


    public String getLoadType() { return loadType; }

    public void setAxisNumber(int axisNumber) { this.axisNumber = axisNumber; }


    public void setLoadType(String loadType) { this.loadType = loadType; }

    public String[] getOriginPlaces(){ return originPlaces; }

    public String[] getDestinyPlaces(){ return destinyPlaces; }

    public void setOriginPlaced(boolean b){ this.originPlaced = b; }

    public void setDestinyPlaced(boolean b){ this.destinyPlaced = b; }

    public void setOriginCompleted(boolean b){ this.originCompleted = b; }

    public void setDestinyCompleted(boolean b){ this.destinyCompleted = b; }
}
