package com.ipwnage.betterfly;

public class PlayerPrefs {
	private boolean _state;
	private float _speed;
	
	public PlayerPrefs(boolean state, float speed) {
		this._state = state;
		this._speed = speed;
	}
	
	public PlayerPrefs(String loaded) {
		String[] savedprefs = loaded.split(",");
		this._state = Boolean.valueOf(savedprefs[0]);
		this._speed = Float.valueOf(savedprefs[1]);
	}
	
	public Float getSpeed() {
		return _speed;
	}
	
	public Boolean getFly() {
		return _state;
	}
	
	public void toggleFly() {
		this._state = !_state;
	}
	
	public void setSpeed(float speed) {
		this._speed = speed;
	}
	
	public String toString() {
		return String.valueOf(this._state) + "," + String.valueOf(this._speed);
	}
}
