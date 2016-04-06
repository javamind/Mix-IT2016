package com.ehret.mixit.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.adapter.ListMemberAdapter;
import com.ehret.mixit.adapter.ListTalkAdapter;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;
import com.ehret.mixit.utils.UIUtils;

public class DataListFragment extends Fragment {

    private ListView liste;

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DataListFragment newInstance(String typeAppel, String filterQuery, int sectionNumber) {
        DataListFragment fragment = new DataListFragment();
        Bundle args = new Bundle();
        args.putString(UIUtils.ARG_LIST_TYPE, typeAppel);
        args.putString(UIUtils.ARG_LIST_FILTER, filterQuery);
        args.putInt(UIUtils.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putAll(getArguments());
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity!=null && getArguments().getString(UIUtils.ARG_LIST_TYPE)!=null){
            ((HomeActivity) getActivity()).onSectionAttached(
                    "title_" + getArguments().getString(UIUtils.ARG_LIST_TYPE),
                    "color_" + getArguments().getString(UIUtils.ARG_LIST_TYPE));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_datalist, container, false);

        //Handle with layout
        this.liste = (ListView) rootView.findViewById(R.id.liste_content);

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(savedInstanceState!=null && getArguments()!=null && getArguments().getInt(UIUtils.ARG_SECTION_NUMBER)==0){
            getArguments().putString(UIUtils.ARG_LIST_TYPE, getArguments().getString(UIUtils.ARG_LIST_TYPE));
            getArguments().putString(UIUtils.ARG_LIST_FILTER, getArguments().getString(UIUtils.ARG_LIST_FILTER));
            getArguments().putInt(UIUtils.ARG_SECTION_NUMBER, getArguments().getInt(UIUtils.ARG_SECTION_NUMBER));
            setArguments(savedInstanceState);
        }

    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();
        switch (TypeFile.getTypeFile(getArguments().getString(UIUtils.ARG_LIST_TYPE))) {
            case members:
                afficherMembre();
                break;
            case staff:
                afficherMembre();
                break;
            case sponsor:
                afficherMembre();
                break;
            case talks:
                afficherConference();
                break;
            case workshops:
                afficherConference();
                break;
            case lightningtalks:
                afficherConference();
                break;
            case favorites:
                afficherConference();
                break;
            default:
                //Par defaut on affiche les speakers
                afficherMembre();

        }
    }


    /**
     * Affichage des conferences
     */
    private void afficherMembre() {
        Context context = getActivity().getBaseContext();

        liste.setClickable(true);
        liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Member membre = (Member) liste.getItemAtPosition(position);
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        PeopleDetailFragment.newInstance(
                                getArguments().getString(UIUtils.ARG_LIST_TYPE),
                                membre.getIdMember(),
                                getArguments().getInt(UIUtils.ARG_SECTION_NUMBER)),
                        getArguments().getString(UIUtils.ARG_LIST_TYPE));
            }
        });

        //On trie la liste retournée
        liste.setAdapter(
                new ListMemberAdapter(
                        context,
                        MembreFacade.getInstance().getMembres(
                                context,
                                getArguments().getString(UIUtils.ARG_LIST_TYPE),
                                getArguments().getString(UIUtils.ARG_LIST_FILTER))));
    }


    /**
     * Affichage des confs
     */
    private void afficherConference() {
        Context context = getActivity().getBaseContext();
        liste.setClickable(true);
        liste.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Talk conf = (Talk) liste.getItemAtPosition(position);
                ((HomeActivity) getActivity()).changeCurrentFragment(
                        SessionDetailFragment.newInstance(
                                getArguments().getString(UIUtils.ARG_LIST_TYPE),
                                conf.getIdSession(),
                                getArguments().getInt(UIUtils.ARG_SECTION_NUMBER)),
                        getArguments().getString(UIUtils.ARG_LIST_TYPE));
            }
        });
        String filter = getArguments().getString(UIUtils.ARG_LIST_FILTER);

        switch (TypeFile.getTypeFile(getArguments().getString(UIUtils.ARG_LIST_TYPE))) {
            case workshops:
                liste.setAdapter(new ListTalkAdapter(context, ConferenceFacade.getInstance().getWorkshops(context, filter)));
                break;
            case talks:
                liste.setAdapter(new ListTalkAdapter(context, ConferenceFacade.getInstance().getTalks(context, filter)));
                break;
            case lightningtalks:
                liste.setAdapter(new ListTalkAdapter(context, ConferenceFacade.getInstance().getLightningTalks(context, filter)));
                break;
            default:
                liste.setAdapter(new ListTalkAdapter(context, ConferenceFacade.getInstance().getFavorites(context, filter)));

        }
    }


}
