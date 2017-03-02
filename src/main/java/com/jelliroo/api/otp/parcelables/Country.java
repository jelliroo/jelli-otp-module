package com.jelliroo.api.otp.parcelables;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by roger on 2/28/2017.
 */

public class Country implements Parcelable{

    private String name;

    private Integer code;

    private Country(){}


    protected Country(Parcel in) {
        name = in.readString();
        code = in.readByte() == 0x00 ? null : in.readInt();
    }


    public static Country getInstance(String name, Integer code){
        Country country = new Country();
        country.setName(name);
        country.setCode(code);
        return country;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        if(code == null){
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) 0x01);
            dest.writeInt(code);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Country> CREATOR = new Creator<Country>() {
        @Override
        public Country createFromParcel(Parcel in) {
            return new Country(in);
        }

        @Override
        public Country[] newArray(int size) {
            return new Country[size];
        }
    };


    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    @Override
    public int hashCode() {
        return code;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Country){
            Country country = (Country) obj;
            if(country.code != null && code != null){
                if(country.code == this.code){
                    return true;
                } else return false;
            } else return false;
        } else return false;
    }
}
