package com.mobileproject.game;

/**
 * Created by 100520993 on 11/12/2016.
 */

/* Stores a user's resources */
public class User {



    private String username;
    private int gold;
    private int food;
    private int tiles;
    private int tilesTaken;
    private int goldObtained;
    private int foodObtained;
    private int totalGoldObtained;
    private int totalFoodObtained;
    private int totalSoldiers;
    private int soldiersAvailable;

    public User(String username, int gold, int food, int tiles, int tilesTaken, int goldObtained,
                int foodObtained, int totalGoldObtained,int totalFoodObtained,
                int totalSoldiers, int soldiersAvailable) {
        this.username = username;
        this.soldiersAvailable = soldiersAvailable;
        this.gold = gold;
        this.food = food;
        this.tiles = tiles;
        this.tilesTaken = tilesTaken;
        this.goldObtained = goldObtained;
        this.foodObtained = foodObtained;
        this.totalGoldObtained = totalGoldObtained;
        this.totalFoodObtained = totalFoodObtained;
        this.totalSoldiers = totalSoldiers;
    }

    public User(String username) {
        this.username = username;
        gold = 0;
        food = 0;
        tiles = 0;
        tilesTaken = 0;
        goldObtained = 0;
        foodObtained = 0;
        totalGoldObtained = 0;
        totalFoodObtained = 0;
        totalSoldiers = 0;
        soldiersAvailable = 0;
    }

    public int getFood() {
        return food;
    }

    public void setFood(int food) {
        this.food = food;
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }

    public int getTiles() {
        return tiles;
    }

    public void setTiles(int tiles) {
        this.tiles = tiles;
    }

    public int getTilesTaken() {
        return tilesTaken;
    }

    public void setTilesTaken(int tilesTaken) {
        this.tilesTaken = tilesTaken;
    }

    public int getGoldObtained() {
        return goldObtained;
    }

    public void setGoldObtained(int goldObtained) {
        this.goldObtained = goldObtained;
    }

    public int getFoodObtained() {
        return foodObtained;
    }

    public void setFoodObtained(int foodObtained) {
        this.foodObtained = foodObtained;
    }

    public int getTotalGoldObtained() {
        return totalGoldObtained;
    }

    public void setTotalGoldObtained(int totalGoldObtained) {
        this.totalGoldObtained = totalGoldObtained;
    }

    public int getTotalFoodObtained() {
        return totalFoodObtained;
    }

    public void setTotalFoodObtained(int totalFoodObtained) {
        this.totalFoodObtained = totalFoodObtained;
    }

    public int getTotalSoldiers() {
        return totalSoldiers;
    }

    public void setTotalSoldiers(int totalSoldiers) {
        this.totalSoldiers = totalSoldiers;
    }

    public int getSoldiersAvailable() {
        return soldiersAvailable;
    }

    public void setSoldiersAvailable(int soldiersAvailable) {
        this.soldiersAvailable = soldiersAvailable;
    }

    public String getUsername() {
        return username;
    }

}
