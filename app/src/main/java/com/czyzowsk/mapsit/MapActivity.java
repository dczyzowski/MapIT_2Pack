package com.czyzowsk.mapsit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.ColorRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import com.czyzowsk.mapsit.interfaces.GetGeocodings;
import com.czyzowsk.mapsit.interfaces.GetPackages;
import com.czyzowsk.mapsit.models.*;
import com.here.android.mpa.common.*;
import com.here.android.mpa.mapping.*;
import com.here.android.mpa.routing.RouteManager;
import com.here.android.mpa.routing.RouteOptions;
import com.here.android.mpa.routing.RoutePlan;
import com.here.android.mpa.routing.RouteResult;
import com.here.android.mpa.search.*;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    // permissions request code
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;

    Retrofit retrofit;
    String baseURL = "http://autocomplete.geocoder.api.here.com/6.2/";
    String baseAPIURL = "https://9aa07144.ngrok.io/";

    /**
     * Permissions that need to be explicitly requested from end user.
     */
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    MapFragment mapFragment;
    Map map;
    GeoCoordinate coordinate = new GeoCoordinate(50.0281837, 19.9087314);
    android.support.v7.widget.SearchView searchView;
    TextView logText;
    MapRoute mapRoute;
    MapRoute proposedMapRoute;
    ListView hints;
    ArrayList<String> adresses;
    ArrayList<Packages> packages;
    ArrayAdapter adapter;

    User user = new User();
    UserAuth userAuth = new UserAuth();

    GeoCoordinate destination;

    ArrayList<MapObject> objects;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String userName = getIntent().getStringExtra("name").isEmpty() ? "Nieznajomy"
                : getIntent().getStringExtra("name");
        String userSurrname = getIntent().getStringExtra("surrname").isEmpty() ? ""
                : getIntent().getStringExtra("surrname");
        String userEmail = getIntent().getStringExtra("email").isEmpty() ? "-"
                : getIntent().getStringExtra("email");
        String userToken = getIntent().getStringExtra("token").isEmpty() ? "-"
                : getIntent().getStringExtra("token");

        user.setEmail(userEmail);
        user.setEmail(userName);
        user.setEmail(userSurrname);
        userAuth.setAuth(true);
        userAuth.setToken(userToken);

        final String query = "";

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (coordinate != null)
                    map.setCenter(coordinate, Map.Animation.NONE);

                if(mapRoute != null) {
                    map.removeMapObjects(objects);
                    objects.clear();
                    packages.clear();
                }


                Snackbar.make(view, "Locating...", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        logText = (TextView) findViewById(R.id.log_text);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View navView = navigationView.getHeaderView(0);

        TextView userNameText = (TextView) navView.findViewById(R.id.user_name_text);
        TextView userEmailText = (TextView) navView.findViewById(R.id.user_email_text);

        userNameText.setText(getString(R.string.user_full_name, userName, userSurrname));
        userEmailText.setText(userEmail);

        objects = new ArrayList<>();

        hints = (ListView) findViewById(R.id.hints);
        adresses = new ArrayList<>();
        packages = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, adresses);
        hints.setAdapter(adapter);
        hints.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String temp = adresses.get(position);
                ResultListener<List<Location>> listener = new GeocodeListener();
                GeocodeRequest request = new GeocodeRequest(temp).setCollectionSize(1).setSearchArea(coordinate, 400);
                if (request.execute(listener) != ErrorCode.NONE) {

                }
                searchView.setQuery(temp, false);
                adapter.clear();
            }
        });
    }

    class GeocodeListener implements ResultListener<List<Location>> {
        @Override
        public void onCompleted(List<Location> data, ErrorCode error) {
            if (error != ErrorCode.NONE) {
                Log.e("Blad", error.toString());
            } else {
                destination = data.get(0).getCoordinate();
                ArrayList<GeoCoordinate> l = new ArrayList<>();
                l.add(destination);
                l.add(coordinate);
                getDirections(coordinate, destination);
                drawPointsOnMap(l);
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        checkPermissions();
    }

    @SuppressWarnings("deprecation")
    private MapFragment getMapFragment() {
        return (MapFragment) getFragmentManager().findFragmentById(R.id.fragment);
    }

    private void initialize() {

        // Search for the map fragment to finish setup by calling init().
        mapFragment = getMapFragment();
        mapFragment.init(new OnEngineInitListener() {
            @Override
            public void onEngineInitializationCompleted(OnEngineInitListener.Error error) {
                if (error == OnEngineInitListener.Error.NONE) {
                    // retrieve a reference of the map from the map fragment
                    map = mapFragment.getMap();
                    // Set the map center coordinate to the Vancouver region (no animation)
                    map.setCenter(new GeoCoordinate(50.0281837, 19.9087314, 0.0),
                            Map.Animation.NONE);
                    // Set the map zoom level to the average between min and max (no animation)
                    map.setZoomLevel((map.getMaxZoomLevel() + map.getMinZoomLevel()) / 2);
                    map.getPositionIndicator().setVisible(true);
                    PositioningManager.getInstance().start(PositioningManager.LocationMethod.GPS_NETWORK);
                    mapFragment.getMapGesture().addOnGestureListener(listener);
                } else {
                    Log.e("LOG", "Cannot initialize MapFragment (" + error + ")");
                }
            }
        });

        PositioningManager.getInstance().addListener(new WeakReference<>(positionListener));

    }

    private PositioningManager.OnPositionChangedListener positionListener = new
            PositioningManager.OnPositionChangedListener() {

                public void onPositionUpdated(PositioningManager.LocationMethod method,
                                              GeoPosition position, boolean isMapMatched) {
                    // set the center only when the app is in the foreground
                    // to reduce CPU consumption
                    coordinate = position.getCoordinate();
                    logText.setText(coordinate.toString());

                }

                public void onPositionFixChanged(PositioningManager.LocationMethod method,
                                                 PositioningManager.LocationStatus status) {
                }
            };

    protected void checkPermissions() {
        final List<String> missingPermissions = new ArrayList<String>();
        // check all required dynamic permissions
        for (final String permission : REQUIRED_SDK_PERMISSIONS) {
            final int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }
        if (!missingPermissions.isEmpty()) {
            // request all missing permissions
            final String[] permissions = missingPermissions
                    .toArray(new String[missingPermissions.size()]);
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_ASK_PERMISSIONS);
        } else {
            final int[] grantResults = new int[REQUIRED_SDK_PERMISSIONS.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(REQUEST_CODE_ASK_PERMISSIONS, REQUIRED_SDK_PERMISSIONS,
                    grantResults);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                for (int index = permissions.length - 1; index >= 0; --index) {
                    if (grantResults[index] != PackageManager.PERMISSION_GRANTED) {
                        // exit the app if one permission is not granted
                        Toast.makeText(this, "Required permission '" + permissions[index]
                                + "' not granted, exiting", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                }
                // all permissions were granted
                initialize();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.map, menu);

        searchView = (android.support.v7.widget.SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setIconifiedByDefault(false);

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        searchView.setOnQueryTextListener(new android.support.v7.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                new GetPackagesAsync().execute();
                adresses.clear();
                adapter.notifyDataSetChanged();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                new QueryTask().execute(s);

                return false;
            }
        });
        return true;
    }

    private class GetPackagesAsync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            packages.clear();
            retrofit = new Retrofit.Builder().baseUrl(baseAPIURL).addConverterFactory(GsonConverterFactory.create()).build();

            GetPackages userClient = retrofit.create(GetPackages.class);
            Call<ArrayList<Packages>> call = userClient.getPackages(userAuth.getToken());
            Response<ArrayList<Packages>> response;
            try {
                response = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (response.body() != null) {
                packages = response.body();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

        }


        @Override
        protected void onPostExecute(Void result) {
            drawPointsOnMap(packages, true);
        }
    }

    private class QueryTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... arg0) {
            adresses.clear();
            retrofit = new Retrofit.Builder().baseUrl(baseURL).addConverterFactory(GsonConverterFactory.create()).build();

            GetGeocodings userClient = retrofit.create(GetGeocodings.class);
            Call<Suggestions> call = userClient.getSuggestions("4ijUqlXvIviy8nwHMA4w",
                    "M_MBVY4t_IsHgtc0xbWuSQ", arg0[0], "", "");
            Response<Suggestions> response;
            try {
                response = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }

            if (response.body() != null) {
                for (Suggestion iterator : response.body().getSuggestions()) {
                    adresses.add(iterator.getLabel());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    // Functionality for taps of the "Get Directions" button
    public void getDirections(GeoCoordinate from, GeoCoordinate to) {
        // 1. clear previous results
        if (map != null && mapRoute != null) {
            map.removeMapObject(mapRoute);
            objects.remove(mapRoute);
            mapRoute = null;
        }


        // 2. Initialize RouteManager
        RouteManager routeManager = new RouteManager();

        // 3. Select routing options
        RoutePlan routePlan = new RoutePlan();

        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);

        // 4. Select Waypoints for your routes
        routePlan.addWaypoint(from);

        // END: Airport, YVR
        routePlan.addWaypoint(to);

        // 5. Retrieve Routing information via RouteManagerEventListener
        RouteManager.Error error = routeManager.calculateRoute(routePlan, routeManagerListener);
        if (error != RouteManager.Error.NONE) {
            Toast.makeText(getApplicationContext(),
                    "Route calculation failed with: " + error.toString(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getDirections(ArrayList<GeoCoordinate> coordinates) {
        // 1. clear previous results
        if (map != null && mapRoute != null) {
            map.removeMapObject(mapRoute);
            objects.remove(mapRoute);
            mapRoute = null;
        }


        // 2. Initialize RouteManager
        RouteManager routeManager = new RouteManager();

        // 3. Select routing options
        RoutePlan routePlan = new RoutePlan();

        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);

        // 4. Select Waypoints for your routes
        for (GeoCoordinate coordinate : coordinates)
            if (coordinate != null)
                routePlan.addWaypoint(coordinate);

        // 5. Retrieve Routing information via RouteManagerEventListener
        RouteManager.Error error = routeManager.calculateRoute(routePlan, routeManagerListener);
        if (error != RouteManager.Error.NONE) {
            Toast.makeText(getApplicationContext(),
                    "Route calculation failed with: " + error.toString(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void getProposeDirections(ArrayList<GeoCoordinate> coordinates) {
        // 1. clear previous results
        if (map != null && proposedMapRoute != null) {
            map.removeMapObject(proposedMapRoute);
            objects.remove(proposedMapRoute);
            proposedMapRoute = null;
        }

        // 2. Initialize RouteManager
        RouteManager routeManager = new RouteManager();

        // 3. Select routing options
        RoutePlan routePlan = new RoutePlan();

        RouteOptions routeOptions = new RouteOptions();
        routeOptions.setTransportMode(RouteOptions.TransportMode.CAR);
        routeOptions.setRouteType(RouteOptions.Type.FASTEST);
        routePlan.setRouteOptions(routeOptions);

        // 4. Select Waypoints for your routes
        for (GeoCoordinate coordinate : coordinates)
            if (coordinate != null)
                routePlan.addWaypoint(coordinate);

        // 5. Retrieve Routing information via RouteManagerEventListener
        RouteManager.Error error = routeManager.calculateRoute(routePlan, proposeMapRouteListener);
        if (error != RouteManager.Error.NONE) {
            Toast.makeText(getApplicationContext(),
                    "Route calculation failed with: " + error.toString(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void drawPointsOnMap(ArrayList<GeoCoordinate> coordinants) {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker);
        Image im = new Image();
        im.setBitmap(image);
        for (GeoCoordinate i : coordinants)
            if (i != null) {
            MapMarker marker = new MapMarker(i, im);
                objects.add(marker);
                map.addMapObject(marker);
            }
    }


    private void drawPointsOnMap(ArrayList<Packages> packages, boolean bool) {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.marker);
        Image im = new Image();
        im.setBitmap(image);
        for (Packages i : packages) {
            MapMarker marker = new MapMarker(new GeoCoordinate(i.getFromXPos(), i.getFromYPos()), im);
            marker.setDescription("aaa");
            objects.add(marker);
            map.addMapObject(objects.get(objects.size() - 1));
        }
    }

    private RouteManager.Listener routeManagerListener = new RouteManager.Listener() {
        public void onCalculateRouteFinished(RouteManager.Error errorCode,
                                             List<RouteResult> result) {

            if (errorCode == RouteManager.Error.NONE && result.get(0).getRoute() != null) {
                // create a map route object and place it on the map
                mapRoute = new MapRoute(result.get(0).getRoute());
                objects.add(mapRoute);
                map.addMapObject(mapRoute);

                // Get the bounding box containing the route and zoom in (no animation)
                GeoBoundingBox gbb = result.get(0).getRoute().getBoundingBox();
                map.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION);


            } else {

            }
        }

        public void onProgress(int percentage) {
        }
    };

    private RouteManager.Listener proposeMapRouteListener = new RouteManager.Listener() {
        public void onCalculateRouteFinished(RouteManager.Error errorCode,
                                             List<RouteResult> result) {

            if (errorCode == RouteManager.Error.NONE && result.get(0).getRoute() != null) {
                // create a map route object and place it on the map
                proposedMapRoute = new MapRoute(result.get(0).getRoute()).setColor(R.color.colorAccent);
                objects.add(proposedMapRoute);
                map.addMapObject(proposedMapRoute);

                // Get the bounding box containing the route and zoom in (no animation)
                GeoBoundingBox gbb = result.get(0).getRoute().getBoundingBox();
                map.zoomTo(gbb, Map.Animation.NONE, Map.MOVE_PRESERVE_ORIENTATION);


            } else {

            }
        }

        public void onProgress(int percentage) {
        }
    };


    MapGesture.OnGestureListener listener =
            new MapGesture.OnGestureListener.OnGestureListenerAdapter() {
                @Override
                public boolean onMapObjectsSelected(List<ViewObject> mObjects) {
                    for (ViewObject viewObj : mObjects) {
                        if (viewObj.getBaseType() == ViewObject.Type.USER_OBJECT) {
                            if (((MapObject)viewObj).getType() == MapObject.Type.MARKER) {
                                MapMarker selectedMapMarker = ((MapMarker) viewObj);
                                selectedMapMarker.getDescription();

                                if(objects.contains(viewObj)){
                                    final Packages pack = packages.get(3);
                                    final ArrayList<GeoCoordinate> coordinates = new ArrayList<>();
                                    coordinates.add(coordinate);
                                    coordinates.add(new GeoCoordinate(pack.getFromXPos(), pack.getFromYPos()));
                                    coordinates.add(new GeoCoordinate(pack.getToXPos(), pack.getToYPos()));
                                    if(destination != null)
                                        coordinates.add(destination);
                                    AlertDialog.Builder dialog = new AlertDialog.Builder(MapActivity.this);
                                    dialog.setTitle("Przejmij paczkę");
                                    String[] message = {"Wynagrodzenie: " + pack.getProposedPrice(),
                                            "Status: " + pack.getStatus(),
                                            "Opis: " + pack.getDescription(),
                                            "Waga: " + pack.getWeight(),
                                            "Wymiary: " + pack.getWidth() + "x" + pack.getWidth() + "x" + pack.getHeight()};
                                    dialog.setItems(message, null);
                                    dialog.setCancelable(true);
                                    dialog.setPositiveButton("Odbierz", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getDirections(coordinates);
                                            drawPointsOnMap(coordinates);
                                        }
                                    });
                                    dialog.setNeutralButton("Sprawdz trasę", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            getProposeDirections(coordinates);
                                            drawPointsOnMap(coordinates);
                                        }
                                    });
                                    AlertDialog dialog1 = dialog.create();
                                    dialog1.show();
                                }
                            }
                        }
                    }
                    // return false to allow the map to handle this callback also
                    return false;
                }
            };


}
