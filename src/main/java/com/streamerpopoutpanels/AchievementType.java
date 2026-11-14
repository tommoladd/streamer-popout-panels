package com.streamerpopoutpanels;

import java.awt.Color;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AchievementType
{
	LEVEL_UP("Level up", new Color(0x4FC3F7)),
	COLLECTION_LOG("Collection log", new Color(0xFFB300)),
	QUEST("Quest complete", new Color(0x81C784)),
	COMBAT_TASK("Combat task", new Color(0xE57373)),
	DIARY("Achievement diary", new Color(0xBA68C8)),
	PET("Pet!", new Color(0xF06292)),
	DROP("Drop", new Color(0xFFD54F)),
	PERSONAL_BEST("Personal best", new Color(0x4DD0E1));

	private final String label;
	private final Color color;
}
