package com.android.djs.publicbike.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.supermap.android.maps.Point2D;

public class BikeStation implements Parcelable{

	private String id;
	private Address address;
	private int totalNumber;
	private int leftNumber;
	private String stationName;

	public Point2D getPoint(){

		return new Point2D(address.getX(),address.getY());
	}


	protected BikeStation(Parcel in) {
		id = in.readString();
		address = in.readParcelable(Address.class.getClassLoader());
		totalNumber = in.readInt();
		leftNumber = in.readInt();
		stationName = in.readString();
	}

	public static final Creator<BikeStation> CREATOR = new Creator<BikeStation>() {
		@Override
		public BikeStation createFromParcel(Parcel in) {
			return new BikeStation(in);
		}

		@Override
		public BikeStation[] newArray(int size) {
			return new BikeStation[size];
		}
	};

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(int totalNumber) {
		this.totalNumber = totalNumber;
	}

	public int getLeftNumber() {
		return leftNumber;
	}

	public void setLeftNumber(int leftNumber) {
		this.leftNumber = leftNumber;
	}

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    @Override
    public String toString() {
        return "BikeStation{" +
                "id='" + id + '\'' +
                ", address=" + address +
                ", totalNumber=" + totalNumber +
                ", leftNumber=" + leftNumber +
                ", stationName='" + stationName + '\'' +
                '}';
    }

    public BikeStation() {

	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeParcelable(address, flags);
		dest.writeInt(totalNumber);
		dest.writeInt(leftNumber);
		dest.writeString(stationName);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof BikeStation)) return false;

		BikeStation station = (BikeStation) o;

		if (getTotalNumber() != station.getTotalNumber()) return false;
		if (getLeftNumber() != station.getLeftNumber()) return false;
		if (getId() != null ? !getId().equals(station.getId()) : station.getId() != null)
			return false;
		if (getAddress() != null ? !getAddress().equals(station.getAddress()) : station.getAddress() != null)
			return false;
		return getStationName() != null ? getStationName().equals(station.getStationName()) : station.getStationName() == null;

	}

	@Override
	public int hashCode() {
		int result = getId() != null ? getId().hashCode() : 0;
		result = 31 * result + (getAddress() != null ? getAddress().hashCode() : 0);
		result = 31 * result + getTotalNumber();
		result = 31 * result + getLeftNumber();
		result = 31 * result + (getStationName() != null ? getStationName().hashCode() : 0);
		return result;
	}
}
