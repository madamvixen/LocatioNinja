package com.mobileapp.rutgers.locationinja;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Malabika on 10/10/2015.
 */
public class checkedInLocation {

    private int _id;
    private String _nameofplace, _latitude, _longitude;
//    private String _latlngentries;

    public checkedInLocation(int id, String nameofplace, String latitude, String longitude){
        _id = id;
        _nameofplace = nameofplace;
        _latitude = latitude;
        _longitude = longitude;
    }

    public int getId(){ return _id;}
    public String get_nameofplace(){ return _nameofplace;}
    public String get_latitude(){ return _latitude;}
    public String get_longitude(){ return _longitude;}

    public void set_id(int id){_id = id; }
    public void set_nameofplace(String nameofplace){_nameofplace = nameofplace; }
}
