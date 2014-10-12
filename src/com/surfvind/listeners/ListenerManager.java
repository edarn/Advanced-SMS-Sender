package com.surfvind.listeners;

import java.util.Hashtable;
import java.util.Vector;

public abstract class ListenerManager {

	public static Vector<Listener> mainInput1Listeners;
	
	public static Hashtable<Integer, Vector<Listener>> listeners;
	
	public static void registerListener(int id, Listener listener) {
		if(listeners == null) {
			listeners = new Hashtable<Integer, Vector<Listener>>();
		}
		
		Vector<Listener> tmp;
		if(listeners.contains(id)) {
			tmp = listeners.get(id);
			tmp.add(listener);
		} else {
			tmp = new Vector<Listener>();
			tmp.add(listener);
		}
		listeners.put(id, tmp);
	}
	
	public synchronized static void onEvent(int id, Object o, CallbackMessage message) {
		Vector<Listener> v = listeners.get(id);
		for(Listener l : v) {
			l.onEvent(o, message);
		}
	}
	
}
