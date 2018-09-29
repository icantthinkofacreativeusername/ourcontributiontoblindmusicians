package cosc345.app.controller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import cosc345.app.R;
import cosc345.app.model.Difficulty;
import cosc345.app.model.Playable;
import cosc345.app.model.Scale;
import cosc345.app.model.ScaleExerciseGrader;
import cosc345.app.model.TextToSpeechManager;
import cosc345.app.model.VoiceRecognitionManager;

public class ScalesExercise extends VoiceControlActivity implements Playable.Delegate {
    private Button startBtn;
    private Button stopBtn;
    Scale targetScale;

    private ScaleExerciseGrader scaleExerciseGrader;
    private Difficulty difficulty;
    private int timesPlayed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scales_exercise);

        startBtn = findViewById(R.id.scalesExercise_startBtn);
        stopBtn = findViewById(R.id.scalesExercise_stopBtn);

        startBtn.setOnClickListener(v -> startExercise());
        stopBtn.setOnClickListener(v -> stopExercise());

        String difficulty = getIntent().getStringExtra("EXTRA_DIFFICULTY");

        if (difficulty.equals(Difficulty.EASY.toString())) {
            this.difficulty = Difficulty.EASY;
        } else if (difficulty.equals(Difficulty.MEDIUM.toString())) {
            this.difficulty = Difficulty.MEDIUM;
        } else {
            this.difficulty = Difficulty.HARD;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        stopExercise();
        TextToSpeechManager.getInstance().close();
    }

    private void startExercise() {
        // TODO: remove this line when bug with VoiceRecognitionManager is fixed.
        VoiceRecognitionManager.getInstance().close();

        timesPlayed = 0;

        startBtn.setVisibility(View.GONE);
        stopBtn.setVisibility(View.VISIBLE);

        scaleExerciseGrader = new ScaleExerciseGrader(difficulty);
        scaleExerciseGrader.setOnSuccessCallback(this::onGradingDone);
        scaleExerciseGrader.setCallback(this::showStartButton);
        targetScale = scaleExerciseGrader.scale;
        targetScale.setDelegate(this);
        targetScale.play();
    }


    private void stopExercise() {
        if (scaleExerciseGrader != null) {
            scaleExerciseGrader.scale.stop();
            scaleExerciseGrader.stop();
        }

        showStartButton();
    }

    @Override
    public void onPlaybackStarted() {
        timesPlayed++;
    }

    @Override
    public void onPlaybackFinished() {
        if (timesPlayed < 2) {
            targetScale.play();
        } else {
            scaleExerciseGrader.start();
        }
    }

    @Override
    public void onDone() {

    }

    private void onGradingDone() {
        double grade = scaleExerciseGrader.getScore();
        String msg;

        if (grade < 60.0) {
            msg = "Your score was bad";
        } else if (grade < 80.0) {
            msg = "Your score was ok";
        } else if (grade < 90.0) {
            msg = "Your score was good";
        } else {
            msg = "Your score was perfect";
        }

        TextToSpeechManager.getInstance().speak(msg);
    }

    private void showStartButton() {
        startBtn.setVisibility(View.VISIBLE);
        stopBtn.setVisibility(View.GONE);
    }
}
