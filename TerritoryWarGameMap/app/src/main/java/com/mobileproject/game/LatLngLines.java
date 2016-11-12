package com.mobileproject.game;

/**
 * Created by 100520993 on 11/11/2016.
 */

import com.google.android.gms.maps.model.LatLng;

/**
 * Holds points for the polylines.
 */
class LatLngLines {
    private LatLng pointA, pointB;

    public LatLngLines() {
        pointA = null;
        pointB = null;
    }

    public LatLngLines(LatLng A, LatLng B) {
        pointA = A;
        pointB = B;
    }

    public void setLine(LatLng A, LatLng B) {
        pointA = A;
        pointB = B;
    }

    public LatLng getA() {
        return pointA;
    }

    public LatLng getB() {
        return pointB;
    }
}
