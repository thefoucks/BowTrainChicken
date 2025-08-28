package com.bowtrain.stats;

import com.bowtrain.BowTrainChicken;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class PeriodClock {
    
    private final BowTrainChicken plugin;
    private final YamlStatsStore statsStore;
    
    public PeriodClock(BowTrainChicken plugin) {
        this.plugin = plugin;
        this.statsStore = new YamlStatsStore(plugin);
    }
    
    public void checkAndResetStats() {
        LocalDateTime now = LocalDateTime.now();
        
        // Check daily reset (midnight)
        LocalDateTime lastDaily = statsStore.getLastReset("daily");
        if (now.getDayOfYear() != lastDaily.getDayOfYear() || now.getYear() != lastDaily.getYear()) {
            plugin.getStatisticsManager().resetDaily();
            statsStore.setLastReset("daily", now);
        }
        
        // Check weekly reset (Saturday midnight)
        LocalDateTime lastWeekly = statsStore.getLastReset("weekly");
        if (now.getDayOfWeek() == DayOfWeek.SATURDAY && 
            (now.getDayOfWeek() != lastWeekly.getDayOfWeek() || 
             now.getDayOfYear() != lastWeekly.getDayOfYear() || 
             now.getYear() != lastWeekly.getYear())) {
            plugin.getStatisticsManager().resetWeekly();
            statsStore.setLastReset("weekly", now);
        }
        
        // Check monthly reset (1st of month)
        LocalDateTime lastMonthly = statsStore.getLastReset("monthly");
        if (now.getDayOfMonth() == 1 && 
            (now.getDayOfMonth() != lastMonthly.getDayOfMonth() || 
             now.getMonthValue() != lastMonthly.getMonthValue() || 
             now.getYear() != lastMonthly.getYear())) {
            plugin.getStatisticsManager().resetMonthly();
            statsStore.setLastReset("monthly", now);
        }
        
        // Check yearly reset (January 1st)
        LocalDateTime lastYearly = statsStore.getLastReset("yearly");
        if (now.getDayOfYear() == 1 && now.getYear() != lastYearly.getYear()) {
            plugin.getStatisticsManager().resetYearly();
            statsStore.setLastReset("yearly", now);
        }
    }
}