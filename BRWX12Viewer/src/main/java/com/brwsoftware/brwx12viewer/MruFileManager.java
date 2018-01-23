package com.brwsoftware.brwx12viewer;

import java.io.File;
import java.util.LinkedList;
import java.util.prefs.Preferences;

public class MruFileManager {

	private static final String PREF_MRU_KEY = "mru_";
	
	private int maxItems = 4;
	private LinkedList<File> mruList = new LinkedList<File>();

	//Singleton implementation
	private static MruFileManager instance;
	private MruFileManager() {		
	}
    public static MruFileManager getInstance(){
        if(instance == null){
        	instance = new MruFileManager();
        }
        return instance;
    }

	private int find(File file) {
		int found = -1;
		int count = mruList.size();
		for (int i = 0; i < count; i++) {
			if (mruList.get(i).equals(file)) {
				found = i;
				break;
			}
		}

		return found;
	}

	public void add(File file) {
		int index = find(file);
		if (index == -1) {
			mruList.addFirst(file);
		} else {
			File mruFile = mruList.remove(index);
			mruList.addFirst(mruFile);
		}
		if (mruList.size() > maxItems) {
			mruList.removeLast();
		}
	}
	
	public int size() {
		return mruList.size();
	}

	public File get(int index) {
		return mruList.get(index);
	}
	
	public void save() {
		Preferences pref = 	Preferences.userNodeForPackage(MruFileManager.class);
		int count = mruList.size();
		for (int i = 0; i < count; i++) {
			String key = String.format("%s%d", PREF_MRU_KEY, i + 1);
			pref.put(key, mruList.get(i).getPath());
		}		
	}
	
	public void load() {
		Preferences pref = 	Preferences.userNodeForPackage(MruFileManager.class);
		for (int i = 0; i < maxItems; i++) {
			String key = String.format("%s%d", PREF_MRU_KEY, i + 1);
			String path = pref.get(key, null);
			if(path != null) {
				mruList.add(new File(path));
			}
		}
	}
}
