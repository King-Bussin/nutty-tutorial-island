import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.input.Camera;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.input.Mouse;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.util.Random;

@ScriptManifest(
    name = "Bussin Tut",
    author = "Nutmeg Dan",
    description = "Completes Tutorial Island.",
    category = Category.MISC,
    version = 1.0
)
public class Main extends AbstractScript {

    private Random random = new Random();
    private Area survivalArea = new Area(3105, 3093, 3100, 3098);
    private long startTime;
    private String currentAction = "Starting...";
    private int antiBanCount = 0;
    private String lastAntiBan = "None";
    private long lastAntiBanTime = 0;
    private int lastVarp = 0;

    private static final int FINAL_VARP = 1000;
    private static final String[] STEP_NAMES = {
        "Character Creation", "Talk to Gielinor Guide", "Open Settings", "Talk to Guide Again",
        "Exit Door", "Walk to Survival Expert", "Open Inventory", "Fish Shrimps",
        "Open Skills", "Talk to Survival Expert", "Chop Tree", "Light Fire",
        "Cook Shrimps", "Walk to Kitchen", "Enter Kitchen", "Talk to Chef",
        "Make Dough", "Cook Bread", "Exit Kitchen", "Enable Run",
        "Run to Quest Guide", "Talk to Quest Guide", "Open Quest Tab", "Talk to Quest Guide",
        "Go Down Ladder", "Talk to Mining Instructor", "Continue Dialogue",
        "Mine Tin", "Mine Copper", "Smelt Bronze", "Talk to Instructor",
        "Use Anvil", "Smith Dagger", "Walk to Gate", "Talk to Combat Instructor",
        "Open Equipment", "More Info", "Equip Dagger", "Talk to Instructor",
        "Equip Sword+Shield", "Open Combat Tab", "Enter Rat Pen", "Kill Rat",
        "Exit Rat Pen", "Equip Ranged", "Kill Rat (Ranged)", "Climb Ladder",
        "Use Bank", "Close Bank + Poll", "Open Door", "Talk to Account Guide",
        "Open Account Tab", "Talk to Account Guide", "Walk Through Door",
        "Talk to Brother Brace", "Open Prayer Tab", "Talk to Brother Brace",
        "Leave Chapel", "Run to Magic Instructor", "Open Magic Tab",
        "Talk to Magic Instructor", "Cast Wind Strike", "Final Talk",
        "Home Teleport", "Complete!"
    };
    private static final int[] STEP_VARPS = {
        1, 2, 3, 7, 10, 20, 30, 40, 50, 60, 70, 80, 90,
        120, 130, 140, 150, 160, 170, 200, 210, 220, 230, 240,
        250, 260, 270, 300, 310, 320, 330, 340, 350, 360, 370,
        390, 400, 405, 410, 420, 430, 440, 460, 470, 480, 490, 500,
        510, 520, 525, 530, 531, 532, 540, 550, 560, 570,
        610, 620, 630, 640, 650, 671, 680, 1000
    };

    @Override
    public void onStart() {
        startTime = System.currentTimeMillis();
        Logger.log("Hello! Bussin Tut is starting...");
    }

    @Override
    public int onLoop() {
        int varp = PlayerSettings.getConfig(281);
        lastVarp = varp;
        Logger.log("[DEBUG] onLoop - varp: " + varp);

        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] == varp) {
                currentAction = STEP_NAMES[i];
                break;
            }
        }

        if (random.nextInt(100) < 45) {
            performAntiBan();
        } else if (random.nextInt(100) < 25) {
            mouseDrift();
        }

        switch (varp) {

            case 1: // Character Creation
                Logger.log("[DEBUG] case 1: Character Creation");
                return handleCharacterCreation();

            case 2: // Talk to Gielinor Guide
                Logger.log("[DEBUG] case 2: Talk to Gielinor Guide");
                if (NPCs.closest(3308) == null) { Logger.log("[DEBUG] case 2: NPC null"); break; }
                NPCs.closest(3308).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 3: // Open Settings tab
                Logger.log("[DEBUG] case 3: Open Settings tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 41) == null) { Logger.log("[DEBUG] case 3: widget null"); break; }
                Logger.log("[DEBUG] case 3: interact result=" + Widgets.get(164, 41).interact());
                postActionSleep();
                break;

            case 7: // Talk to Gielinor Guide again
                Logger.log("[DEBUG] case 7: Talk to Gielinor Guide again");
                if (NPCs.closest(3308) == null) { Logger.log("[DEBUG] case 7: NPC null"); break; }
                NPCs.closest(3308).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 10: // Exit nearby door
                Logger.log("[DEBUG] case 10: Exit nearby door");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door10 = GameObjects.closest(9398);
                Logger.log("[DEBUG] case 10: door=" + door10);
                if (door10 != null) {
                    door10.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 20: // Walk to Survival Instructor and talk
                Logger.log("[DEBUG] case 20: Walk to Survival Instructor");
                while (!survivalArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(survivalArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(8503) == null) { Logger.log("[DEBUG] case 20: NPC null"); break; }
                NPCs.closest(8503).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 30: // Open Inventory tab
                Logger.log("[DEBUG] case 30: Open Inventory tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 55) == null) { Logger.log("[DEBUG] case 30: widget null"); break; }
                Logger.log("[DEBUG] case 30: interact result=" + Widgets.get(164, 55).interact());
                postActionSleep();
                break;

            case 40: // Fish a shrimp
                Logger.log("[DEBUG] case 40: Fish a shrimp");
                if (NPCs.closest(3317) == null) { Logger.log("[DEBUG] case 40: fish spot null"); break; }
                NPCs.closest(3317).interact("Net");
                while (Inventory.count("Raw shrimps") == 0) {
                    gaussianSleep(3000, 500, 2000);
                    if (random.nextInt(100) < 40) mouseDrift();
                    performAntiBan();
                }
                postActionSleep();
                break;

            case 50: // Open Skills tab
                Logger.log("[DEBUG] case 50: Open Skills tab");
                if (Widgets.get(164, 53) == null) { Logger.log("[DEBUG] case 50: widget null"); break; }
                Logger.log("[DEBUG] case 50: interact result=" + Widgets.get(164, 53).interact());
                postActionSleep();
                break;

            case 60: // Talk to Survival Instructor again
                Logger.log("[DEBUG] case 60: Talk to Survival Instructor again");
                if (NPCs.closest(8503) == null) { Logger.log("[DEBUG] case 60: NPC null"); break; }
                NPCs.closest(8503).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 70: // Cut down a tree
                Logger.log("[DEBUG] case 70: Cut down a tree");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tree70 = GameObjects.closest(9730);
                Logger.log("[DEBUG] case 70: tree=" + tree70);
                if (tree70 == null) break;
                tree70.interact("Chop down");
                while (Inventory.count("Logs") == 0) {
                    gaussianSleep(3000, 500, 2000);
                    if (random.nextInt(100) < 40) mouseDrift();
                    performAntiBan();
                }
                postActionSleep();
                break;

            case 80: // Light the logs
                Logger.log("[DEBUG] case 80: Light the logs");
                Walking.walk(survivalArea.getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                if (Inventory.interact("Tinderbox", "Use")) {
                    if (Inventory.interact("Logs", "Use")) {
                        Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 5000);
                        gaussianSleep(6000, 800, 4000);
                    }
                }
                postActionSleep();
                break;

            case 90: // Cook the shrimp
                Logger.log("[DEBUG] case 90: Cook the shrimp");
                Item rawShrimps90 = Inventory.get(2514);
                GameObject fire90 = GameObjects.closest(26185);
                Logger.log("[DEBUG] case 90: shrimps=" + rawShrimps90 + ", fire=" + fire90);
                if (rawShrimps90 != null && fire90 != null) {
                    if (rawShrimps90.interact("Use")) {
                        gaussianSleep(650, 150, 350);
                        if (fire90.interact("Use")) {
                            Sleep.sleepUntil(() -> !Inventory.contains(2514), 10000);
                        }
                    }
                }
                postActionSleep();
                break;

            case 120: // Click continue and walk through next gate
                Logger.log("[DEBUG] case 120: Walk through gate");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(550, 120, 350);
                }
                Walking.walk(new Area(3090, 3094, 3093, 3091).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject gate120 = GameObjects.closest(9470, 9708);
                Logger.log("[DEBUG] case 120: gate=" + gate120);
                if (gate120 != null) {
                    gate120.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 130: // Walk closer to kitchen and go through door
                Logger.log("[DEBUG] case 130: Walk to kitchen door");
                Walking.walk(new Area(3079, 3086, 3082, 3082).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject door130 = GameObjects.closest(9709);
                Logger.log("[DEBUG] case 130: door=" + door130);
                if (door130 != null) {
                    door130.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 140: // Talk to Cooking Instructor
                Logger.log("[DEBUG] case 140: Talk to Cooking Instructor");
                if (NPCs.closest(3305) == null) { Logger.log("[DEBUG] case 140: NPC null"); break; }
                NPCs.closest(3305).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 150: // Mix flour and water to make dough
                Logger.log("[DEBUG] case 150: Make dough");
                for (int i = 0; i < 10 && Dialogues.inDialogue(); i++) {
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                Logger.log("[DEBUG] case 150: flour=" + Inventory.get(2516) + ", water=" + Inventory.get(1929));
                if (Inventory.interact("Pot of flour", "Use")) {
                    gaussianSleep(650, 150, 350);
                    if (Inventory.interact("Bucket of water", "Use")) {
                        Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 5000);
                    }
                }
                postActionSleep();
                break;

            case 160: // Cook dough on range to make bread
                Logger.log("[DEBUG] case 160: Cook bread");
                Item dough160 = Inventory.get("Bread dough");
                GameObject range160 = GameObjects.closest(9736);
                Logger.log("[DEBUG] case 160: dough=" + dough160 + ", range=" + range160);
                if (dough160 != null && range160 != null) {
                    dough160.interact("Use");
                    gaussianSleep(650, 150, 350);
                    range160.interact("Use");
                    Sleep.sleepUntil(() -> Inventory.contains("Bread"), 5000);
                }
                postActionSleep();
                break;

            case 170: // Exit the kitchen
                Logger.log("[DEBUG] case 170: Exit kitchen");
                GameObject door170 = GameObjects.closest(9710);
                Logger.log("[DEBUG] case 170: door=" + door170);
                if (door170 != null) {
                    door170.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 200: // Click run energy
                Logger.log("[DEBUG] case 200: Click run energy");
                if (Widgets.get(160, 28) == null) { Logger.log("[DEBUG] case 200: widget null"); break; }
                Logger.log("[DEBUG] case 200: interact result=" + Widgets.get(160, 28).interact());
                postActionSleep();
                break;

            case 210: // Run to Quest Guide
                Logger.log("[DEBUG] case 210: Run to Quest Guide");
                if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 10) {
                    Walking.toggleRun();
                    gaussianSleep(450, 100, 350);
                }
                Area questGuideArea = new Area(3088, 3126, 3082, 3128);
                while (!questGuideArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(questGuideArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject door210 = GameObjects.closest(9716);
                Logger.log("[DEBUG] case 210: door=" + door210);
                if (door210 != null) {
                    door210.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 220: // Talk to Quest Guide
                Logger.log("[DEBUG] case 220: Talk to Quest Guide");
                if (NPCs.closest(3312) == null) { Logger.log("[DEBUG] case 220: NPC null"); break; }
                NPCs.closest(3312).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 230: // Open Quest tab
                Logger.log("[DEBUG] case 230: Open Quest tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                Logger.log("[DEBUG] case 230: widget=" + Widgets.get(164, 54) + ", visible=" + (Widgets.get(164, 54) != null ? Widgets.get(164, 54).isVisible() : "null"));
                if (Widgets.get(164, 54) == null) { Logger.log("[DEBUG] case 230: widget null"); break; }
                Logger.log("[DEBUG] case 230: interact result=" + Widgets.get(164, 54).interact());
                postActionSleep();
                break;

            case 240: // Talk to Quest Guide again
                Logger.log("[DEBUG] case 240: Talk to Quest Guide again");
                if (NPCs.closest(3312) == null) { Logger.log("[DEBUG] case 240: NPC null"); break; }
                NPCs.closest(3312).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 250: // Go down the ladder
                Logger.log("[DEBUG] case 250: Go down ladder");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject ladder250 = GameObjects.closest(9726);
                Logger.log("[DEBUG] case 250: ladder=" + ladder250);
                if (ladder250 != null) {
                    ladder250.interact("Climb-down");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 260: // Talk to mining instructor
                Logger.log("[DEBUG] case 260: Talk to mining instructor");
                Area miningArea = new Area(3078, 9507, 3085, 9501);
                while (!miningArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(miningArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3311) == null) { Logger.log("[DEBUG] case 260: NPC null"); break; }
                NPCs.closest(3311).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 270: // Continue mining instructor dialogue
                Logger.log("[DEBUG] case 270: Continue mining instructor dialogue");
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 300: // Mine tin ore
                Logger.log("[DEBUG] case 300: Mine tin ore");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tinRock300 = GameObjects.closest(10080);
                Logger.log("[DEBUG] case 300: rock=" + tinRock300);
                if (tinRock300 == null) break;
                tinRock300.interact("Mine");
                Sleep.sleepUntil(() -> Inventory.contains("Tin ore"), 10000);
                postActionSleep();
                break;

            case 310: // Mine copper ore
                Logger.log("[DEBUG] case 310: Mine copper ore");
                GameObject copperRock310 = GameObjects.closest(10079);
                Logger.log("[DEBUG] case 310: rock=" + copperRock310);
                if (copperRock310 == null) break;
                copperRock310.interact("Mine");
                Sleep.sleepUntil(() -> Inventory.contains("Copper ore"), 10000);
                postActionSleep();
                break;

            case 320: // Use furnace to smelt bronze bar
                Logger.log("[DEBUG] case 320: Smelt bronze bar");
                GameObject furnace320 = GameObjects.closest(10082);
                Logger.log("[DEBUG] case 320: furnace=" + furnace320);
                if (furnace320 == null) break;
                furnace320.interact("Use");
                Sleep.sleepUntil(() -> Inventory.contains("Bronze bar"), 10000);
                postActionSleep();
                break;

            case 330: // Talk to mining instructor again
                Logger.log("[DEBUG] case 330: Talk to mining instructor again");
                if (NPCs.closest(3311) == null) { Logger.log("[DEBUG] case 330: NPC null"); break; }
                NPCs.closest(3311).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 340: // Click the anvil
                Logger.log("[DEBUG] case 340: Click anvil");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject anvil340 = GameObjects.closest(2097);
                Logger.log("[DEBUG] case 340: anvil=" + anvil340);
                if (anvil340 != null) {
                    if (!anvil340.isOnScreen()) {
                        Logger.log("[DEBUG] case 340: anvil not on screen, rotating camera");
                        Camera.rotateToEntity(anvil340);
                        gaussianSleep(700, 150, 350);
                    }
                    anvil340.interact("Smith");
                    Sleep.sleepUntil(() -> Widgets.get(312, 9) != null && Widgets.get(312, 9).isVisible(), 3000);
                }
                postActionSleep();
                break;

            case 350: // Smith the bronze dagger
                Logger.log("[DEBUG] case 350: Smith bronze dagger");
                if (Widgets.get(312, 9) != null) {
                    Logger.log("[DEBUG] case 350: interact result=" + Widgets.get(312, 9).interact("Smith"));
                    Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 10000);
                } else {
                    Logger.log("[DEBUG] case 350: widget null");
                }
                postActionSleep();
                break;

            case 360: // Move to next area and open gate
                Logger.log("[DEBUG] case 360: Move to gate");
                Area smithingExit = new Area(3093, 9503, 3091, 9502);
                while (!smithingExit.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(smithingExit.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate360 = GameObjects.closest(9717, 9718);
                Logger.log("[DEBUG] case 360: gate=" + gate360);
                if (gate360 != null) {
                    gate360.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 370: // Talk to combat instructor
                Logger.log("[DEBUG] case 370: Talk to combat instructor");
                if (NPCs.closest(3307) == null) {
                    Logger.log("[DEBUG] case 370: NPC null, walking closer");
                    Walking.walk(new Area(3104, 9508, 3107, 9505).getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!NPCs.closest(3307).isOnScreen()) {
                    Camera.rotateToEntity(NPCs.closest(3307));
                    gaussianSleep(700, 150, 350);
                }
                NPCs.closest(3307).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 390: // Open Equipment tab
                Logger.log("[DEBUG] case 390: Open Equipment tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 56) == null) { Logger.log("[DEBUG] case 390: widget null"); break; }
                Logger.log("[DEBUG] case 390: interact result=" + Widgets.get(164, 56).interact());
                postActionSleep();
                break;

            case 400: // Equipped - More Info button
                Logger.log("[DEBUG] case 400: More Info button");
                if (Widgets.get(387, 1) == null) { Logger.log("[DEBUG] case 400: widget null"); break; }
                Logger.log("[DEBUG] case 400: interact result=" + Widgets.get(387, 1).interact());
                postActionSleep();
                break;

            case 405: // Equip the bronze dagger
                Logger.log("[DEBUG] case 405: Equip bronze dagger");
                Inventory.interact(1205, "Equip");
                Sleep.sleepUntil(() -> Equipment.contains(1205), 5000);
                postActionSleep();
                break;

            case 410: // Close interface and talk to Combat Instructor again
                Logger.log("[DEBUG] case 410: Close interface + talk Combat Instructor");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(84, 3, 0) != null && Widgets.get(84, 3, 0).isVisible()) {
                    Widgets.get(84, 3, 11).interact();
                    gaussianSleep(550, 120, 350);
                }
                if (NPCs.closest(3307) == null) {
                    Logger.log("[DEBUG] case 410: NPC null, walking closer");
                    Walking.walk(new Area(3104, 9508, 3107, 9505).getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!NPCs.closest(3307).isOnScreen()) {
                    Logger.log("[DEBUG] case 410: NPC off screen, rotating camera");
                    Camera.rotateToEntity(NPCs.closest(3307));
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3307) == null || !NPCs.closest(3307).isOnScreen()) {
                    Logger.log("[DEBUG] case 410: NPC still off screen, walking closer");
                    Walking.walk(new Area(3104, 9508, 3107, 9505).getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                NPCs.closest(3307).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 5000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 420: // Equip bronze sword and shield
                Logger.log("[DEBUG] case 420: Equip sword and shield");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                Inventory.interact(1277, "Wield");
                gaussianSleep(650, 150, 350);
                Inventory.interact(1171, "Wield");
                Sleep.sleepUntil(() -> Equipment.contains(1277) && Equipment.contains(1171), 5000);
                postActionSleep();
                break;

            case 430: // Open Combat tab
                Logger.log("[DEBUG] case 430: Open Combat tab");
                if (Widgets.get(164, 52) == null) { Logger.log("[DEBUG] case 430: widget null"); break; }
                Logger.log("[DEBUG] case 430: interact result=" + Widgets.get(164, 52).interact());
                postActionSleep();
                break;

            case 440: // Go in rat pen and attack a rat
                Logger.log("[DEBUG] case 440: Rat pen");
                Area ratPen = new Area(3111, 9520, 3113, 9517);
                while (!ratPen.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ratPen.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate440 = GameObjects.closest(9720);
                Logger.log("[DEBUG] case 440: gate=" + gate440);
                if (gate440 != null) {
                    gate440.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3313) == null) { Logger.log("[DEBUG] case 440: rat null"); break; }
                NPCs.closest(3313).interact("Attack");
                Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                postActionSleep();
                break;

            case 460: // Attack rat inside pen
                Logger.log("[DEBUG] case 460: Attack rat inside pen");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(550, 120, 350);
                }
                if (!Players.getLocal().isInCombat()) {
                    if (NPCs.closest(3313) == null) { Logger.log("[DEBUG] case 460: rat null"); break; }
                    NPCs.closest(3313).interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                postActionSleep();
                break;

            case 470: // Pass back through gate and talk to Combat Instructor
                Logger.log("[DEBUG] case 470: Back through gate + talk");
                GameObject gate470 = GameObjects.closest(9720);
                Logger.log("[DEBUG] case 470: gate=" + gate470);
                if (gate470 != null) {
                    gate470.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3307) == null) {
                    Logger.log("[DEBUG] case 470: NPC null, walking closer");
                    Walking.walk(new Area(3104, 9508, 3107, 9505).getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!NPCs.closest(3307).isOnScreen()) {
                    Camera.rotateToEntity(NPCs.closest(3307));
                    gaussianSleep(700, 150, 350);
                }
                NPCs.closest(3307).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 480: // Equip ranged gear and attack a rat
                Logger.log("[DEBUG] case 480: Equip ranged + attack");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (!Equipment.contains(841)) {
                    Inventory.interact(841, "Wield");
                    gaussianSleep(650, 150, 350);
                }
                if (!Equipment.contains(882)) {
                    Inventory.interact(882, "Wield");
                    gaussianSleep(650, 150, 350);
                }
                Sleep.sleepUntil(() -> Equipment.contains(841) && Equipment.contains(882), 5000);
                Logger.log("[DEBUG] case 480: bow=" + Equipment.contains(841) + ", arrows=" + Equipment.contains(882));
                if (NPCs.closest(3313) == null) { Logger.log("[DEBUG] case 480: rat null"); break; }
                NPCs.closest(3313).interact("Attack");
                Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 10000);
                postActionSleep();
                break;

            case 490: // Kill rat with ranged
                Logger.log("[DEBUG] case 490: Kill rat with ranged");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(550, 120, 350);
                }
                if (!Players.getLocal().isInCombat()) {
                    if (!Equipment.contains(841)) {
                        Inventory.interact(841, "Wield");
                        gaussianSleep(650, 150, 350);
                    }
                    if (NPCs.closest(3313) == null) { Logger.log("[DEBUG] case 490: rat null"); break; }
                    NPCs.closest(3313).interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                postActionSleep();
                break;

            case 500: // Travel up ladder
                Logger.log("[DEBUG] case 500: Climb up ladder");
                Area ladderArea = new Area(3110, 9528, 3112, 9522);
                while (!ladderArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ladderArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject ladder500 = GameObjects.closest(9727);
                Logger.log("[DEBUG] case 500: ladder=" + ladder500);
                if (ladder500 != null) {
                    ladder500.interact("Climb-up");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 510: // Run inside bank area and click bank booth
                Logger.log("[DEBUG] case 510: Bank booth");
                Area bankArea = new Area(3119, 3124, 3124, 3120);
                while (!bankArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(bankArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject booth510 = GameObjects.closest(10083);
                Logger.log("[DEBUG] case 510: booth=" + booth510);
                if (booth510 != null) {
                    booth510.interact("Use");
                    gaussianSleep(3000, 600, 1500);
                }
                postActionSleep();
                break;

            case 520: // Close bank interface then click poll booth
                Logger.log("[DEBUG] case 520: Close bank + poll booth");
                if (Widgets.get(12, 2, 0) != null && Widgets.get(12, 2, 0).isVisible()) {
                    Widgets.get(12, 2, 11).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject pollBooth520 = GameObjects.closest(26815);
                Logger.log("[DEBUG] case 520: pollBooth=" + pollBooth520);
                if (pollBooth520 != null) {
                    pollBooth520.interact("Use");
                    gaussianSleep(550, 120, 350);
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                    while (Dialogues.inDialogue()) {
                        if (PlayerSettings.getConfig(281) != varp) break;
                        if (Dialogues.canContinue()) {
                            Dialogues.clickContinue();
                            gaussianSleep(550, 120, 350);
                        } else if (Dialogues.getOptions() != null) {
                            Dialogues.chooseOption(1);
                            gaussianSleep(550, 120, 350);
                        } else {
                            gaussianSleep(450, 80, 350);
                        }
                    }
                }
                postActionSleep();
                break;

            case 525: // Close poll booth then open door to next area
                Logger.log("[DEBUG] case 525: Close poll + open door");
                if (Widgets.get(928, 3, 0) != null && Widgets.get(928, 3, 0).isVisible()) {
                    Widgets.get(928, 4).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door525 = GameObjects.closest(9721);
                Logger.log("[DEBUG] case 525: door=" + door525);
                if (door525 != null) {
                    door525.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 530: // Talk to Account Guide
                Logger.log("[DEBUG] case 530: Talk to Account Guide");
                if (NPCs.closest(3310) == null) { Logger.log("[DEBUG] case 530: NPC null"); break; }
                NPCs.closest(3310).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 531: // Open Account Management interface
                Logger.log("[DEBUG] case 531: Open Account Management, inDialogue=" + Dialogues.inDialogue());
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 39) == null) { Logger.log("[DEBUG] case 531: widget null"); break; }
                Logger.log("[DEBUG] case 531: interact result=" + Widgets.get(164, 39).interact());
                postActionSleep();
                break;

            case 532: // Talk to Account Guide again
                Logger.log("[DEBUG] case 532: Talk to Account Guide again");
                if (NPCs.closest(3310) == null) { Logger.log("[DEBUG] case 532: NPC null"); break; }
                NPCs.closest(3310).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 540: // Walk through next door
                Logger.log("[DEBUG] case 540: Walk through door");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door540 = GameObjects.closest(9722);
                Logger.log("[DEBUG] case 540: door=" + door540);
                if (door540 != null) {
                    door540.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 550: // Run to chapel and talk to Brother Brace
                Logger.log("[DEBUG] case 550: Run to chapel");
                Area chapelArea = new Area(3122, 3108, 3127, 3105);
                while (!chapelArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(chapelArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3319) == null) { Logger.log("[DEBUG] case 550: NPC null"); break; }
                NPCs.closest(3319).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 560: // Open Prayer tab
                Logger.log("[DEBUG] case 560: Open Prayer tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 57) == null) { Logger.log("[DEBUG] case 560: widget null"); break; }
                Logger.log("[DEBUG] case 560: interact result=" + Widgets.get(164, 57).interact());
                postActionSleep();
                break;

            case 570: // Talk to Brother Brace again
                Logger.log("[DEBUG] case 570: Talk to Brother Brace again");
                if (NPCs.closest(3319) == null) { Logger.log("[DEBUG] case 570: NPC null"); break; }
                NPCs.closest(3319).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 610: // Leave through door
                Logger.log("[DEBUG] case 610: Leave through door");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door610 = GameObjects.closest(9723);
                Logger.log("[DEBUG] case 610: door=" + door610);
                if (door610 != null) {
                    door610.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 620: // Run to Magic Instructor and talk
                Logger.log("[DEBUG] case 620: Run to Magic Instructor");
                Area magicInstructorArea = new Area(3134, 3089, 3141, 3085);
                while (!magicInstructorArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(magicInstructorArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3309) == null) { Logger.log("[DEBUG] case 620: NPC null"); break; }
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 630: // Open Magic tab
                Logger.log("[DEBUG] case 630: Open Magic tab");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 58) == null) { Logger.log("[DEBUG] case 630: widget null"); break; }
                Logger.log("[DEBUG] case 630: interact result=" + Widgets.get(164, 58).interact());
                postActionSleep();
                break;

            case 640: // Talk to Magic Instructor again
                Logger.log("[DEBUG] case 640: Talk to Magic Instructor again");
                if (NPCs.closest(3309) == null) { Logger.log("[DEBUG] case 640: NPC null"); break; }
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 650: // Cast wind strike on a chicken
                Logger.log("[DEBUG] case 650: Wind strike on chicken");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(218, 12) != null && Widgets.get(218, 12).interact()) {
                    gaussianSleep(1000, 250, 350);
                    if (NPCs.closest(3316) != null) {
                        NPCs.closest(3316).interact("Cast");
                        Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 10000);
                    } else {
                        Logger.log("[DEBUG] case 650: chicken null");
                    }
                } else {
                    Logger.log("[DEBUG] case 650: spell widget null or interact failed");
                }
                postActionSleep();
                break;

            case 671: // Close final interface and talk to Magic Instructor one last time
                Logger.log("[DEBUG] case 671: Final magic instructor talk");
                if (Widgets.get(153, 16) != null && Widgets.get(153, 16).isVisible()) {
                    Widgets.get(153, 16).interact();
                    gaussianSleep(1000, 250, 350);
                }
                if (NPCs.closest(3309) == null) { Logger.log("[DEBUG] case 671: NPC null"); break; }
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else if (Dialogues.getOptions() != null) {
                        String[] options = Dialogues.getOptions();
                        if (options != null && options.length > 0 && options[0] != null && options[0].toLowerCase().contains("ironman")) {
                            Dialogues.chooseOption(3);
                        } else {
                            Dialogues.chooseOption(1);
                        }
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 680: // Cast Home Teleport
                Logger.log("[DEBUG] case 680: Home Teleport");
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(218, 7) != null && Widgets.get(218, 7).interact()) {
                    gaussianSleep(700, 150, 350);
                    Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == 9599, 10000);
                    gaussianSleep(12000, 1500, 10000);
                } else {
                    Logger.log("[DEBUG] case 680: spell widget null or interact failed");
                }
                postActionSleep();
                break;

            case 1000: // Final dialogue
                Logger.log("[DEBUG] case 1000: Final dialogue");
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        gaussianSleep(550, 120, 350);
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                Logger.log("Tutorial Island completed!");
                gaussianSleep(2000, 500, 1000);
                currentAction = "Complete! Logging out...";
                Logger.log("Logging out and stopping script...");
                if (Widgets.get(164, 34) != null) {
                    Widgets.get(164, 34).interact();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(182, 8) != null) {
                    Widgets.get(182, 8).interact();
                    gaussianSleep(3000, 500, 2000);
                }
                stop();
                break;

            default:
                Logger.log("Unhandled varp: " + varp);
                break;
        }

        return (int) Math.max(600 + random.nextGaussian() * 200, 350);
    }

    private void drawLogo(Graphics2D g2, int x, int y) {
        // Outer circle - dark bg with green border
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x, y, 36, 36);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(0, 200, 83));
        g2.drawOval(x, y, 36, 36);

        // Stylized "B" letter
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.setColor(new Color(0, 200, 83));
        g2.drawString("B", x + 9, y + 27);

        // Small flame accent on top-right of the B
        g2.setStroke(new BasicStroke(1.8f));
        g2.setColor(new Color(255, 140, 0));
        int fx = x + 25;
        int fy = y + 8;
        g2.drawLine(fx, fy + 6, fx - 1, fy + 2);
        g2.drawLine(fx - 1, fy + 2, fx + 1, fy);
        g2.drawLine(fx + 1, fy, fx + 3, fy + 3);
        g2.setColor(new Color(255, 200, 50));
        g2.drawLine(fx, fy + 6, fx + 1, fy + 3);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long elapsed = System.currentTimeMillis() - startTime;
        String timeStr = String.format("%02d:%02d:%02d", elapsed / 3600000, (elapsed / 60000) % 60, (elapsed / 1000) % 60);

        int currentStep = 0;
        int totalSteps = STEP_VARPS.length;
        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] <= lastVarp) currentStep = i + 1;
        }
        double progress = (double) currentStep / totalSteps;

        int x = 10;
        int y = 200;
        int w = 240;
        int h = 138;

        // Main panel background
        g2.setColor(new Color(15, 15, 15, 200));
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Border with subtle glow
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(0, 200, 83, 60));
        g2.drawRoundRect(x - 1, y - 1, w + 2, h + 2, 13, 13);
        g2.setColor(new Color(0, 200, 83, 140));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Logo
        drawLogo(g2, x + 8, y + 6);

        // Brand text next to logo
        g2.setFont(new Font("Arial", Font.BOLD, 16));
        g2.setColor(new Color(0, 200, 83));
        g2.drawString("BUSSIN", x + 50, y + 21);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(160, 160, 160));
        g2.drawString("Tut  |  by NutmegDan", x + 50, y + 34);

        // Divider line
        g2.setColor(new Color(60, 60, 60));
        g2.setStroke(new BasicStroke(1f));
        g2.drawLine(x + 10, y + 44, x + w - 10, y + 44);

        // Info rows
        g2.setFont(new Font("Arial", Font.PLAIN, 11));

        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Time", x + 12, y + 60);
        g2.setColor(Color.WHITE);
        g2.drawString(timeStr, x + 60, y + 60);

        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Action", x + 12, y + 76);
        g2.setColor(Color.WHITE);
        String displayAction = currentAction.length() > 22 ? currentAction.substring(0, 22) + ".." : currentAction;
        g2.drawString(displayAction, x + 60, y + 76);

        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Step", x + 12, y + 92);
        g2.setColor(Color.WHITE);
        g2.drawString(currentStep + " / " + totalSteps, x + 60, y + 92);

        // Percentage on the right
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(0, 200, 83));
        g2.drawString((int)(progress * 100) + "%", x + w - 38, y + 92);

        // Anti-ban row
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("AB", x + 12, y + 108);
        g2.setColor(new Color(255, 180, 50));
        String abText;
        if (lastAntiBanTime == 0) {
            abText = "None yet";
        } else {
            long ago = (System.currentTimeMillis() - lastAntiBanTime) / 1000;
            abText = lastAntiBan + " (" + ago + "s ago)";
        }
        String displayAb = abText.length() > 26 ? abText.substring(0, 26) + ".." : abText;
        g2.drawString(displayAb, x + 60, y + 108);

        // Anti-ban count on the right
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(255, 180, 50));
        g2.drawString("#" + antiBanCount, x + w - 38, y + 108);

        // Progress bar
        int barX = x + 12;
        int barY = y + 118;
        int barW = w - 24;
        int barH = 10;
        g2.setColor(new Color(40, 40, 40));
        g2.fillRoundRect(barX, barY, barW, barH, 5, 5);
        if (progress > 0) {
            g2.setColor(new Color(0, 200, 83));
            g2.fillRoundRect(barX, barY, Math.max((int)(barW * progress), 5), barH, 5, 5);
        }
        g2.setColor(new Color(0, 200, 83, 50));
        g2.drawRoundRect(barX, barY, barW, barH, 5, 5);
    }

    @Override
    public void onExit() {
        long elapsed = System.currentTimeMillis() - startTime;
        String timeStr = String.format("%02d:%02d:%02d", elapsed / 3600000, (elapsed / 60000) % 60, (elapsed / 1000) % 60);
        int currentStep = 0;
        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] <= lastVarp) currentStep = i + 1;
        }
        Logger.log("========================================");
        Logger.log("  Bussin Tut - Final Report");
        Logger.log("========================================");
        Logger.log("  Runtime:    " + timeStr);
        Logger.log("  Last Varp:  " + lastVarp);
        Logger.log("  Progress:   " + currentStep + "/" + STEP_VARPS.length + " (" + (int)((double) currentStep / STEP_VARPS.length * 100) + "%)");
        Logger.log("  Last Action: " + currentAction);
        Logger.log("  Anti-ban:   " + antiBanCount + " actions");
        if (lastVarp >= 1000) {
            Logger.log("  Status:     COMPLETED!");
        } else {
            Logger.log("  Status:     Stopped early");
        }
        Logger.log("========================================");
    }

    private void gaussianSleep(int mean, int stddev, int min) {
        int delay = (int) (mean + random.nextGaussian() * stddev);
        Sleep.sleep(Math.max(delay, min));
    }

    private void postActionSleep() {
        postClickIdle();
        gaussianSleep(1200, 350, 350);
    }

    private void performAntiBan() {
        int roll = random.nextInt(100);
        if (roll < 10) {
            logAntiBan("Camera rotate");
            Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
        } else if (roll < 17) {
            logAntiBan("Camera nudge");
            Camera.rotateTo(Camera.getYaw() + random.nextInt(60) - 30, Camera.getPitch() + random.nextInt(20) - 10);
        } else if (roll < 27) {
            logAntiBan("Mouse jiggle");
            mouseJiggle();
        } else if (roll < 32) {
            logAntiBan("Mouse off screen");
            Mouse.moveOutsideScreen();
            gaussianSleep(2500, 800, 1000);
            mouseJiggle();
        }
    }

    private void logAntiBan(String action) {
        Logger.log("[ANTI-BAN] " + action);
        lastAntiBan = action;
        lastAntiBanTime = System.currentTimeMillis();
        antiBanCount++;
    }

    private void mouseJiggle() {
        Point pos = Mouse.getPosition();
        int dx = random.nextInt(30) - 15;
        int dy = random.nextInt(30) - 15;
        int newX = Math.max(5, Math.min(760, pos.x + dx));
        int newY = Math.max(5, Math.min(500, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void mouseDrift() {
        logAntiBan("Mouse drift");
        Point pos = Mouse.getPosition();
        int dx = random.nextInt(10) - 5;
        int dy = random.nextInt(10) - 5;
        int newX = Math.max(5, Math.min(760, pos.x + dx));
        int newY = Math.max(5, Math.min(500, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void postClickIdle() {
        int roll = random.nextInt(100);
        if (roll < 30) {
            // Small drift away from click position
            Point pos = Mouse.getPosition();
            int dx = random.nextInt(80) - 40;
            int dy = random.nextInt(80) - 40;
            int newX = Math.max(5, Math.min(760, pos.x + dx));
            int newY = Math.max(5, Math.min(500, pos.y + dy));
            gaussianSleep(400, 150, 350);
            Mouse.move(new Point(newX, newY));
        } else if (roll < 45) {
            // Move toward inventory area
            int newX = 580 + random.nextInt(60);
            int newY = 230 + random.nextInt(100);
            gaussianSleep(500, 200, 350);
            Mouse.move(new Point(newX, newY));
        } else if (roll < 55) {
            // Move toward minimap area
            int newX = 620 + random.nextInt(50);
            int newY = 20 + random.nextInt(60);
            gaussianSleep(500, 200, 350);
            Mouse.move(new Point(newX, newY));
        }
        // 45% - do nothing, leave mouse where it is
    }

    private int handleCharacterCreation() {
        Logger.log("[DEBUG] handleCharacterCreation");
        // Phase 2: "How familiar are you?" screen
        if (Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible()) {
            Logger.log("Selecting experience level...");
            Widgets.get(929, 7).interact();
            gaussianSleep(750, 150, 350);
            Logger.log("Character creation completed!");
            return (int) Math.max(1200 + random.nextGaussian() * 300, 600);
        }

        // Phase 1: Wait for appearance screen to load
        if (Widgets.get(679, 74) == null || !Widgets.get(679, 74).isVisible()) {
            Logger.log("Waiting for character creation screen...");
            return (int) Math.max(700 + random.nextGaussian() * 150, 350);
        }

        Logger.log("Starting character creation...");

        int[][] designOptions = {
            {15, 16}, {19, 20}, {23, 24}, {27, 28}, {31, 32}, {35, 36}, {39, 40}
        };
        int[][] colourOptions = {
            {46, 47}, {50, 51}, {54, 55}, {58, 59}, {62, 63}
        };

        for (int[] option : designOptions) {
            int clicks = random.nextInt(4) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
        }

        for (int[] option : colourOptions) {
            int clicks = random.nextInt(3) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
        }

        Widgets.get(679, 74).interact();
        Sleep.sleepUntil(() -> Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible(), 10000);

        return (int) Math.max(700 + random.nextGaussian() * 150, 350);
    }
}
