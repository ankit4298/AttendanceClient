package LocationTracking;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;

import java.util.ArrayList;
import java.util.List;

public class GeoFence {

    // test coords
    double lat = 20.014316;
    double lng = 73.764120;
    LatLng IN_testCoords = new LatLng(20.014325, 73.763782);
    LatLng OUT_testCoords = new LatLng(20.015979, 73.760338);
    // ------

    // polygon coordinates
    final List<LatLng> poly = new ArrayList<>();
    LatLngBounds bounds;

    //test home coords
    //20.005409, 73.801264      TL
    //20.005418, 73.801585
    //20.005195, 73.801595
    //20.005138, 73.801307      BL

    public GeoFence(){
        // POLYGON Bounds - (GEOFence)
        poly.add(new LatLng(20.014919, 73.762501)); // 1    Top Left
        poly.add(new LatLng(20.015005, 73.764832)); // 2    TR
        poly.add(new LatLng(20.013648, 73.764525)); // 3    BR
        poly.add(new LatLng(20.013790, 73.762158)); // 4    BL
        bounds = new LatLngBounds(poly.get(3), poly.get(1));
    }


    // overloaded methods for checking in out
    public boolean checkAgainstBounds(double lat,double lng){
        boolean inout = PolyUtil.containsLocation(lat,lng, poly, true);
        return inout;
    }
    public boolean checkAgainstBounds(LatLng coords){
        boolean inout = PolyUtil.containsLocation(coords, poly, true);
        return inout;
    }


}
