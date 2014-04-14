package nl.evolutioncoding.AreaShop;

import java.util.HashMap;

import nl.evolutioncoding.AreaShop.AreaShop.RegionEventType;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;

import com.sk89q.worldguard.protection.managers.RegionManager;

/**
 * Checks for placement of signs for this plugin
 * @author NLThijs48
 */
public final class SignChangeListener implements Listener {
	AreaShop plugin;
	String chatPrefix;
	String rentSign;
	String buySign;
	String signRentable;
	String signBuyable;
	
	/**
	 * Constructor
	 * @param plugin The AreaShop plugin
	 */
	public SignChangeListener(AreaShop plugin) {
		this.plugin = plugin;
		chatPrefix = plugin.fixColors(plugin.config().getString("chatPrefix"));
		rentSign = plugin.config().getString("rentSign");
		buySign = plugin.config().getString("buySign");
		signRentable = plugin.fixColors(plugin.config().getString("signRentable"));
		signBuyable = plugin.fixColors(plugin.config().getString("signBuyable"));
	}
	
	/**
	 * Called when a sign is changed
	 * @param event The event
	 */
	@EventHandler(priority = EventPriority.HIGH)
	public void onSignChange(SignChangeEvent event) {
		Player player = event.getPlayer();
		
		/* Check if the sign is meant for this plugin */
		if(event.getLine(0).contains(rentSign)) {
			if(!player.hasPermission("areashop.createrent")) {
				player.sendMessage(chatPrefix + "You don't have permission for setting up renting of regions");				
				return;
			}
			
			
			/* Get the other lines */
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			String fourthLine = event.getLine(3);
			
			/* Get the regionManager for accessing regions */
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
		
			/* check if all the lines are correct */			
			if(secondLine == null || secondLine.length() == 0) {
				player.sendMessage(chatPrefix + "You did not specify a region on the second line!");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				player.sendMessage(chatPrefix + "The region you specified does not exist!");
				return;
			} else if(plugin.getFileManager().getRent(secondLine) != null) {
				player.sendMessage(chatPrefix + "The region you specified already has a sign for renting");
				return;
			} else if(thirdLine == null || thirdLine.length() == 0) {
				player.sendMessage(chatPrefix + "You did not specify how long the region can be rented, do this on the third line");
				return;
			} else if(!plugin.checkTimeFormat(thirdLine)) {
				player.sendMessage(chatPrefix + "The time specified is not in the correct format");
				return;
			} else if(fourthLine == null || fourthLine.length() == 0) {
				player.sendMessage(chatPrefix + "You did not specify how much the renting costs on the fourth line!");
				return;
			} else {
				/* Check the fourth line */
				try {
					Double.parseDouble(fourthLine);
				} catch (NumberFormatException e) {
					player.sendMessage(chatPrefix + "You did not specify the renting cost correctly, use a number only");
					return;
				}
								
				/* Set the first line to signRentable */
				event.setLine(0, signRentable);
				event.setLine(1, regionManager.getRegion(secondLine).getId());
				event.setLine(3, plugin.formatCurrency(fourthLine));
				
				/* Add rent to the FileManager */
				HashMap<String,String> rent = new HashMap<String,String>();
				rent.put(plugin.keyWorld, event.getBlock().getWorld().getName());
				rent.put(plugin.keyX, String.valueOf(event.getBlock().getX()));
				rent.put(plugin.keyY, String.valueOf(event.getBlock().getY()));
				rent.put(plugin.keyZ, String.valueOf(event.getBlock().getZ()));
				rent.put(plugin.keyDuration, thirdLine);
				rent.put(plugin.keyPrice, fourthLine);
				rent.put(plugin.keyName, regionManager.getRegion(secondLine).getId());
				rent.put(plugin.keyRestore, "general");
				rent.put(plugin.keySchemProfile, "default");
				
				plugin.getFileManager().addRent(secondLine, rent);
				plugin.getFileManager().handleSchematicEvent(secondLine, true, RegionEventType.CREATED);
				
				/* Set the flags for the region */
				plugin.getFileManager().setRegionFlags(secondLine, plugin.config().getConfigurationSection("flagsForRent"), true);

				player.sendMessage(chatPrefix + "Renting of the region is setup correctly");
			}
		} else if (event.getLine(0).contains(buySign)) {
			/* Check for permission */
			if(!player.hasPermission("areashop.createbuy")) {
				player.sendMessage(chatPrefix + "You don't have permission for setting up buying of regions");				
				return;
			}
			
			/* Get the other lines */
			String secondLine = event.getLine(1);
			String thirdLine = event.getLine(2);
			
			/* Get the regionManager for accessing regions */
			RegionManager regionManager = plugin.getWorldGuard().getRegionManager(event.getPlayer().getWorld());
		
			/* Check if all the lines are correct */			
			if(secondLine == null || secondLine.length() == 0) {
				player.sendMessage(chatPrefix + "You did not specify a region on the second line!");
				return;
			} else if(regionManager.getRegion(secondLine) == null) {
				player.sendMessage(chatPrefix + "The region you specified does not exist!");
				return;
			} else if(plugin.getFileManager().getBuy(secondLine) != null) {
				player.sendMessage(chatPrefix + "The region you specified already has a sign for buying");
				return;
			} else if(thirdLine == null || thirdLine.length() == 0) {
				player.sendMessage(chatPrefix + "You did not specify how much the buying costs on the fourth line!");
				return;
			} else {
				/* Check the fourth line */
				try {
					Double.parseDouble(thirdLine);
				} catch (NumberFormatException e) {
					player.sendMessage(chatPrefix + "You did not specify the buying cost correctly, use a number only");
					return;
				}
								
				/* Set the first line to signbuyable */
				event.setLine(0, signBuyable);
				event.setLine(1, regionManager.getRegion(secondLine).getId());
				event.setLine(2, plugin.formatCurrency(thirdLine));
				
				/* Add buy to the FileManager */
				HashMap<String,String> buy = new HashMap<String,String>();
				buy.put(plugin.keyWorld, event.getBlock().getWorld().getName());
				buy.put(plugin.keyX, String.valueOf(event.getBlock().getX()));
				buy.put(plugin.keyY, String.valueOf(event.getBlock().getY()));
				buy.put(plugin.keyZ, String.valueOf(event.getBlock().getZ()));
				buy.put(plugin.keyPrice, thirdLine);
				buy.put(plugin.keyName, regionManager.getRegion(secondLine).getId());
				buy.put(plugin.keyRestore, "general");
				buy.put(plugin.keySchemProfile, "default");
				
				plugin.getFileManager().addBuy(secondLine, buy);
				plugin.getFileManager().handleSchematicEvent(secondLine, false, RegionEventType.CREATED);
				
				/* Set the flags for the region */
				plugin.getFileManager().setRegionFlags(secondLine, plugin.config().getConfigurationSection("flagsForSale"), false);
				
				player.sendMessage(chatPrefix + "Buying of the region is setup correctly");
			}
		}
	}
}
































