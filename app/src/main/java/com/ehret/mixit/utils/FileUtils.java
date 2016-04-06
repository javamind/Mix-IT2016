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
package com.ehret.mixit.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.ehret.mixit.R;
import com.ehret.mixit.domain.JsonFile;
import com.ehret.mixit.domain.TypeFile;
import com.ehret.mixit.domain.people.Member;
import com.ehret.mixit.model.ConferenceFacade;
import com.ehret.mixit.model.MembreFacade;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Regroupe les classes utilitaires sur les fichiers
 */
public class FileUtils {

    /**
     * Indique si stockage dispo
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Cette méthode  le fichier que l'on souhaite recuperer
     */
    public static File createFileJson(Context context, TypeFile typeFile) throws IOException {
        File myFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), typeFile.name() + ".json");
        if (myFile.exists()) {
            myFile.delete();
        }
        myFile.createNewFile();
        return myFile;
    }


    public static void razFileJson(Context context) {
        for (JsonFile json : JsonFile.values()) {
            File myFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), json.getType().name() + ".json");
            if (myFile.exists()) {
                myFile.delete();
            }
        }
        //Suppression des données en cache
        ConferenceFacade.getInstance().viderCache();
        MembreFacade.getInstance().viderCache();

    }

    /**
     * Cette méthode  recupere le fichier correspondant à une ressource. Si la ressource n'a pu être téléchargée
     * on s'appuie sur la version en local
     */
    public static File getFileJson(Context context, TypeFile typeFile) throws IOException {
        File myFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), typeFile.name() + ".json");
        if (myFile.exists()) {
            return myFile;
        }
        return null;

    }

    public static Bitmap getImageProfile(Context context, Member membre) {
        if (membre == null)
            return null;
        if (membre.getUrlImage() != null) {
            File myFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "membre" + membre.getIdMember() + ".jpg");
            if (myFile.exists()) {
                try {
                    return BitmapFactory.decodeStream(new FileInputStream(myFile));
                } catch (IOException e) {
                    return null;
                }
            }

        }
        return null;
    }


    public static Bitmap getImageLogo(Context context, Member membre) {
        if (membre.getLogo() != null) {
            File myFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DCIM), "logo" + membre.getIdMember() + ".jpg");
            if (myFile.exists()) {
                try {
                    return BitmapFactory.decodeStream(new FileInputStream(myFile));
                } catch (IOException e) {
                    return null;
                }
            }

        }
        return null;
    }

    /**
     * @throws java.io.IOException
     */
    public static InputStream getRawFileJson(Context context, TypeFile typeFile) throws IOException {
        //Sinon on prend celui dans les raw file
        InputStream is;
        switch (typeFile) {
             case lightningtalks:
                is = context.getResources().openRawResource(R.raw.ligthningtalk);
                break;
            case speaker:
                is = context.getResources().openRawResource(R.raw.speaker);
                break;
            case members:
                is = context.getResources().openRawResource(R.raw.member);
                break;
            case sponsor:
                is = context.getResources().openRawResource(R.raw.sponsor);
                break;
            case staff:
                is = context.getResources().openRawResource(R.raw.staff);
                break;
            default:
                is = context.getResources().openRawResource(R.raw.talks);
        }
        return is;
    }


}
