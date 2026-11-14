# Streamer Popout Panels

Pop parts of your game out into their own windows so you can add them to your stream layout.
The panels update live as things change in game.

## Panels

- **Inventory**: your 28 inventory slots
- **Equipment**: your worn equipment, laid out like the in-game tab
- **Skills**: every skill with its current (boosted) and real level, plus your total level
- **Recent achievements**: a feed of level ups, new collection log slots, quests, combat tasks,
  achievement diaries, pets, valuable drops and new personal bests. New entries are highlighted for a short time.
  The feed is saved per account and survives restarts. Right-click the panel to clear it.
- **Session XP**: time logged in this session, XP gained per skill, XP/hr and time to next level.
  Right-click to reset the session.
- **Loot tracker**: total loot value this session, your most recent loot and your best drops.
  Right-click to reset the session. Loot from chests, clues and raids needs the Loot Tracker plugin to be enabled.
- **Slayer task**: your current task, location, kills left, slayer points and task streak.
- **Goals**: progress bars towards goals you set in the plugin settings, one per line:
  - `Slayer 99` for a level (up to 126)
  - `Fishing 50m` or `Fishing 13,034,431 xp` for an amount of XP
  - `Total 2000` for a total level
- **Kill counts**: boss kill counts and personal best times, most recently killed first.
  New personal bests are highlighted. Saved per account; right-click to clear.
- **Vitals**: hitpoints, prayer and run energy bars. The hitpoints bar changes colour when you're poisoned,
  envenomed or diseased, and the run bar changes colour while a stamina potion is active.

Each window can be resized freely and the content scales to fit. Window positions and sizes are remembered.

## Adding a panel to OBS

1. Turn the panel on in the plugin's settings.
2. In OBS, add a **Window Capture** source and select the window called `Streamer Popout - <panel name>`.
3. Optionally, set the plugin's **Background colour** to a solid green and add a **Chroma Key** filter in OBS
   to make the background transparent.

Collection log and drop entries rely on the matching in-game chat notifications being turned on
(Settings > Chat: *Collection log - New addition notification* and *Loot drop notifications*).
