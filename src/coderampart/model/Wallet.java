package coderampart.model;

import coderampart.DAO.WalletDAO;

public class Wallet {

    private Integer balance;
    private Integer earnedCoins;

    public Integer getBalance() { return this.balance; }
    public Integer getEarnedCoins() { return this.earnedCoins; }
    public ArrayList<Item> getItemList() { return ItemDAO.itemList; }

    public void addItem(Item item) {
        ItemDAO.itemList().add(item);
    }

    public String toString() {
        String walletData = "\nbalance: " + this.getID()
                          + "\nearnedCoins: " + this.getName();

        return walletData;
    }
}
