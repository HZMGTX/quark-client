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
        register(new AutoStrafe());
        register(new Surround());
        register(new SwordBlock());
        register(new TotemPop());
        register(new TridentAura());
        register(new TriggerBot());
        register(new Vampire());
        register(new Velocity());
        register(new WTap());
        register(new TotemSwap());
        register(new WTap2());
        register(new KillAura2());
        register(new AutoGapple2());
        register(new ShieldBlock());
        register(new BlastProof());
        register(new NoHitCooldown());
        register(new AntiAutoClicker());
        register(new AntiFireball2());
        register(new AutoEnderPearl());
        register(new AutoObsidian());
        register(new AutoPearl2());
        register(new CombatRecorder());
        register(new CritSpam());
        register(new HurtTimer());
        register(new ShieldSpoof());
        register(new SpeedAura());
        register(new ThornsAura());
        register(new AntiVelocity2());
        register(new ArrowDodge());
        register(new AutoShoot());
        register(new AutoSmite());
        register(new AutoTrap());
        register(new BowSpam());
        register(new CombatLog());
        register(new CrystalSwitch());
        register(new DamageIndicator());
        register(new FightBot());
        register(new QuickShield());
        register(new SilentMine());
        register(new SwordSprint());
        register(new TargetFollow());
        register(new TeamMode());
        register(new AntiBot2());
        register(new AntiCrit());
        register(new AntiGapple());
        register(new AntiWitch());
        register(new ArmorBreaker());
        register(new AutoFortify());
        register(new BloodEffect());
        register(new BowCharge());
        register(new CombatAssist());
        register(new ComboExtender());
        register(new CrystalBase());
        register(new DeathCounter());
        register(new GhostHit());
        register(new HitSound());
        register(new HitboxExpand());
        register(new InstantPop());
        register(new KillCounter());
        register(new MaceAura());
        register(new NoSwingDelay());
        register(new PotionAura());
        register(new Replenish2());
        register(new SpeedCrit());
        register(new SurroundBreak());
        register(new SwordFilter());
        register(new WeaponSwitch());
        register(new AnchorPlace());
        register(new AntiTotem());
        register(new AutoArrow());
        register(new AutoBow());
        register(new AutoEnchant());
        register(new AutoObsidian2());
        register(new AutoVelocity());
        register(new AxisAura());
        register(new Backtrack());
        register(new BowPredict());
        register(new CritBoost());
        register(new CrystalPhase());
        register(new DamageFlash());
        register(new FastCrystal());
        register(new GappleAura());
        register(new HolePush());
        register(new NoDebuff());
        register(new ShieldPop());
        register(new SurroundFill());
        register(new SurroundPlus());
        register(new TargetSelector());
        register(new TotemAlert());
        register(new TrapDamage());
        register(new CombatPause());
        register(new ForcePush());

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
        register(new AirBrake());
        register(new AutoParkour());
        register(new BounceFly());
        register(new ClipWalk());
        register(new FastStop());
        register(new ForwardBoost());
        register(new HighStep());
        register(new KnifeSpeed());
        register(new PacketStep());
        register(new ParkourHelper());
        register(new SpeedBoost());
        register(new StepAssist());
        register(new SurfaceSwim());
        register(new WallJump());
        register(new AntiSwim());
        register(new BoatSpeed());
        register(new FastBridge());
        register(new FastElytra());
        register(new GhostMode());
        register(new HoverHeight());
        register(new LegitFly());
        register(new NoFallDamage3());
        register(new NoJumpAnimation());
        register(new NoSlowness());
        register(new RidingSpeed());
        register(new RocketBoost());
        register(new SlopeClimb());
        register(new SpringJump());
        register(new WaterJet());
        register(new AirStrafe2());
        register(new BouncePad2());
        register(new DolphinSwim());
        register(new FlyBoost());
        register(new StairJump());
        register(new AirWalk());
        register(new AntiSlow2());
        register(new AntiVelocity3());
        register(new AutoElytra());
        register(new BunnyFly());
        register(new CobwebBypass());
        register(new CrouchBoost());
        register(new GlideSpeed());
        register(new GroundSneak());
        register(new NoCobweb());
        register(new NoFriction());
        register(new Orbit());
        register(new Skid());
        register(new SoulSandSpeed());
        register(new StepUp());
        register(new StrafeBoost());
        register(new TridentFly());
        register(new WaterBoost());
        register(new AntiLag());
        register(new cc.quark.module.modules.movement.AntiVoid2());
        register(new AutoRoll());
        register(new FreeFly());
        register(new GhostFly());
        register(new KiteStrafe());
        register(new NoSlowV());
        register(new OverclockSprint());
        register(new PearlPhase());
        register(new Rappel());
        register(new RocketJump());
        register(new SilentStep());
        register(new SlimeJump());
        register(new SnowWalk());
        register(new SoulWalk());
        register(new TeleportStep());
        register(new UnderwaterSpeed());
        register(new VaultJump());
        register(new VerticalFly());
        register(new WindBoost());
        register(new XZFly());
        register(new YawLock());
        register(new ZeroVelocity());

        // -------- PLAYER --------
        register(new AFKMode());
        register(new AntiAFK());
        register(new AntiAFK2());
        register(new AntiBlind());
        register(new AntiCactus());
        register(new AntiDebuff());
        register(new AntiHungerLoss());
        register(new AntiKick());
        register(new cc.quark.module.modules.player.AntiVoid2());
        register(new AutoArmorEquip());
        register(new AutoBridge());
        register(new AutoBucket());
        register(new AutoClose());
        register(new AutoCraft());
        register(new AutoDrop());
        register(new AutoEat());
        register(new AutoFeed());
        register(new cc.quark.module.modules.combat.AntiPoison());
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
        register(new AutoBot());
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
        register(new cc.quark.module.modules.movement.NoFall());
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
        register(new NoParticles());
        register(new InfiniteReach());
        register(new ArmorAlert());
        register(new AutoOffhand());
        register(new NoDeathScreen());
        register(new AutoPickup());
        register(new GhostHand());
        register(new AntiGrief());
        register(new StorageOrganizer());
        register(new AutoSit());
        register(new PotionRefill());
        register(new ArmorDrop());
        register(new AutoLead());
        register(new AutoShear());
        register(new CorpseESP());
        register(new DeathLogger());
        register(new FoodAlert());
        register(new HotbarLock());
        register(new ItemSucker());
        register(new ItemTracker());
        register(new SmartEat());
        register(new AntiEffect());
        register(new AntiFirework());
        register(new AntiGrief3());
        register(new AntiItemDrop());
        register(new AntiPoison2());
        register(new AntiSleep());
        register(new ArmorSwap());
        register(new AutoFeed2());
        register(new AutoHarvest2());
        register(new AutoLavaWalk());
        register(new AutoRecipe());
        register(new AutoRefill());
        register(new AutoSleep());
        register(new BedSet());
        register(new HealthDisplay());
        register(new OffhandSwitch());
        register(new PickupFilter());
        register(new QuickCraft());
        register(new SaturationDisplay());
        register(new SpawnPoint());
        register(new ThrowFilter());
        register(new UseOnSelf());
        register(new AntiNausea2());
        register(new AntiNocturia());
        register(new AutoCure());
        register(new AutoElytra2());
        register(new AutoLevelUp());
        register(new AutoNote2());
        register(new AutoSwim());
        register(new ChestSort());
        register(new CustomGravity());
        register(new DurabilityAlert());
        register(new FakeLag2());
        register(new HeadRoll());
        register(new NoCrouchAnim());
        register(new NoOverlay());
        register(new QuickBank());

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
        register(new HealthVignette());
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
        register(new BlockESP());
        register(new StorageESP());
        register(new TargetESP());
        register(new TargetHUD());
        register(new TNTTimer());
        register(new TimeChanger());
        register(new Trajectories());
        register(new Tracers());
        register(new TrueSight());
        register(new MotionGraph());
        register(new TabList());
        register(new ViewModel());
        register(new WeatherChanger());
        register(new VoidESP());
        register(new XRay());
        register(new Zoom());
        register(new ChunkBoundary());
        register(new MotionBlur());
        register(new CaveFinder());
        register(new Tracers2());
        register(new NameTags());
        register(new Ambience());
        register(new ESP2());
        register(new BeaconESP());
        register(new FreeCam());
        register(new Nametag());
        register(new CompassHUD());
        register(new CustomBobbing());
        register(new GlowESP());
        register(new HitboxColor());
        register(new InventoryViewer());
        register(new PlayerInfo());
        register(new RegionBorders());
        register(new ActiveEffects());
        register(new AntiNausea());
        register(new AntiOverlay());
        register(new ArmourOverlay());
        register(new BlockHighlight2());
        register(new CustomSky());
        register(new DeathPosition());
        register(new EntityOutlines());
        register(new LightESP());
        register(new PortalOverlay());
        register(new WeaponInfo());
        register(new AmmoHUD());
        register(new AntiBlindRender());
        register(new ArmorESP());
        register(new BiomeOverlay());
        register(new BlockOverlay());
        register(new ChestESP2());
        register(new CombatOverlay());
        register(new CrystalTimer());
        register(new CustomCrosshair());
        register(new EntityGlow());
        register(new ExperienceHUD());
        register(new FireworkESP());
        register(new HealthBar());
        register(new LightLevel());
        register(new OxygenHUD());
        register(new PopupCounter2());
        register(new ProjectileESP());
        register(new RainbowHUD());
        register(new SaturationHUD());
        register(new SlimeChunkESP());
        register(new SpawnChunkESP());
        register(new SwordESP());
        register(new TotemCounter());
        register(new TotemESP());
        register(new VehicleESP());
        register(new ArmorStandESP());
        register(new BeeESP());
        register(new BorderESP());
        register(new CaveESP());
        register(new ChunkLoadESP());
        register(new CornerESP());
        register(new DragonESP());
        register(new EnderChestESP());
        register(new FurnaceESP());
        register(new HeatMap());
        register(new ItemFrameESP());
        register(new MessageESP());
        register(new MobCounter());
        register(new MobSpawnESP());
        register(new PetESP());
        register(new PlayerCount());
        register(new PortalESP());
        register(new SkyESP());
        register(new SlimeESP());
        register(new TNTTracers());
        register(new WitherESP());
        register(new CombatStats());
        register(new DurabilityHUD());
        register(new EnchantHUD());
        register(new StatHUD());

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
        register(new AntiDrops());
        register(new TorchPlacer());
        register(new Scaffolding());
        register(new AutoFurnace());
        register(new VeinMiner2());
        register(new ChestStealer2());
        register(new AutoSmith());
        register(new SpawnProofer());
        register(new AutoWorkbench());
        register(new BlockRecorder());
        register(new AntiEnderpearl());
        register(new AutoDispenser());
        register(new AutoCompost());
        register(new AutoFlint());
        register(new AutoMilk());
        register(new AutoNetherFarm());
        register(new AutoReplant2());
        register(new FloodPlacement());
        register(new PathFinder());
        register(new StructureHighlight());
        register(new AutoAnvil());
        register(new AutoBrew());
        register(new AutoEnchanting());
        register(new AutoFarm2());
        register(new AutoLoom());
        register(new AutoStonecutter());
        register(new EntityCleaner());
        register(new LiquidFiller());
        register(new AutoBarrel());
        register(new AutoCartography());
        register(new AutoCauldron());
        register(new AutoCompost2());
        register(new AutoDropper());
        register(new AutoGrinder());
        register(new AutoHopper());
        register(new AutoLectern());
        register(new AutoSorter());
        register(new AutoWeed());
        register(new AutoWorkbench2());
        register(new ChestHarvest());
        register(new ContainerFill());
        register(new ItemMover());
        register(new LavaRemover());
        register(new MapArtHelper());
        register(new PlantAll());
        register(new TillLand());
        register(new TreasureHunter2());
        register(new WaterPlacer());
        register(new AntiFireSpread());
        register(new AutoEndRod());
        register(new AutoFlint2());
        register(new AutoLeash());
        register(new AutoRemove());
        register(new AutoSaddle());
        register(new AutoShear2());
        register(new AutoStrip());
        register(new BedBomb());
        register(new BlockFilter());
        register(new BonemealFarm());
        register(new InventoryFill());
        register(new MobKiller());
        register(new NetherRoof());
        register(new PortalTrap());

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
        register(new ChunkLoad());
        register(new AntiHunger2());
        register(new NoRenderDistance());
        register(new AutoInteract());
        register(new PacketLogger2());
        register(new AntiSpectator());
        register(new CrashClient());
        register(new SignBypass());
        register(new AntiDesync());
        register(new AntiTablist());
        register(new BookBot());
        register(new BypassCooldown());
        register(new EntitySpammer());
        register(new LagBack());
        register(new PacketSniffer());
        register(new SpeedHack());
        register(new TabListSpoof());
        register(new TeleportExploit());
        register(new AntiKick3());
        register(new EntityDesync());
        register(new FakeLatency());
        register(new InventorySpoof());
        register(new PacketDuplicate());
        register(new PortalSpoof());
        register(new BlockGlitch());
        register(new ChunkBypass());
        register(new EntityTP());
        register(new NoSlowPacket());

        // -------- MISC --------
        register(new AntiDetect());
        register(new BetterChat());
        register(new Macros());
        register(new Panic());
        register(new AutoConfig());
        register(new ServerInfo());
        register(new Waypoints());
        register(new StreamerMode());
        register(new ChatLogger());
        register(new GuiTheme());
        register(new FriendList());
        register(new PopupCounter());
        register(new SendCoords());
        register(new TextReplace());
        register(new AutoCommand());
        register(new SessionStats());
        register(new ModuleHistory());
        register(new QuickMessages());
        register(new ArmourComparator());
        register(new AutoStatus());
        register(new CPSDisplay());
        register(new ChatHistory());
        register(new ClockHUD());
        register(new ConnectionInfo());
        register(new MemoryHUD());
        register(new Notifications2());
        register(new ScoreboardHUD());
        register(new TabListInfo());
        register(new AfkDuration());
        register(new AutoResponse());
        register(new ChatTranslator());
        register(new CustomWatermark());
        register(new DeathAlert());
        register(new FriendAlert());
        register(new JoinAlert());
        register(new KillAlert());
        register(new MessageHistory());
        register(new PlayerRadar());
        register(new PlayerTracker());
        register(new ServerWatch());
        register(new SpellCheck());
        register(new TimeDisplay());
        register(new AltManager());
        register(new PingChecker());
        register(new TitleManager());
        register(new ConfigSync());
        register(new FontManager());
        register(new HotkeyManager());
        register(new ModuleSearch());
        register(new ScreenCapture());
        register(new SessionTimer());
        register(new ThemeColor());
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
