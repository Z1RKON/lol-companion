package com.lolcompanion.exception;

/**
 * Исключение, выбрасываемое когда призыватель не найден ни в локальной БД, ни в Riot API.
 */
public class SummonerNotFoundException extends RuntimeException {
    
    private final String summonerName;
    private final String region;
    
    public SummonerNotFoundException(String summonerName, String region) {
        super(String.format("Призыватель '%s' не найден в регионе %s", summonerName, region));
        this.summonerName = summonerName;
        this.region = region;
    }
    
    public SummonerNotFoundException(String message) {
        super(message);
        this.summonerName = null;
        this.region = null;
    }
    
    public String getSummonerName() {
        return summonerName;
    }
    
    public String getRegion() {
        return region;
    }
}
