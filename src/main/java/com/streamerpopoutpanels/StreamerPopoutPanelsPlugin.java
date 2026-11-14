package com.streamerpopoutpanels;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.inject.Provides;
import com.streamerpopoutpanels.tracker.BossLogTracker;
import com.streamerpopoutpanels.tracker.LootSessionTracker;
import com.streamerpopoutpanels.tracker.SlayerTracker;
import com.streamerpopoutpanels.tracker.VitalsTracker;
import com.streamerpopoutpanels.tracker.XpSessionTracker;
import com.streamerpopoutpanels.ui.AchievementsView;
import com.streamerpopoutpanels.ui.BossLogView;
import com.streamerpopoutpanels.ui.EquipmentView;
import com.streamerpopoutpanels.ui.GoalsView;
import com.streamerpopoutpanels.ui.InventoryView;
import com.streamerpopoutpanels.ui.LootView;
import com.streamerpopoutpanels.ui.PopoutView;
import com.streamerpopoutpanels.ui.PopoutWindow;
import com.streamerpopoutpanels.ui.SkillsView;
import com.streamerpopoutpanels.ui.SlayerView;
import com.streamerpopoutpanels.ui.VitalsView;
import com.streamerpopoutpanels.ui.XpTrackerView;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.EquipmentInventorySlot;
import net.runelite.api.GameState;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Skill;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.StatChanged;
import net.runelite.api.gameval.InventoryID;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.EventBus;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.RuneScapeProfileChanged;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.SkillIconManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Streamer Popout Panels",
	description = "Pop out your inventory, equipment, skills, goals, loot and more into separate windows for stream overlays",
	tags = {"stream", "streamer", "twitch", "obs", "overlay", "popout", "inventory", "equipment", "achievements", "xp", "loot", "slayer", "goals", "kc"}
)
public class StreamerPopoutPanelsPlugin extends Plugin
{
	private static final String ACHIEVEMENTS_KEY = "achievements";
	private static final int MAX_STORED_ACHIEVEMENTS = 50;
	/**
	 * Ticks after login during which stat changes only set the baseline, so logging in
	 * doesn't announce every skill as a level up.
	 */
	private static final int LEVEL_UP_GRACE_TICKS = 2;
	private static final int BOUNDS_SAVE_DELAY_MS = 500;

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EventBus eventBus;

	@Inject
	private StreamerPopoutPanelsConfig config;

	@Inject
	private ConfigManager configManager;

	@Inject
	private ItemManager itemManager;

	@Inject
	private SkillIconManager skillIconManager;

	@Inject
	private Gson gson;

	@Inject
	private XpSessionTracker xpTracker;

	@Inject
	private LootSessionTracker lootTracker;

	@Inject
	private SlayerTracker slayerTracker;

	@Inject
	private BossLogTracker bossLogTracker;

	@Inject
	private VitalsTracker vitalsTracker;

	// Client thread state
	private final Map<Skill, Integer> lastLevels = new EnumMap<>(Skill.class);
	private boolean levelUpsReady;
	private int ticksSinceLogin;

	// Swing event dispatch thread state
	private final Map<PanelType, PopoutWindow> windows = new EnumMap<>(PanelType.class);
	private final Map<PanelType, PopoutView> views = new EnumMap<>(PanelType.class);
	private final Map<PanelType, Rectangle> pendingBounds = new EnumMap<>(PanelType.class);
	private InventoryView inventoryView;
	private EquipmentView equipmentView;
	private SkillsView skillsView;
	private AchievementsView achievementsView;
	private XpTrackerView xpTrackerView;
	private LootView lootView;
	private SlayerView slayerView;
	private GoalsView goalsView;
	private BossLogView bossLogView;
	private VitalsView vitalsView;
	private Timer boundsSaveTimer;
	private List<Achievement> achievements = new ArrayList<>();

	@Override
	protected void startUp()
	{
		lastLevels.clear();
		levelUpsReady = false;
		ticksSinceLogin = 0;

		SwingUtilities.invokeLater(this::createUi);
		loadAchievements();

		xpTracker.setListener(session ->
		{
			if (xpTrackerView != null)
			{
				xpTrackerView.setSession(session);
			}
		});
		lootTracker.setListener(session ->
		{
			if (lootView != null)
			{
				lootView.setSession(session);
			}
		});
		slayerTracker.setListener(task ->
		{
			if (slayerView != null)
			{
				slayerView.setTask(task);
			}
		});
		bossLogTracker.setListener(records ->
		{
			if (bossLogView != null)
			{
				bossLogView.setRecords(records);
			}
		});
		vitalsTracker.setListener(vitals ->
		{
			if (vitalsView != null)
			{
				vitalsView.setVitals(vitals);
			}
		});
		bossLogTracker.setNewPersonalBestListener((boss, time) ->
		{
			if (config.trackPersonalBests())
			{
				addAchievement(new Achievement(AchievementType.PERSONAL_BEST, boss + " " + time, null, System.currentTimeMillis()));
			}
		});

		eventBus.register(xpTracker);
		eventBus.register(lootTracker);
		eventBus.register(slayerTracker);
		eventBus.register(bossLogTracker);
		eventBus.register(vitalsTracker);

		clientThread.invokeLater(() ->
		{
			bossLogTracker.load();
			vitalsTracker.forceRefresh();
			if (client.getGameState() != GameState.LOGGED_IN)
			{
				return;
			}
			updateInventory(client.getItemContainer(InventoryID.INV));
			updateEquipment(client.getItemContainer(InventoryID.WORN));
			updateSkills();
			for (Skill skill : SkillsView.LAYOUT)
			{
				lastLevels.put(skill, client.getRealSkillLevel(skill));
			}
			levelUpsReady = true;
			xpTracker.startLoggedIn();
			slayerTracker.refreshNextTick();
		});
	}

	@Override
	protected void shutDown()
	{
		eventBus.unregister(xpTracker);
		eventBus.unregister(lootTracker);
		eventBus.unregister(slayerTracker);
		eventBus.unregister(bossLogTracker);
		eventBus.unregister(vitalsTracker);

		xpTracker.setListener(null);
		vitalsTracker.setListener(null);
		lootTracker.setListener(null);
		slayerTracker.setListener(null);
		bossLogTracker.setListener(null);
		bossLogTracker.setNewPersonalBestListener(null);

		SwingUtilities.invokeLater(this::destroyUi);
	}

	@Provides
	StreamerPopoutPanelsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(StreamerPopoutPanelsConfig.class);
	}

	// ---------------------------------------------------------------- events (client thread)

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.INV)
		{
			updateInventory(event.getItemContainer());
		}
		else if (event.getContainerId() == InventoryID.WORN)
		{
			updateEquipment(event.getItemContainer());
		}
	}

	@Subscribe
	public void onStatChanged(StatChanged event)
	{
		checkLevelUp(event.getSkill(), event.getLevel());
		updateSkills();
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameState state = event.getGameState();
		if (state == GameState.LOGIN_SCREEN || state == GameState.HOPPING)
		{
			lastLevels.clear();
			levelUpsReady = false;
			ticksSinceLogin = 0;
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		if (!levelUpsReady && ++ticksSinceLogin >= LEVEL_UP_GRACE_TICKS)
		{
			for (Skill skill : SkillsView.LAYOUT)
			{
				lastLevels.putIfAbsent(skill, client.getRealSkillLevel(skill));
			}
			levelUpsReady = true;
		}
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		if (event.getType() != ChatMessageType.GAMEMESSAGE && event.getType() != ChatMessageType.SPAM)
		{
			return;
		}

		Achievement achievement = AchievementParser.parse(Text.removeTags(event.getMessage()), System.currentTimeMillis());
		if (achievement != null && isTracked(achievement.getType()))
		{
			addAchievement(achievement);
		}
	}

	@Subscribe
	public void onRuneScapeProfileChanged(RuneScapeProfileChanged event)
	{
		loadAchievements();
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		if (StreamerPopoutPanelsConfig.GROUP.equals(event.getGroup()) && !event.getKey().startsWith("bounds."))
		{
			SwingUtilities.invokeLater(this::applyConfig);
		}
	}

	// ---------------------------------------------------------------- game state snapshots (client thread)

	private void updateInventory(ItemContainer container)
	{
		BufferedImage[] images = new BufferedImage[InventoryView.SLOTS];
		if (container != null)
		{
			for (int i = 0; i < images.length; i++)
			{
				images[i] = itemImage(container.getItem(i));
			}
		}

		SwingUtilities.invokeLater(() ->
		{
			if (inventoryView != null)
			{
				inventoryView.setItems(images);
			}
		});
	}

	private void updateEquipment(ItemContainer container)
	{
		Map<EquipmentInventorySlot, BufferedImage> images = new EnumMap<>(EquipmentInventorySlot.class);
		if (container != null)
		{
			for (EquipmentInventorySlot slot : EquipmentView.displayedSlots())
			{
				BufferedImage image = itemImage(container.getItem(slot.getSlotIdx()));
				if (image != null)
				{
					images.put(slot, image);
				}
			}
		}

		SwingUtilities.invokeLater(() ->
		{
			if (equipmentView != null)
			{
				equipmentView.setItems(images);
			}
		});
	}

	private BufferedImage itemImage(Item item)
	{
		if (item == null || item.getId() < 0 || item.getQuantity() <= 0)
		{
			return null;
		}
		boolean stackable = item.getQuantity() > 1 || itemManager.getItemComposition(item.getId()).isStackable();
		return itemManager.getImage(item.getId(), item.getQuantity(), stackable);
	}

	private void updateSkills()
	{
		Map<Skill, Integer> real = new EnumMap<>(Skill.class);
		Map<Skill, Integer> boosted = new EnumMap<>(Skill.class);
		Map<Skill, Integer> experience = new EnumMap<>(Skill.class);
		for (Skill skill : SkillsView.LAYOUT)
		{
			real.put(skill, client.getRealSkillLevel(skill));
			boosted.put(skill, client.getBoostedSkillLevel(skill));
			experience.put(skill, client.getSkillExperience(skill));
		}
		int total = client.getTotalLevel();

		SwingUtilities.invokeLater(() ->
		{
			if (skillsView != null)
			{
				skillsView.setLevels(real, boosted, total);
			}
			if (goalsView != null)
			{
				goalsView.setProgress(experience, total);
			}
		});
	}

	private void checkLevelUp(Skill skill, int level)
	{
		Integer previous = lastLevels.put(skill, level);
		if (!levelUpsReady || previous == null || level <= previous || !config.trackLevelUps())
		{
			return;
		}

		String text = "Level " + level + " " + skill.getName() + (level == 99 ? "!" : "");
		addAchievement(new Achievement(AchievementType.LEVEL_UP, text, skill.name(), System.currentTimeMillis()));
	}

	private boolean isTracked(AchievementType type)
	{
		switch (type)
		{
			case LEVEL_UP:
				return config.trackLevelUps();
			case COLLECTION_LOG:
				return config.trackCollectionLog();
			case QUEST:
				return config.trackQuests();
			case COMBAT_TASK:
				return config.trackCombatAchievements();
			case DIARY:
				return config.trackDiaries();
			case PET:
				return config.trackPets();
			case DROP:
				return config.trackDrops();
			case PERSONAL_BEST:
				return config.trackPersonalBests();
			default:
				return false;
		}
	}

	// ---------------------------------------------------------------- achievements

	private void addAchievement(Achievement achievement)
	{
		log.debug("New achievement: {}", achievement);
		SwingUtilities.invokeLater(() ->
		{
			achievements.add(0, achievement);
			if (achievements.size() > MAX_STORED_ACHIEVEMENTS)
			{
				achievements.subList(MAX_STORED_ACHIEVEMENTS, achievements.size()).clear();
			}
			saveAchievements();
			updateAchievementsView();
		});
	}

	private void loadAchievements()
	{
		List<Achievement> loaded = new ArrayList<>();
		String json = configManager.getRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, ACHIEVEMENTS_KEY);
		if (json != null)
		{
			try
			{
				Achievement[] stored = gson.fromJson(json, Achievement[].class);
				if (stored != null)
				{
					loaded = Arrays.stream(stored)
						.filter(Objects::nonNull)
						.filter(a -> a.getType() != null && a.getText() != null)
						.limit(MAX_STORED_ACHIEVEMENTS)
						.collect(Collectors.toCollection(ArrayList::new));
				}
			}
			catch (JsonParseException e)
			{
				log.debug("Unable to load stored achievements", e);
			}
		}

		List<Achievement> result = loaded;
		SwingUtilities.invokeLater(() ->
		{
			achievements = result;
			updateAchievementsView();
		});
	}

	/**
	 * Must be called on the event dispatch thread.
	 */
	private void saveAchievements()
	{
		if (configManager.getRSProfileKey() != null)
		{
			configManager.setRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, ACHIEVEMENTS_KEY, gson.toJson(achievements));
		}
	}

	/**
	 * Must be called on the event dispatch thread.
	 */
	private void clearAchievements()
	{
		achievements = new ArrayList<>();
		if (configManager.getRSProfileKey() != null)
		{
			configManager.unsetRSProfileConfiguration(StreamerPopoutPanelsConfig.GROUP, ACHIEVEMENTS_KEY);
		}
		updateAchievementsView();
	}

	/**
	 * Must be called on the event dispatch thread.
	 */
	private void updateAchievementsView()
	{
		if (achievementsView != null)
		{
			int shown = Math.min(achievements.size(), config.maxAchievements());
			achievementsView.setEntries(new ArrayList<>(achievements.subList(0, shown)));
		}
	}

	// ---------------------------------------------------------------- windows (event dispatch thread)

	private void createUi()
	{
		inventoryView = new InventoryView();
		equipmentView = new EquipmentView();
		skillsView = new SkillsView(skillIconManager);
		achievementsView = new AchievementsView(skillIconManager);
		xpTrackerView = new XpTrackerView(skillIconManager);
		lootView = new LootView();
		slayerView = new SlayerView(skillIconManager);
		goalsView = new GoalsView(skillIconManager);
		bossLogView = new BossLogView();
		vitalsView = new VitalsView(skillIconManager);

		achievementsView.addMenuAction("Clear achievements", this::clearAchievements);
		xpTrackerView.addMenuAction("Reset session", () -> clientThread.invokeLater(xpTracker::reset));
		lootView.addMenuAction("Reset session", () -> clientThread.invokeLater(lootTracker::reset));
		bossLogView.addMenuAction("Clear kill counts", () -> clientThread.invokeLater(bossLogTracker::clear));

		views.put(PanelType.INVENTORY, inventoryView);
		views.put(PanelType.EQUIPMENT, equipmentView);
		views.put(PanelType.SKILLS, skillsView);
		views.put(PanelType.ACHIEVEMENTS, achievementsView);
		views.put(PanelType.XP_TRACKER, xpTrackerView);
		views.put(PanelType.LOOT, lootView);
		views.put(PanelType.SLAYER, slayerView);
		views.put(PanelType.GOALS, goalsView);
		views.put(PanelType.BOSS_LOG, bossLogView);
		views.put(PanelType.VITALS, vitalsView);

		boundsSaveTimer = new Timer(BOUNDS_SAVE_DELAY_MS, e -> flushBounds());
		boundsSaveTimer.setRepeats(false);

		applyConfig();
		updateAchievementsView();
	}

	private void destroyUi()
	{
		for (PanelType type : new ArrayList<>(windows.keySet()))
		{
			closeWindow(type);
		}
		flushBounds();
		if (boundsSaveTimer != null)
		{
			boundsSaveTimer.stop();
			boundsSaveTimer = null;
		}
		views.values().forEach(PopoutView::stop);
		views.clear();
		inventoryView = null;
		equipmentView = null;
		skillsView = null;
		achievementsView = null;
		xpTrackerView = null;
		lootView = null;
		slayerView = null;
		goalsView = null;
		bossLogView = null;
		vitalsView = null;
	}

	private void applyConfig()
	{
		if (views.isEmpty())
		{
			return;
		}

		for (PopoutView view : views.values())
		{
			view.setBackgroundColor(config.backgroundColor());
		}
		achievementsView.setHighlightSeconds(config.highlightSeconds());
		bossLogView.setHighlightSeconds(config.highlightSeconds());
		goalsView.setGoals(GoalParser.parse(config.goals()));
		updateAchievementsView();

		for (PanelType type : PanelType.values())
		{
			boolean wanted = isPanelEnabled(type);
			PopoutWindow window = windows.get(type);
			if (wanted && window == null)
			{
				openWindow(type);
			}
			else if (!wanted && window != null)
			{
				closeWindow(type);
			}
			else if (window != null && window.isAlwaysOnTop() != config.alwaysOnTop())
			{
				window.setAlwaysOnTop(config.alwaysOnTop());
			}
		}
	}

	private boolean isPanelEnabled(PanelType type)
	{
		switch (type)
		{
			case INVENTORY:
				return config.showInventory();
			case EQUIPMENT:
				return config.showEquipment();
			case SKILLS:
				return config.showSkills();
			case ACHIEVEMENTS:
				return config.showAchievements();
			case XP_TRACKER:
				return config.showXpTracker();
			case LOOT:
				return config.showLoot();
			case SLAYER:
				return config.showSlayer();
			case GOALS:
				return config.showGoals();
			case BOSS_LOG:
				return config.showBossLog();
			case VITALS:
				return config.showVitals();
			default:
				return false;
		}
	}

	private void openWindow(PanelType type)
	{
		PopoutWindow window = new PopoutWindow(
			type,
			views.get(type),
			loadBounds(type),
			config.alwaysOnTop(),
			// Closing the window turns the panel off, which closes it via applyConfig()
			() -> configManager.setConfiguration(StreamerPopoutPanelsConfig.GROUP, type.getConfigKey(), false),
			bounds -> queueBoundsSave(type, bounds));
		windows.put(type, window);
		window.setAutoRequestFocus(false);
		window.setVisible(true);
	}

	private void closeWindow(PanelType type)
	{
		PopoutWindow window = windows.remove(type);
		if (window != null)
		{
			window.dispose();
		}
	}

	private Rectangle loadBounds(PanelType type)
	{
		String value = configManager.getConfiguration(StreamerPopoutPanelsConfig.GROUP, type.getBoundsKey());
		if (value == null)
		{
			return null;
		}
		try
		{
			String[] parts = value.split(",");
			if (parts.length == 4)
			{
				return new Rectangle(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
			}
		}
		catch (NumberFormatException e)
		{
			log.debug("Invalid saved bounds for {}: {}", type, value);
		}
		return null;
	}

	/**
	 * Window moves and resizes fire continuously while dragging, so saves are debounced.
	 */
	private void queueBoundsSave(PanelType type, Rectangle bounds)
	{
		pendingBounds.put(type, bounds);
		if (boundsSaveTimer != null)
		{
			boundsSaveTimer.restart();
		}
	}

	private void flushBounds()
	{
		for (Map.Entry<PanelType, Rectangle> entry : pendingBounds.entrySet())
		{
			Rectangle r = entry.getValue();
			configManager.setConfiguration(StreamerPopoutPanelsConfig.GROUP, entry.getKey().getBoundsKey(),
				r.x + "," + r.y + "," + r.width + "," + r.height);
		}
		pendingBounds.clear();
	}
}
