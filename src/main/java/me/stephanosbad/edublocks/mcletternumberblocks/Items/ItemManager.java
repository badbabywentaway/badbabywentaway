package me.stephanosbad.edublocks.mcletternumberblocks.Items;

import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.th0rgal.oraxen.api.OraxenBlocks;
import io.th0rgal.oraxen.api.OraxenItems;
import io.th0rgal.oraxen.compatibilities.CompatibilityProvider;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import me.stephanosbad.edublocks.mcletternumberblocks.McLetterNumberBlocks;
import me.stephanosbad.edublocks.mcletternumberblocks.Rewards.DropReward;
import me.stephanosbad.edublocks.mcletternumberblocks.Rewards.Reward;
import me.stephanosbad.edublocks.mcletternumberblocks.Rewards.RewardType;
import me.stephanosbad.edublocks.mcletternumberblocks.Rewards.VaultCurrencyReward;
import me.stephanosbad.edublocks.mcletternumberblocks.Utility.LocationPair;
import me.stephanosbad.edublocks.mcletternumberblocks.Utility.SimpleTuple;
import me.stephanosbad.edublocks.mcletternumberblocks.Utility.WordDict;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.NoteBlock;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Heart of item control.
 */
public class ItemManager extends CompatibilityProvider<McLetterNumberBlocks> implements Listener {

    /**
     * Wood material list.
     */
    private final HashMap<Material, Material> list = new HashMap() {
        {
            put(Material.ACACIA_LOG, Material.STRIPPED_ACACIA_LOG);
            put(Material.SPRUCE_LOG, Material.STRIPPED_SPRUCE_LOG);
            put(Material.OAK_LOG, Material.STRIPPED_OAK_LOG);
            put(Material.DARK_OAK_LOG, Material.STRIPPED_DARK_OAK_LOG);
            put(Material.JUNGLE_LOG, Material.STRIPPED_JUNGLE_LOG);
            put(Material.BIRCH_LOG, Material.STRIPPED_BIRCH_LOG);
            put(Material.MANGROVE_LOG, Material.STRIPPED_MANGROVE_LOG);
            put(Material.CHERRY_LOG, Material.STRIPPED_CHERRY_LOG);
            put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM);
            put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM);
            put(Material.BAMBOO_BLOCK, Material.STRIPPED_BAMBOO_BLOCK);
        }
    };


    private final HashMap<Material, Material> listForNumberDrops = new HashMap() {
        {
            put(Material.WARPED_STEM, Material.STRIPPED_WARPED_STEM);
            put(Material.CRIMSON_STEM, Material.STRIPPED_CRIMSON_STEM);
        }
    };

    private List<ItemStack> characterBlocksAvailableInNether = null;

    /**
     * Exclusion zone for use of this plugin.
     */
    private LocationPair exclude = null;

    /**
     * Inclusion zone for use of this plugin. If defined, acts as an exclusive include.
     */
    private LocationPair include = null;

    /**
     * World Guard anti griefing tool. Accessor.
     */
    WorldGuard worldGuard = null;

    /**
     * World Guard anti griefing tool. Plugin accessor.
     */
    WorldGuardPlugin worldGuardPlugin = null;

    /**
     * Grief Prevention anti griefing tool. Accessor
     */
    GriefPrevention griefPrevention = null;

    /**
     * Reward implementations
     */
    public List<Reward> rewards = new ArrayList<>();

    public ItemManager()
    {
        this(null);
    }
    /**
     * Constructor
     * @param localPlugin - Master plugin
     */
    public ItemManager(McLetterNumberBlocks localPlugin ) {
        this.plugin = localPlugin;
        try {
            worldGuardPlugin = WorldGuardPlugin.inst();
            worldGuard = WorldGuard.getInstance();
            if (worldGuardPlugin != null && worldGuard != null) {
                Bukkit.getLogger().info("WorldGuard found.");
            } else {
                throw new NullPointerException("Class variable did not instantiate");
            }
        } catch (Exception | Error e) {
            Bukkit.getLogger().info("WorldGuard not available.");
        }

        try {
            griefPrevention = GriefPrevention.instance;
            if (griefPrevention != null) {
                Bukkit.getLogger().info("GriefPrevention found.");
            } else {
                throw new NullPointerException("Class variable did not instantiate");
            }
        } catch (Exception | Error e) {
            Bukkit.getLogger().info("GriefPrevention not available.");
        }

        try {
            setRewards();
        } catch (Exception | Error e) {
            Bukkit.getLogger().info("Rewards not available.");
        }
    }

    /**
     * Retrieve letter blocks. Right now Oraxen only.
     * @param commandString - Name of oraxen item.
     * @return - ItemStack of a single letter/number block
     */
    public static ItemStack getBlocks(String commandString) {
        return OraxenItems.getItemById(commandString).build();
    }

    /**
     * Retrieve all character block names
     * @return All Oraxen character block names.
     */
    public static @NotNull Set<String> getCharacterBlockNames() {
        var retValue = new HashSet<String>();

        for (var letter : LetterBlock.values()) {
            retValue.add(letter.id);
        }
        return retValue;
    }

    /**
     * combined action for wood block or letter block rewards
     *
     * @param e - block break event
     */
    @EventHandler
    public void onBreakWoodOrLetter(@NotNull BlockBreakEvent e) {
        var player = e.getPlayer();
        player.getInventory().getItemInMainHand();
        player.getInventory().getItemInMainHand().getEnchantments();

        if (!(player.getInventory().getItemInMainHand().containsEnchantment(Enchantment.SILK_TOUCH))) {
            //If there is no silk touch on it

            Material material = e.getBlock().getBlockData().getMaterial();
            if (list.containsKey(material)) {
                var hand = e.getPlayer().getInventory().getItemInMainHand();


                //Must be gold item in hand
                if (hand.getItemMeta() == null)
                {
                    return;
                }
                if (!hand.getType().name().toLowerCase(Locale.ROOT).contains("gold") &&
                !hand.getItemMeta().getItemName().toLowerCase(Locale.ROOT).contains("gold")) {
                    return;
                }

                var chance = .03;
                if(hand.containsEnchantment(Enchantment.LOOTING))
                {
                    switch(hand.getEnchantments().get(Enchantment.LOOTING))
                    {
                        case 1:
                            chance = .05;
                            break;
                        case 2:
                            chance = .08;
                            break;
                        case 3:
                            chance = .1;
                            break;
                        default:
                            break;
                    }
                }

                if(Math.random() < chance) {
                    //check wood
                    woodBlockBreak(e, list.get(material), material);
                }
            } else {
                //check letter
                letterBlockBreak(e);
            }
        }
    }

    /**
     * Check if it was a wood block that was broken.
     * @param e - break event.
     * @param material - Material to replace block
     * @param oldMaterial - Old material of block
     */
    private void woodBlockBreak(BlockBreakEvent e, Material material, Material oldMaterial) {
        var block = LetterBlock.randomPickOraxenBlock();
        var player = e.getPlayer();
        if (protectedSpot(player, e.getBlock().getLocation(), e.getBlock())) {
            player.sendMessage("Protected.");
            return;
        }
        if (block != null) {
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
            if(listForNumberDrops.containsKey(oldMaterial))
            {
                player.getWorld().dropItemNaturally(e.getBlock().getLocation(), randomNumAndCharacter());
            }
            player.getWorld().dropItemNaturally(e.getBlock().getLocation(), block);
            player.getWorld().dropItemNaturally(e.getBlock().getLocation(), new ItemStack(material, 1));

        }
    }

    private ItemStack randomNumAndCharacter() {
        if(characterBlocksAvailableInNether == null)
        {
            characterBlocksAvailableInNether = new ArrayList<>();
            for(var x : NumericBlock.values())
            {
                characterBlocksAvailableInNether.add(x.itemStack);
            }
            for(var x : NonAlphaNumBlocks.values())
            {
                characterBlocksAvailableInNether.add(x.itemStack);
            }
        }

        return characterBlocksAvailableInNether.get((int)(Math.random()*characterBlocksAvailableInNether.size()));

    }

    /**
     * Check if it was a letter block that was broken.
     * @param e - break event.
     */
    public void letterBlockBreak(BlockBreakEvent e) {
        var hand = e.getPlayer().getInventory().getItemInMainHand();

        if (protectedSpot(e.getPlayer(), e.getBlock().getLocation(), e.getBlock())) {
            e.getPlayer().sendMessage("Protected block: " + e.getBlock().getLocation());
            return;
        }
        if(hand.getItemMeta() == null)
        {
            return;
        }
        if (!hand.getType().name().toLowerCase(Locale.ROOT).contains("gold") &&
                !hand.getItemMeta().getItemName().toLowerCase(Locale.ROOT).contains("gold")) {
            return;
        }

        var testBlock = e.getBlock();
        var score = 0D;
        var c = testForLetter(e.getPlayer(), testBlock);
        if (c.first == '\0') {
            return;
        }
        var lateralDirection = checkLateralBlocks(e.getPlayer(), testBlock);
        StringBuilder outString = new StringBuilder();
        List<Location> blockArray = new ArrayList<>(List.of());

        if (lateralDirection.isValid()) {
            while (c.first != '\0') {
                score += c.second + 10;
                blockArray.add(testBlock.getLocation());
                outString.append(c.first);
                testBlock = offsetBlock(testBlock, lateralDirection);
                c = testForLetter(e.getPlayer(), testBlock);
            }
        }
        if (WordDict.singleton.Words.contains(outString.toString().toLowerCase(Locale.ROOT))) {
            e.setCancelled(true);
            e.getPlayer().sendMessage("Hit: " + score);

            for (var locationOfBlock : blockArray) {
                e.getBlock().getWorld().getBlockAt(locationOfBlock).setType(Material.AIR);
            }
            applyScore(e.getPlayer(), score);
        } else {
            e.getPlayer().sendMessage("Miss");
        }
    }

    /**
     * Apply the score to the player. Drops or cash.
     * @param player - Player to apply score
     * @param score - score
     */
    private void applyScore(@NotNull Player player, double score) {
        for (var reward : rewards) {
            if (reward instanceof VaultCurrencyReward) {
                ((VaultCurrencyReward) reward).applyReward(player, score);
            } else if (reward instanceof DropReward) {
                ((DropReward) reward).applyReward(player, player.getLocation(), score);
            }
        }
    }

    /**
     * Find block adjacent to another
     * @param testBlock - block from which to find the adjacent
     * @param lateralDirection - Direction in which to test
     * @return adjacent block
     */
    private Block offsetBlock(Block testBlock, LateralDirection lateralDirection) {
        var x = testBlock.getX() + lateralDirection.xOffset;
        var y = testBlock.getY();
        var z = testBlock.getZ() + lateralDirection.zOffset;
        return testBlock.getWorld().getBlockAt(x, y, z);
    }

    /**
     * Check the block for the next lateral block.
     * @param player - player (used in grief protecting)
     * @param testBlock - block under test
     * @return Direction of block.
     */
    private @NotNull LateralDirection checkLateralBlocks(Player player, @NotNull Block testBlock) {
        var retValue = new LateralDirection(0, 0);
        var world = testBlock.getWorld();
        var x = testBlock.getX();
        var y = testBlock.getY();
        var z = testBlock.getZ();

        boolean xUp = testForLetter(player, world.getBlockAt(x + 1, y, z)).first != '\0';
        boolean xDown = testForLetter(player, world.getBlockAt(x - 1, y, z)).first != '\0';
        boolean zUp = testForLetter(player, world.getBlockAt(x, y, z + 1)).first != '\0';
        boolean zDown = testForLetter(player, world.getBlockAt(x, y, z - 1)).first != '\0';

        if (xUp && !xDown && !zUp && !zDown) {
            retValue.xOffset = 1;
        } else if (!xUp && xDown && !zUp && !zDown) {
            retValue.xOffset = -1;
        } else if (!xUp && !xDown && zUp && !zDown) {
            retValue.zOffset = 1;
        } else if (!xUp && !xDown && !zUp && zDown) {
            retValue.zOffset = -1;
        }

        return retValue;
    }

    /**
     * Test to see if this block is a letter block
     * @param player - player who hit it. Used to null the result if letter block is grief protected.
     * @param testBlock - block to test.
     * @return - character of block and rarity score
     */
    SimpleTuple<Character, Double> testForLetter(Player player, Block testBlock) {
        if (protectedSpot(player, testBlock.getLocation(), testBlock)) {
            Bukkit.getLogger().info("Part of word is protected: " + testBlock.getLocation());
            return new SimpleTuple<>('\0', 0.0);
        }
        if (!(testBlock.getState().getBlockData() instanceof NoteBlock)) {
            return new SimpleTuple<>('\0', 0.0);
        }
        AtomicReference<SimpleTuple<Character, Double>> match = new AtomicReference<>(new SimpleTuple<>('\0', 0D));
        var variation = getCustomVariation(testBlock);
        if (Arrays.stream(LetterBlock.values()).anyMatch((v) -> {
            boolean found = variation == v.customVariation;
            if (found) {
                match.set(new SimpleTuple<>(v.character, v.frequencyFactor));

            }
            return found;
        })) {
            return match.get();
        }
        return new SimpleTuple<>('\0', 0.0);
    }

    /**
     * Get "noteblock" variation code. When Oraxen obsoletes this, this will change.
     * @param block - noteblock block
     * @return - Oraxen's noteblock variation code
     */
    int getCustomVariation(Block block) {
        /*NoteBlock noteBlock = (NoteBlock) block.getState().getBlockData();
        NoteBlockMechanic mech = NoteBlockMechanicFactory.getBlockMechanic((int) (noteBlock
                .getInstrument().getType()) * 25 + (int) noteBlock.getNote().getId()
                + (noteBlock.isPowered() ? 400 : 0) - 26);
        return mech.getCustomVariation();*/
        return OraxenBlocks.getNoteBlockMechanic(block).getCustomVariation();
    }

    /**
     * Determine if this location is protected from this player
     * @param player - MC Player
     * @param location - location to examine
     * @param block - block to examine (some grief plugins require this)
     * @return - verification that location is being protected
     */
    boolean protectedSpot(Player player, Location location, Block block) {
        if (griefPrevention != null && griefPrevention.allowBreak(player, block, location) != null) {
            return true;
        }

        if (worldGuardPlugin != null &&
                !worldGuardPlugin.createProtectionQuery().testBlockBreak(player, block)) {
            return true;
        }

        return ourConfigProtects(location);
    }

    /**
     * Determine if our config protects this location
     * @param location - location to examine
     * @return - verification that location is being protected
     */
    private boolean ourConfigProtects(Location location) {

        if (exclude == null) {
            exclude = new LocationPair(
                    plugin.configDataHandler.configuration.getLocation("exclude.from", null),
                    plugin.configDataHandler.configuration.getLocation("exclude.to", null));
        }

        if (include == null) {
            include = new LocationPair(
                    plugin.configDataHandler.configuration.getLocation("include.from", null),
                    plugin.configDataHandler.configuration.getLocation("include.to", null));
        }

        if (exclude.isValid() && exclude.check(location)) {
            return true;
        }

        return include.isValid() && !include.check(location);
    }

    /**
     * Setup rewards from config file
     */
    private void setRewards() {
        for (var t : RewardType.values()) {
            var configuration = plugin.configDataHandler.configuration;
            switch (t) {

                case VaultCurrency: {
                    try {
                        var vaultConfig = configuration.getConfigurationSection("VaultCurrency");
                        double minimumRewardCount = vaultConfig.getDouble("minimumRewardCount");
                        double multiplier = vaultConfig.getDouble("multiplier");
                        double minimumThreshold = vaultConfig.getDouble("minimumThreshold");
                        double maximumRewardCap = vaultConfig.getDouble("maximumRewardCap");
                        var vaultReward = new VaultCurrencyReward(plugin, minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap);
                        rewards.add(vaultReward);
                    } catch (Exception | Error e) {
                        Bukkit.getLogger().info(e.toString());
                    }
                }
                break;
                case Drop: {

                    var listOfDropConfigs = configuration.getList("Drop");

                    assert listOfDropConfigs != null;
                    for (var drop : listOfDropConfigs) {
                        try {
                            if(!(drop instanceof Map) )
                            {
                                continue;
                            }
                            var dropParams = (Map<String, Object>) drop;
                            String materialName = (String) dropParams.get("materialName");
                            double minimumRewardCount = (double) dropParams.get("minimumRewardCount");
                            double multiplier = (double) dropParams.get("multiplier");
                            double minimumThreshold = (double) dropParams.get("minimumThreshold");
                            double maximumRewardCap = (double) dropParams.get("maximumRewardCap");
                            rewards.add(new DropReward(materialName, minimumRewardCount, multiplier, minimumThreshold, maximumRewardCap));

                        } catch (Exception | Error e) {
                            Bukkit.getLogger().info(e.toString());
                        }
                    }
                }
                break;
                default:
                    break;
            }
        }
    }
}
