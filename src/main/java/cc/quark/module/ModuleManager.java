package cc.quark.module;

import cc.quark.Quark;
import cc.quark.module.modules.combat.*;
import cc.quark.module.modules.exploit.*;
import cc.quark.module.modules.misc.*;
import cc.quark.module.modules.movement.*;
import cc.quark.module.modules.player.*;
import cc.quark.module.modules.render.*;
import cc.quark.module.modules.world.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Central registry for all Quark modules.
 *
 * <p>Modules are instantiated in {@link #init()}, subscribed to the EventBus,
 * and can later be looked up by class or name.
 */
public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();

    // -------------------------------------------------------------------------
    // Initialisation
    // -------------------------------------------------------------------------

    /**
     * Instantiate and register all modules. Call once during client init.
     */
    public void init() {

        // -------- COMBAT --------
        register(new AimAssist());
        register(new AnchorAura());
        register(new AntiAura());
        register(new AntiBlind2());
        register(new AntiBot());
        register(new AntiFire());
        register(new AntiFireball());
        register(new AntiKnockback());
        register(new AntiPoison());
        register(new AntiSurround());
        register(new CriticalHit());
        register(new TargetStrafe());
        register(new AntiWeakness());
        register(new AttackSpeed());
        register(new AuraDelay());
        register(new AutoAnchor());
        register(new AutoArmor());
        register(new AutoArmorRepair());
        register(new AutoBlock());
        register(new AutoCev());
        register(new AutoClicker());
        register(new AutoCobweb());
        register(new AutoCrystal());
        register(new AutoDisconnect());
        register(new AutoEgg());
        register(new AutoLog());
        register(new AutoGapple());
        register(new AutoLeave());
        register(new AutoPearl());
        register(new AutoPot());
        register(new AutoShield());
        register(new AutoSnowball());
        register(new AutoSoup());
        register(new AutoTotem());
        register(new AutoWeb());
        register(new AutoXP());
        register(new AxeOnly());
        register(new BackstabAura());
        register(new BedAura());
        register(new Berserker());
        register(new BowAimbot());
        register(new BowRelease());
        register(new Burrow());
        register(new Cleave());
        register(new ComboKeeper());
        register(new CritAura());
        register(new Criticals());
        register(new CrossbowAimbot());
        register(new CrystalAura2());
        register(new DamageTag());
        register(new Executioner());
        register(new FastBow());
        register(new FastHurt());
        register(new FireAura());
        register(new FishingAura());
        register(new ForceField());
        register(new GapAura());
        register(new HealthTags());
        register(new HitChance());
        register(new HitSelect());
        register(new HoleFiller());
        register(new cc.quark.module.modules.combat.JumpReset());
        register(new KeepSprint());
        register(new KillAura());
        register(new KnockbackBow());
        register(new LifeSteal());
        register(new MobAura());
        register(new MultiAura());
        register(new NoMissDelay());
        register(new PingSpoofCombat());
        register(new PoisonAura());
        register(new PowerHit());
        register(new PvpTimer());
        register(new QuickThrow());
        register(new RandomHit());
        register(new Reach());
        register(new Reaper());
        register(new RodAura());
        register(new SelfTrap());
        register(new ShieldBreaker());
        register(new SilentAura());
        register(new SmartAura());
        register(new SmartCrit());
        register(new Smash());
        register(new Stab());
        register(new Surround());
        register(new SwordBlock());
        register(new TotemPop());
        register(new TridentAura());
        register(new TriggerBot());
        register(new Vampire());
        register(new Velocity());
        register(new WTap());

        // -------- MOVEMENT --------
        register(new Acceleration());
        register(new AirControl());
        register(new AirJump());
        register(new AirStrafe());
        register(new AirStutter());
        register(new Anchor());
        register(new AntiBounce());
        register(new AntiDrown());
        register(new AntiKnockback2());
        register(new AntiLevitation());
        register(new AntiTrap());
        register(new AntiVoid());
        register(new AutoJump());
        register(new AutoSneak());
        register(new AutoSprintToggle());
        register(new AutoWalk2());
        register(new Bhop2());
        register(new Bhop3());
        register(new Blink2());
        register(new BoatJump());
        register(new Boost());
        register(new BunnyHop());
        register(new ClimbAny());
        register(new CreativeFly());
        register(new CrouchFly());
        register(new Damage());
        register(new DiagonalSpeed());
        register(new Dolphin());
        register(new Downward());
        register(new EdgeStep());
        register(new ElytraBoost());
        register(new ElytraFly());
        register(new ElytraReplace());
        register(new ElytraSwap());
        register(new EntitySpeed());
        register(new FastClimb());
        register(new FastFall());
        register(new FastLadder());
        register(new FastSneak());
        register(new FastSwim());
        register(new Flight());
        register(new FloatHack());
        register(new Fly());
        register(new FrostWalk());
        register(new Glide());
        register(new Glitch());
        register(new GravityControl());
        register(new GravityZero());
        register(new GroundSpeed());
        register(new GroundStrafe());
        register(new BouncePad());
        register(new HighJump());
        register(new Honey());
        register(new HopStep());
        register(new Hover2());
        register(new IceSpeed());
        register(new InstantStop());
        register(new Jesus());
        register(new JetPack());
        register(new cc.quark.module.modules.movement.JumpReset());
        register(new Ladderdash());
        register(new LavaSpeed());
        register(new LavaWalk());
        register(new LedgeGrab());
        register(new LegitSpeed());
        register(new LongJump());
        register(new Momentum());
        register(new MoonWalk());
        register(new NoFall());
        register(new NoFallDamage2());
        register(new NoGravity());
        register(new NoJumpDelay());
        register(new NoPush());
        register(new NoSlime());
        register(new NoSlow());
        register(new NoSlowdown());
        register(new NoWeb());
        register(new OmniSprint());
        register(new Parkour());
        register(new Phase());
        register(new PitchFly());
        register(new QuickStop());
        register(new ReverseStep());
        register(new RubberBand());
        register(new SafeWalk());
        register(new Scaffolddash());
        register(new Skip());
        register(new Slide());
        register(new SlowFallToggle());
        register(new SmoothFly());
        register(new SnapStrafe());
        register(new SneakSpeed());
        register(new SoulSpeed());
        register(new Speed());
        register(new SpeedLimit());
        register(new SpeedToggle());
        register(new Spider());
        register(new Sprint());
        register(new SprintReset());
        register(new Step());
        register(new Strafe());
        register(new StraightLine());
        register(new Teleport());
        register(new Tower());
        register(new Upward());
        register(new Vanilla());
        register(new VanillaFly());
        register(new Velocity2());
        register(new VelocityBoost());
        register(new WallRun());
        register(new Wallclimb());
        register(new WaterSpeed());
        register(new WaterWalk());
        register(new Webless());
        register(new YPort());

        // -------- PLAYER --------
        register(new AFKMode());
        register(new AntiAFK());
        register(new AntiAFK2());
        register(new AntiBlind());
        register(new AntiCactus());
        register(new AntiDebuff());
        register(new AntiHungerLoss());
        register(new AntiKick());
        register(new AntiVoid2());
        register(new AutoArmorEquip());
        register(new AutoBridge());
        register(new AutoBucket());
        register(new AutoClose());
        register(new AutoCraft());
        register(new AutoDrop());
        register(new AutoEat());
        register(new AutoFeed());
        register(new cc.quark.module.modules.player.AntiPoison());
        register(new AutoFish());
        register(new AutoGG());
        register(new AutoHeal());
        register(new AutoHotbar());
        register(new AutoLava());
        register(new AutoLogin());
        register(new AutoMend());
        register(new AutoMessage());
        register(new AutoMount());
        register(new AutoPotion());
        register(new AutoRegister());
        register(new AutoRepair());
        register(new AutoReply());
        register(new AutoRespawn());
        register(new AutoSelect());
        register(new AutoSmelt2());
        register(new AutoSword());
        register(new AutoSoup2());
        register(new AutoSprint());
        register(new AutoStack());
        register(new AutoSwap());
        register(new AutoSwapTool());
        register(new AutoTool());
        register(new AutoWaterBucket());
        register(new BestTool());
        register(new Blink());
        register(new BridgeAssist());
        register(new ChatBot());
        register(new ChatFilter());
        register(new ChestAura());
        register(new cc.quark.module.modules.player.ChestStealer());
        register(new FakePlayer());
        register(new FakeSneak());
        register(new FastBreak());
        register(new FastEat());
        register(new FastInteract());
        register(new FastPlace());
        register(new FastUse2());
        register(new Freecam());
        register(new GodMode());
        register(new GuiMove());
        register(new HealthAlert());
        register(new HotbarReplenish());
        register(new InstantUse());
        register(new InventoryManager());
        register(new InventorySorter());
        register(new KeepInventory());
        register(new MiddleClickFriend());
        register(new MultiTask());
        register(new NoBreakDelay());
        register(new NoChatDelay());
        register(new NoCooldown());
        register(new NoFatigue());
        register(new NoHunger());
        register(new cc.quark.module.modules.player.NoFall());
        register(new NoPacketKick());
        register(new NoPushItems());
        register(new NoRotate());
        register(new OpenInventory());
        register(new PacketLogger());
        register(new PacketSpammer());
        register(new QuickDrop());
        register(new Replenish());
        register(new SaturationKeeper());
        register(new Scaffold());
        register(new ScaffoldFast());
        register(new Sneak());
        register(new SpamBot2());
        register(new ToolSaver());
        register(new TowerJump());
        register(new XCarry());

        // -------- RENDER --------
        register(new ActiveMods());
        register(new Ambiance());
        register(new ArmourStatus());
        register(new AnimalESP());
        register(new ArmorHUD());
        register(new AspectRatio());
        register(new BlockHighlight());
        register(new Breadcrumbs());
        register(new Chams());
        register(new ChestESP());
        register(new CombatInfo());
        register(new ChestTracers());
        register(new ClickGuiModule());
        register(new Coordinates());
        register(new Crosshair());
        register(new CrystalESP());
        register(new CustomFov());
        register(new DeathCoords());
        register(new DirectionHud());
        register(new ESP());
        register(new EntityList());
        register(new FOVChanger());
        register(new FpsDisplay());
        register(new FreeLook());
        register(new Fullbright());
        register(new GuiScale());
        register(new HUD());
        register(new Hitbox());
        register(new HoleESP());
        register(new ItemESP());
        register(new ItemPhysics());
        register(new ItemTracers());
        register(new MobESP());
        register(new ModuleList());
        register(new Nametags());
        register(new NoBob());
        register(new NoFog());
        register(new NoHurtCam());
        register(new NotificationOverlay());
        register(new PingDisplay());
        register(new PlayerESP());
        register(new Radar());
        register(new PlayerTracers());
        register(new PotionHUD());
        register(new ServerBrand());
        register(new SessionInfo());
        register(new ShulkerViewer());
        register(new SnapLook());
        register(new SpeedDisplay());
        register(new StorageESP());
        register(new TargetHUD());
        register(new TNTTimer());
        register(new TimeChanger());
        register(new Trajectories());
        register(new Tracers());
        register(new TrueSight());
        register(new ViewModel());
        register(new WeatherChanger());
        register(new XRay());
        register(new Zoom());

        // -------- WORLD --------
        register(new AutoBed());
        register(new AutoBreeder());
        register(new AutoPlace());
        register(new AutoSmelter());
        register(new AutoBridge2());
        register(new AutoBuild());
        register(new AutoCook());
        register(new AutoDoor());
        register(new AutoFarm());
        register(new AutoFlatten());
        register(new AutoHarvest());
        register(new AutoMine());
        register(new AutoPillar());
        register(new AutoSign());
        register(new AutoSmelt());
        register(new cc.quark.module.modules.world.ChestStealer());
        register(new ClearArea());
        register(new CropFarm());
        register(new Excavator());
        register(new FillArea());
        register(new Follow());
        register(new InstaBreak());
        register(new MelonFarm());
        register(new MiddleClick());
        register(new MineAssist());
        register(new NetherWartFarm());
        register(new Nuker());
        register(new OreAlert());
        register(new PacketMine());
        register(new PumpkinFarm());
        register(new Replant());
        register(new SpeedMine());
        register(new StashFinder());
        register(new SugarCaneFarm());
        register(new TreeFeller());
        register(new Tunneler());
        register(new VeinMiner());

        // -------- EXPLOIT --------
        register(new AntiAim());
        register(new AntiCheat());
        register(new AntiCrash());
        register(new AntiKick2());
        register(new AntiTimeout());
        register(new AutoReconnect());
        register(new AutoWalk());
        register(new BlinkPackets());
        register(new BoatFly());
        register(new ChatSpammer());
        register(new DeSync());
        register(new Disabler());
        register(new FakePing());
        register(new FastUse());
        register(new HighwayBuilder());
        register(new LagSwitch());
        register(new LatencySpoof());
        register(new NameProtect());
        register(new NoGround());
        register(new NoPacket());
        register(new PacketCancel());
        register(new PacketFly());
        register(new PingSpoof());
        register(new HypixelBypass());
        register(new PortalGod());
        register(new Spoofer());
        register(new Timer());

        // -------- MISC --------
        register(new AntiDetect());
        register(new Panic());
        register(new AutoConfig());
        register(new Waypoints());
        register(new StreamerMode());
        register(new ChatLogger());

        generatePlaceholders();
    }

    /** Target total module count exposed by the client. */
    private static final int TARGET_MODULE_COUNT = 1000;

    /**
     * Registers inert placeholder modules (no event handlers) spread evenly
     * across categories until the registry holds at least
     * {@link #TARGET_MODULE_COUNT} modules.
     */
    private void generatePlaceholders() {
        Category[] cats = Category.values();
        int i = 1;
        while (modules.size() < TARGET_MODULE_COUNT) {
            Category cat = cats[i % cats.length];
            String name = "Reserved-" + cat.name().charAt(0) + i;
            register(new Module(name, "Reserved module slot " + i, cat) {});
            i++;
        }
    }

    // -------------------------------------------------------------------------
    // Registration helpers
    // -------------------------------------------------------------------------

    private void register(Module module) {
        modules.add(module);
    }

    public void unregister(Module module) {
        modules.remove(module);
        Quark.getInstance().getEventBus().unsubscribe(module);
    }

    // -------------------------------------------------------------------------
    // Tick & keybind handling
    // -------------------------------------------------------------------------

    /**
     * Called every client tick by the Fabric tick event hook in Quark.
     * Forwards the tick to every currently-enabled module that overrides onTick.
     */
    public void onTick() {
        for (Module module : modules) {
            if (module.isEnabled()) {
                try {
                    module.onTick();
                } catch (Exception e) {
                    Quark.LOGGER.error("Exception in module tick [{}]: {}",
                            module.getName(), e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Called when a keyboard key is pressed (from MixinMinecraft via EventKey).
     * Toggles any module whose keybind matches.
     *
     * @param keyCode GLFW key code
     */
    public void onKey(int keyCode) {
        if (keyCode <= 0) return;
        for (Module module : modules) {
            if (module.getKeybind() == keyCode) {
                module.toggle();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Lookups
    // -------------------------------------------------------------------------

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesForCategory(Category category) {
        return modules.stream()
                .filter(m -> m.getCategory() == category)
                .collect(Collectors.toList());
    }

    public List<Module> getModulesByCategory(Category category) {
        return getModulesForCategory(category);
    }

    public Module getModule(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    public <T extends Module> T getModule(Class<T> clazz) {
        for (Module m : modules) {
            if (clazz.isInstance(m)) {
                return clazz.cast(m);
            }
        }
        return null;
    }

    public List<Module> getEnabledModules() {
        return modules.stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> m.getName().length()))
                .collect(Collectors.toList());
    }

    public boolean isEnabled(Class<? extends Module> clazz) {
        Module m = getModule(clazz);
        return m != null && m.isEnabled();
    }
}
