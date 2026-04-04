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
import org.dreambot.api.input.mouse.algorithm.StandardMouseAlgorithm;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Graphics2D;
import java.awt.Color;
import java.awt.Font;
import java.awt.RenderingHints;
import java.awt.BasicStroke;
import java.util.Random;
import javax.swing.JOptionPane;

@ScriptManifest(
    name = "Nutty Tutorial Island",
    author = "Nutmeg Dan",
    description = "Automatically completes Tutorial Island with human-like mouse movement, anti-ban, and adaptive timing. Start script on character customization screen.",
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
    private long lastVarpChangeTime = 0;
    private HumanMouseAlgorithm mouseAlgo;
    private boolean varpJustChanged = false;
    private long completionTime = -1;
    private int ironmanMode = 0; // 0=normal, 1=ironman, 2=hardcore, 3=ultimate

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
        lastVarpChangeTime = System.currentTimeMillis();
        mouseAlgo = new HumanMouseAlgorithm();
        Mouse.setMouseAlgorithm(mouseAlgo);
        int choice = JOptionPane.showConfirmDialog(null, "Enable Ironman mode?", "Nutty Tutorial Island",
            JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        ironmanMode = (choice == JOptionPane.YES_OPTION) ? 1 : 0;
        Logger.log("Nutty Tutorial Island starting (mode: " + (ironmanMode == 1 ? "Ironman" : "Normal") + ")");
    }

    @Override
    public int onLoop() {
        int varp = PlayerSettings.getConfig(281);
        if (varp != lastVarp) {
            lastVarpChangeTime = System.currentTimeMillis();
            varpJustChanged = true;
        }
        lastVarp = varp;

        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] == varp) {
                currentAction = STEP_NAMES[i];
                break;
            }
        }

        // Stuck detection — if varp hasn't changed in 3 minutes, attempt recovery
        if (System.currentTimeMillis() - lastVarpChangeTime > 180000) {
            Logger.log("[WATCHDOG] Stuck for 3+ minutes at varp " + varp + ", attempting recovery...");
            currentAction = "Stuck recovery...";
            if (Dialogues.inDialogue()) {
                Dialogues.clickContinue();
                gaussianSleep(1000, 250, 350);
            }
            lastVarpChangeTime = System.currentTimeMillis();
        }

        // Periodic run energy check (run unlocked at varp 200+)
        if (varp >= 200 && !Walking.isRunEnabled() && Walking.getRunEnergy() > 20) {
            Walking.toggleRun();
            gaussianSleep(450, 100, 350);
        }

        // Variable reaction time after varp change
        if (varpJustChanged) {
            varpJustChanged = false;
            int reactionRoll = random.nextInt(100);
            if (reactionRoll < 25) {
                gaussianSleep(275, 50, 200);       // instant — was already watching
            } else if (reactionRoll < 55) {
                gaussianSleep(575, 120, 351);      // quick reaction
            } else if (reactionRoll < 85) {
                gaussianSleep(1150, 200, 801);     // normal reaction
            } else {
                gaussianSleep(2250, 400, 1501);    // slow — was looking away
            }
        }

        if (random.nextInt(100) < 45) {
            performAntiBan();
        } else if (random.nextInt(100) < 25) {
            mouseDrift();
        } else if (random.nextInt(1000) < 8) {
            shortAFK();
        }

        switch (varp) {

            case 1: // Character Creation
                return handleCharacterCreation();

            case 2: // Talk to Gielinor Guide
                preActionHesitation();
                if (NPCs.closest(3308) == null) break;
                NPCs.closest(3308).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 3: // Open Settings tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 41) == null) break;
                Widgets.get(164, 41).interact();
                postActionSleep();
                break;

            case 7: // Talk to Gielinor Guide again
                preActionHesitation();
                if (NPCs.closest(3308) == null) break;
                NPCs.closest(3308).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 10: // Exit nearby door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door10 = GameObjects.closest(9398);
                if (door10 != null) {
                    if (!door10.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door10.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 20: // Walk to Survival Instructor and talk
                preActionHesitation();
                while (!survivalArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(survivalArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(8503) == null) break;
                NPCs.closest(8503).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 30: // Open Inventory tab
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 55) == null) break;
                Widgets.get(164, 55).interact();
                postActionSleep();
                break;

            case 40: // Fish a shrimp
                preActionHesitation();
                if (NPCs.closest(3317) == null) {
                    Area fishingArea40 = new Area(3101, 3097, 3104, 3094);
                    if (!Players.getLocal().isMoving()) Walking.walk(fishingArea40.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!NPCs.closest(3317).isOnScreen()) {
                    Camera.rotateToEntity(NPCs.closest(3317));
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3317) == null) break;
                NPCs.closest(3317).interact("Net");
                long fishStart40 = System.currentTimeMillis();
                while (Inventory.count("Raw shrimps") == 0 && System.currentTimeMillis() - fishStart40 < 30000) {
                    gaussianSleep(3000, 500, 2000);
                    if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                        if (NPCs.closest(3317) != null) {
                            if (!NPCs.closest(3317).isOnScreen()) Camera.rotateToEntity(NPCs.closest(3317));
                            NPCs.closest(3317).interact("Net");
                        }
                        gaussianSleep(2000, 500, 1000);
                    }
                    if (random.nextInt(100) < 40) mouseDrift();
                    else if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 50: // Open Skills tab
                preActionHesitation();
                if (Widgets.get(164, 53) == null) break;
                Widgets.get(164, 53).interact();
                postActionSleep();
                break;

            case 60: // Talk to Survival Instructor again
                preActionHesitation();
                if (NPCs.closest(8503) == null) break;
                NPCs.closest(8503).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 70: // Cut down a tree
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tree70 = GameObjects.closest(9730);
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
                preActionHesitation();
                if (!Inventory.contains("Logs")) {
                    GameObject tree80 = GameObjects.closest(9730);
                    if (tree80 == null) break;
                    tree80.interact("Chop down");
                    Sleep.sleepUntil(() -> Inventory.contains("Logs"), 15000);
                    break;
                }
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
                preActionHesitation();
                Item rawShrimps90 = Inventory.get(2514);
                GameObject fire90 = GameObjects.closest(26185);
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
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                Walking.walk(new Area(3090, 3094, 3093, 3091).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject gate120 = GameObjects.closest(9470, 9708);
                if (gate120 != null) {
                    if (!gate120.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate120.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 130: // Walk closer to kitchen and go through door
                preActionHesitation();
                Walking.walk(new Area(3079, 3086, 3082, 3082).getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                GameObject door130 = GameObjects.closest(9709);
                if (door130 != null) {
                    if (!door130.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door130.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 140: // Talk to Cooking Instructor
                preActionHesitation();
                if (NPCs.closest(3305) == null) break;
                NPCs.closest(3305).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 150: // Mix flour and water to make dough
                preActionHesitation();
                for (int i = 0; i < 10 && Dialogues.inDialogue(); i++) {
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                if (Inventory.interact("Pot of flour", "Use")) {
                    gaussianSleep(650, 150, 350);
                    if (Inventory.interact("Bucket of water", "Use")) {
                        Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 5000);
                    }
                }
                postActionSleep();
                break;

            case 160: // Cook dough on range to make bread
                preActionHesitation();
                Item dough160 = Inventory.get("Bread dough");
                GameObject range160 = GameObjects.closest(9736);
                if (dough160 != null && range160 != null) {
                    dough160.interact("Use");
                    gaussianSleep(650, 150, 350);
                    range160.interact("Use");
                    Sleep.sleepUntil(() -> Inventory.contains("Bread"), 5000);
                }
                postActionSleep();
                break;

            case 170: // Exit the kitchen
                preActionHesitation();
                GameObject door170 = GameObjects.closest(9710);
                if (door170 != null) {
                    if (!door170.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door170.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 200: // Click run energy
                preActionHesitation();
                if (Widgets.get(160, 28) == null) break;
                Widgets.get(160, 28).interact();
                postActionSleep();
                break;

            case 210: // Run to Quest Guide
                preActionHesitation();
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
                if (door210 != null) {
                    if (!door210.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door210.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 220: // Talk to Quest Guide
                preActionHesitation();
                if (NPCs.closest(3312) == null) break;
                NPCs.closest(3312).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 230: // Open Quest tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 54) == null) break;
                Widgets.get(164, 54).interact();
                postActionSleep();
                break;

            case 240: // Talk to Quest Guide again
                preActionHesitation();
                if (NPCs.closest(3312) == null) break;
                NPCs.closest(3312).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 250: // Go down the ladder
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject ladder250 = GameObjects.closest(9726);
                if (ladder250 != null) {
                    ladder250.interact("Climb-down");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 260: // Talk to mining instructor
                preActionHesitation();
                Area miningArea = new Area(3078, 9507, 3085, 9501);
                while (!miningArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(miningArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3311) == null) break;
                NPCs.closest(3311).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 270: // Continue mining instructor dialogue
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 300: // Mine tin ore
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject tinRock300 = GameObjects.closest(10080);
                if (tinRock300 == null) break;
                tinRock300.interact("Mine");
                long mineStart300 = System.currentTimeMillis();
                while (!Inventory.contains("Tin ore") && System.currentTimeMillis() - mineStart300 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 310: // Mine copper ore
                preActionHesitation();
                GameObject copperRock310 = GameObjects.closest(10079);
                if (copperRock310 == null) break;
                copperRock310.interact("Mine");
                long mineStart310 = System.currentTimeMillis();
                while (!Inventory.contains("Copper ore") && System.currentTimeMillis() - mineStart310 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 320: // Use furnace to smelt bronze bar
                preActionHesitation();
                GameObject furnace320 = GameObjects.closest(10082);
                if (furnace320 == null) break;
                furnace320.interact("Use");
                long smeltStart320 = System.currentTimeMillis();
                while (!Inventory.contains("Bronze bar") && System.currentTimeMillis() - smeltStart320 < 15000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 30) performAntiBan();
                }
                postActionSleep();
                break;

            case 330: // Talk to mining instructor again
                preActionHesitation();
                if (NPCs.closest(3311) == null) break;
                NPCs.closest(3311).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 340: // Click the anvil
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject anvil340 = GameObjects.closest(2097);
                if (anvil340 != null) {
                    if (!anvil340.isOnScreen()) {
                        Camera.rotateToEntity(anvil340);
                        gaussianSleep(700, 150, 350);
                    }
                    anvil340.interact("Smith");
                    Sleep.sleepUntil(() -> Widgets.get(312, 9) != null && Widgets.get(312, 9).isVisible(), 3000);
                }
                postActionSleep();
                break;

            case 350: // Smith the bronze dagger
                preActionHesitation();
                if (Widgets.get(312, 9) != null) {
                    Widgets.get(312, 9).interact("Smith");
                    Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 10000);
                }
                postActionSleep();
                break;

            case 360: // Move to next area and open gate
                preActionHesitation();
                Area smithingExit = new Area(3093, 9503, 3091, 9502);
                while (!smithingExit.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(smithingExit.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate360 = GameObjects.closest(9717, 9718);
                if (gate360 != null) {
                    if (!gate360.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate360.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 370: // Talk to combat instructor
                preActionHesitation();
                if (NPCs.closest(3307) == null) {
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
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 390: // Open Equipment tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 56) == null) break;
                Widgets.get(164, 56).interact();
                postActionSleep();
                break;

            case 400: // Equipped - More Info button
                preActionHesitation();
                if (Widgets.get(387, 1) == null) break;
                Widgets.get(387, 1).interact();
                postActionSleep();
                break;

            case 405: // Equip the bronze dagger
                Inventory.interact(1205, "Equip");
                Sleep.sleepUntil(() -> Equipment.contains(1205), 5000);
                postActionSleep();
                break;

            case 410: // Close interface and talk to Combat Instructor again
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(84, 3, 0) != null && Widgets.get(84, 3, 0).isVisible()) {
                    Widgets.get(84, 3, 11).interact();
                    dialoguePause();
                }
                if (NPCs.closest(3307) == null) {
                    Walking.walk(new Area(3104, 9508, 3107, 9505).getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                    break;
                }
                if (!NPCs.closest(3307).isOnScreen()) {
                    Camera.rotateToEntity(NPCs.closest(3307));
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3307) == null || !NPCs.closest(3307).isOnScreen()) {
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
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 420: // Equip bronze sword and shield
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
                preActionHesitation();
                if (Widgets.get(164, 52) == null) break;
                Widgets.get(164, 52).interact();
                postActionSleep();
                break;

            case 440: // Go in rat pen and attack a rat
                preActionHesitation();
                Area ratPen = new Area(3111, 9520, 3113, 9517);
                while (!ratPen.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ratPen.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject gate440 = GameObjects.closest(9720);
                if (gate440 != null) {
                    gate440.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3313) == null) break;
                NPCs.closest(3313).interact("Attack");
                Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                postActionSleep();
                break;

            case 460: // Attack rat inside pen
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                if (!Players.getLocal().isInCombat()) {
                    if (NPCs.closest(3313) == null) break;
                    NPCs.closest(3313).interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                postActionSleep();
                break;

            case 470: // Pass back through gate and talk to Combat Instructor
                GameObject gate470 = GameObjects.closest(9720);
                if (gate470 != null) {
                    gate470.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 6000);
                    gaussianSleep(700, 150, 350);
                }
                if (NPCs.closest(3307) == null) {
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
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 480: // Equip ranged gear and attack a rat
                preActionHesitation();
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
                if (NPCs.closest(3313) == null) break;
                NPCs.closest(3313).interact("Attack");
                long combatStart480 = System.currentTimeMillis();
                while (!Players.getLocal().isInCombat() && System.currentTimeMillis() - combatStart480 < 12000) {
                    gaussianSleep(2000, 500, 1000);
                    if (random.nextInt(100) < 25) mouseDrift();
                }
                postActionSleep();
                break;

            case 490: // Kill rat with ranged
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                if (!Players.getLocal().isInCombat()) {
                    if (!Equipment.contains(841)) {
                        Inventory.interact(841, "Wield");
                        gaussianSleep(650, 150, 350);
                    }
                    if (NPCs.closest(3313) == null) break;
                    NPCs.closest(3313).interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 5000);
                }
                postActionSleep();
                break;

            case 500: // Travel up ladder
                preActionHesitation();
                Area ladderArea = new Area(3110, 9528, 3112, 9522);
                while (!ladderArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(ladderArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject ladder500 = GameObjects.closest(9727);
                if (ladder500 != null) {
                    ladder500.interact("Climb-up");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 510: // Run inside bank area and click bank booth
                preActionHesitation();
                Area bankArea = new Area(3119, 3124, 3124, 3120);
                while (!bankArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(bankArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                GameObject booth510 = GameObjects.closest(10083);
                if (booth510 != null) {
                    booth510.interact("Use");
                    gaussianSleep(3000, 600, 1500);
                }
                postActionSleep();
                break;

            case 520: // Close bank interface then click poll booth
                if (Widgets.get(12, 2, 0) != null && Widgets.get(12, 2, 0).isVisible()) {
                    Widgets.get(12, 2, 11).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject pollBooth520 = GameObjects.closest(26815);
                if (pollBooth520 != null) {
                    pollBooth520.interact("Use");
                    dialoguePause();
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                    while (Dialogues.inDialogue()) {
                        if (PlayerSettings.getConfig(281) != varp) break;
                        if (Dialogues.canContinue()) {
                            Dialogues.clickContinue();
                            dialoguePause();
                        } else if (Dialogues.getOptions() != null) {
                            Dialogues.chooseOption(1);
                            dialoguePause();
                        } else {
                            gaussianSleep(450, 80, 350);
                        }
                    }
                }
                postActionSleep();
                break;

            case 525: // Close poll booth then open door to next area
                preActionHesitation();
                if (Widgets.get(928, 3, 0) != null && Widgets.get(928, 3, 0).isVisible()) {
                    Widgets.get(928, 4).interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door525 = GameObjects.closest(9721);
                if (door525 != null) {
                    if (!door525.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door525.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 530: // Talk to Account Guide
                preActionHesitation();
                if (NPCs.closest(3310) == null) break;
                NPCs.closest(3310).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 531: // Open Account Management interface
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 39) == null) break;
                Widgets.get(164, 39).interact();
                postActionSleep();
                break;

            case 532: // Talk to Account Guide again
                if (NPCs.closest(3310) == null) break;
                NPCs.closest(3310).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 540: // Walk through next door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door540 = GameObjects.closest(9722);
                if (door540 != null) {
                    if (!door540.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door540.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 550: // Run to chapel and talk to Brother Brace
                preActionHesitation();
                Area chapelArea = new Area(3122, 3108, 3127, 3105);
                while (!chapelArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(chapelArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3319) == null) break;
                NPCs.closest(3319).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 560: // Open Prayer tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 57) == null) break;
                Widgets.get(164, 57).interact();
                postActionSleep();
                break;

            case 570: // Talk to Brother Brace again
                preActionHesitation();
                if (NPCs.closest(3319) == null) break;
                NPCs.closest(3319).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 610: // Leave through door
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door610 = GameObjects.closest(9723);
                if (door610 != null) {
                    if (!door610.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door610.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 3000);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                postActionSleep();
                break;

            case 620: // Run to Magic Instructor and talk
                preActionHesitation();
                Area magicInstructorArea = new Area(3134, 3089, 3141, 3085);
                while (!magicInstructorArea.contains(Players.getLocal())) {
                    if (!Players.getLocal().isMoving()) {
                        Walking.walk(magicInstructorArea.getRandomTile());
                    }
                    gaussianSleep(1200, 300, 600);
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000);
                }
                if (NPCs.closest(3309) == null) break;
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 630: // Open Magic tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(164, 58) == null) break;
                Widgets.get(164, 58).interact();
                postActionSleep();
                break;

            case 640: // Talk to Magic Instructor again
                preActionHesitation();
                if (NPCs.closest(3309) == null) break;
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        Dialogues.chooseOption(1);
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 650: // Cast wind strike on a chicken
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(218, 12) != null && Widgets.get(218, 12).interact()) {
                    gaussianSleep(1000, 250, 350);
                    if (NPCs.closest(3316) != null) {
                        NPCs.closest(3316).interact("Cast");
                        Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 10000);
                    }
                }
                postActionSleep();
                break;

            case 671: // Close final interface and talk to Magic Instructor one last time
                preActionHesitation();
                if (Widgets.get(153, 16) != null && Widgets.get(153, 16).isVisible()) {
                    Widgets.get(153, 16).interact();
                    gaussianSleep(1000, 250, 350);
                }
                if (NPCs.closest(3309) == null) break;
                NPCs.closest(3309).interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 3000);
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else if (Dialogues.getOptions() != null) {
                        String[] options = Dialogues.getOptions();
                        if (options != null && options.length > 0 && options[0] != null && options[0].toLowerCase().contains("ironman")) {
                            if (ironmanMode == 1) Dialogues.chooseOption(1);  // Ironman
                            else Dialogues.chooseOption(3);                   // Normal
                        } else {
                            Dialogues.chooseOption(1);
                        }
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                postActionSleep();
                break;

            case 680: // Cast Home Teleport
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                if (Widgets.get(218, 7) != null && Widgets.get(218, 7).interact()) {
                    gaussianSleep(700, 150, 350);
                    Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == 9599, 10000);
                    gaussianSleep(12000, 1500, 10000);
                }
                postActionSleep();
                break;

            case 1000: // Final dialogue
                while (Dialogues.inDialogue()) {
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (Dialogues.canContinue()) {
                        Dialogues.clickContinue();
                        dialoguePause();
                    } else {
                        gaussianSleep(450, 80, 350);
                    }
                }
                Logger.log("Tutorial Island completed!");
                currentAction = "Complete!";
                completionTime = System.currentTimeMillis() - startTime;
                break;

            default:
                Logger.log("Unhandled varp: " + varp);
                break;
        }

        return (int) Math.max(600 + random.nextGaussian() * 200, 350);
    }

    private void drawLogo(Graphics2D g2, int x, int y) {
        // Outer circle - dark bg with warm brown border
        g2.setColor(new Color(20, 20, 20));
        g2.fillOval(x, y, 36, 36);
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(180, 120, 50));
        g2.drawOval(x, y, 36, 36);

        // Acorn body (rounded bottom)
        g2.setColor(new Color(160, 100, 40));
        g2.fillOval(x + 9, y + 14, 18, 18);
        // Acorn highlight
        g2.setColor(new Color(200, 140, 60));
        g2.fillOval(x + 12, y + 17, 8, 10);

        // Acorn cap (top hat shape)
        g2.setColor(new Color(100, 70, 30));
        g2.fillRoundRect(x + 7, y + 11, 22, 8, 6, 6);
        // Cap texture lines
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(80, 55, 20));
        g2.drawLine(x + 12, y + 13, x + 12, y + 17);
        g2.drawLine(x + 18, y + 12, x + 18, y + 17);
        g2.drawLine(x + 24, y + 13, x + 24, y + 17);

        // Stem
        g2.setStroke(new BasicStroke(2f));
        g2.setColor(new Color(100, 70, 30));
        g2.drawLine(x + 18, y + 11, x + 18, y + 5);
        // Small leaf on stem
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(0, 180, 70));
        g2.drawLine(x + 18, y + 7, x + 22, y + 4);
        g2.drawLine(x + 18, y + 7, x + 23, y + 7);
    }

    @Override
    public void onPaint(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        long elapsed = completionTime > 0 ? completionTime : System.currentTimeMillis() - startTime;
        String timeStr = String.format("%02d:%02d:%02d", elapsed / 3600000, (elapsed / 60000) % 60, (elapsed / 1000) % 60);

        int currentStep = 0;
        int totalSteps = STEP_VARPS.length;
        for (int i = 0; i < STEP_VARPS.length; i++) {
            if (STEP_VARPS[i] <= lastVarp) currentStep = i + 1;
        }
        double progress = (double) currentStep / totalSteps;

        int x = 10;
        int y = 164;
        int w = 240;
        int h = 170;

        // Main panel background
        g2.setColor(new Color(15, 15, 15, 200));
        g2.fillRoundRect(x, y, w, h, 12, 12);

        // Border with subtle glow
        g2.setStroke(new BasicStroke(1.5f));
        g2.setColor(new Color(180, 120, 50, 60));
        g2.drawRoundRect(x - 1, y - 1, w + 2, h + 2, 13, 13);
        g2.setColor(new Color(180, 120, 50, 140));
        g2.drawRoundRect(x, y, w, h, 12, 12);

        // Logo
        drawLogo(g2, x + 8, y + 6);

        // Brand text next to logo
        g2.setFont(new Font("Arial", Font.BOLD, 18));
        g2.setColor(new Color(200, 140, 60));
        g2.drawString("NUTTY", x + 50, y + 22);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("Tutorial Island", x + 50, y + 34);
        g2.setColor(new Color(90, 90, 90));
        g2.drawString("by NutmegDan", x + 140, y + 34);

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

        // Varp row
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Varp", x + 12, y + 108);
        g2.setColor(new Color(130, 180, 255));
        g2.drawString(String.valueOf(lastVarp), x + 60, y + 108);

        // Mode on the right
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(ironmanMode == 1 ? new Color(150, 150, 150) : new Color(100, 100, 100));
        g2.drawString(ironmanMode == 1 ? "Ironman" : "Normal", x + w - 55, y + 108);
        g2.setFont(new Font("Arial", Font.PLAIN, 11));

        // Mouse profile row
        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Mouse", x + 12, y + 124);
        g2.setColor(new Color(180, 130, 255));
        g2.drawString(mouseAlgo != null ? mouseAlgo.getProfileName() : "Default", x + 60, y + 124);

        // Anti-ban row
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("AB", x + 12, y + 140);
        g2.setColor(new Color(255, 180, 50));
        String abText;
        if (lastAntiBanTime == 0) {
            abText = "None yet";
        } else {
            long ago = (System.currentTimeMillis() - lastAntiBanTime) / 1000;
            abText = lastAntiBan + " (" + ago + "s ago)";
        }
        String displayAb = abText.length() > 26 ? abText.substring(0, 26) + ".." : abText;
        g2.drawString(displayAb, x + 60, y + 140);

        // Anti-ban count on the right
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(255, 180, 50));
        g2.drawString("#" + antiBanCount, x + w - 38, y + 140);

        // Progress bar
        int barX = x + 12;
        int barY = y + 150;
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

    private void preActionHesitation() {
        int roll = random.nextInt(100);
        if (roll < 72) return;
        if (roll < 92) {
            gaussianSleep(800, 250, 400);
        } else {
            gaussianSleep(2000, 500, 1000);
        }
    }

    private void shortAFK() {
        logAntiBan("Short AFK");
        if (random.nextInt(100) < 50) {
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(500)));
            else if (side == 1) Mouse.move(new Point(765 + random.nextInt(10), random.nextInt(500)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(760), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(760), 505 + random.nextInt(10)));
            gaussianSleep(8000, 3000, 5000);
            Mouse.move(new Point(200 + random.nextInt(360), 100 + random.nextInt(250)));
        } else {
            gaussianSleep(6000, 2000, 4000);
        }
    }

    private void dialoguePause() {
        int roll = random.nextInt(100);
        if (roll < 5) {
            gaussianSleep(2500, 600, 1200);  // really reading it carefully
        } else if (roll < 15) {
            gaussianSleep(1500, 350, 800);   // reading normally
        } else if (roll < 40) {
            gaussianSleep(800, 180, 450);    // skimming
        } else {
            gaussianSleep(500, 100, 350);    // spam-clicking through
        }
    }

    private double fatigueFactor() {
        long runtime = System.currentTimeMillis() - startTime;
        double minutes = runtime / 60000.0;
        return Math.min(1.0 + (minutes * 0.015), 1.2);
    }

    private void gaussianSleep(int mean, int stddev, int min) {
        int adjustedMean = (int)(mean * fatigueFactor());
        int delay = (int) (adjustedMean + random.nextGaussian() * stddev);
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
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(500)));
            else if (side == 1) Mouse.move(new Point(765 + random.nextInt(10), random.nextInt(500)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(760), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(760), 505 + random.nextInt(10)));
            gaussianSleep(2500, 800, 1000);
            Mouse.move(new Point(200 + random.nextInt(360), 100 + random.nextInt(250)));
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
            if (random.nextInt(100) < 25) continue; // 25% chance to skip (leave default)
            int clicks = random.nextInt(5) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
            if (random.nextInt(100) < 20) gaussianSleep(600, 200, 350); // occasional pause between options
        }

        for (int[] option : colourOptions) {
            if (random.nextInt(100) < 20) continue; // 20% chance to skip
            int clicks = random.nextInt(4) + 1;
            for (int i = 0; i < clicks; i++) {
                Widgets.get(679, option[random.nextInt(2)]).interact();
                gaussianSleep(400, 80, 350);
            }
            if (random.nextInt(100) < 20) gaussianSleep(600, 200, 350);
        }

        Widgets.get(679, 74).interact();
        Sleep.sleepUntil(() -> Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible(), 10000);

        return (int) Math.max(700 + random.nextGaussian() * 150, 350);
    }

    private static class HumanMouseAlgorithm extends StandardMouseAlgorithm {
        private final Random mouseRandom = new Random();
        private long lastProfileChange = System.currentTimeMillis();
        private int currentProfile = 1; // 0=slow, 1=normal, 2=fast
        private long profileDurationMs = 30000 + new Random().nextInt(60000);
        private double speedMultiplier = 1.0;

        private void maybeUpdateProfile() {
            if (System.currentTimeMillis() - lastProfileChange > profileDurationMs) {
                int roll = mouseRandom.nextInt(100);
                if (roll < 30) {
                    currentProfile = 0;
                    speedMultiplier = 0.7 + mouseRandom.nextDouble() * 0.15;
                } else if (roll < 75) {
                    currentProfile = 1;
                    speedMultiplier = 0.95 + mouseRandom.nextDouble() * 0.15;
                } else {
                    currentProfile = 2;
                    speedMultiplier = 1.2 + mouseRandom.nextDouble() * 0.3;
                }
                speedMultiplier += mouseRandom.nextGaussian() * 0.05;
                speedMultiplier = Math.max(0.5, Math.min(1.8, speedMultiplier));
                profileDurationMs = 15000 + mouseRandom.nextInt(75000);
                lastProfileChange = System.currentTimeMillis();
            }
        }

        @Override
        public double getMaxMagnitude() {
            maybeUpdateProfile();
            return Math.max(1.0, super.getMaxMagnitude() * speedMultiplier);
        }

        @Override
        public double getMinMagnitude() {
            maybeUpdateProfile();
            return Math.max(1.0, super.getMinMagnitude() * speedMultiplier);
        }

        @Override
        public double getAccelerationRate() {
            double noise = 0.9 + mouseRandom.nextDouble() * 0.2;
            return Math.max(1.0, super.getAccelerationRate() * speedMultiplier * noise);
        }

        @Override
        public double getMaxdTheta() {
            double inverseFactor = speedMultiplier > 0.01 ? 1.0 / speedMultiplier : 1.0;
            return Math.max(1.0, super.getMaxdTheta() * inverseFactor);
        }

        public String getProfileName() {
            if (currentProfile == 0) return "Slow";
            if (currentProfile == 2) return "Fast";
            return "Normal";
        }
    }
}
