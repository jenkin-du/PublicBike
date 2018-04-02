package com.android.djs.publicbike.bean;

import android.os.Parcel;
import android.os.Parcelable;

public class Address implements Parcelable{

    private String id;
    private String province;
    private String city;
    private String district;
    private String detailAddr;

    private double x;
    private double y;

    protected Address(Parcel in) {
        id = in.readString();
        province = in.readString();
        city = in.readString();
        district = in.readString();
        detailAddr = in.readString();
        x = in.readDouble();
        y = in.readDouble();
    }

    public static final Creator<Address> CREATOR = new Creator<Address>() {
        @Override
        public Address createFromParcel(Parcel in) {
            return new Address(in);
        }

        @Override
        public Address[] newArray(int size) {
            return new Address[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getDetailAddr() {
        return detailAddr;
    }

    public void setDetailAddr(String detailAddr) {
        this.detailAddr = detailAddr;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", province='" + province + '\'' +
                ", city='" + city + '\'' +
                ", district='" + district + '\'' +
                ", detailAddr='" + detailAddr + '\'' +
                ", x=" + x +
                ", y=" + y +
                '}';
    }

    public Address() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(province);
        dest.writeString(city);
        dest.writeString(district);
        dest.writeString(detailAddr);
        dest.writeDouble(x);
        dest.writeDouble(y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Address)) return false;

        Address address = (Address) o;

        if (Double.compare(address.getX(), getX()) != 0) return false;
        if (Double.compare(address.getY(), getY()) != 0) return false;
        if (getId() != null ? !getId().equals(address.getId()) : address.getId() != null)
            return false;
        if (getProvince() != null ? !getProvince().equals(address.getProvince()) : address.getProvince() != null)
            return false;
        if (getCity() != null ? !getCity().equals(address.getCity()) : address.getCity() != null)
            return false;
        if (getDistrict() != null ? !getDistrict().equals(address.getDistrict()) : address.getDistrict() != null)
            return false;
        return getDetailAddr() != null ? getDetailAddr().equals(address.getDetailAddr()) : address.getDetailAddr() == null;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getId() != null ? getId().hashCode() : 0;
        result = 31 * result + (getProvince() != null ? getProvince().hashCode() : 0);
        result = 31 * result + (getCity() != null ? getCity().hashCode() : 0);
        result = 31 * result + (getDistrict() != null ? getDistrict().hashCode() : 0);
        result = 31 * result + (getDetailAddr() != null ? getDetailAddr().hashCode() : 0);
        temp = Double.doubleToLongBits(getX());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
