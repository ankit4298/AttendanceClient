package LocationTracking;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;

import java.util.ArrayList;
import java.util.List;

public class GeoFence {

    // polygon coordinates
    final List<LatLng> poly = new ArrayList<>();
    LatLngBounds bounds;


    public GeoFence() {
        // POLYGON Bounds - (GEOFence)
        poly.add(new LatLng(20.015367, 73.763616)); // 3    TL
        poly.add(new LatLng(20.015311, 73.764276)); // 4    TR
        poly.add(new LatLng(20.014469, 73.764178)); // 5
        poly.add(new LatLng(20.014343, 73.764696)); // 6
        poly.add(new LatLng(20.013637, 73.764554)); // 1    BR
        poly.add(new LatLng(20.013684, 73.763493)); // 2    BL

        bounds = new LatLngBounds(poly.get(5), poly.get(1));    // a/c to TL,BR
//        bounds = new LatLngBounds(poly.get(4), poly.get(6));    // a/c to NorthEast / SouthWest
    }


    // overloaded methods for checking in out
    public boolean checkAgainstBounds(double lat, double lng) {
        boolean inout = PolyUtil.containsLocation(lat, lng, poly, true);
        return inout;
    }

    public boolean checkAgainstBounds(LatLng coords) {
        boolean inout = PolyUtil.containsLocation(coords, poly, true);
        return inout;
    }

    public boolean isWithinCircle(double center_lat, double center_lng, double lat, double lng, double radius) {

        LatLng center_latlng = new LatLng(center_lat, center_lng);
        LatLng latlng = new LatLng(lat, lng);

        double distance = SphericalUtil.computeDistanceBetween(center_latlng, latlng);

        if (distance < radius) {
            return true;
        } else {
            return false;
        }

    }

    public boolean isWithinCircle(LatLng center_latlng, double currLat, double currLng, double radius) {

        LatLng latlng = new LatLng(currLat, currLng);

        double distance = SphericalUtil.computeDistanceBetween(center_latlng, latlng);

        if (distance < radius) {
            return true;
        } else {
            return false;
        }
    }


}
