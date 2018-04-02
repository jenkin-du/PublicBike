package com.android.djs.publicbike.bean;

import com.supermap.android.maps.Point2D;

public class BusStation {

	private String name;
	private int id;
	private Address address;

	public Point2D getPoint(){
		return new Point2D(address.getX(),address.getY());
	}


	public BusStation() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return "BusStation [id=" + id + ", address=" + address + ", name=" + name + "]";
	}

}
