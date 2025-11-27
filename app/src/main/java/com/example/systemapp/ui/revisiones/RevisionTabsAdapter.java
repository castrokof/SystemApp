package com.example.systemapp.ui.revisiones;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.systemapp.data.model.DBOrdenRevision;
import com.example.systemapp.ui.revisiones.tabs.*;

/**
 * Adapter para el ViewPager2 de los 6 tabs de revisión
 */
public class RevisionTabsAdapter extends FragmentStateAdapter {

    private DBOrdenRevision orden;
    private Tab1LecturaFragment tab1;
    private Tab2ResidenteFragment tab2;
    private Tab3AcometidaFragment tab3;
    private Tab4CensosFragment tab4;
    private Tab5ClasificacionFragment tab5;
    private Tab6CierreFragment tab6;

    public RevisionTabsAdapter(@NonNull FragmentActivity fragmentActivity, DBOrdenRevision orden) {
        super(fragmentActivity);
        this.orden = orden;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                tab1 = Tab1LecturaFragment.newInstance(orden);
                return tab1;
            case 1:
                tab2 = Tab2ResidenteFragment.newInstance(orden);
                return tab2;
            case 2:
                tab3 = Tab3AcometidaFragment.newInstance(orden);
                return tab3;
            case 3:
                tab4 = Tab4CensosFragment.newInstance(orden);
                return tab4;
            case 4:
                tab5 = Tab5ClasificacionFragment.newInstance(orden);
                return tab5;
            case 5:
                tab6 = Tab6CierreFragment.newInstance(orden);
                return tab6;
            default:
                return tab1;
        }
    }

    @Override
    public int getItemCount() {
        return 6;
    }

    // Getters para acceder a los fragments
    public Tab1LecturaFragment getTab1() { return tab1; }
    public Tab2ResidenteFragment getTab2() { return tab2; }
    public Tab3AcometidaFragment getTab3() { return tab3; }
    public Tab4CensosFragment getTab4() { return tab4; }
    public Tab5ClasificacionFragment getTab5() { return tab5; }
    public Tab6CierreFragment getTab6() { return tab6; }
}
