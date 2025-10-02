package com.dddpeter.app.rainweather.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class GameDatabaseHelper extends SQLiteOpenHelper {
    
    private static final String DATABASE_NAME = "game2048.db";
    private static final int DATABASE_VERSION = 1;
    
    // 游戏状态表
    private static final String TABLE_GAME_STATE = "game_state";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_SCORE = "score";
    private static final String COLUMN_BEST_SCORE = "best_score";
    private static final String COLUMN_BOARD_STATE = "board_state";
    private static final String COLUMN_CREATED_TIME = "created_time";
    
    // 创建游戏状态表的SQL
    private static final String CREATE_GAME_STATE_TABLE = 
        "CREATE TABLE " + TABLE_GAME_STATE + " (" +
        COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        COLUMN_SCORE + " INTEGER NOT NULL, " +
        COLUMN_BEST_SCORE + " INTEGER NOT NULL, " +
        COLUMN_BOARD_STATE + " TEXT NOT NULL, " +
        COLUMN_CREATED_TIME + " INTEGER NOT NULL" +
        ")";
    
    public GameDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_GAME_STATE_TABLE);
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME_STATE);
        onCreate(db);
    }
    
    /**
     * 保存游戏状态
     */
    public void saveGameState(int score, int bestScore, int[][] board) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        
        values.put(COLUMN_SCORE, score);
        values.put(COLUMN_BEST_SCORE, bestScore);
        values.put(COLUMN_BOARD_STATE, boardToString(board));
        values.put(COLUMN_CREATED_TIME, System.currentTimeMillis());
        
        // 先删除旧的状态，只保留最新的
        db.delete(TABLE_GAME_STATE, null, null);
        
        // 插入新状态
        db.insert(TABLE_GAME_STATE, null, values);
        db.close();
    }
    
    /**
     * 加载游戏状态
     */
    public GameState loadGameState() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GAME_STATE, 
            new String[]{COLUMN_SCORE, COLUMN_BEST_SCORE, COLUMN_BOARD_STATE},
            null, null, null, null, COLUMN_CREATED_TIME + " DESC", "1");
        
        GameState gameState = null;
        if (cursor.moveToFirst()) {
            int score = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SCORE));
            int bestScore = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_BEST_SCORE));
            String boardState = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BOARD_STATE));
            
            gameState = new GameState(score, bestScore, stringToBoard(boardState));
        }
        
        cursor.close();
        db.close();
        
        return gameState;
    }
    
    /**
     * 更新最高分
     */
    public void updateBestScore(int bestScore) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BEST_SCORE, bestScore);
        
        db.update(TABLE_GAME_STATE, values, null, null);
        db.close();
    }
    
    /**
     * 清除游戏状态
     */
    public void clearGameState() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_GAME_STATE, null, null);
        db.close();
    }
    
    /**
     * 将游戏板转换为字符串
     */
    private String boardToString(int[][] board) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                sb.append(board[i][j]);
                if (i < 3 || j < 3) {
                    sb.append(",");
                }
            }
        }
        return sb.toString();
    }
    
    /**
     * 将字符串转换为游戏板
     */
    private int[][] stringToBoard(String boardState) {
        int[][] board = new int[4][4];
        String[] values = boardState.split(",");
        
        for (int i = 0; i < 16; i++) {
            int row = i / 4;
            int col = i % 4;
            board[row][col] = Integer.parseInt(values[i]);
        }
        
        return board;
    }
    
    /**
     * 游戏状态数据类
     */
    public static class GameState {
        public int score;
        public int bestScore;
        public int[][] board;
        
        public GameState(int score, int bestScore, int[][] board) {
            this.score = score;
            this.bestScore = bestScore;
            this.board = board;
        }
    }
}
