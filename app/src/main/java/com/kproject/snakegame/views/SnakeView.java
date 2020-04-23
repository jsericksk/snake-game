package com.kproject.snakegame.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import com.kproject.snakegame.activities.MainActivity;
import com.kproject.snakegame.utils.Constants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SnakeView extends View implements Runnable {
    private static final int UP = 2, DOWN = 8, LEFT = 4, RIGHT = 6;
    
    private Context context;
    private Paint paint;
    private Paint paintScoreText;
    private AlertDialog dialogLose;
    private Thread thread;
    private Random random;
    
    private int screenWidth;
    private int screenHeight;
    private int snakeLenght;
    private int score;
    private int currentDirection;
    
    private static final int NUM_BLOCKS_WIDTH = 40;
    private int numBlocksHeight;
    private int blockSize;
    
    // Posição X/Y da comida
    private int foodX;
    private int foodY;
    
    // Opções do usuário
    private int difficulty;
    private String snakeHeadColor;
    private String snakeBodyColor;
    private String foodColor;
    private String backgroundColor;
    private String scoreTextColor;
    
    private int[] snakeX = new int[200];
    private int[] snakeY = new int[200];
    
    private boolean isPlaying;
    private boolean isPaused;
    
    public SnakeView(Context context, int screenWidth, int screenHeight) {
        super(context);
        this.context = context;
        this.paint = new Paint();
        this.paintScoreText = new Paint();
        this.thread = new Thread(this);
        this.random = new Random();
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.snakeLenght = 2;
        
        this.blockSize = screenWidth / NUM_BLOCKS_WIDTH;
        this.numBlocksHeight = screenHeight / blockSize;
        
        /*
         * Determina as coordenadas iniciais de onde
         * a cobra deve iniciar
         */
        this.snakeX[0] = (NUM_BLOCKS_WIDTH / 2) + 1;
        this.snakeY[0] = numBlocksHeight / 2;
        this.snakeX[1] = NUM_BLOCKS_WIDTH / 2;
        this.snakeY[1] = numBlocksHeight / 2;
        
        generateFood();
        loadPreferences();
        
        // Inicializa os dados do paint dedicado apenas ao texto de score
        paintScoreText.setColor(Color.parseColor(scoreTextColor));
        int spSize = 20;
        float scaledSizeInPixels = spSize * getResources().getDisplayMetrics().scaledDensity;
        paintScoreText.setTextSize(scaledSizeInPixels);
        paintScoreText.setFakeBoldText(true);
        paintScoreText.setTextAlign(Paint.Align.CENTER);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Cor de fundo
        canvas.drawColor(Color.parseColor(backgroundColor));
        
        // Cor do texto do score
        canvas.drawText(("Score: " + score), (canvas.getWidth() / 2), (paintScoreText.getTextSize() + 5), paintScoreText);
        
        // Cor da comida
        paint.setColor(Color.parseColor(foodColor));
        canvas.drawRect(foodX * blockSize,
                        foodY * blockSize,
                        (foodX * blockSize) + blockSize,
                        (foodY * blockSize) + blockSize,
                        paint);

        for (int i = 0; i < snakeLenght; i++) {
            // Cor da cabeça e do corpo da cobra
            paint.setColor(i == 0 ? Color.parseColor(snakeHeadColor) : Color.parseColor(snakeBodyColor));
            canvas.drawRect(snakeX[i] * blockSize,
                            snakeY[i] * blockSize,
                            (snakeX[i] * blockSize) + blockSize,
                            (snakeY[i] * blockSize) + blockSize,
                            paint);


        }
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            float touchX = event.getX();
            float touchY = event.getY();

            /*
             * Verifica a direção do toque na tela. Se foi mais à direita ou à esquerda (direção X) e
             * se foi mais acima ou abaixo (direção Y)
             */

            // RIGHT e UP
            if ((touchX >= (screenWidth / 2)) && (touchY < (screenHeight / 2))) {
                changeDirection(RIGHT, UP);
            // RIGHT e DOWN
            } else if ((touchX >= (screenWidth / 2)) && (touchY >= (screenHeight / 2))) {
                changeDirection(RIGHT, DOWN);
            // LEFT e UP
            } else if ((touchX < (screenWidth / 2)) && (touchY < (screenHeight / 2))) {
                changeDirection(LEFT, UP);
            // LEFT e DOWN
            } else if ((touchX < (screenWidth / 2)) && (touchY >= (screenHeight / 2))) {
                changeDirection(LEFT, DOWN);
            } 
            
            // Verifica qual é o status atual do jogo para executar a ação apropriada
            if (!isPlaying && !isPaused) {
                startGame();
            } else if (isPaused) {
                resumeGame();
            } else {
                postInvalidate();
            }
        }
        return true;
    }

    @Override
    public void run() {
        while (isPlaying) {
            eatFood();
            moveSnake();
            try {
                /*
                 * O nível de dificuldade do jogo é definido
                 * pela velocidade com que a cobra se move,
                 * que, nesse caso, é em milissegundos
                 */
                thread.sleep(difficulty);
                this.post(new Runnable() {
                    @Override
                    public void run() {
                        postInvalidate();
                    }
                });
            } catch (InterruptedException e) {}
        }
    }

    public void startGame() {
        this.score = 0;
        this.isPlaying = true;
        thread = new Thread(this);
        thread.start();
    }
    
    public void resumeGame() {
        this.isPlaying = true;
        this.isPaused = false;
        thread = new Thread(this);
        thread.start();
    }
    
    public void pauseGame() {
        this.isPlaying = false;
        this.isPaused = true;
        try {
            thread.join();
        } catch (InterruptedException e) {}
    }
    
    private void restartGame() {
        /*
         * O jogo deve se manter parado até que o jogador toque na tela, 
         * e não deve estar em um estado pausado
         */
        this.isPlaying = false;
        this.isPaused = false;
        this.snakeLenght = 2;
        this.score = 0;
        this.snakeX[0] = (NUM_BLOCKS_WIDTH / 2) + 1;
        this.snakeY[0] = numBlocksHeight / 2;
        this.snakeX[1] = NUM_BLOCKS_WIDTH / 2;
        this.snakeY[1] = numBlocksHeight / 2;
        // Reseta a direção atual, para se mover somente para à esquerda ou direita no restart
        this.currentDirection = 0;
        generateFood();
        postInvalidate();
    }

    private void moveSnake() {
        for (int i = snakeLenght; i > 0; i--) {
            snakeX[i] = snakeX[i - 1];
            snakeY[i] = snakeY[i - 1];
        }

        /*
         * UP, DOWN = Y
         * LEFT, RIGHT = X
         */
        if (currentDirection == UP) {
            snakeY[0]--;
        } else if (currentDirection == DOWN) {
            snakeY[0]++;
        } else if (currentDirection == LEFT) {
            snakeX[0]--;
        } else if (currentDirection == RIGHT) {
            snakeX[0]++;
        }

        if (isDead()) {
            isPlaying = false;
            isPaused = true;
            saveScore();
            dialogLose();
        }
    }

    /*
     * Muda a direção (currentDirection) dependendo do toque e da direção atual
     */
    private void changeDirection(int directionX, int directionY) {
        /*
         * Testa a direção RIGHT.
         * A cobra não pode se mover para a direita se já estiver indo para à esquerda,
         * por isso a direção Y é verificada, para se mover apropriadamente para cima ou baixo
         */
        if (directionX == RIGHT && currentDirection != RIGHT && currentDirection != LEFT) {
            currentDirection = RIGHT;
            return;
        }
        if (directionX == RIGHT && currentDirection != RIGHT && currentDirection == LEFT && directionY == UP) {
            currentDirection = UP;
            return;
        }
        if (directionX == RIGHT && currentDirection != RIGHT && currentDirection == LEFT && directionY == DOWN) {
            currentDirection = DOWN;
            return;
        }
        /*
         * Verifica se já está se movendo para a direita. Se sim, verifica se a direção Y
         * (o toque) foi mais acima (UP) ou abaixo (DOWN) da tela
         */
        if (directionX == RIGHT && currentDirection == RIGHT && directionY == UP) {
            currentDirection = UP;
            return;
        }
        if (directionX == RIGHT && currentDirection == RIGHT && directionY == DOWN) {
            currentDirection = DOWN;
            return;
        }

        /*
         * Testa a direção LEFT.
         * A mesma lógica acima é usada aqui
         */
        if (directionX == LEFT && currentDirection != LEFT && currentDirection != RIGHT) {
            currentDirection = LEFT;
            return;
        }
        if (directionX == LEFT && currentDirection != LEFT && currentDirection == RIGHT && directionY == UP) {
            currentDirection = UP;
            return;
        }
        if (directionX == LEFT && currentDirection != LEFT && currentDirection == RIGHT && directionY == DOWN) {
            currentDirection = DOWN;
            return;
        }

        if (directionX == LEFT && currentDirection == LEFT && directionY == UP) {
            currentDirection = UP;
            return;
        }
        if (directionX == LEFT && currentDirection == LEFT && directionY == DOWN) {
            currentDirection = DOWN;
            return;
        }
    }

    private void eatFood() {
        if (snakeX[0] == foodX && snakeY[0] == foodY) {
            generateFood();
            snakeLenght++;
            score++;
        }
    }

    private void generateFood() {
        foodX = random.nextInt(screenWidth / blockSize) - 1;
        foodY = random.nextInt(screenHeight / blockSize) - 1;
        // Pode ser gerado um número 0 e, com a subtração, se tornará -1
        if (foodX == -1) foodX = 0;
        if (foodY == -1) foodY = 0;
    }
    
    private boolean isDead() {
        if (snakeX[0] == -1 || snakeY[0] == -1) {
            return true;
        }
        
        if (snakeX[0] == NUM_BLOCKS_WIDTH || snakeY[0] == numBlocksHeight) {
            return true;
        }

        for (int i = snakeLenght - 1; i > 0; i--) {
            // A cabeça da cobra tocou uma parte do seu corpo
            if (snakeX[0] == snakeX[i] && snakeY[0] == snakeY[i]) {
                return true;
            }
        }

        return false;
    }
    
    private void loadPreferences() {
        SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        this.difficulty = prefs.getInt("difficulty", 160);
        getThemeColors(prefs);
    }
    
    private void getThemeColors(SharedPreferences prefs) {
        final int savedTheme = prefs.getInt("gameTheme", 0);
        // [0] snakeHeadColor, [1] snakeBodyColor, [2] foodColor, [3] backgroundColor
        String[] themeColors = null;
        String scoreColor = null;
        if (savedTheme == Constants.THEME_RGB) {
            themeColors = new String[]{"#FF0000", "#00FF00", "#0000FF", "#000000"};
            scoreColor = "#FFFFFF";
        } else if (savedTheme == Constants.THEME_CLASSIC) {
            themeColors = new String[]{"#003300", "#005500", "#003300", "#88BB00"};
            scoreColor = "#003300";
        } else if (savedTheme == Constants.THEME_BLACK_WHITE) {
            themeColors = new String[]{"#FFFFFF", "#C3C3C3", "#F5F5F5", "#000000"};
            scoreColor = "#FFFFFF";
        } else if (savedTheme == Constants.THEME_WHITE_BLACK) {
            themeColors = new String[]{"#000000", "#888888", "#000000", "#F5F5F5"};
            scoreColor = "#000000";
        } else if (savedTheme == Constants.THEME_EARTH) {
            themeColors = new String[]{"#008800", "#005500", "#008800", "#000055"};
            scoreColor = "#F5F5F5";
        } 
        
        this.snakeHeadColor = themeColors[0];
        this.snakeBodyColor = themeColors[1];
        this.foodColor = themeColors[2];
        this.backgroundColor = themeColors[3];
        this.scoreTextColor = scoreColor;
    }
    
    private void saveScore() {
        String prefKey = "";
        if (difficulty == Constants.EASY) {
            prefKey = "easyModeHighScores";
        } else if (difficulty == Constants.MEDIUM) {
            prefKey = "mediumModeHighScores";
        } else if (difficulty == Constants.HARD) {
            prefKey = "hardModeHighScores";
        } else if (difficulty == Constants.VERY_HARD) {
            prefKey = "veryHardModeHighScores";
        }

        SharedPreferences prefs = context.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        String prefSavedScores = prefs.getString(prefKey, "0,0,0,0,0");
        String[] savedScores = prefSavedScores.split(",");
        Integer[] highScores = new Integer[5];
        int index = 0;
        for (String score : savedScores) {
            highScores[index] = Integer.parseInt(score);
            index++;
        }
        
        // Organiza em forma decrescente (5, 4, 3, 2, 1)
        Arrays.sort(highScores, Collections.reverseOrder());

        /*
         * Checa se o score atual é menor que o score mais baixo armazenado ou
         * se o score atual já está armazenado. Nesse caso, não há necessidade
         * de prosseguir para salvar o score atual
         */
        List<Integer> scoreList = new ArrayList<Integer>(Arrays.asList(highScores));
        if (score < highScores[highScores.length - 1] || scoreList.contains(score)) {
            return;
        }
        
        // Substitui o menor score armazenado no array pelo score atual
        highScores[highScores.length - 1] = score;
        // Organiza novamente
        Arrays.sort(highScores, Collections.reverseOrder());
        StringBuilder highScoreStr = new StringBuilder();
        for (int i = 0; i < highScores.length; i++) {
            highScoreStr.append(highScores[i]);
            if (i < highScores.length - 1) {
                highScoreStr.append(",");
            }
        }
        
        // Salva os scores organizados e formatados na SharedPreferences
        prefs.edit().putString(prefKey, highScoreStr.toString()).commit();
    }
    
    private void dialogLose() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Game Over");
        builder.setMessage("Score: " + score);
        builder.setCancelable(false);
        builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                dialogInterface.dismiss();
                restartGame();
            }
        });
        builder.setNegativeButton("Back to Home", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                dialogInterface.dismiss();
            }
        });
        // Para ser chamado na Thread UI
        this.post(new Runnable() {
            @Override
            public void run() {
                if (dialogLose == null) {
                    dialogLose = builder.create();
                }
                /*
                 * Evita do dialog ser exibido novamente, caso já esteja sendo exibido.
                 * Isso pode ser útil para alguns dispositivos
                 */
                 if (!dialogLose.isShowing()) {
                    dialogLose.show();
                }
            }
        });
    }
    
}
