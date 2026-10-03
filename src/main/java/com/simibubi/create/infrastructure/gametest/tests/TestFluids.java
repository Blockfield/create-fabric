package com.simibubi.create.infrastructure.gametest.tests;

import com.simibubi.create.AllBlockEntityTypes;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.fluids.drain.ItemDrainBlockEntity;
import com.simibubi.create.content.fluids.hosePulley.HosePulleyFluidHandler;
import com.simibubi.create.content.fluids.pipes.valve.FluidValveBlock;
import com.simibubi.create.content.fluids.potion.PotionFluid;
import com.simibubi.create.content.fluids.potion.PotionFluid.BottleType;
import com.simibubi.create.content.fluids.transfer.GenericItemEmptying;
import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.content.kinetics.gauge.SpeedGaugeBlockEntity;
import com.simibubi.create.content.kinetics.gauge.StressGaugeBlockEntity;
import com.simibubi.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import com.simibubi.create.foundation.fluid.SmartFluidTank;
import com.simibubi.create.infrastructure.fabric.transfer.TransferUtil;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidStack;
import com.simibubi.create.infrastructure.fabric.transfer.fluid.FluidTank;
import com.simibubi.create.infrastructure.gametest.CreateGameTestHelper;
import com.simibubi.create.infrastructure.gametest.GameTestGroup;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestAssertException;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.level.material.Fluids;

import org.apache.commons.lang3.mutable.MutableBoolean;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@GameTestGroup(path = "fluids")
public class TestFluids {
    @GameTest(template = "spouting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
    public static void bucketEmptyingAndDrain(CreateGameTestHelper helper) {
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        var simulated = GenericItemEmptying.emptyItem(helper.getLevel(), bucket, true);
        helper.assertTrue(
                simulated.getFirst().getFluid() == Fluids.WATER
                        && simulated.getFirst().getAmount() == FluidConstants.BUCKET,
                "Bucket simulation produced wrong fluid");
        helper.assertTrue(simulated.getSecond().is(Items.BUCKET), "Simulation lost empty bucket");
        helper.assertTrue(
                bucket.is(Items.WATER_BUCKET) && bucket.getCount() == 1,
                "Simulation consumed bucket");

        try (Transaction outer = Transaction.openOuter()) {
            var nested = GenericItemEmptying.emptyItem(helper.getLevel(), bucket, true, outer);
            helper.assertTrue(
                    nested.getFirst().getAmount() == FluidConstants.BUCKET,
                    "Nested bucket simulation produced wrong amount");
            helper.assertTrue(
                    nested.getSecond().is(Items.BUCKET), "Nested simulation lost empty bucket");
            outer.commit();
        }
        helper.assertTrue(
                bucket.is(Items.WATER_BUCKET) && bucket.getCount() == 1,
                "Nested simulation consumed bucket");

        ItemStack consumed = bucket.copy();
        var emptied = GenericItemEmptying.emptyItem(helper.getLevel(), consumed, false);
        helper.assertTrue(consumed.isEmpty(), "Real emptying did not consume bucket");
        helper.assertTrue(
                emptied.getFirst().getAmount() == FluidConstants.BUCKET
                        && emptied.getSecond().is(Items.BUCKET),
                "Real emptying lost fluid or empty bucket");

        BlockPos pos = new BlockPos(5, 2, 1);
        helper.setBlock(pos, Blocks.AIR);
        helper.setBlock(pos, AllBlocks.ITEM_DRAIN.get());
        ItemDrainBlockEntity drain =
                helper.getBlockEntity(AllBlockEntityTypes.ITEM_DRAIN.get(), pos);
        DirectBeltInputBehaviour input = helper.getBehavior(pos, DirectBeltInputBehaviour.TYPE);
        helper.assertTrue(
                input.handleInsertion(bucket, Direction.WEST, false).isEmpty(),
                "Item drain rejected water bucket");
        helper.succeedWhen(
                () -> {
                    helper.assertFluidPresent(
                            new FluidStack(Fluids.WATER, FluidConstants.BUCKET), pos);
                    helper.assertTrue(
                            drain.getHeldItemStack().is(Items.BUCKET),
                            "Item drain did not return empty bucket");
                });
    }

    @GameTest(template = "spouting")
    public static void fluidTankSerialization(CreateGameTestHelper helper) {
        FluidTank source = new FluidTank(FluidConstants.BUCKET * 2);
        FluidTank restored = new FluidTank(FluidConstants.BUCKET * 2);
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Pulling", true);
        List<FluidStack> cases =
                List.of(
                        new FluidStack(Fluids.WATER, FluidConstants.BUCKET / 2),
                        new FluidStack(Fluids.LAVA, FluidConstants.BUCKET),
                        PotionFluid.of(
                                FluidConstants.BOTTLE,
                                new PotionContents(Potions.FIRE_RESISTANCE),
                                BottleType.SPLASH),
                        FluidStack.EMPTY);
        for (FluidStack expected : cases) {
            source.setFluid(expected);
            source.writeToNBT(helper.getLevel().registryAccess(), tag);
            restored.readFromNBT(helper.getLevel().registryAccess(), tag);
            helper.assertTrue(
                    restored.getFluidAmount() == expected.getAmount(),
                    "Fluid tank round-trip changed amount");
            helper.assertTrue(
                    restored.getFluid().getVariant().equals(expected.getVariant()),
                    "Fluid tank round-trip changed fluid or components");
            helper.assertTrue(tag.getBoolean("Pulling"), "Fluid tank erased unrelated pipe data");
        }
        helper.assertTrue(!tag.contains("Fluid"), "Empty tank retained stale saved fluid");
        helper.succeed();
    }

    @GameTest(template = "hose_pulley_transfer", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
    public static void hosePulleyTransfer(CreateGameTestHelper helper) {
        BlockPos lever = new BlockPos(7, 7, 5);
        helper.pullLever(lever);
        helper.succeedWhen(
                () -> {
                    helper.assertSecondsPassed(15);
                    // check filled
                    BlockPos filledLowerCorner = new BlockPos(2, 3, 2);
                    BlockPos filledUpperCorner = new BlockPos(4, 5, 4);
                    BlockPos.betweenClosed(filledLowerCorner, filledUpperCorner)
                            .forEach(pos -> helper.assertBlockPresent(Blocks.WATER, pos));
                    // check emptied
                    BlockPos emptiedLowerCorner = new BlockPos(8, 3, 2);
                    BlockPos emptiedUpperCorner = new BlockPos(10, 5, 4);
                    BlockPos.betweenClosed(emptiedLowerCorner, emptiedUpperCorner)
                            .forEach(pos -> helper.assertBlockPresent(Blocks.AIR, pos));
                    // check nothing left in pulley
                    BlockPos pulleyPos = new BlockPos(4, 7, 3);
                    Storage<FluidVariant> storage = helper.fluidStorageAt(pulleyPos);
                    if (storage instanceof HosePulleyFluidHandler hose) {
                        SmartFluidTank internalTank = hose.getInternalTank();
                        if (!internalTank.isEmpty()) helper.fail("Pulley not empty");
                    } else {
                        helper.fail("Not a pulley");
                    }
                });
    }

    @GameTest(template = "in_world_pumping_out")
    public static void inWorldPumpingOut(CreateGameTestHelper helper) {
        BlockPos lever = new BlockPos(4, 3, 3);
        BlockPos basin = new BlockPos(5, 2, 2);
        BlockPos output = new BlockPos(2, 2, 2);
        helper.pullLever(lever);
        helper.succeedWhen(
                () -> {
                    helper.assertBlockPresent(Blocks.WATER, output);
                    helper.assertTankEmpty(basin);
                });
    }

    @GameTest(template = "in_world_pumping_in")
    public static void inWorldPumpingIn(CreateGameTestHelper helper) {
        BlockPos lever = new BlockPos(4, 3, 3);
        BlockPos basin = new BlockPos(5, 2, 2);
        BlockPos water = new BlockPos(2, 2, 2);
        FluidStack expectedResult = new FluidStack(Fluids.WATER, FluidConstants.BUCKET);
        helper.pullLever(lever);
        helper.succeedWhen(
                () -> {
                    helper.assertBlockPresent(Blocks.AIR, water);
                    helper.assertFluidPresent(expectedResult, basin);
                });
    }

    @GameTest(template = "steam_engine")
    public static void steamEngine(CreateGameTestHelper helper) {
        BlockPos lever = new BlockPos(4, 3, 3);
        helper.pullLever(lever);
        BlockPos stressometer = new BlockPos(5, 2, 5);
        BlockPos speedometer = new BlockPos(4, 2, 5);
        helper.succeedWhen(
                () -> {
                    StressGaugeBlockEntity stress =
                            helper.getBlockEntity(
                                    AllBlockEntityTypes.STRESSOMETER.get(), stressometer);
                    SpeedGaugeBlockEntity speed =
                            helper.getBlockEntity(
                                    AllBlockEntityTypes.SPEEDOMETER.get(), speedometer);
                    float capacity = stress.getNetworkCapacity();
                    helper.assertCloseEnoughTo(capacity, 2048);
                    float rotationSpeed = Mth.abs(speed.getSpeed());
                    helper.assertCloseEnoughTo(rotationSpeed, 16);
                });
    }

    @GameTest(template = "3_pipe_combine", timeoutTicks = CreateGameTestHelper.TWENTY_SECONDS)
    public static void threePipeCombine(CreateGameTestHelper helper) {
        BlockPos tank1Pos = new BlockPos(5, 2, 1);
        BlockPos tank2Pos = tank1Pos.south();
        BlockPos tank3Pos = tank2Pos.south();
        long initialContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos);

        BlockPos pumpPos = new BlockPos(2, 2, 2);
        helper.flipBlock(pumpPos);
        helper.succeedWhen(
                () -> {
                    helper.assertSecondsPassed(13);
                    // make sure fully drained
                    helper.assertTanksEmpty(tank1Pos, tank2Pos, tank3Pos);
                    // and fully moved
                    BlockPos outputTankPos = new BlockPos(1, 2, 2);
                    long moved = helper.getFluidInTanks(outputTankPos);
                    if (moved != initialContents)
                        helper.fail(
                                "Wrong amount of fluid amount. expected [%s], got [%s]"
                                        .formatted(initialContents, moved));
                    // verify nothing was duped or deleted
                });
    }

    @GameTest(template = "3_pipe_split", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
    public static void threePipeSplit(CreateGameTestHelper helper) {
        BlockPos pumpPos = new BlockPos(2, 2, 2);
        BlockPos tank1Pos = new BlockPos(5, 2, 1);
        BlockPos tank2Pos = tank1Pos.south();
        BlockPos tank3Pos = tank2Pos.south();
        BlockPos outputTankPos = new BlockPos(1, 2, 2);

        long totalContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos, outputTankPos);
        helper.flipBlock(pumpPos);

        helper.succeedWhen(
                () -> {
                    helper.assertSecondsPassed(7);
                    FluidStack contents = helper.getTankContents(outputTankPos);
                    if (!contents.isEmpty()) {
                        helper.fail("Tank not empty: " + contents.getAmount());
                    }
                    long newTotalContents = helper.getFluidInTanks(tank1Pos, tank2Pos, tank3Pos);
                    if (newTotalContents != totalContents) {
                        helper.fail(
                                "Wrong total fluid amount. expected [%s], got [%s]"
                                        .formatted(totalContents, newTotalContents));
                    }
                });
    }

    @GameTest(template = "large_waterwheel", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
    public static void largeWaterwheel(CreateGameTestHelper helper) {
        BlockPos wheel = new BlockPos(4, 3, 2);
        BlockPos leftEnd = new BlockPos(6, 2, 2);
        BlockPos rightEnd = new BlockPos(2, 2, 2);
        List<BlockPos> edges = List.of(new BlockPos(4, 5, 1), new BlockPos(4, 5, 3));
        BlockPos openLever = new BlockPos(3, 8, 1);
        BlockPos leftLever = new BlockPos(5, 7, 1);
        waterwheel(helper, wheel, 4, 512, leftEnd, rightEnd, edges, openLever, leftLever);
    }

    @GameTest(template = "small_waterwheel", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
    public static void smallWaterwheel(CreateGameTestHelper helper) {
        BlockPos wheel = new BlockPos(3, 2, 2);
        BlockPos leftEnd = new BlockPos(4, 2, 2);
        BlockPos rightEnd = new BlockPos(2, 2, 2);
        List<BlockPos> edges = List.of(new BlockPos(3, 3, 1), new BlockPos(3, 3, 3));
        BlockPos openLever = new BlockPos(2, 6, 1);
        BlockPos leftLever = new BlockPos(4, 5, 1);
        waterwheel(helper, wheel, 8, 256, leftEnd, rightEnd, edges, openLever, leftLever);
    }

    private static void waterwheel(
            CreateGameTestHelper helper,
            BlockPos wheel,
            float expectedRpm,
            float expectedSU,
            BlockPos leftEnd,
            BlockPos rightEnd,
            List<BlockPos> edges,
            BlockPos openLever,
            BlockPos leftLever) {
        BlockPos speedometer = wheel.north();
        BlockPos stressometer = wheel.south();
        helper.pullLever(openLever);
        helper.succeedWhen(
                () -> {
                    // must always be true
                    edges.forEach(pos -> helper.assertBlockNotPresent(Blocks.WATER, pos));
                    helper.assertBlockPresent(Blocks.WATER, rightEnd);
                    // first step: expect water on left end while flow is allowed
                    if (!helper.getBlockState(leftLever).getValue(LeverBlock.POWERED)) {
                        helper.assertBlockPresent(Blocks.WATER, leftEnd);
                        // water is present. both sides should cancel.
                        helper.assertSpeedometerSpeed(speedometer, 0);
                        helper.assertStressometerCapacity(stressometer, 0);
                        // success, pull the lever, enter step 2
                        helper.powerLever(leftLever);
                        helper.fail("Entering step 2");
                    } else {
                        // lever is pulled, flow should stop
                        helper.assertBlockNotPresent(Blocks.WATER, leftEnd);
                        // 1-sided flow, should be spinning
                        helper.assertSpeedometerSpeed(speedometer, expectedRpm);
                        helper.assertStressometerCapacity(stressometer, expectedSU);
                    }
                });
    }

    @GameTest(
            template = "waterwheel_materials",
            timeoutTicks = CreateGameTestHelper.FIFTEEN_SECONDS)
    public static void waterwheelMaterials(CreateGameTestHelper helper) {
        List<Item> planks =
                BuiltInRegistries.BLOCK.getOrCreateTag(BlockTags.PLANKS).stream()
                        .map(Holder::value)
                        .map(ItemLike::asItem)
                        .collect(Collectors.toCollection(ArrayList::new));
        List<BlockPos> chests = List.of(new BlockPos(6, 4, 2), new BlockPos(6, 4, 3));
        List<BlockPos> deployers = chests.stream().map(pos -> pos.below(2)).toList();
        helper.runAfterDelay(
                3,
                () ->
                        chests.forEach(
                                chest ->
                                        planks.forEach(
                                                plank ->
                                                        TransferUtil.insert(
                                                                helper.itemStorageAt(chest),
                                                                new ItemStack(plank)))));

        BlockPos smallWheel = new BlockPos(4, 2, 2);
        BlockPos largeWheel = new BlockPos(3, 3, 3);
        BlockPos lever = new BlockPos(5, 3, 1);
        helper.pullLever(lever);

        helper.succeedWhen(
                () -> {
                    Item plank = planks.get(0);
                    if (!(plank instanceof BlockItem blockItem))
                        throw new GameTestAssertException(
                                BuiltInRegistries.ITEM.getKey(plank) + " is not a BlockItem");
                    Block block = blockItem.getBlock();

                    WaterWheelBlockEntity smallWheelBe =
                            helper.getBlockEntity(
                                    AllBlockEntityTypes.WATER_WHEEL.get(), smallWheel);
                    if (!smallWheelBe.material.is(block))
                        helper.fail(
                                "Small waterwheel has not consumed "
                                        + BuiltInRegistries.ITEM.getKey(plank));

                    WaterWheelBlockEntity largeWheelBe =
                            helper.getBlockEntity(
                                    AllBlockEntityTypes.LARGE_WATER_WHEEL.get(), largeWheel);
                    if (!largeWheelBe.material.is(block))
                        helper.fail(
                                "Large waterwheel has not consumed "
                                        + BuiltInRegistries.ITEM.getKey(plank));

                    // next item
                    planks.remove(0);
                    deployers.forEach(pos -> TransferUtil.clear(helper.itemStorageAt(pos)));
                    if (!planks.isEmpty()) helper.fail("Not all planks have been consumed");
                });
    }

    @GameTest(template = "smart_observer_pipes")
    public static void smartObserverPipes(CreateGameTestHelper helper) {
        BlockPos lever = new BlockPos(3, 3, 1);
        BlockPos output = new BlockPos(3, 4, 4);
        BlockPos tankOutput = new BlockPos(1, 2, 4);
        FluidStack expected = new FluidStack(Fluids.WATER, 2 * FluidConstants.BUCKET);
        helper.pullLever(lever);
        helper.succeedWhen(
                () -> {
                    helper.assertFluidPresent(expected, tankOutput);
                    helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, output);
                });
    }

    // Fabric's staged fill/drain exceeds 400 ticks at this fixture's branch pressures.
    @GameTest(
            template = "threshold_switch",
            timeoutTicks = 30 * CreateGameTestHelper.TICKS_PER_SECOND)
    public static void thresholdSwitch(CreateGameTestHelper helper) {
        BlockPos leftHandle = new BlockPos(4, 2, 4);
        BlockPos leftValve = new BlockPos(4, 2, 3);
        BlockPos leftTank = new BlockPos(5, 2, 3);

        BlockPos rightHandle = new BlockPos(2, 2, 4);
        BlockPos rightValve = new BlockPos(2, 2, 3);
        BlockPos rightTank = new BlockPos(1, 2, 3);

        BlockPos drainHandle = new BlockPos(3, 3, 2);
        BlockPos drainValve = new BlockPos(3, 3, 1);
        BlockPos lamp = new BlockPos(1, 3, 1);
        BlockPos tank = new BlockPos(2, 2, 1);
        helper.succeedWhenWithDiagnostics(
                () -> {
                    if (!helper.getBlockState(leftValve)
                            .getValue(FluidValveBlock.ENABLED)) { // step 1
                        helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), leftHandle)
                                .activate(false); // open the valve, fill 4 buckets
                        helper.fail("Entering step 2");
                    } else if (!helper.getBlockState(rightValve)
                            .getValue(FluidValveBlock.ENABLED)) { // step 2
                        helper.assertFluidPresent(
                                FluidStack.EMPTY, leftTank); // wait for left tank to drain
                        helper.assertBlockProperty(
                                lamp, RedstoneLampBlock.LIT, false); // should not be on yet
                        helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), rightHandle)
                                .activate(false); // fill another 4 buckets
                        helper.fail("Entering step 3");
                    } else if (!helper.getBlockState(drainValve)
                            .getValue(FluidValveBlock.ENABLED)) { // step 3
                        helper.assertFluidPresent(
                                FluidStack.EMPTY, rightTank); // wait for right tank to drain
                        // 8 buckets inserted. tank full, lamp on.
                        helper.assertBlockProperty(lamp, RedstoneLampBlock.LIT, true);
                        // drain what's filled so far
                        helper.getBlockEntity(AllBlockEntityTypes.VALVE_HANDLE.get(), drainHandle)
                                .activate(false); // drain all 8 buckets
                        helper.fail("Entering step 4");
                    } else {
                        helper.assertTankEmpty(tank); // wait for it to empty
                        helper.assertBlockProperty(
                                lamp, RedstoneLampBlock.LIT, false); // should be off now
                    }
                },
                () ->
                        helper.snapshot(
                                leftTank,
                                rightTank,
                                tank,
                                tank.west(),
                                leftValve,
                                rightValve,
                                drainValve));
    }

    @GameTest(template = "open_pipes")
    public static void openPipes(CreateGameTestHelper helper) {
        BlockPos effects = new BlockPos(2, 4, 2);
        BlockPos removers = new BlockPos(3, 5, 2);

        BlockPos firstSeat = new BlockPos(4, 2, 1);
        BlockPos secondSeat = firstSeat.south(2);

        Zombie firstZombie = helper.spawnSeated(EntityType.ZOMBIE, firstSeat);
        Zombie secondZombie = helper.spawnSeated(EntityType.ZOMBIE, secondSeat);

        helper.pullLever(effects);

        MutableBoolean stage1 = new MutableBoolean(true);

        helper.succeedWhen(
                () -> {
                    if (stage1.booleanValue()) {
                        helper.assertTrue(firstZombie.isOnFire(), "not ignited");
                        helper.assertFalse(secondZombie.getActiveEffects().isEmpty(), "no effects");
                        // success, stage 2 time
                        stage1.setFalse();
                        helper.pullLever(effects);
                        helper.pullLever(removers);
                        helper.fail("switching stages");
                    } else {
                        helper.assertFalse(firstZombie.isOnFire(), "not extinguished");
                        helper.assertTrue(secondZombie.getActiveEffects().isEmpty(), "has effects");
                        // all done
                    }
                });
    }

    @GameTest(template = "spouting", timeoutTicks = CreateGameTestHelper.TEN_SECONDS)
    public static void spouting(CreateGameTestHelper helper) {
        BlockPos farmland = new BlockPos(3, 2, 3);
        BlockPos depot = new BlockPos(5, 2, 1);
        helper.pullLever(2, 3, 2);
        ItemStack waterBottle = PotionContents.createItemStack(Items.POTION, Potions.WATER);

        helper.succeedWhen(
                () -> {
                    // lava
                    helper.assertBlockPresent(Blocks.LAVA_CAULDRON, 3, 2, 1);
                    // water
                    helper.assertBlockProperty(farmland, FarmBlock.MOISTURE, 7);
                    helper.assertBlockPresent(Blocks.MUD, farmland.east(1));
                    helper.assertBlockPresent(Blocks.MUD, farmland.east(2));
                    helper.assertBlockPresent(Blocks.MUD, farmland.east(3));
                    helper.assertBlockPresent(Blocks.WATER_CAULDRON, farmland.east(4));

                    helper.assertContainerContains(depot, Items.WATER_BUCKET);
                    helper.assertContainerContains(depot.east(1), waterBottle);
                    helper.assertContainerContains(depot.east(2), Items.GRASS_BLOCK);
                });
    }
}
