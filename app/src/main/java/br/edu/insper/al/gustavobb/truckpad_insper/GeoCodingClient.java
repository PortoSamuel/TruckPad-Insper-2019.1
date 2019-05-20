package br.edu.insper.al.gustavobb.truckpad_insper;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class GeoCodingClient {
    private GeoCodingInterface client;

    public GeoCodingClient(){
        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl("https://geo.api.truckpad.io/v1/")
                .addConverterFactory(GsonConverterFactory.create());

        Retrofit retrofit = builder.build();
        client = retrofit.create(GeoCodingInterface.class);
    }

    public void getAddresses(String address){
        Call<GeoCoding> call = client.getGeoAPI(address);

        call.enqueue(new Callback<GeoCoding>() {
            @Override
            public void onResponse(Call<GeoCoding> call, Response<GeoCoding> response) {
                GeoCoding geoCoding = response.body();
                System.out.println(geoCoding.getPlaces().get(0).getAddress());


            }

            @Override
            public void onFailure(Call<GeoCoding> call, Throwable t) {

            }
        });
    }
}
