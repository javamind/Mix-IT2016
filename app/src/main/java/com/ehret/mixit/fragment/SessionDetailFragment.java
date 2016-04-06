/*
 * Copyright 2015 Guillaume EHRET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ehret.mixit.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ehret.mixit.HomeActivity;
import com.ehret.mixit.R;
import com.ehret.mixit.SalleActivity;
import com.ehret.mixit.builder.TextViewTableBuilder;
import com.ehret.mixit.domain.Salle;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.domain.talk.Speaker;
import com.ehret.mixit.domain.talk.Talk;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;
import com.ehret.mixit.utils.FileUtils;
import com.ehret.mixit.utils.HtmlTagHandler;
import com.ehret.mixit.utils.UIUtils;
import com.github.rjeschke.txtmark.Configuration;
import com.github.rjeschke.txtmark.Processor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Activité permettant d'afficher les informations sur un talk
 */
public class SessionDetailFragment extends Fragment {

    private TextView horaire;
    private TextView level;
    private TextView name;
    private TextView summary;
    private TextView descriptif;
    private TextView salle;
    private ImageView imageFavorite;
    private ImageView langImage;
    private LinearLayout sessionLinkList;
    private LinearLayout sessionPersonList;
    private LayoutInflater mInflater;
    private HtmlTagHandler htmlTagHandler = new HtmlTagHandler();

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SessionDetailFragment newInstance(String typeAppel, Long message, int sectionNumber) {
        SessionDetailFragment fragment = new SessionDetailFragment();
        Bundle args = new Bundle();
        args.putString(UIUtils.ARG_LIST_TYPE, typeAppel);
        args.putLong(UIUtils.ARG_ID, message);
        args.putInt(UIUtils.ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        ViewGroup rootView = (ViewGroup) inflater.inflate(R.layout.fragment_session, container, false);

        this.imageFavorite = (ImageView) rootView.findViewById(R.id.talk_image_favorite);
        this.horaire = (TextView) rootView.findViewById(R.id.talk_horaire);
        this.level = (TextView) rootView.findViewById(R.id.talk_level);
        this.name = (TextView) rootView.findViewById(R.id.talk_name);
        this.summary = (TextView) rootView.findViewById(R.id.talk_summary);
        this.descriptif = (TextView) rootView.findViewById(R.id.talk_desciptif);
        this.salle = (TextView) rootView.findViewById(R.id.talk_salle);
        this.sessionLinkList = (LinearLayout) rootView.findViewById(R.id.sessionLinkList);
        this.sessionPersonList = (LinearLayout) rootView.findViewById(R.id.sessionPersonList);
        this.langImage = (ImageView) rootView.findViewById(R.id.talk_image_language);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        ((HomeActivity) activity).onSectionAttached(
                "title_detail_" + getArguments().getString(UIUtils.ARG_LIST_TYPE),
                "color_" + getArguments().getString(UIUtils.ARG_LIST_TYPE));
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    /**
     * Recuperation des marques de la partie en cours
     */
    @Override
    public void onResume() {
        super.onResume();

        Context context = getActivity().getBaseContext();

        //On commence par recuperer le Membre que l'on sohaite afficher
        Long id = getArguments().getLong(UIUtils.ARG_ID);
        String type = getArguments().getString(UIUtils.ARG_LIST_TYPE);

        Talk conference;
        if (TypeFile.lightningtalks.name().equals(type)) {
            conference = ConferenceFacade.getInstance().getLightningtalk(context, id);
        } else if (TypeFile.workshops.name().equals(type)) {
            conference = ConferenceFacade.getInstance().getTalk(context, id);
        } else {
            conference = ConferenceFacade.getInstance().getTalk(context, id);
        }

        addGeneralInfo(conference);
        addSpeakerInfo(conference);
        addInterestInfo(conference);
    }

    private void addGeneralInfo(Talk conference){
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        if (conference.getStart() != null && conference.getEnd() != null) {
            horaire.setText(String.format(getResources().getString(R.string.periode),
                    sdf.format(conference.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getStart()),
                    DateFormat.getTimeInstance(DateFormat.SHORT).format(conference.getEnd())
            ));
        } else {
            horaire.setText(getResources().getString(R.string.pasdate));

        }
        level.setText("votes : "+ conference.getPositiveVotes());
        name.setText(conference.getTitle());
        summary.setText(Html.fromHtml(Processor.process(conference.getSummary()).trim()));

        descriptif.setText(
                Html.fromHtml(
                        Processor.process(conference.getDescription(), Configuration.builder().forceExtentedProfile().build()).trim(), null, htmlTagHandler),
                TextView.BufferType.SPANNABLE);

        final Salle room = Salle.getSalle(conference.getRoom());


        if(conference.getLang()!=null && "en".equals(conference.getLang())){
            langImage.setImageDrawable(getResources().getDrawable(R.drawable.en));
        }
        else{
            langImage.setImageDrawable(getResources().getDrawable(R.drawable.fr));
        }
        if (Salle.INCONNU != room) {
            salle.setText(String.format(getString(R.string.Salle), room.getNom()));
            if(room.getDrawable()!=0){
                salle.setBackgroundResource(room.getDrawable());
            }
            else{
                salle.setBackgroundColor(getActivity().getBaseContext().getResources().getColor(room.getColor()));
            }
            salle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String, Object> parametres = new HashMap<>();
                    parametres.put(UIUtils.ARG_KEY_ROOM, room.getEtage());
                    UIUtils.startActivity(SalleActivity.class, getActivity(), parametres);
                }
            });
        }
    }

    private void addSpeakerInfo(Talk conference){
        //On vide les éléments
        sessionPersonList.removeAllViews();

        List<Member> speakers = new ArrayList<>();
        for (Speaker member : conference.getSpeakers()) {
            Member membre = MembreFacade.getInstance().getMembre(getActivity(), TypeFile.speaker.name(), member.getIdMember());
            if(membre==null){
                membre = MembreFacade.getInstance().getMembre(getActivity(), TypeFile.speakerlt.name(), member.getIdMember());
            }
            if (membre != null) {
                speakers.add(membre);
            }
        }

        //On affiche les liens que si on a recuperer des choses
        if (!speakers.isEmpty()) {
            //On utilisait auparavant une liste pour afficher ces éléments dans la page mais cette liste
            //empêche d'avoir un ScrollView englobant pour toute la page. Nous utilisons donc un tableau

            //On ajoute un table layout
            TableLayout.LayoutParams tableParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
            TableLayout tableLayout = new TableLayout(getActivity().getBaseContext());
            tableLayout.setLayoutParams(tableParams);

            if(mInflater!=null) {
                for (final Member membre : speakers) {
                    LinearLayout row = (LinearLayout) mInflater.inflate(R.layout.item_person, tableLayout, false);
                    row.setBackgroundResource(R.drawable.row_transparent_background);

                    //Dans lequel nous allons ajouter le contenu que nous faisons mappé dans
                    TextView userName = (TextView) row.findViewById(R.id.person_user_name);
                    TextView descriptif = (TextView) row.findViewById(R.id.person_shortdesciptif);
                    TextView level = (TextView) row.findViewById(R.id.person_level);
                    ImageView profileImage = (ImageView) row.findViewById(R.id.person_user_image);

                    userName.setText(membre.getCompleteName());

                    if (membre.getShortDescription() != null) {
                        descriptif.setText(membre.getShortDescription().trim());
                    }

                    //Recuperation de l'mage liee au profil
                    Bitmap image = FileUtils.getImageProfile(getActivity(), membre);
                    if (image == null) {
                        profileImage.setImageDrawable(getResources().getDrawable(R.drawable.person_image_empty));
                    } else {
                        //On regarde dans les images embarquees
                        profileImage.setImageBitmap(image);
                    }

                    row.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ((HomeActivity) getActivity()).changeCurrentFragment(
                                    PeopleDetailFragment.newInstance(
                                            TypeFile.speaker.toString(),
                                            membre.getIdMember(),
                                            7),
                                    TypeFile.speaker.toString());
                        }
                    });

                    tableLayout.addView(row);
                }
            }
            sessionPersonList.addView(tableLayout);
        }
    }


    private void addInterestInfo(Talk conference){
        //On vide les éléments
        sessionLinkList.removeAllViews();
        //On affiche les liens que si on a recuperer des choses
        if (conference.getInterests() != null && !conference.getInterests().isEmpty()) {

            StringBuilder interets = new StringBuilder();
            for (final String interet : conference.getInterests()) {
                if (interet != null) {
                    if (interets.length() > 0) {
                        interets.append(", ");
                    }
                    interets.append(interet);
                }
            }
            TextView text = new TextViewTableBuilder()
                    .buildView(getActivity())
                    .addText(interets.toString())
                    .addPadding(4, 10, 4)
                    .addTextColor(getResources().getColor(R.color.black))
                    .getView();
            text.setSingleLine(false);
            sessionLinkList.addView(text);
        }
    }

    /**
     * Icon change according to the session if it's present or not in the favorites
     */
    public void updateMenuItem(MenuItem item, Boolean isFavorite) {
        if(isFavorite==null){
            isFavorite = isTalkFavorite();
        }
        if (isFavorite) {
            //On affiche bouton pour l'enlever
            item.setTitle(R.string.description_favorite_del);
            item.setIcon(getResources().getDrawable(R.drawable.ic_action_del_event));
            imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_important));
        }
        else {
            //On affiche bouton pour l'ajouter
            item.setTitle(R.string.description_favorite_add);
            item.setIcon(getResources().getDrawable(R.drawable.ic_action_add_event));
            imageFavorite.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_not_important));
        }
    }


    /**
     * Verifie si l'activité st dans les favoris
     */
    private boolean isTalkFavorite() {
        SharedPreferences settings = getActivity().getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
        return settings.getBoolean(String.valueOf(getArguments().getLong(UIUtils.ARG_ID)), false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        MenuItem item = menu.findItem(R.id.menu_favorites).setVisible(true);
        updateMenuItem(item, null);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_favorites) {
            //On recupere id
            long id = getArguments().getLong(UIUtils.ARG_ID);
            if (id > 0) {
                //On sauvegarde le choix de l'utilsateur
                SharedPreferences settings = getActivity().getSharedPreferences(UIUtils.PREFS_FAVORITES_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                if (isTalkFavorite()) {
                    //S'il l'est et on qu'on a cliquer sur le bouton on supprime
                    editor.remove(String.valueOf(id));
                    updateMenuItem(item, false);

                } else {
                    editor.putBoolean(String.valueOf(id), Boolean.TRUE);
                    updateMenuItem(item, true);
                }
                editor.apply();
            }
        }
        return super.onOptionsItemSelected(item);
    }
}
