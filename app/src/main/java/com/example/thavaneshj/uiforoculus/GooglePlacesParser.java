package com.example.thavaneshj.uiforoculus;

import java.util.List;

/**
 * Created by NEW on 2/17/2019.
 */

public class GooglePlacesParser {

    public List<Object> htmlAttributions = null;
    public String nextPageToken;
    public List<Result> results = null;
    public String status;


    public class Geometry {

        public Location location;
        public Viewport viewport;

    }

    public class Location {

        public Float lat;
        public Float lng;

    }

    public class Northeast {

        public Float lat;
        public Float lng;

    }

    public class OpeningHours {

        public Boolean openNow;

    }

    public class Photo {

        public Integer height;
        public List<String> htmlAttributions = null;
        public String photoReference;
        public Integer width;

    }

    public class PlusCode {

        public String compoundCode;
        public String globalCode;

    }

    public class Result {

        public Geometry geometry;
        public String icon;
        public String id;
        public String name;
        public OpeningHours openingHours;
        public List<Photo> photos = null;
        public String placeId;
        public PlusCode plusCode;
        public Float rating;
        public String reference;
        public String scope;
        public List<String> types = null;
        public Integer userRatingsTotal;
        public String vicinity;


    }

    public class Southwest {

        public Float lat;
        public Float lng;

    }

    public class Viewport {

        public Northeast northeast;
        public Southwest southwest;

    }

}
