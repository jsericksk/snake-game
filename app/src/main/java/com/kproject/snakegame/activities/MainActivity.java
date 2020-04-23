package com.kproject.snakegame.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import com.kproject.snakegame.R;
import com.kproject.snakegame.utils.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void btOnClick(View view) {
        switch (view.getId()) {
            case R.id.btStart:
                startActivity(new Intent(this, GameActivity.class));
                return;
            case R.id.btSettings:
                SettingsDialogFragment settingsDialog = new SettingsDialogFragment();
                settingsDialog.setCancelable(false);
                settingsDialog.show(getSupportFragmentManager(), settingsDialog.getTag());
                return;
            case R.id.btHighScores:
                dialogHighScores();
                return;
        }
    }
    
    private void dialogHighScores() {
        SharedPreferences prefs = getSharedPreferences("preferences", Context.MODE_PRIVATE);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_high_scores, null);
        TextView tvEasyModeScore = view.findViewById(R.id.tvEasyModeScore);
        TextView tvMediumModeScore = view.findViewById(R.id.tvMediumModeScore);
        TextView tvHardModeScore = view.findViewById(R.id.tvHardModeScore);
        TextView tvVeryHardModeScore = view.findViewById(R.id.tvVeryHardModeScore);
        tvEasyModeScore.setText(formatScores(prefs.getString("easyModeHighScores", "0,0,0,0,0")));
        tvMediumModeScore.setText(formatScores(prefs.getString("mediumModeHighScores", "0,0,0,0,0")));
        tvHardModeScore.setText(formatScores(prefs.getString("hardModeHighScores", "0,0,0,0,0")));
        tvVeryHardModeScore.setText(formatScores(prefs.getString("veryHardModeHighScores", "0,0,0,0,0")));
        
        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.show();
        
        view.findViewById(R.id.btOk).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
    }
    
    private String formatScores(String savedScores) {
        StringBuilder formattedScores = new StringBuilder();
        int position = 1;
        for (String score : savedScores.split(",")) {
            formattedScores.append(position + ". " + score + "\n");
            position++;
        }
        return formattedScores.toString();
    }
    
    /*
     * Classe responsável pelo dialog de configurações. É utilizada uma classe em particular
     * para facilitar a organização de seus sub-diálogos. Porém, é importante lembrar que dialogs
     * só sobrevivem à mudanças de configurações (como rotação de tela) sendo feitos desta forma,
     * ou seja, criar uma classe que estende DialogFragmemt é a maneira recomendada, apesar de vários
     * dialogs neste projeto não fazerem isso
     */ 
    public static class SettingsDialogFragment extends DialogFragment implements View.OnClickListener {
        private SharedPreferences prefs;
        
        public SettingsDialogFragment() {}

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            prefs = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
            
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_settings, null);
            view.findViewById(R.id.btDifficulty).setOnClickListener(this);
            view.findViewById(R.id.btTheme).setOnClickListener(this);
            view.findViewById(R.id.btOk).setOnClickListener(this);
            
            dialog.setView(view);
            return dialog.create();
        }
        
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btDifficulty:
                    dialogDifficulty();
                    break;
                case R.id.btTheme:
                    dialogTheme();
                    break;
                case R.id.btOk:
                    this.dismiss();
                    break;
            }
        }
        
        private void dialogDifficulty() {
            /*
             * Obtém o nível de dificuldade atual.
             * O nível é armazenado em milissegundos
             */
            int difficulty = prefs.getInt("difficulty", Constants.EASY);
            final String[] options = {"Easy", "Medium", "Hard", "Very Hard"};
            final Integer[] levels = {Constants.EASY, Constants.MEDIUM, Constants.HARD, Constants.VERY_HARD};
            final List<Integer> levelsInMilli = new ArrayList<Integer>(Arrays.asList(levels));
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Difficulty");
            dialog.setSingleChoiceItems(options, levelsInMilli.indexOf(new Integer(difficulty)), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    prefs.edit().putInt("difficulty", levelsInMilli.get(pos)).commit();
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
        }
        
        private void dialogTheme() {
            int savedTheme = prefs.getInt("gameTheme", 0);
            final String[] options = {"RGB", "Classic", "Black/White", "White/Black", "Earth"};
            AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
            dialog.setTitle("Game Theme");
            dialog.setSingleChoiceItems(options, savedTheme, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int pos) {
                    prefs.edit().putInt("gameTheme", pos).commit();
                    dialogInterface.dismiss();
                }
            });
            dialog.show();
        }
        
    }
    
}
