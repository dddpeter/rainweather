package com.dddpeter.app.rainweather.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.dddpeter.app.rainweather.R;
import com.dddpeter.app.rainweather.database.GameDatabaseHelper;
import com.dddpeter.app.rainweather.utils.SoundManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SuppressLint("NonConstantResourceId")
public class AboutFragment extends Fragment {

    private static final int GRID_SIZE = 4;
    private static final int WINNING_VALUE = 2048;
    
    private GridLayout gameGrid;
    private TextView scoreText;
    private TextView bestScoreText;
    private Button newGameBtn;
    private Button resetBtn;
    private androidx.cardview.widget.CardView gameBoardContainer;
    
    private int[][] gameBoard;
    private int currentScore;
    private int bestScore;
    private GameDatabaseHelper dbHelper;
    private GestureDetector gestureDetector;
    private SoundManager soundManager;
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup viewGroup, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, viewGroup, false);
        
        initViews(view);
        initGame();
        setupGestureDetector();
        
        return view;
    }
    
    private void initViews(View view) {
        gameGrid = view.findViewById(R.id.game_grid);
        scoreText = view.findViewById(R.id.score_text);
        bestScoreText = view.findViewById(R.id.best_score_text);
        newGameBtn = view.findViewById(R.id.new_game_btn);
        resetBtn = view.findViewById(R.id.reset_btn);
        gameBoardContainer = view.findViewById(R.id.game_board_container);
        
        newGameBtn.setOnClickListener(v -> showNewGameConfirmDialog());
        resetBtn.setOnClickListener(v -> showResetConfirmDialog());
        
        dbHelper = new GameDatabaseHelper(getContext());
        soundManager = SoundManager.getInstance(getContext());
        
        // 设置游戏棋盘为正方形
        setupSquareGameBoard();
    }
    
    private void setupSquareGameBoard() {
        // 使用ViewTreeObserver来确保在布局完成后设置尺寸
        gameBoardContainer.getViewTreeObserver().addOnGlobalLayoutListener(new android.view.ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gameBoardContainer.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                
                // 获取屏幕尺寸
                int screenWidth = getResources().getDisplayMetrics().widthPixels;
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                
                // 计算可用的棋盘尺寸
                // 减去左右边距 (16dp * 2) + padding (16dp * 2) = 64dp
                int horizontalMargin = (int) (64 * getResources().getDisplayMetrics().density);
                int availableWidth = screenWidth - horizontalMargin;
                
                // 为其他UI元素预留空间：标题(40dp) + 分数(80dp) + 说明(40dp) + 按钮(80dp) + 边距(40dp) = 280dp
                int reservedHeight = (int) (280 * getResources().getDisplayMetrics().density);
                int availableHeight = screenHeight - reservedHeight;
                
                // 取较小值作为棋盘尺寸，确保不超出屏幕
                int finalSize = Math.min(availableWidth, availableHeight);
                
                // 设置最小和最大尺寸限制
                int minSize = (int) (200 * getResources().getDisplayMetrics().density); // 最小200dp
                int maxSize = (int) (400 * getResources().getDisplayMetrics().density); // 最大400dp
                finalSize = Math.max(minSize, Math.min(finalSize, maxSize));
                
                // 设置游戏棋盘容器为正方形
                android.view.ViewGroup.LayoutParams params = gameBoardContainer.getLayoutParams();
                params.width = finalSize;
                params.height = finalSize;
                gameBoardContainer.setLayoutParams(params);
                
                // 重新初始化游戏网格
                updateGrid();
            }
        });
    }
    
    private void initGame() {
        gameBoard = new int[GRID_SIZE][GRID_SIZE];
        currentScore = 0;
        
        // 尝试加载保存的游戏状态
        GameDatabaseHelper.GameState savedState = dbHelper.loadGameState();
        if (savedState != null) {
            gameBoard = savedState.board;
            currentScore = savedState.score;
            bestScore = savedState.bestScore;
        } else {
            // 如果没有保存的状态，初始化新游戏
            bestScore = 0;
            addRandomTile();
            addRandomTile();
        }
        
        updateScore();
        updateGrid();
    }
    
    private void setupGestureDetector() {
        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                float deltaX = e2.getX() - e1.getX();
                float deltaY = e2.getY() - e1.getY();
                
                // 设置最小滑动距离
                float minSwipeDistance = 50;
                
                if (Math.abs(deltaX) > Math.abs(deltaY) && Math.abs(deltaX) > minSwipeDistance) {
                    // 水平滑动
                    if (deltaX > 0) {
                        moveRight();
                    } else {
                        moveLeft();
                    }
                    return true;
                } else if (Math.abs(deltaY) > minSwipeDistance) {
                    // 垂直滑动
                    if (deltaY > 0) {
                        moveDown();
                    } else {
                        moveUp();
                    }
                    return true;
                }
                
                return false;
            }
        });
        
        // 为整个游戏区域设置触控监听
        gameGrid.setOnTouchListener((v, event) -> {
            boolean handled = gestureDetector.onTouchEvent(event);
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                return true; // 确保能接收到后续事件
            }
            return handled;
        });
        
        // 确保GridLayout可以接收触控事件
        gameGrid.setClickable(true);
        gameGrid.setFocusable(true);
    }
    
    private void startNewGame() {
        gameBoard = new int[GRID_SIZE][GRID_SIZE];
        currentScore = 0;
        
        addRandomTile();
        addRandomTile();
        
        updateScore();
        updateGrid();
        saveGameState();
        
        Toast.makeText(getContext(), "新游戏开始！", Toast.LENGTH_SHORT).show();
    }
    
    private void resetGame() {
        dbHelper.clearGameState();
        startNewGame();
        Toast.makeText(getContext(), "游戏已重置！", Toast.LENGTH_SHORT).show();
    }
    
    private void showNewGameConfirmDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("新游戏")
                .setMessage("确定要开始新游戏吗？当前进度将会丢失。")
                .setPositiveButton("确定", (dialog, which) -> {
                    startNewGame();
                    Toast.makeText(getContext(), "新游戏开始！", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
    }
    
    private void showResetConfirmDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("重置游戏")
                .setMessage("确定要重置游戏吗？所有数据（包括最高分）都将被清除，此操作不可恢复！")
                .setPositiveButton("确定重置", (dialog, which) -> {
                    resetGame();
                })
                .setNegativeButton("取消", null)
                .setCancelable(true)
                .show();
    }
    
    private void moveLeft() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = new int[GRID_SIZE];
            for (int j = 0; j < GRID_SIZE; j++) {
                row[j] = gameBoard[i][j];
            }
            
            int[] merged = mergeRow(row);
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] != merged[j]) {
                    moved = true;
                }
                gameBoard[i][j] = merged[j];
            }
        }
        
        if (moved) {
            addRandomTile();
            updateScore();
            updateGrid();
            saveGameState();
            checkGameOver();
            soundManager.playMoveSound();
        }
    }
    
    private void moveRight() {
        boolean moved = false;
        for (int i = 0; i < GRID_SIZE; i++) {
            int[] row = new int[GRID_SIZE];
            for (int j = 0; j < GRID_SIZE; j++) {
                row[j] = gameBoard[i][GRID_SIZE - 1 - j];
            }
            
            int[] merged = mergeRow(row);
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][GRID_SIZE - 1 - j] != merged[j]) {
                    moved = true;
                }
                gameBoard[i][GRID_SIZE - 1 - j] = merged[j];
            }
        }
        
        if (moved) {
            addRandomTile();
            updateScore();
            updateGrid();
            saveGameState();
            checkGameOver();
            soundManager.playMoveSound();
        }
    }
    
    private void moveUp() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = gameBoard[i][j];
            }
            
            int[] merged = mergeRow(column);
            for (int i = 0; i < GRID_SIZE; i++) {
                if (gameBoard[i][j] != merged[i]) {
                    moved = true;
                }
                gameBoard[i][j] = merged[i];
            }
        }
        
        if (moved) {
            addRandomTile();
            updateScore();
            updateGrid();
            saveGameState();
            checkGameOver();
            soundManager.playMoveSound();
        }
    }
    
    private void moveDown() {
        boolean moved = false;
        for (int j = 0; j < GRID_SIZE; j++) {
            int[] column = new int[GRID_SIZE];
            for (int i = 0; i < GRID_SIZE; i++) {
                column[i] = gameBoard[GRID_SIZE - 1 - i][j];
            }
            
            int[] merged = mergeRow(column);
            for (int i = 0; i < GRID_SIZE; i++) {
                if (gameBoard[GRID_SIZE - 1 - i][j] != merged[i]) {
                    moved = true;
                }
                gameBoard[GRID_SIZE - 1 - i][j] = merged[i];
            }
        }
        
        if (moved) {
            addRandomTile();
            updateScore();
            updateGrid();
            saveGameState();
            checkGameOver();
            soundManager.playMoveSound();
        }
    }
    
    private int[] mergeRow(int[] row) {
        // 移除零值
        List<Integer> nonZero = new ArrayList<>();
        for (int value : row) {
            if (value != 0) {
                nonZero.add(value);
            }
        }
        
        // 合并相同的相邻值
        List<Integer> merged = new ArrayList<>();
        for (int i = 0; i < nonZero.size(); i++) {
            if (i < nonZero.size() - 1 && nonZero.get(i).equals(nonZero.get(i + 1))) {
                int mergedValue = nonZero.get(i) * 2;
                merged.add(mergedValue);
                currentScore += mergedValue;
                soundManager.playMergeSound(); // 播放合并音效
                i++; // 跳过下一个元素
            } else {
                merged.add(nonZero.get(i));
            }
        }
        
        // 填充到4个元素
        while (merged.size() < GRID_SIZE) {
            merged.add(0);
        }
        
        return merged.stream().mapToInt(Integer::intValue).toArray();
    }
    
    private void addRandomTile() {
        List<int[]> emptyCells = new ArrayList<>();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == 0) {
                    emptyCells.add(new int[]{i, j});
                }
            }
        }
        
        if (!emptyCells.isEmpty()) {
            Random random = new Random();
            int[] cell = emptyCells.get(random.nextInt(emptyCells.size()));
            gameBoard[cell[0]][cell[1]] = random.nextFloat() < 0.9f ? 2 : 4;
        }
    }
    
    private void updateScore() {
        scoreText.setText(String.valueOf(currentScore));
        
        if (currentScore > bestScore) {
            bestScore = currentScore;
            bestScoreText.setText(String.valueOf(bestScore));
        }
    }
    
    private void updateGrid() {
        gameGrid.removeAllViews();
        
        // 等待GridLayout布局完成后再计算方块大小
        gameGrid.post(() -> {
            // 计算每个方块的大小，确保是正方形
            int gridWidth = gameGrid.getWidth();
            int gridHeight = gameGrid.getHeight();
            
            // 如果GridLayout还没有尺寸，使用容器尺寸
            if (gridWidth <= 0 || gridHeight <= 0) {
                gridWidth = gameBoardContainer.getWidth() - 32; // 减去padding
                gridHeight = gameBoardContainer.getHeight() - 32; // 减去padding
            }
            
            int gridSize = Math.min(gridWidth, gridHeight);
            int cellSize = (gridSize - (GRID_SIZE + 1) * 12) / GRID_SIZE; // 减去边距
            
            for (int i = 0; i < GRID_SIZE; i++) {
                for (int j = 0; j < GRID_SIZE; j++) {
                    TextView cell = new TextView(getContext());
                    cell.setTextSize(20);
                    cell.setGravity(android.view.Gravity.CENTER);
                    cell.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
                    
                    GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                    params.width = cellSize;
                    params.height = cellSize;
                    params.columnSpec = GridLayout.spec(j);
                    params.rowSpec = GridLayout.spec(i);
                    params.setMargins(6, 6, 6, 6);
                    cell.setLayoutParams(params);
                    
                    int value = gameBoard[i][j];
                    if (value == 0) {
                        cell.setText("");
                        cell.setBackgroundColor(getResources().getColor(R.color.x_black_tertiary, null));
                    } else {
                        cell.setText(String.valueOf(value));
                        cell.setBackgroundColor(getCellColor(value));
                        cell.setTextColor(getTextColor(value));
                    }
                    
                    gameGrid.addView(cell);
                }
            }
        });
    }
    
    private int getCellColor(int value) {
        switch (value) {
            case 2: return getResources().getColor(R.color.game_2, null);
            case 4: return getResources().getColor(R.color.game_4, null);
            case 8: return getResources().getColor(R.color.game_8, null);
            case 16: return getResources().getColor(R.color.game_16, null);
            case 32: return getResources().getColor(R.color.game_32, null);
            case 64: return getResources().getColor(R.color.game_64, null);
            case 128: return getResources().getColor(R.color.game_128, null);
            case 256: return getResources().getColor(R.color.game_256, null);
            case 512: return getResources().getColor(R.color.game_512, null);
            case 1024: return getResources().getColor(R.color.game_1024, null);
            case 2048: return getResources().getColor(R.color.game_2048, null);
            default: return getResources().getColor(R.color.x_black_tertiary, null);
        }
    }
    
    private int getTextColor(int value) {
        // 对于较小的数字使用深色文字，较大的数字使用浅色文字
        if (value <= 4) {
            return getResources().getColor(R.color.game_text_dark, null);
        } else {
            return getResources().getColor(R.color.game_text_light, null);
        }
    }
    
    private void checkGameOver() {
        // 检查是否达到2048
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == WINNING_VALUE) {
                    soundManager.playGameWinSound();
                    Toast.makeText(getContext(), "恭喜！你达到了2048！", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
        
        // 检查是否还有空位
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (gameBoard[i][j] == 0) {
                    return; // 还有空位，游戏继续
                }
            }
        }
        
        // 检查是否还能合并
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                int current = gameBoard[i][j];
                // 检查右边
                if (j < GRID_SIZE - 1 && gameBoard[i][j + 1] == current) {
                    return; // 还能合并，游戏继续
                }
                // 检查下边
                if (i < GRID_SIZE - 1 && gameBoard[i + 1][j] == current) {
                    return; // 还能合并，游戏继续
                }
            }
        }
        
        // 游戏结束
        soundManager.playGameOverSound();
        Toast.makeText(getContext(), "游戏结束！最终得分：" + currentScore, Toast.LENGTH_LONG).show();
    }
    
    private void saveGameState() {
        dbHelper.saveGameState(currentScore, bestScore, gameBoard);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (soundManager != null) {
            soundManager.release();
        }
    }
}