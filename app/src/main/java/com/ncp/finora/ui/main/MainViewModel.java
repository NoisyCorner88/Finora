package com.ncp.finora.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/** Menyimpan tab aktif; dipakai bersama antara MainActivity dan fragment-nya. */
public class MainViewModel extends ViewModel {
    public static final int TAB_HOME = 0;
    public static final int TAB_STATS = 1;
    public static final int TAB_ACTIVITY = 2;
    public static final int TAB_PROFILE = 3;

    private final MutableLiveData<Integer> selectedTab = new MutableLiveData<>(TAB_HOME);

    public LiveData<Integer> getSelectedTab() {
        return selectedTab;
    }

    public void select(int tab) {
        if (!Integer.valueOf(tab).equals(selectedTab.getValue())) selectedTab.setValue(tab);
    }
}
