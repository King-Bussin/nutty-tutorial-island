import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.methods.settings.PlayerSettings;
import org.dreambot.api.methods.dialogues.Dialogues;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.widgets.WidgetChild;
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
import org.dreambot.api.input.Keyboard;
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
    private int ironmanMode = 0; // 0=normal, 1=ironman
    private String ironmanPin = "";
    private static final int IRONMAN_TUTOR = 7942;
    private long lastAbActionTime = 0;
    private long nextAbCooldown = 5000;
    private Point nextTargetHint = null;

    // Screen dimensions
    private static final int SCREEN_W = 765;
    private static final int SCREEN_H = 503;
    private static final int FINAL_VARP = 1000;

    // NPC IDs
    private static final int GIELINOR_GUIDE = 3308;
    private static final int SURVIVAL_EXPERT = 8503;
    private static final int MASTER_CHEF = 3305;
    private static final int QUEST_GUIDE = 3312;
    private static final int MINING_INSTRUCTOR = 3311;
    private static final int COMBAT_INSTRUCTOR = 3307;
    private static final int ACCOUNT_GUIDE = 3310;
    private static final int BROTHER_BRACE = 3319;
    private static final int MAGIC_INSTRUCTOR = 3309;
    private static final int GIANT_RAT = 3313;
    private static final int CHICKEN = 3316;
    private static final int FISHING_SPOT = 3317;

    // Areas
    private Area survivalArea = new Area(3105, 3093, 3100, 3098);
    private Area fishingArea = new Area(3101, 3097, 3104, 3094);
    private Area combatInstructorArea = new Area(3104, 9508, 3107, 9505);
    private Area questGuideArea = new Area(3088, 3126, 3082, 3128);
    private Area miningArea = new Area(3078, 9507, 3085, 9501);
    private Area smithingExitArea = new Area(3093, 9503, 3091, 9502);
    private Area ratPenArea = new Area(3111, 9520, 3113, 9517);
    private Area bankArea = new Area(3119, 3124, 3124, 3120);
    private Area chapelArea = new Area(3122, 3108, 3127, 3105);
    private Area magicInstructorArea = new Area(3134, 3089, 3141, 3085);
    private Area kitchenWalkArea = new Area(3090, 3094, 3093, 3091);
    private Area kitchenDoorArea = new Area(3079, 3086, 3082, 3082);
    private Area ladderArea = new Area(3110, 9528, 3112, 9522);
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
        if (ironmanMode == 1) {
            String pin = "";
            while (pin.length() != 4 || !pin.matches("[1-9]{4}")) {
                pin = JOptionPane.showInputDialog(null, "Enter a 4-digit bank PIN (digits 1-9):", "Bank PIN", JOptionPane.QUESTION_MESSAGE);
                if (pin == null) {
                    ironmanMode = 0;
                    Logger.log("Bank PIN entry cancelled, switching to Normal mode");
                    break;
                }
            }
            ironmanPin = pin != null ? pin : "";
        }
        Logger.log("Nutty Tutorial Island starting (mode: " + (ironmanMode == 1 ? "Ironman" : "Normal") + ")");
    }

    @Override
    public int onLoop() {
        if (Players.getLocal() == null) {
            currentAction = "Logged out...";
            return 5000;
        }
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

        // Stuck detection — if varp hasn't changed in 60 seconds, attempt recovery
        if (System.currentTimeMillis() - lastVarpChangeTime > 60000) {
            Logger.log("[WATCHDOG] Stuck for 60s at varp " + varp + ", attempting recovery...");
            currentAction = "Stuck recovery...";
            if (Dialogues.inDialogue()) {
                Dialogues.clickContinue();
                gaussianSleep(1000, 250, 350);
            } else if (!Players.getLocal().isMoving() && !Players.getLocal().isAnimating()) {
                Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                gaussianSleep(500, 100, 350);
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

        // Anti-ban with short cooldown (2-6s) — skips during dialogue
        if (System.currentTimeMillis() - lastAbActionTime > nextAbCooldown && !Dialogues.inDialogue()) {
            int abRoll = random.nextInt(100);
            if (abRoll < 25) {
                performAntiBan();
            } else if (abRoll < 45) {
                mouseDrift();
            } else if (abRoll < 48) {
                shortAFK();
            } else if (abRoll < 57) {
                tabFidget(varp);
            } else if (abRoll < 66) {
                inventoryHover();
            } else if (abRoll < 73) {
                examineNearby();
            } else if (abRoll < 80) {
                minimapHover();
            } else if (abRoll < 88) {
                microBreak();
            }
            // 12% chance of nothing — still resets cooldown
            lastAbActionTime = System.currentTimeMillis();
            double expRaw = Math.exp(random.nextGaussian() * 0.8) * 3000;
            nextAbCooldown = (long) Math.max(1000, Math.min(15000, expRaw));
        }

        switch (varp) {

            case 1: // Character Creation
                return handleCharacterCreation();

            case 2: // Talk to Gielinor Guide
                preActionHesitation();
                NPC guide2 = NPCs.closest(GIELINOR_GUIDE);
                if (guide2 == null) break;
                if (!guide2.isOnScreen()) rotateCameraTo(guide2);
                talkToNpc(guide2);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 3: // Open Settings tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild settings3 = Widgets.get(164, 41);
                if (settings3 == null) break;
                settings3.interact();
                nextTargetHint = new Point(400, 300); // hint toward center where NPC will be
                postActionSleep();
                break;

            case 7: // Talk to Gielinor Guide again
                preActionHesitation();
                NPC guide7 = NPCs.closest(GIELINOR_GUIDE);
                if (guide7 == null) break;
                if (!guide7.isOnScreen()) rotateCameraTo(guide7);
                guide7.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 20: // Walk to Survival Instructor and talk
                preActionHesitation();
                walkToArea(survivalArea);
                NPC survival20 = NPCs.closest(SURVIVAL_EXPERT);
                if (survival20 == null) break;
                if (!survival20.isOnScreen()) rotateCameraTo(survival20);
                talkToNpc(survival20);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 30: // Open Inventory tab
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild inv30 = Widgets.get(164, 55);
                if (inv30 == null) break;
                inv30.interact();
                nextTargetHint = new Point(620, 300); // hint toward inventory area
                postActionSleep();
                break;

            case 40: // Fish a shrimp
                preActionHesitation();
                NPC fishSpot40 = NPCs.closest(FISHING_SPOT);
                if (fishSpot40 == null) {
                    if (!Players.getLocal().isMoving()) Walking.walk(fishingArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                    break;
                }
                if (!fishSpot40.isOnScreen()) rotateCameraTo(fishSpot40);
                preClickHover(fishSpot40);
                fishSpot40.interact("Net");
                long fishStart40 = System.currentTimeMillis();
                while (Inventory.count("Raw shrimps") == 0 && PlayerSettings.getConfig(281) == varp && System.currentTimeMillis() - fishStart40 < 30000) {
                    gaussianSleep(1500, 400, 800);
                    if (PlayerSettings.getConfig(281) != varp) break;
                    if (!Players.getLocal().isAnimating() && !Players.getLocal().isMoving()) {
                        NPC respot40 = NPCs.closest(FISHING_SPOT);
                        if (respot40 != null) {
                            if (!respot40.isOnScreen()) Camera.rotateToEntity(respot40);
                            respot40.interact("Net");
                        }
                        gaussianSleep(2000, 500, 1000);
                    }
                    int fishRoll = random.nextInt(100);
                    if (fishRoll < 25) mouseDrift();
                    else if (fishRoll < 45) performAntiBan();
                    else if (fishRoll < 55) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                }
                postActionSleep();
                if (random.nextInt(100) < 25) checkSkillsTab();
                break;

            case 50: // Open Skills tab
                preActionHesitation();
                WidgetChild skills50 = Widgets.get(164, 53);
                if (skills50 == null) break;
                skills50.interact();
                nextTargetHint = new Point(620, 300); // hint toward skills panel
                postActionSleep();
                break;

            case 60: // Talk to Survival Instructor again
                preActionHesitation();
                NPC survival60 = NPCs.closest(SURVIVAL_EXPERT);
                if (survival60 == null) break;
                if (!survival60.isOnScreen()) rotateCameraTo(survival60);
                preClickHover(survival60);
                survival60.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                preClickHover(tree70);
                tree70.interact("Chop down");
                long chopStart70 = System.currentTimeMillis();
                while (Inventory.count("Logs") == 0 && System.currentTimeMillis() - chopStart70 < 15000) {
                    gaussianSleep(3000, 500, 2000);
                    int chopRoll = random.nextInt(100);
                    if (chopRoll < 30) mouseDrift();
                    else if (chopRoll < 55) performAntiBan();
                    else if (chopRoll < 65) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                }
                postActionSleep();
                break;

            case 80: // Light the logs
                preActionHesitation();
                if (!Inventory.contains("Logs")) {
                    GameObject tree80 = GameObjects.closest(9730);
                    if (tree80 == null) break;
                    tree80.interact("Chop down");
                    Sleep.sleepUntil(() -> Inventory.contains("Logs"), 12000 + random.nextInt(6000));
                    break;
                }
                Walking.walk(survivalArea.getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                if (Inventory.interact("Tinderbox", "Use")) {
                    if (Inventory.interact("Logs", "Use")) {
                        Sleep.sleepUntil(() -> Players.getLocal().isAnimating(), 4000 + random.nextInt(2000));
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
                            Sleep.sleepUntil(() -> !Inventory.contains(2514), 8000 + random.nextInt(4000));
                        }
                    }
                }
                postActionSleep();
                if (random.nextInt(100) < 25) checkSkillsTab();
                break;

            case 120: // Click continue and walk through next gate
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                Walking.walk(kitchenWalkArea.getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                GameObject gate120 = GameObjects.closest(9470, 9708);
                if (gate120 != null) {
                    if (!gate120.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate120.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 130: // Walk closer to kitchen and go through door
                preActionHesitation();
                Walking.walk(kitchenDoorArea.getRandomTile());
                Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                GameObject door130 = GameObjects.closest(9709);
                if (door130 != null) {
                    if (!door130.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door130.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 140: // Talk to Cooking Instructor
                preActionHesitation();
                NPC chef140 = NPCs.closest(MASTER_CHEF);
                if (chef140 == null) break;
                if (!chef140.isOnScreen()) rotateCameraTo(chef140);
                preClickHover(chef140);
                maybeMisclick();
                chef140.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                        Sleep.sleepUntil(() -> Inventory.contains("Bread dough"), 4000 + random.nextInt(2000));
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
                    Sleep.sleepUntil(() -> Inventory.contains("Bread"), 4000 + random.nextInt(2000));
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
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 200: // Click run energy
                preActionHesitation();
                WidgetChild run200 = Widgets.get(160, 28);
                if (run200 == null) break;
                run200.interact();
                postActionSleep();
                break;

            case 210: // Run to Quest Guide
                preActionHesitation();
                if (!Walking.isRunEnabled() && Walking.getRunEnergy() > 10) {
                    Walking.toggleRun();
                    gaussianSleep(450, 100, 350);
                }
                walkToArea(questGuideArea);
                GameObject door210 = GameObjects.closest(9716);
                if (door210 != null) {
                    if (!door210.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door210.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 220: // Talk to Quest Guide
                preActionHesitation();
                NPC quest220 = NPCs.closest(QUEST_GUIDE);
                if (quest220 == null) break;
                if (!quest220.isOnScreen()) rotateCameraTo(quest220);
                talkToNpc(quest220);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 230: // Open Quest tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild quest230 = Widgets.get(164, 54);
                if (quest230 == null) break;
                quest230.interact();
                postActionSleep();
                break;

            case 240: // Talk to Quest Guide again
                preActionHesitation();
                NPC quest240 = NPCs.closest(QUEST_GUIDE);
                if (quest240 == null) break;
                if (!quest240.isOnScreen()) rotateCameraTo(quest240);
                quest240.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 260: // Talk to mining instructor
                preActionHesitation();
                walkToArea(miningArea);
                NPC miner260 = NPCs.closest(MINING_INSTRUCTOR);
                if (miner260 == null) break;
                if (!miner260.isOnScreen()) rotateCameraTo(miner260);
                talkToNpc(miner260);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 270: // Continue mining instructor dialogue
                handleDialogue(varp);
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
                    int tinRoll = random.nextInt(100);
                    if (tinRoll < 25) performAntiBan();
                    else if (tinRoll < 35) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                    else if (tinRoll < 45) mouseDrift();
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
                    int copperRoll = random.nextInt(100);
                    if (copperRoll < 25) performAntiBan();
                    else if (copperRoll < 35) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                    else if (copperRoll < 45) mouseDrift();
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
                    int smeltRoll = random.nextInt(100);
                    if (smeltRoll < 25) performAntiBan();
                    else if (smeltRoll < 35) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                    else if (smeltRoll < 45) mouseDrift();
                }
                postActionSleep();
                break;

            case 330: // Talk to mining instructor again
                preActionHesitation();
                NPC miner330 = NPCs.closest(MINING_INSTRUCTOR);
                if (miner330 == null) break;
                if (!miner330.isOnScreen()) rotateCameraTo(miner330);
                miner330.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    if (!anvil340.isOnScreen()) rotateCameraTo(anvil340);
                    anvil340.interact("Smith");
                    Sleep.sleepUntil(() -> Widgets.get(312, 9) != null && Widgets.get(312, 9).isVisible(), 2500 + random.nextInt(1500));
                }
                postActionSleep();
                break;

            case 350: // Smith the bronze dagger
                preActionHesitation();
                WidgetChild smith350 = Widgets.get(312, 9);
                if (smith350 != null) {
                    smith350.interact("Smith");
                    Sleep.sleepUntil(() -> Inventory.contains("Bronze dagger"), 8000 + random.nextInt(4000));
                }
                postActionSleep();
                if (random.nextInt(100) < 25) checkSkillsTab();
                break;

            case 360: // Move to next area and open gate
                preActionHesitation();
                walkToArea(smithingExitArea);
                GameObject gate360 = GameObjects.closest(9717, 9718);
                if (gate360 != null) {
                    if (!gate360.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate360.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 370: // Talk to combat instructor
                preActionHesitation();
                NPC combat370 = NPCs.closest(COMBAT_INSTRUCTOR);
                if (combat370 == null) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                    break;
                }
                if (!combat370.isOnScreen()) rotateCameraTo(combat370);
                talkToNpc(combat370);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 390: // Open Equipment tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild equip390 = Widgets.get(164, 56);
                if (equip390 == null) break;
                equip390.interact();
                postActionSleep();
                break;

            case 400: // Equipped - More Info button
                preActionHesitation();
                WidgetChild info400 = Widgets.get(387, 1);
                if (info400 == null) break;
                info400.interact();
                postActionSleep();
                break;

            case 405: // Equip the bronze dagger
                preActionHesitation();
                Inventory.interact(1205, "Equip");
                Sleep.sleepUntil(() -> Equipment.contains(1205), 4000 + random.nextInt(2000));
                postActionSleep();
                break;

            case 410: // Close interface and talk to Combat Instructor again
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild equipScreen410 = Widgets.get(84, 3, 0);
                if (equipScreen410 != null && equipScreen410.isVisible()) {
                    WidgetChild closeBtn410 = Widgets.get(84, 3, 11);
                    if (closeBtn410 != null) closeBtn410.interact();
                    dialoguePause();
                }
                NPC combat410 = NPCs.closest(COMBAT_INSTRUCTOR);
                if (combat410 == null) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                    break;
                }
                if (!combat410.isOnScreen()) rotateCameraTo(combat410);
                if (!combat410.isOnScreen()) {
                    Walking.walk(combatInstructorArea.getRandomTile());
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                    break;
                }
                preClickHover(combat410);
                maybeMisclick();
                combat410.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 4000 + random.nextInt(2000));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 420: // Equip bronze sword and shield
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                Inventory.interact(1277, "Wield");
                gaussianSleep(800 + random.nextInt(600), 200, 400);
                Inventory.interact(1171, "Wield");
                Sleep.sleepUntil(() -> Equipment.contains(1277) && Equipment.contains(1171), 4000 + random.nextInt(2000));
                postActionSleep();
                break;

            case 430: // Open Combat tab
                preActionHesitation();
                WidgetChild combat430 = Widgets.get(164, 52);
                if (combat430 == null) break;
                combat430.interact();
                postActionSleep();
                break;

            case 440: // Go in rat pen and attack a rat
                preActionHesitation();
                walkToArea(ratPenArea);
                GameObject gate440 = GameObjects.closest(9720);
                if (gate440 != null) {
                    if (!gate440.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        gate440.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000 + random.nextInt(2000));
                    gaussianSleep(700, 150, 350);
                }
                NPC rat440 = NPCs.closest(GIANT_RAT);
                if (rat440 == null) break;
                if (random.nextInt(100) < 60) gaussianSleep(400, 120, 200);
                rat440.interact("Attack");
                Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 4000 + random.nextInt(2000));
                postActionSleep();
                break;

            case 460: // Attack rat inside pen
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                }
                if (!Players.getLocal().isInCombat()) {
                    NPC rat460 = NPCs.closest(GIANT_RAT);
                    if (rat460 == null) break;
                    if (random.nextInt(100) < 60) gaussianSleep(400, 120, 200);
                    rat460.interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 4000 + random.nextInt(2000));
                }
                if (Players.getLocal().isInCombat()) {
                    long combatWait460 = System.currentTimeMillis();
                    while (Players.getLocal().isInCombat() && PlayerSettings.getConfig(281) == varp && System.currentTimeMillis() - combatWait460 < 15000) {
                        gaussianSleep(2000, 500, 1000);
                        int combatRoll460 = random.nextInt(100);
                        if (combatRoll460 < 20) mouseDrift();
                        else if (combatRoll460 < 35) performAntiBan();
                        else if (combatRoll460 < 42) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                    }
                }
                postActionSleep();
                if (random.nextInt(100) < 25) checkSkillsTab();
                break;

            case 470: // Pass back through gate and talk to Combat Instructor
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(700, 150, 350);
                }
                // Go through the gate if it's nearby
                GameObject gate470 = GameObjects.closest(9720);
                if (gate470 != null && gate470.distance() < 5) {
                    gate470.interact("Open");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 5000 + random.nextInt(2000));
                    gaussianSleep(700, 150, 350);
                }
                // Walk to combat instructor area regardless of current position
                walkToArea(combatInstructorArea);
                NPC combat470 = NPCs.closest(COMBAT_INSTRUCTOR);
                if (combat470 == null) break;
                if (!combat470.isOnScreen()) rotateCameraTo(combat470);
                preClickHover(combat470);
                combat470.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    Sleep.sleepUntil(() -> Equipment.contains(841), 2500 + random.nextInt(1500));
                    gaussianSleep(550 + random.nextInt(500), 150, 350);
                }
                if (!Equipment.contains(882)) {
                    Inventory.interact(882, "Wield");
                    Sleep.sleepUntil(() -> Equipment.contains(882), 2500 + random.nextInt(1500));
                    gaussianSleep(550 + random.nextInt(500), 150, 350);
                }
                NPC rat480 = NPCs.closest(GIANT_RAT);
                if (rat480 == null) break;
                if (random.nextInt(100) < 60) gaussianSleep(400, 120, 200);
                rat480.interact("Attack");
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
                        Sleep.sleepUntil(() -> Equipment.contains(841), 2500 + random.nextInt(1500));
                        gaussianSleep(550 + random.nextInt(500), 150, 350);
                    }
                    if (!Equipment.contains(882)) {
                        Inventory.interact(882, "Wield");
                        Sleep.sleepUntil(() -> Equipment.contains(882), 2500 + random.nextInt(1500));
                        gaussianSleep(550 + random.nextInt(500), 150, 350);
                    }
                    NPC rat490 = NPCs.closest(GIANT_RAT);
                    if (rat490 == null) break;
                    if (random.nextInt(100) < 60) gaussianSleep(400, 120, 200);
                    rat490.interact("Attack");
                    Sleep.sleepUntil(() -> Players.getLocal().isInCombat(), 4000 + random.nextInt(2000));
                }
                if (Players.getLocal().isInCombat()) {
                    long combatWait490 = System.currentTimeMillis();
                    while (Players.getLocal().isInCombat() && PlayerSettings.getConfig(281) == varp && System.currentTimeMillis() - combatWait490 < 15000) {
                        gaussianSleep(2000, 500, 1000);
                        int combatRoll490 = random.nextInt(100);
                        if (combatRoll490 < 20) mouseDrift();
                        else if (combatRoll490 < 35) performAntiBan();
                        else if (combatRoll490 < 42) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
                    }
                }
                postActionSleep();
                break;

            case 500: // Travel up ladder
                preActionHesitation();
                walkToArea(ladderArea);
                GameObject ladder500 = GameObjects.closest(9727);
                if (ladder500 != null) {
                    ladder500.interact("Climb-up");
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 510: // Run inside bank area and click bank booth
                preActionHesitation();
                walkToArea(bankArea);
                GameObject booth510 = GameObjects.closest(10083);
                if (booth510 != null) {
                    booth510.interact("Use");
                    gaussianSleep(3000, 600, 1500);
                }
                postActionSleep();
                break;

            case 520: // Close bank interface then click poll booth
                WidgetChild bankScreen520 = Widgets.get(12, 2, 0);
                if (bankScreen520 != null && bankScreen520.isVisible()) {
                    WidgetChild closeBank520 = Widgets.get(12, 2, 11);
                    if (closeBank520 != null) closeBank520.interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject pollBooth520 = GameObjects.closest(26815);
                if (pollBooth520 != null) {
                    pollBooth520.interact("Use");
                    dialoguePause();
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                    handleDialogue(varp);
                }
                postActionSleep();
                break;

            case 525: // Close poll booth then open door to next area
                preActionHesitation();
                WidgetChild pollScreen525 = Widgets.get(928, 3, 0);
                if (pollScreen525 != null && pollScreen525.isVisible()) {
                    WidgetChild closePoll525 = Widgets.get(928, 4);
                    if (closePoll525 != null) closePoll525.interact();
                    gaussianSleep(1000, 250, 350);
                }
                GameObject door525 = GameObjects.closest(9721);
                if (door525 != null) {
                    if (!door525.interact("Open")) {
                        gaussianSleep(800, 200, 350);
                        door525.interact("Open");
                    }
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 530: // Talk to Account Guide
                preActionHesitation();
                NPC account530 = NPCs.closest(ACCOUNT_GUIDE);
                if (account530 == null) break;
                if (!account530.isOnScreen()) rotateCameraTo(account530);
                talkToNpc(account530);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 531: // Open Account Management interface
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild acctTab531 = Widgets.get(164, 39);
                if (acctTab531 == null) break;
                acctTab531.interact();
                postActionSleep();
                break;

            case 532: // Talk to Account Guide again
                NPC account532 = NPCs.closest(ACCOUNT_GUIDE);
                if (account532 == null) break;
                if (!account532.isOnScreen()) rotateCameraTo(account532);
                account532.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 550: // Run to chapel and talk to Brother Brace
                preActionHesitation();
                walkToArea(chapelArea);
                NPC brace550 = NPCs.closest(BROTHER_BRACE);
                if (brace550 == null) break;
                if (!brace550.isOnScreen()) rotateCameraTo(brace550);
                talkToNpc(brace550);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 560: // Open Prayer tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild prayer560 = Widgets.get(164, 57);
                if (prayer560 == null) break;
                prayer560.interact();
                postActionSleep();
                break;

            case 570: // Talk to Brother Brace again
                preActionHesitation();
                NPC brace570 = NPCs.closest(BROTHER_BRACE);
                if (brace570 == null) break;
                if (!brace570.isOnScreen()) rotateCameraTo(brace570);
                brace570.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
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
                    Sleep.sleepUntil(() -> Players.getLocal().isMoving(), 2500 + random.nextInt(1500));
                    Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
                }
                postActionSleep();
                break;

            case 620: // Run to Magic Instructor and talk
                preActionHesitation();
                walkToArea(magicInstructorArea);
                NPC magic620 = NPCs.closest(MAGIC_INSTRUCTOR);
                if (magic620 == null) break;
                if (!magic620.isOnScreen()) rotateCameraTo(magic620);
                talkToNpc(magic620);
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 630: // Open Magic tab
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild magicTab630 = Widgets.get(164, 58);
                if (magicTab630 == null) break;
                magicTab630.interact();
                postActionSleep();
                break;

            case 640: // Talk to Magic Instructor again
                preActionHesitation();
                NPC magic640 = NPCs.closest(MAGIC_INSTRUCTOR);
                if (magic640 == null) break;
                if (!magic640.isOnScreen()) rotateCameraTo(magic640);
                preClickHover(magic640);
                magic640.interact("Talk-to");
                Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                handleDialogue(varp);
                postActionSleep();
                break;

            case 650: // Cast wind strike on a chicken
                preActionHesitation();
                if (Dialogues.inDialogue()) {
                    Dialogues.clickContinue();
                    gaussianSleep(1000, 250, 350);
                }
                WidgetChild windStrike650 = Widgets.get(218, 12);
                if (windStrike650 != null && windStrike650.interact()) {
                    gaussianSleep(1000, 250, 350);
                    NPC chicken650 = NPCs.closest(CHICKEN);
                    if (chicken650 != null) {
                        if (random.nextInt(100) < 60) gaussianSleep(400, 120, 200);
                        chicken650.interact("Cast");
                        long castStart650 = System.currentTimeMillis();
                        while (!Players.getLocal().isInCombat() && System.currentTimeMillis() - castStart650 < 12000) {
                            gaussianSleep(2000, 500, 1000);
                            if (!Players.getLocal().isInCombat()) {
                                NPC rechicken650 = NPCs.closest(CHICKEN);
                                if (rechicken650 != null) {
                                    WidgetChild recast650 = Widgets.get(218, 12);
                                    if (recast650 != null) recast650.interact();
                                    gaussianSleep(1000, 250, 350);
                                    rechicken650.interact("Cast");
                                }
                            }
                        }
                    }
                }
                postActionSleep();
                if (random.nextInt(100) < 25) checkSkillsTab();
                break;

            case 671: // Close final interface and talk to Magic Instructor one last time
                preActionHesitation();
                WidgetChild ironmanScreen671 = Widgets.get(153, 16);
                if (ironmanScreen671 != null && ironmanScreen671.isVisible()) {
                    ironmanScreen671.interact();
                    gaussianSleep(1000, 250, 350);
                }
                if (ironmanMode == 1) {
                    // Ironman path: magic instructor → choose ironman → tutor → PIN → magic instructor again
                    handleIronmanSetup(varp);
                } else {
                    // Normal path: magic instructor → choose normal → varp advances to 680
                    NPC magic671 = NPCs.closest(MAGIC_INSTRUCTOR);
                    if (magic671 == null) {
                        walkToArea(new Area(3140, 3089, 3142, 3085));
                        gaussianSleep(1000, 250, 350);
                        magic671 = NPCs.closest(MAGIC_INSTRUCTOR);
                        if (magic671 == null) break;
                    }
                    if (!magic671.isOnScreen()) rotateCameraTo(magic671);
                    magic671.interact("Talk-to");
                    Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
                    while (Dialogues.inDialogue()) {
                        if (PlayerSettings.getConfig(281) != varp) break;
                        if (Dialogues.canContinue()) {
                            Dialogues.clickContinue();
                            dialoguePause();
                        } else if (Dialogues.getOptions() != null) {
                            String[] options = Dialogues.getOptions();
                            if (options != null && options.length > 0 && options[0] != null && options[0].toLowerCase().contains("ironman")) {
                                Dialogues.chooseOption(3); // Normal
                            } else {
                                Dialogues.chooseOption(1);
                            }
                            dialoguePause();
                        } else {
                            gaussianSleep(450, 80, 350);
                        }
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
                WidgetChild homeTele680 = Widgets.get(218, 7);
                if (homeTele680 != null && homeTele680.interact()) {
                    gaussianSleep(700, 150, 350);
                    Sleep.sleepUntil(() -> Players.getLocal().getAnimation() == 9599, 8000 + random.nextInt(4000));
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

        int baseDelay = 600 + (int)(random.nextGaussian() * 250);
        if (random.nextInt(100) < 8) baseDelay += 800 + random.nextInt(1200);
        return Math.max(baseDelay, 300);
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
        int pct = (int)(progress * 100);

        int x = 6;
        int y = 152;
        int w = 258;
        int h = 186;
        int pad = 12;

        // ── PANEL ──
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillRoundRect(x + 3, y + 3, w, h, 14, 14);
        g2.setColor(new Color(10, 10, 10, 225));
        g2.fillRoundRect(x, y, w, h, 14, 14);
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(180, 120, 50, 100));
        g2.drawRoundRect(x, y, w, h, 14, 14);

        // ── HEADER ──
        drawLogo(g2, x + pad - 2, y + 7);
        g2.setFont(new Font("Arial", Font.BOLD, 17));
        g2.setColor(new Color(215, 155, 65));
        g2.drawString("NUTTY", x + 48, y + 23);
        g2.setFont(new Font("Arial", Font.PLAIN, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("Tutorial Island", x + 48, y + 35);
        g2.setColor(new Color(75, 75, 75));
        g2.drawString("by NutmegDan", x + 138, y + 35);

        // ── PROGRESS BAR (full-width accent) ──
        int barX = x;
        int barY = y + 44;
        int barW = w;
        int barH = 6;
        g2.setColor(new Color(30, 30, 30));
        g2.fillRect(barX, barY, barW, barH);
        if (progress > 0) {
            int fillW = Math.max((int)(barW * progress), 3);
            g2.setColor(new Color(0, 190, 80));
            g2.fillRect(barX, barY, fillW, barH);
            g2.setColor(new Color(0, 220, 100, 60));
            g2.fillRect(barX + fillW - 12, barY, 12, barH);
        }

        // ── PROGRESS TEXT ──
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.setColor(new Color(0, 210, 90));
        String pctStr = pct + "%";
        int pctW = g2.getFontMetrics().stringWidth(pctStr);
        g2.drawString(pctStr, x + pad, y + 78);

        g2.setFont(new Font("Arial", Font.PLAIN, 11));
        g2.setColor(new Color(120, 120, 120));
        g2.drawString("Step " + currentStep + " / " + totalSteps, x + pad + pctW + 8, y + 78);

        // Time + ETA on the right
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(220, 220, 220));
        String etaStr = "";
        if (currentStep > 0 && currentStep < totalSteps && completionTime < 0) {
            long avgPerStep = elapsed / currentStep;
            long remaining = avgPerStep * (totalSteps - currentStep);
            etaStr = String.format(" ~%02d:%02d", (remaining / 60000) % 60, (remaining / 1000) % 60);
        }
        int timeW = g2.getFontMetrics().stringWidth(timeStr);
        g2.drawString(timeStr, x + w - pad - timeW, y + 70);
        if (!etaStr.isEmpty()) {
            g2.setFont(new Font("Arial", Font.PLAIN, 10));
            g2.setColor(new Color(140, 140, 140));
            int etaW = g2.getFontMetrics().stringWidth(etaStr.trim());
            g2.drawString(etaStr.trim(), x + w - pad - etaW, y + 82);
        }

        // ── CURRENT ACTION (highlighted strip) ──
        g2.setColor(new Color(22, 22, 22));
        g2.fillRoundRect(x + pad - 2, y + 86, w - pad * 2 + 4, 20, 6, 6);
        g2.setColor(new Color(180, 120, 50, 40));
        g2.drawRoundRect(x + pad - 2, y + 86, w - pad * 2 + 4, 20, 6, 6);
        g2.setFont(new Font("Arial", Font.BOLD, 11));
        g2.setColor(new Color(255, 255, 255));
        String displayAction = currentAction.length() > 30 ? currentAction.substring(0, 30) + ".." : currentAction;
        g2.drawString(displayAction, x + pad + 6, y + 100);

        // ── STATS TABLE ──
        int tableX = x + 6;
        int tableW = w - 12;
        int rowH = 18;
        int tableY = y + 112;
        int valX = x + 72;
        int col2LabelX = x + w / 2 + 6;
        int col2ValX = x + w / 2 + 42;

        // Row 1: Account + Anti-ban (darker bg)
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(tableX, tableY, tableW, rowH, 4, 4);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("ACCOUNT", tableX + 8, tableY + 13);
        String modeStr = ironmanMode == 1 ? "Ironman" : "Normal";
        g2.setColor(ironmanMode == 1 ? new Color(220, 180, 120) : new Color(180, 180, 180));
        g2.drawString(modeStr, valX, tableY + 13);

        g2.setColor(new Color(140, 140, 140));
        g2.drawString("AB", col2LabelX, tableY + 13);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(255, 180, 50));
        g2.drawString(String.valueOf(antiBanCount), col2ValX, tableY + 13);

        // Row 2: Mouse + Varp (no bg, alternating)
        int row2Y = tableY + rowH + 2;

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("MOUSE", tableX + 8, row2Y + 13);
        String profileName = mouseAlgo != null ? mouseAlgo.getProfileName() : "Default";
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        if ("Slow".equals(profileName)) g2.setColor(new Color(100, 180, 255));
        else if ("Fast".equals(profileName)) g2.setColor(new Color(255, 140, 80));
        else g2.setColor(new Color(200, 200, 200));
        g2.drawString(profileName, valX, row2Y + 13);

        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(140, 140, 140));
        g2.drawString("VARP", col2LabelX, row2Y + 13);
        g2.setFont(new Font("Arial", Font.BOLD, 10));
        g2.setColor(new Color(100, 150, 220));
        g2.drawString(String.valueOf(lastVarp), col2ValX, row2Y + 13);

        // Row 3: Last AB action (full width, subtle)
        int row3Y = row2Y + rowH + 2;
        g2.setColor(new Color(20, 20, 20, 200));
        g2.fillRoundRect(tableX, row3Y, tableW, rowH, 4, 4);

        if (lastAntiBanTime > 0) {
            long ago = (System.currentTimeMillis() - lastAntiBanTime) / 1000;
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.setColor(new Color(130, 130, 130));
            g2.drawString("LAST AB", tableX + 8, row3Y + 12);
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.setColor(new Color(100, 100, 100));
            String abDisplay = lastAntiBan + " (" + ago + "s ago)";
            if (abDisplay.length() > 26) abDisplay = abDisplay.substring(0, 26) + "..";
            g2.drawString(abDisplay, tableX + 56, row3Y + 12);
        } else {
            g2.setColor(new Color(55, 55, 55));
            g2.drawString("No anti-ban actions yet", tableX + 8, row3Y + 12);
        }
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
        Logger.log("  Nutty Tutorial Island - Final Report");
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
        if (roll < 60) return;
        if (roll < 88) {
            gaussianSleep(800, 250, 400);
        } else {
            gaussianSleep(2000, 500, 1000);
        }
    }

    private void shortAFK() {
        logAntiBan("Short AFK");
        if (random.nextInt(100) < 50) {
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 1) Mouse.move(new Point(SCREEN_W + random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(SCREEN_W), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(SCREEN_W), SCREEN_H + random.nextInt(10)));
            gaussianSleep(8000, 3000, 5000);
            Mouse.move(new Point(50 + random.nextInt(SCREEN_W - 100), 30 + random.nextInt(SCREEN_H - 60)));
        } else {
            gaussianSleep(6000, 2000, 4000);
        }
    }

    private void handleDialogue(int varp) {
        int kbBias = 25 + random.nextInt(30); // 25-55% keyboard per dialogue encounter
        while (Dialogues.inDialogue()) {
            if (PlayerSettings.getConfig(281) != varp) break;
            if (Dialogues.canContinue()) {
                int continueRoll = random.nextInt(100);
                if (continueRoll < kbBias) {
                    Keyboard.type(" ");
                } else if (continueRoll < kbBias + 8) {
                    // Double-click continue — common real player behavior
                    Dialogues.clickContinue();
                    gaussianSleep(80, 30, 40);
                    Dialogues.clickContinue();
                } else {
                    Dialogues.clickContinue();
                }
                dialoguePause();
            } else if (Dialogues.getOptions() != null) {
                if (random.nextInt(100) < kbBias) {
                    Keyboard.type("1");
                } else {
                    Dialogues.chooseOption(1);
                }
                dialoguePause();
            } else {
                gaussianSleep(450, 80, 350);
            }
        }
    }

    private void handleIronmanSetup(int varp) {
        Logger.log("[IRONMAN] Starting ironman setup...");

        // Step 1: Talk to Magic Instructor — continue dialogue, choose first option when it appears
        NPC magic = NPCs.closest(MAGIC_INSTRUCTOR);
        if (magic == null) {
            Logger.log("[IRONMAN] Magic instructor not found, walking to area...");
            walkToArea(new Area(3140, 3089, 3142, 3085));
            gaussianSleep(1000, 250, 350);
            magic = NPCs.closest(MAGIC_INSTRUCTOR);
            if (magic == null) {
                Logger.log("[IRONMAN] Magic instructor still not found, aborting");
                return;
            }
        }
        if (!magic.isOnScreen()) rotateCameraTo(magic);
        preClickHover(magic);
        magic.interact("Talk-to");
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 2500 + random.nextInt(1500));
        // Continue dialogue until options appear, choose option 1, then move on immediately
        while (Dialogues.inDialogue()) {
            if (Dialogues.canContinue()) {
                Dialogues.clickContinue();
                dialoguePause();
            } else if (Dialogues.getOptions() != null) {
                Dialogues.chooseOption(1);
                dialoguePause();
                break;
            } else {
                gaussianSleep(450, 80, 350);
            }
        }
        Logger.log("[IRONMAN] Option chosen, heading to tutor...");

        // Step 2: Find and talk to Ironman Tutor
        Sleep.sleepUntil(() -> NPCs.closest(IRONMAN_TUTOR) != null, 4000 + random.nextInt(2000));
        NPC tutor = NPCs.closest(IRONMAN_TUTOR);
        if (tutor == null) {
            Logger.log("[IRONMAN] Tutor not found, walking to tutor area...");
            walkToArea(new Area(3134, 3088, 3132, 3086));
            gaussianSleep(1000, 250, 350);
            Sleep.sleepUntil(() -> NPCs.closest(IRONMAN_TUTOR) != null, 4000 + random.nextInt(2000));
            tutor = NPCs.closest(IRONMAN_TUTOR);
            if (tutor == null) {
                Logger.log("[IRONMAN] Ironman tutor still not found, aborting");
                return;
            }
        }
        if (!tutor.isOnScreen()) rotateCameraTo(tutor);
        preClickHover(tutor);
        tutor.interact("Talk-to");
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 4000 + random.nextInt(2000));
        gaussianSleep(500, 100, 350);

        // Continue all tutor dialogue (spacebar/click continue + choose option 1) until widget (890, 2) appears
        long tutorStart = System.currentTimeMillis();
        while (Widgets.get(890, 2) == null && System.currentTimeMillis() - tutorStart < 60000) {
            if (Dialogues.canContinue()) {
                if (random.nextInt(100) < 40) Keyboard.type(" ");
                else Dialogues.clickContinue();
                dialoguePause();
            } else if (Dialogues.getOptions() != null) {
                Dialogues.chooseOption(1);
                dialoguePause();
            } else {
                gaussianSleep(450, 80, 350);
            }
        }
        gaussianSleep(800, 200, 350);

        // Click (890, 22) to set ironman mode
        WidgetChild setIronman = Widgets.get(890, 22);
        if (setIronman != null) {
            setIronman.interact();
            gaussianSleep(1000, 250, 350);
        }

        // Wait for confirmation widget (289, 4), click (289, 8)
        Sleep.sleepUntil(() -> Widgets.get(289, 4) != null, 4000 + random.nextInt(2000));
        gaussianSleep(500, 100, 350);
        WidgetChild confirm289 = Widgets.get(289, 8);
        if (confirm289 != null) {
            confirm289.interact();
            gaussianSleep(1000, 250, 350);
        }

        // Wait for bank PIN entry widget (213, 1, 0)
        Sleep.sleepUntil(() -> Widgets.get(213, 1, 0) != null, 4000 + random.nextInt(2000));
        gaussianSleep(600, 150, 350);

        // Enter PIN first time
        Logger.log("[IRONMAN] Entering bank PIN...");
        enterBankPin();
        gaussianSleep(1500, 400, 800);

        // Wait for PIN confirmation (same widget appears again)
        Sleep.sleepUntil(() -> Widgets.get(213, 1, 0) != null, 4000 + random.nextInt(2000));
        gaussianSleep(600, 150, 350);

        // Enter PIN second time
        Logger.log("[IRONMAN] Confirming bank PIN...");
        enterBankPin();
        gaussianSleep(1500, 400, 800);

        // Continue any remaining dialogue
        if (Dialogues.inDialogue()) {
            while (Dialogues.inDialogue()) {
                if (PlayerSettings.getConfig(281) != varp) break;
                if (Dialogues.canContinue()) {
                    Dialogues.clickContinue();
                    dialoguePause();
                } else {
                    gaussianSleep(450, 80, 350);
                }
            }
        }

        // Close (890, 2) if still open
        WidgetChild ironmanWidget = Widgets.get(890, 2);
        if (ironmanWidget != null && ironmanWidget.isVisible()) {
            ironmanWidget.interact();
            gaussianSleep(1000, 250, 350);
        }

        // Talk to Magic Instructor one final time
        gaussianSleep(800, 200, 350);
        NPC finalMagic = NPCs.closest(MAGIC_INSTRUCTOR);
        if (finalMagic == null) {
            Logger.log("[IRONMAN] Magic instructor not found for final talk, walking to area...");
            walkToArea(new Area(3140, 3089, 3142, 3085));
            gaussianSleep(1000, 250, 350);
            finalMagic = NPCs.closest(MAGIC_INSTRUCTOR);
            if (finalMagic == null) {
                Logger.log("[IRONMAN] Magic instructor still not found, aborting");
                return;
            }
        }
        if (!finalMagic.isOnScreen()) rotateCameraTo(finalMagic);
        finalMagic.interact("Talk-to");
        Sleep.sleepUntil(() -> Dialogues.inDialogue(), 4000 + random.nextInt(2000));

        // Choose first dialogue option and continue until varp changes
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
        Logger.log("[IRONMAN] Ironman setup complete");
    }

    private void enterBankPin() {
        int[][] pinWidgets = {
            null,           // 0 - not used
            {213, 18, 0},  // 1
            {213, 32, 0},  // 2
            {213, 34, 0},  // 3
            {213, 24, 0},  // 4
            {213, 30, 0},  // 5
            {213, 28, 0},  // 6
            {213, 20, 0},  // 7
            {213, 22, 0},  // 8
            {213, 16, 0},  // 9
        };
        for (int i = 0; i < ironmanPin.length(); i++) {
            int digit = ironmanPin.charAt(i) - '0';
            if (digit < 1 || digit > 9) continue;
            WidgetChild digitBtn = Widgets.get(pinWidgets[digit][0], pinWidgets[digit][1], pinWidgets[digit][2]);
            if (digitBtn != null) {
                digitBtn.interact();
                gaussianSleep(500 + random.nextInt(400), 120 + random.nextInt(80), 300);
            }
        }
    }

    private void walkToArea(Area target) {
        long walkStart = System.currentTimeMillis();
        long walkTimeout = 25000 + random.nextInt(10000);
        while (!target.contains(Players.getLocal()) && System.currentTimeMillis() - walkStart < walkTimeout) {
            if (!Players.getLocal().isMoving()) {
                Walking.walk(target.getRandomTile());
            }
            gaussianSleep(1000 + random.nextInt(500), 300, 600);
            if (random.nextInt(100) < 20) Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
            Sleep.sleepUntil(() -> !Players.getLocal().isMoving(), 4000 + random.nextInt(2000));
        }
    }

    private void dialoguePause() {
        int roll = random.nextInt(100);
        if (roll < 8) {
            gaussianSleep(2500, 600, 1200);  // really reading it carefully
        } else if (roll < 23) {
            gaussianSleep(1500, 350, 800);   // reading normally
        } else if (roll < 55) {
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
        int jitteredMean = mean + random.nextInt(Math.max(1, mean / 5)) - mean / 10; // ±10% jitter on mean
        int adjustedMean = (int)(jitteredMean * fatigueFactor());
        int delay = (int) (adjustedMean + random.nextGaussian() * stddev);
        Sleep.sleep(Math.max(delay, min));
    }

    private void postActionSleep() {
        postClickIdle();
        gaussianSleep(1200, 350, 350);
    }

    private void rotateCameraTo(NPC npc) {
        Camera.rotateToEntity(npc);
        gaussianSleep(500 + random.nextInt(400), 100 + random.nextInt(120), 250 + random.nextInt(200));
    }

    private void rotateCameraTo(GameObject obj) {
        Camera.rotateToEntity(obj);
        gaussianSleep(500 + random.nextInt(400), 100 + random.nextInt(120), 250 + random.nextInt(200));
    }

    private void performAntiBan() {
        int roll = random.nextInt(100);
        if (roll < 20) {
            logAntiBan("Camera rotate");
            Camera.rotateTo(random.nextInt(360), random.nextInt(60) + 320);
        } else if (roll < 35) {
            logAntiBan("Camera nudge");
            Camera.rotateTo(Camera.getYaw() + random.nextInt(60) - 30, Camera.getPitch() + random.nextInt(20) - 10);
        } else if (roll < 55) {
            logAntiBan("Mouse jiggle");
            mouseJiggle();
        } else if (roll < 70) {
            logAntiBan("Idle pause");
            gaussianSleep(1500, 500, 800);
        } else if (roll < 85) {
            logAntiBan("Mouse drift far");
            Point pos = Mouse.getPosition();
            int dx = random.nextInt(150) - 75;
            int dy = random.nextInt(150) - 75;
            Mouse.move(new Point(Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx)), Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy))));
        } else {
            logAntiBan("Mouse off screen");
            int side = random.nextInt(4);
            if (side == 0) Mouse.move(new Point(-5 - random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 1) Mouse.move(new Point(SCREEN_W + random.nextInt(10), random.nextInt(SCREEN_H)));
            else if (side == 2) Mouse.move(new Point(random.nextInt(SCREEN_W), -5 - random.nextInt(10)));
            else Mouse.move(new Point(random.nextInt(SCREEN_W), SCREEN_H + random.nextInt(10)));
            gaussianSleep(2500, 800, 1000);
            Mouse.move(new Point(50 + random.nextInt(SCREEN_W - 100), 30 + random.nextInt(SCREEN_H - 60)));
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
        int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
        int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void mouseDrift() {
        logAntiBan("Mouse drift");
        Point pos = Mouse.getPosition();
        int dx = random.nextInt(10) - 5;
        int dy = random.nextInt(10) - 5;
        int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
        int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
        Mouse.move(new Point(newX, newY));
    }

    private void tabFidget(int varp) {
        // Only fidget with tabs that have been unlocked at this point in the tutorial
        int[][] unlockedTabs = null;
        if (varp >= 630) unlockedTabs = new int[][]{{164,55},{164,53},{164,54},{164,56},{164,52},{164,39},{164,57},{164,58}};
        else if (varp >= 560) unlockedTabs = new int[][]{{164,55},{164,53},{164,54},{164,56},{164,52},{164,39},{164,57}};
        else if (varp >= 531) unlockedTabs = new int[][]{{164,55},{164,53},{164,54},{164,56},{164,52},{164,39}};
        else if (varp >= 430) unlockedTabs = new int[][]{{164,55},{164,53},{164,54},{164,56},{164,52}};
        else if (varp >= 390) unlockedTabs = new int[][]{{164,55},{164,53},{164,54},{164,56}};
        else if (varp >= 230) unlockedTabs = new int[][]{{164,55},{164,53},{164,54}};
        else if (varp >= 50) unlockedTabs = new int[][]{{164,55},{164,53}};
        else if (varp >= 30) unlockedTabs = new int[][]{{164,55}};
        if (unlockedTabs == null || unlockedTabs.length == 0) return;
        int[] tab = unlockedTabs[random.nextInt(unlockedTabs.length)];
        logAntiBan("Tab fidget");
        WidgetChild tabWidget = Widgets.get(tab[0], tab[1]);
        if (tabWidget != null) {
            tabWidget.interact();
            gaussianSleep(800, 200, 400);
        }
    }

    private void inventoryHover() {
        if (Inventory.isEmpty()) return;
        logAntiBan("Inventory hover");
        // Hover over a random occupied inventory slot
        Item[] items = Inventory.all().stream().filter(i -> i != null).toArray(Item[]::new);
        if (items.length == 0) return;
        Item target = items[random.nextInt(items.length)];
        if (target == null) return;
        // Right-click then close (30% chance), otherwise just hover
        if (random.nextInt(100) < 30) {
            target.interact("Cancel");
            gaussianSleep(400, 100, 350);
        } else {
            Mouse.move(target.getDestination());
            gaussianSleep(600, 200, 350);
        }
    }

    private void examineNearby() {
        // Right-click examine a random nearby object or NPC — simulates new player curiosity
        if (random.nextInt(100) < 50) {
            NPC nearNpc = NPCs.closest(n -> n != null && n.isOnScreen() && n.distance() < 10);
            if (nearNpc != null) {
                logAntiBan("Examine NPC");
                nearNpc.interact("Examine");
                gaussianSleep(800, 250, 400);
                return;
            }
        }
        GameObject nearObj = GameObjects.closest(o -> o != null && o.isOnScreen() && o.distance() < 10);
        if (nearObj != null) {
            logAntiBan("Examine object");
            nearObj.interact("Examine");
            gaussianSleep(800, 250, 400);
        }
    }

    private void minimapHover() {
        // Hover over a random spot on the minimap — players glance at it constantly
        logAntiBan("Minimap hover");
        int mx = 627 + random.nextInt(70); // minimap area ~627-697
        int my = 5 + random.nextInt(70);   // minimap area ~5-75
        Mouse.move(new Point(mx, my));
        gaussianSleep(600, 200, 300);
        if (random.nextInt(100) < 30) {
            // Occasionally hover a second spot on the minimap
            mx = 627 + random.nextInt(70);
            my = 5 + random.nextInt(70);
            Mouse.move(new Point(mx, my));
            gaussianSleep(400, 100, 250);
        }
    }

    private void microBreak() {
        // Brief 3-6 second pause — simulates checking phone or glancing away
        logAntiBan("Micro break");
        int roll = random.nextInt(100);
        if (roll < 40) {
            // Small mouse drift then idle
            Point pos = Mouse.getPosition();
            int dx = random.nextInt(80) - 40;
            int dy = random.nextInt(80) - 40;
            Mouse.move(new Point(Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx)), Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy))));
            gaussianSleep(3500, 1200, 2000);
        } else if (roll < 70) {
            // Just idle in place
            gaussianSleep(3000, 1000, 2000);
        } else {
            // Hover minimap then idle
            Mouse.move(new Point(627 + random.nextInt(70), 5 + random.nextInt(70)));
            gaussianSleep(4000, 1500, 2500);
        }
    }

    private void checkSkillsTab() {
        // Open skills tab briefly — simulates checking XP after gaining it
        logAntiBan("Check skills tab");
        WidgetChild skillsTab = Widgets.get(164, 54);
        if (skillsTab != null) {
            skillsTab.interact();
            gaussianSleep(1200, 400, 600);
            // Hover a random skill
            int skillChild = 1 + random.nextInt(23); // skill widget children
            WidgetChild skill = Widgets.get(320, skillChild);
            if (skill != null) {
                Mouse.move(new Point(skill.getX() + skill.getWidth() / 2, skill.getY() + skill.getHeight() / 2));
                gaussianSleep(800, 300, 400);
            }
            // Return to inventory tab
            WidgetChild invTab = Widgets.get(164, 55);
            if (invTab != null) {
                invTab.interact();
                gaussianSleep(400, 100, 250);
            }
        }
    }

    private void talkToNpc(NPC npc) {
        if (npc == null) return;
        hoverWrongEntity(npc);
        preClickHover(npc);
        maybeMisclick();
        if (random.nextInt(100) < 20) {
            // Hover NPC deliberately before clicking — simulates reading name/options
            Point clickPt = npc.getClickablePoint();
            if (clickPt != null) {
                Mouse.move(clickPt);
                gaussianSleep(400, 120, 200);
            }
        }
        npc.interact("Talk-to");
    }

    private void hoverWrongEntity(NPC target) {
        // Before interacting with the correct NPC, briefly hover a nearby wrong one (~10% chance)
        if (target == null || random.nextInt(100) >= 10) return;
        NPC wrong = NPCs.closest(n -> n != null && n.isOnScreen() && n.getIndex() != target.getIndex() && n.distance() < 8);
        if (wrong != null) {
            Point dest = wrong.getClickablePoint();
            if (dest != null) {
                Mouse.move(dest);
                gaussianSleep(300, 100, 150);
            }
        }
    }

    private void preClickHover(NPC entity) {
        if (entity == null || random.nextInt(100) >= 35) return; // 35% chance
        Point dest = entity.getClickablePoint();
        if (dest == null) return;
        int offsetX = random.nextInt(20) - 10;
        int offsetY = random.nextInt(20) - 10;
        Mouse.move(new Point(dest.x + offsetX, dest.y + offsetY));
        gaussianSleep(250, 80, 100);
    }

    private void preClickHover(GameObject obj) {
        if (obj == null || random.nextInt(100) >= 35) return;
        Point dest = obj.getClickablePoint();
        if (dest == null) return;
        int offsetX = random.nextInt(20) - 10;
        int offsetY = random.nextInt(20) - 10;
        Mouse.move(new Point(dest.x + offsetX, dest.y + offsetY));
        gaussianSleep(250, 80, 100);
    }

    private boolean maybeMisclick() {
        if (random.nextInt(100) >= 4) return false; // 4% chance
        Point pos = Mouse.getPosition();
        int offX = 30 + random.nextInt(50);
        int offY = 30 + random.nextInt(50);
        if (random.nextBoolean()) offX = -offX;
        if (random.nextBoolean()) offY = -offY;
        int mx = Math.max(5, Math.min(SCREEN_W - 5, pos.x + offX));
        int my = Math.max(5, Math.min(SCREEN_H - 3, pos.y + offY));
        Mouse.click(new Point(mx, my));
        gaussianSleep(600, 200, 350); // realization pause
        return true;
    }

    private void postClickIdle() {
        int roll = random.nextInt(100);
        if (roll < 25 && nextTargetHint != null) {
            // Drift toward where the next action will be
            int hx = nextTargetHint.x + random.nextInt(40) - 20;
            int hy = nextTargetHint.y + random.nextInt(40) - 20;
            hx = Math.max(5, Math.min(SCREEN_W - 5, hx));
            hy = Math.max(5, Math.min(SCREEN_H - 3, hy));
            gaussianSleep(400, 150, 350);
            Mouse.move(new Point(hx, hy));
            nextTargetHint = null;
        } else if (roll < 55) {
            // Small drift away from click position
            Point pos = Mouse.getPosition();
            int dx = random.nextInt(80) - 40;
            int dy = random.nextInt(80) - 40;
            int newX = Math.max(5, Math.min(SCREEN_W - 5, pos.x + dx));
            int newY = Math.max(5, Math.min(SCREEN_H - 3, pos.y + dy));
            gaussianSleep(400, 150, 350);
            Mouse.move(new Point(newX, newY));
        }
        // remaining % - do nothing, leave mouse where it is
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
        Sleep.sleepUntil(() -> Widgets.get(929, 7) != null && Widgets.get(929, 7).isVisible(), 8000 + random.nextInt(4000));

        return (int) Math.max(700 + random.nextGaussian() * 150, 350);
    }

    private static class HumanMouseAlgorithm extends StandardMouseAlgorithm {
        private final Random mouseRandom = new Random();
        private long lastProfileChange = System.currentTimeMillis();
        private int currentProfile = 1; // 0=slow, 1=normal, 2=fast
        private long profileDurationMs = 30000 + mouseRandom.nextInt(60000);
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
