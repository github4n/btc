package com.example.btc.services.ws.enums;

/**
 * Futures Currencies (Updated on: 2018-03-13 17:39:09)
 *
 * @author Tony Tian
 * @version 1.0.0
 * @date 2018/3/13 17:37
 */
public enum FuturesCurrenciesEnum {

    BTC(0), LTC(1), ETH(2), ETC(4), XRP(15), EOS(20), BCH(301), BSV(302);
    private int symbol;

    FuturesCurrenciesEnum(int symbol) {
        this.symbol = symbol;
    }

    public int getSymbol() {
        return symbol;
    }
}
